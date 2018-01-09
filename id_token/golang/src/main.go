package main

import (
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strings"

	"crypto/rsa"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"time"

	"github.com/coreos/go-oidc"

	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"

	"golang.org/x/oauth2/jws"
	oauthsvc "google.golang.org/api/oauth2/v2"
	"google.golang.org/grpc/credentials/oauth"
)

const (
	GOOGLE_ROOT_CERT_URL     = "https://www.googleapis.com/oauth2/v3/certs"
	SERVICE_ACCOUNT_CERT_URL = "https://www.googleapis.com/service_accounts/v1/metadata/x509/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com"
)

func doExchange(token string) (string, error) {
	d := url.Values{}
	d.Set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
	d.Add("assertion", token)

	client := &http.Client{}
	req, err := http.NewRequest("POST", "https://www.googleapis.com/oauth2/v4/token", strings.NewReader(d.Encode()))
	if err != nil {
		return "", err
	}
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")

	resp, err := client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	return string(body), nil
}

func main() {

	log.Println("Using gcloud ADC: ")
	/*
	  "aud": "764086051850-6qr4p6gpi6hn506pt8ejuq83di341hur.apps.googleusercontent.com",
      "iss": "accounts.google.com",
	*/
	ctx := context.Background()

	src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}

	creds := oauth.TokenSource{src}
	tok, err := creds.Token()
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}
	if tok.Extra("id_token") != nil {
		log.Printf("id_token: ", tok.Extra("id_token").(string))
	}

	/*************************************************************************/

	log.Println("------------------------------------------")

	data, err := ioutil.ReadFile("/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json")
	if err != nil {
		log.Fatal(err)
	}

	/*
		log.Println("Using ServiceAccount JWTAccessTokenSourceFromJSON ")
		// this will acquire a jwt ACCESS_TOKEN by the local service account
		jwt_access_token, err := google.JWTAccessTokenSourceFromJSON(data, "https://www.googleapis.com/oauth2/v4/token")
		if err != nil {
			log.Fatal(err)
		}
		t, err := jwt_access_token.Token()
		if err != nil {
			log.Fatal(err)
		}
		// exchanging it won't work as the 'scope' field or target_audience isn't set
		jwt_access, err := doExchange(t.AccessToken)
		if err != nil {
			log.Fatal(err)
		}
		log.Println(jwt_access)
	*/

	log.Println("Using ServiceAccount JWTConfigFromJSON and manually signed JWT ")

	conf, err := google.JWTConfigFromJSON(data, oauthsvc.UserinfoEmailScope)
	if err != nil {
		log.Fatal(err)
	}

	header := &jws.Header{
		Algorithm: "RS256",
		Typ:       "JWT",
		KeyID:     conf.PrivateKeyID,
	}

	// for iap/endpoints
	private_claims := map[string]interface{}{"target_audience": "https://yourapp.appspot.com/"}
	iat := time.Now()
	exp := iat.Add(time.Hour)

	payload := &jws.ClaimSet{
		Iss:           conf.Email,
		Iat:           iat.Unix(),
		Exp:           exp.Unix(),
		Aud:           "https://www.googleapis.com/oauth2/v4/token",
		PrivateClaims: private_claims,
	}

	// from https://github.com/golang/oauth2/blob/master/internal/oauth2.go#L23
	key := conf.PrivateKey
	block, _ := pem.Decode(key)
	if block != nil {
		key = block.Bytes
	}
	parsedKey, err := x509.ParsePKCS8PrivateKey(key)
	if err != nil {
		parsedKey, err = x509.ParsePKCS1PrivateKey(key)
		if err != nil {
			log.Fatal("private key should be a PEM or plain PKSC1 or PKCS8; parse error: %v", err)
		}
	}
	parsed, ok := parsedKey.(*rsa.PrivateKey)
	if !ok {
		log.Fatal("private key is invalid")
	}

	token, err := jws.Encode(header, payload, parsed)
	if err != nil {
		log.Fatal(err)
	}
	log.Println("Signed ID_token to exchange: ")
	log.Println(token)
	body, _ := doExchange(token)

	var y map[string]interface{}
	err = json.Unmarshal([]byte(body), &y)
	if err != nil {
		log.Fatal(err)
	}

	log.Println("Exchanged Google_ID_TOKEN:", y["id_token"].(string))
	/*
	"aud": "https://yourapp.appspot.com/",
    "iss": "https://accounts.google.com",
	*/
	keySet := oidc.NewRemoteKeySet(ctx, GOOGLE_ROOT_CERT_URL)

	var config = &oidc.Config{
		SkipClientIDCheck: true,
	}
	verifier := oidc.NewVerifier("https://accounts.google.com", keySet, config)

	idt, err := verifier.Verify(ctx, y["id_token"].(string))
	if err != nil {
		log.Fatal(err)
	}
	log.Printf("Verified id_token with issuer: ", idt.Issuer)
}
