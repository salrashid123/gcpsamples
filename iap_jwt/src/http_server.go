package main

import (
	"errors"
	"fmt"
	"log"
	"net/http"

	jwt "github.com/golang-jwt/jwt"
	"github.com/gorilla/mux"
	"github.com/lestrrat/go-jwx/jwk"
	"golang.org/x/net/http2"
)

var (
	jwtSet *jwk.Set
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

func getKey(token *jwt.Token) (interface{}, error) {

	keyID, ok := token.Header["kid"].(string)
	if !ok {
		return nil, errors.New("expecting JWT header to have string kid")
	}

	if key := jwtSet.LookupKeyID(keyID); len(key) == 1 {
		return key[0].Materialize()
	}

	return nil, errors.New("unable to find key")
}

func checkIAPHeaders(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

		iap_header := r.Header.Get(iap_header_name)
		if iap_header == "" && r.RequestURI != "/_ah/health" {
			http.Error(w, http.StatusText(http.StatusForbidden), http.StatusForbidden)
			w.Header().Set("Content-Type", "text/html; charset=UTF-8")
			return
		}

		token, err := jwt.ParseWithClaims(iap_header, &IAPClaims{}, getKey)

		if err != nil {
			log.Printf("Error parsing JWT %v", err)
			http.Error(w, http.StatusText(http.StatusForbidden), http.StatusForbidden)
			w.Header().Set("Content-Type", "text/html; charset=UTF-8")
			return
		}

		if claims, ok := token.Claims.(*IAPClaims); ok && token.Valid {
			log.Printf("Audience %v %v", claims.Audience, claims.StandardClaims.Issuer)
		} else {
			log.Printf("Error parsing Claims")
			http.Error(w, http.StatusText(http.StatusForbidden), http.StatusForbidden)
			w.Header().Set("Content-Type", "text/html; charset=UTF-8")
			return
		}

		next.ServeHTTP(w, r)
	})
}

func fronthandler(w http.ResponseWriter, r *http.Request) {
	log.Println("/ called")
	fmt.Fprint(w, "ok")
}

func healthhandler(w http.ResponseWriter, r *http.Request) {
	log.Println("heathcheck...")
	fmt.Fprint(w, "ok")
}

func main() {

	r := mux.NewRouter()

	r.Handle("/", checkIAPHeaders(http.HandlerFunc(fronthandler))).Methods("GET")
	r.Handle("/_ah/health", checkIAPHeaders(http.HandlerFunc(healthhandler))).Methods("GET")
	http.Handle("/", r)

	srv := &http.Server{
		Addr: ":8080",
	}
	http2.ConfigureServer(srv, &http2.Server{})

	/// load and cache the jwk set forever...
	var err error
	jwtSet, err = jwk.FetchHTTP(jwksURL)
	if err != nil {
		log.Fatal("Unable to load JWK Set: ", err)
	}

	//err := srv.ListenAndServeTLS("server_crt.pem", "server_key.pem")
	err = srv.ListenAndServe()
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
