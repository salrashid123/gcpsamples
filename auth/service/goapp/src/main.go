package main

import (
	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"
	"log"
	//        "io/ioutil"
	"os"
)

func main() {

	serviceAccountJSONFile := "YOUR_SERVICE_ACCOUNT_JSON_FILE"

	//dat, err := ioutil.ReadFile(serviceAccountJSONFile)
	//if err != nil {
	//      log.Fatalf("Unable to read service account file %v", err)
	//}
	//conf, err := google.JWTConfigFromJSON(dat, oauthsvc.UserinfoEmailScope)
	//if err != nil {
	//      log.Fatalf("Unable to acquire generate config: %v", err)
	//}
	//src := conf.TokenSource(oauth2.NoContext)
	//client := conf.Client(oauth2.NoContext)

	os.Setenv("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountJSONFile)
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
