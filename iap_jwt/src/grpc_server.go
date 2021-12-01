package main

import (
	"errors"
	"flag"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"echo"

	jwt "github.com/golang-jwt/jwt"
	"github.com/lestrrat/go-jwx/jwk"
	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

var (
	grpcport       = flag.String("grpcport", ":8080", "grpcport")
	usetls         = flag.Bool("usetls", false, "startup using TLS")
	serverCert     = flag.String("cert", "server_crt.pem", "Server TLS cert")
	serverKey      = flag.String("key", "server_key.pem", "Server TLS key")
	targetAudience = flag.String("targetAudience", "", "OIDC audience to check")
	validateToken  = flag.Bool("validateToken", false, "validateToken field")
	jwtSet         *jwk.Set
)

const (
	issuer          = "https://cloud.google.com/iap"
	aud             = "/projects/5939454960/apps/msachs-staging"
	jwksURL         = "https://www.gstatic.com/iap/verify/public_key-jwk"
	iap_header_name = "x-goog-iap-jwt-assertion"
)

type server struct {
}

type IAPClaims struct {
	Google struct {
		AccessLevels []string `json:"access_levels,omitempty"`
	} `json:"google"`

	Email string `json:"email,omitempty"`
	Hd    string `json:"hd,omitempty"`
	jwt.StandardClaims
}

type contextKey string

func getKey(token *jwt.Token) (interface{}, error) {

	set, err := jwk.FetchHTTP(jwksURL)
	if err != nil {
		return nil, err
	}

	keyID, ok := token.Header["kid"].(string)
	if !ok {
		return nil, errors.New("expecting JWT header to have string kid")
	}

	if key := set.LookupKeyID(keyID); len(key) == 1 {
		return key[0].Materialize()
	}

	return nil, errors.New("unable to find key")
}

func authUnaryInterceptor(
	ctx context.Context,
	req interface{},
	info *grpc.UnaryServerInfo,
	handler grpc.UnaryHandler,
) (interface{}, error) {
	md, _ := metadata.FromIncomingContext(ctx)
	if len(md[iap_header_name]) > 0 {
		iap_header := md[iap_header_name][0]

		token, err := jwt.ParseWithClaims(iap_header, &IAPClaims{}, getKey)

		if err != nil {
			log.Printf("Error parsing JWT %v", err)
			return nil, grpc.Errorf(codes.Unauthenticated, "Error Parsing JWT Header")
		}

		if claims, ok := token.Claims.(*IAPClaims); ok && token.Valid {
			log.Printf("Audience %v %v", claims.Audience, claims.StandardClaims.Issuer)
			newCtx := context.WithValue(ctx, contextKey("email"), claims.Email)
			return handler(newCtx, req)
		} else {
			log.Printf("Error parsing Claims")
			return nil, grpc.Errorf(codes.Unauthenticated, "Error Parsing JWT Header")
		}

		return nil, grpc.Errorf(codes.Unauthenticated, "Error Parsing JWT Header")
	}
	return nil, grpc.Errorf(codes.Unauthenticated, "Authorization header not provided")

}

func (s *server) SayHello(ctx context.Context, in *echo.EchoRequest) (*echo.EchoReply, error) {

	log.Println("Got rpc: --> ", in.Name)

	var respmdheader = metadata.MD{
		"rpcheaderkey": []string{"val"},
	}
	if err := grpc.SendHeader(ctx, respmdheader); err != nil {
		log.Fatalf("grpc.SendHeader(%v, %v) = %v, want %v", ctx, respmdheader, err, nil)
	}
	var respmdfooter = metadata.MD{
		"rpctrailerkey": []string{"val"},
	}
	grpc.SetTrailer(ctx, respmdfooter)

	/*
		var h, err = os.Hostname()
		if err != nil {
			log.Fatalf("Unable to get hostname %v", err)
		}
	*/
	h := os.Getenv("K_REVISION")
	return &echo.EchoReply{Message: "Hello " + in.Name + "  from K_REVISION " + h}, nil
}

func main() {

	flag.Parse()
	var err error
	lis, err := net.Listen("tcp", *grpcport)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	sopts := []grpc.ServerOption{grpc.MaxConcurrentStreams(10)}

	if *usetls {
		ce, err := credentials.NewServerTLSFromFile(*serverCert, *serverKey)
		if err != nil {
			log.Fatalf("Failed to generate credentials %v", err)
		}
		log.Printf("Starting gRPC server with TLS")
		sopts = append(sopts, grpc.Creds(ce))
	}

	if *validateToken {
		sopts = append(sopts, grpc.UnaryInterceptor(authUnaryInterceptor))
		sopts = append(sopts)
	}

	jwtSet, err = jwk.FetchHTTP(jwksURL)
	if err != nil {
		log.Fatal("Unable to load JWK Set: ", err)
	}

	s := grpc.NewServer(sopts...)

	echo.RegisterEchoServerServer(s, &server{})

	log.Println("Starting gRPC server on port :8080")

	var gracefulStop = make(chan os.Signal)
	signal.Notify(gracefulStop, syscall.SIGTERM)
	signal.Notify(gracefulStop, syscall.SIGINT)
	go func() {
		sig := <-gracefulStop
		log.Printf("caught sig: %+v", sig)
		log.Println("Wait for 1 second to finish processing")
		time.Sleep(1 * time.Second)
		os.Exit(0)
	}()
	s.Serve(lis)
}
