package main

import (
	"log"

	"cloud.google.com/go/storage"

	"golang.org/x/net/context"

	"google.golang.org/api/iterator"
)

func main() {

	// ------------------  Using Google Cloud APIs --------------------------------

	ctx := context.Background()

	// with credential file
	//storeageClient, err := storage.NewClient(ctx, option.WithCredentialsFile("/path/to/svc_account.json"))
	// with env var
	// export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc_account.json
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
