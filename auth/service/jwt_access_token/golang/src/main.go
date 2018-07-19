package main

/*

  Optimized API access for
*/
import (
	"fmt"
	"io/ioutil"
	"log"

	"golang.org/x/net/context"
	"golang.org/x/oauth2/google"

	"cloud.google.com/go/pubsub"
	"google.golang.org/api/iterator"
	"google.golang.org/api/option"
)

func main() {

	// https://github.com/googleapis/googleapis/blob/master/google/pubsub/pubsub.yaml#L6

	ctx := context.Background()
	projectID := "YOUR_PROJECT"
	keyfile := "service_account.json"

	audience := "https://pubsub.googleapis.com/google.pubsub.v1.Publisher"

	keyBytes, err := ioutil.ReadFile(keyfile)
	if err != nil {
		log.Fatalf("Unable to read service account key file  %v", err)
	}
	tokenSource, err := google.JWTAccessTokenSourceFromJSON(keyBytes, audience)
	if err != nil {
		log.Fatalf("Error building JWT access token source: %v", err)
	}
	jwt, err := tokenSource.Token()
	if err != nil {
		log.Fatalf("Unable to generate JWT token: %v", err)
	}
	fmt.Println(jwt.AccessToken)

	pubsubClient, err := pubsub.NewClient(ctx, projectID, option.WithTokenSource(tokenSource))
	if err != nil {
		log.Fatalf("Could not create pubsub Client: %v", err)
	}
	pit := pubsubClient.Topics(ctx)
	for {
		topic, err := pit.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			fmt.Println(err)
		}
		fmt.Println(topic)
	}

}
