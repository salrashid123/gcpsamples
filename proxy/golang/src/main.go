package main

import (
	"log"

	"cloud.google.com/go/pubsub"
	"cloud.google.com/go/storage"
	"golang.org/x/net/context"
	"google.golang.org/api/iterator"
)

func main() {

	ctx := context.Background()

	gcs, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatal(err)
	}

	b := gcs.Buckets(ctx, "your_project")
	for {
		t, err := b.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf("bucket: %q\n", t.Name)
	}

	pub, err := pubsub.NewClient(ctx, "your_project")
	if err != nil {
		log.Fatal(err)
	}

	topics := pub.Topics(ctx)
	for {
		t, err := topics.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf("Topic: %q\n", t)
	}
}
