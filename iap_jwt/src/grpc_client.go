package main

import (
	"crypto/tls"
	"crypto/x509"
	pb "echo"
	"flag"
	"io/ioutil"
	"log"
	"time"

	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

const (
	iap_header_name = "x-goog-iap-jwt-assertion"
)

var (
	address       = flag.String("address", "localhost:8080", "host:port of gRPC server")
	usetls        = flag.Bool("usetls", false, "startup using TLS")
	cacert        = flag.String("cacert", "", "root CA Certificate for TLS")
	sniServerName = flag.String("servername", "grpc.domain.com", "SNIServer Name assocaited with the server")
	token         = flag.String("token", "", "IAP Token")
)

func main() {

	flag.Parse()

	ctx := context.Background()

	var conn *grpc.ClientConn
	var err error
	if !*usetls {
		conn, err = grpc.Dial(*address, grpc.WithInsecure())
		if err != nil {
			log.Fatalf("did not connect: %v", err)
		}
	} else {
		var tlsCfg tls.Config
		if len(*cacert) > 0 {
			rootCAs := x509.NewCertPool()
			pem, err := ioutil.ReadFile(*cacert)
			if err != nil {
				log.Fatalf("failed to load root CA certificates  error=%v", err)
			}
			if !rootCAs.AppendCertsFromPEM(pem) {
				log.Fatalf("no root CA certs parsed from file ")
			}
			tlsCfg.RootCAs = rootCAs
		}
		tlsCfg.ServerName = *sniServerName

		ce := credentials.NewTLS(&tlsCfg)
		// TODO: make a tokensource and use WithPerRPCCredentials()
		// https://github.com/salrashid123/oauth2#grpc-withperrpccredentials
		conn, err = grpc.Dial(*address,
			grpc.WithTransportCredentials(ce))
		//	grpc.WithPerRPCCredentials(rpcCreds))
		if err != nil {
			log.Fatalf("did not connect: %v", err)
		}
	}
	defer conn.Close()

	c := pb.NewEchoServerClient(conn)

	var authHeader = metadata.MD{
		iap_header_name: []string{*token},
	}

	ctx = metadata.NewOutgoingContext(context.Background(), authHeader)

	var header, trailer metadata.MD

	for i := 0; i < 5; i++ {
		r, err := c.SayHello(ctx, &pb.EchoRequest{Name: "unary RPC msg "}, grpc.Header(&header), grpc.Trailer(&trailer))
		if err != nil {
			log.Fatalf("could not greet: %v", err)
		}
		time.Sleep(1 * time.Second)
		log.Printf("RPC Response: %v %v", i, r)
	}

}
