package main

import (
	"log"

	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"
)

func main() {
	//src := google.ComputeTokenSource("")
	src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}
	client := oauth2.NewClient(context.Background(), src)
	service, err := oauthsvc.New(client)
	if err != nil {
		log.Fatalf("Unable to create api service: %v", err)
	}
	ui, err := service.Userinfo.Get().Do()
	if err != nil {
		log.Fatalf("Unable to get userinfo: ", err)
	}
	log.Printf("UserInfo: %v", ui.Email)
}
