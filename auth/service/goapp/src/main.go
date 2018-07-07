package main

import (
	"log"
	"os"

	"cloud.google.com/go/storage"

	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"
	//"io/ioutil"
	//"os"

	"google.golang.org/api/iterator"
	//"google.golang.org/api/option"
)

func main() {

	serviceAccountJSONFile := "YOUR_SERVICE_ACCOUNT_JSON_FILE"

	// A: Uncomment and set service account file directly:

	//dat, err := ioutil.ReadFile(serviceAccountJSONFile)
	//if err != nil {
	//      log.Fatalf("Unable to read service account file %v", err)
	//}
	//conf, err := google.JWTConfigFromJSON(dat, oauthsvc.UserinfoEmailScope)
	//if err != nil {
	//      log.Fatalf("Unable to acquire generate config: %v", err)
	//}
	//client := conf.Client(oauth2.NoContext)

	// B: Uncomment use env variable that sets service account credentials

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

	// ------------------  Using Google Cloud APIs --------------------------------

	ctx := context.Background()
	/*
		tokenSource, err := google.DefaultTokenSource(oauth2.NoContext, storage.ScopeReadOnly)
		if err != nil {
			log.Fatalf("Unable to acquire token source: %v", err)
		}
		storeageClient, err := storage.NewClient(ctx, option.WithTokenSource(tokenSource))
	*/

	storeageClient, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatalf("Unable to acquire storage Client: %v", err)
	}

	it := storeageClient.Buckets(ctx, "YOUR_PROJECT_HERE")
	for {
		bucketAttrs, err := it.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf(bucketAttrs.Name)
	}
}
