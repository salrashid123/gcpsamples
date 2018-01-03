package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strings"

	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"
	"google.golang.org/grpc/credentials/oauth"
)

func main() {

	/* USER ACCOUNT 3LO */
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

	/*  SVC ACCOUNT 2LO */
	data, err := ioutil.ReadFile("/path/to/svc/account.json")
	if err != nil {
		log.Fatal(err)
	}
	conf, err := google.JWTConfigFromJSON(data, oauthsvc.UserinfoEmailScope)
	if err != nil {
		log.Fatal(err)
	}
	svc_creds := conf.TokenSource(oauth2.NoContext)
	svc_tok, err := svc_creds.Token()
	// This will just give an access token, not much use here
	log.Println(svc_tok.TokenType, svc_tok.AccessToken)
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}
	if svc_tok.Extra("id_token") != nil {
		log.Printf("id_token: ", svc_tok.Extra("id_token").(string))
	}

	// this will acquire a jwt signed by the local service account
	jwt_access_token, err := google.JWTAccessTokenSourceFromJSON(data, "https://www.googleapis.com/oauth2/v4/token")
	if err != nil {
		log.Fatal(err)
	}

	t, err := jwt_access_token.Token()
	if err != nil {
		log.Fatal(err)
	}

	/* exchange this for the id_token */
        /* https://github.com/golang/oauth2/blob/master/google/jwt.go#L57   << add scope */
	//log.Println(t.AccessToken)
	log.Println("------------------------------------------")

	d := url.Values{}
	d.Set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
	d.Add("assertion", t.AccessToken)

	client := &http.Client{}
	req, err := http.NewRequest("POST", "https://www.googleapis.com/oauth2/v4/token", strings.NewReader(d.Encode()))
	if err != nil {
		log.Fatal(err)
	}
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")

	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	body, _ := ioutil.ReadAll(resp.Body)
	fmt.Println("response Body:", string(body))

}
