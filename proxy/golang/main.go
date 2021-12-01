package main

import (
	"log"

	"cloud.google.com/go/pubsub"
	"cloud.google.com/go/storage"
	"golang.org/x/net/context"
	"google.golang.org/api/iterator"
)

/*

1. user auth
   export http_proxy=http://localhost:3128

   no traffic

2. user auth
    export https_proxy=http://localhost:3128

	pubsub Y
	auth Y
	gcs Y

	1638363908.054   1043 192.168.9.1 TCP_TUNNEL/200 7779 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.15.74 -
	1638363908.054    930 192.168.9.1 TCP_TUNNEL/200 115190 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.73.208 -
	1638363908.054    390 192.168.9.1 TCP_TUNNEL/200 4886 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

3. service account auth
   export https_proxy=http://localhost:3128

   pubsub Y
   auth Y
   gcs Y

	1638363985.052    837 192.168.9.1 TCP_TUNNEL/200 5610 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.15.74 -
	1638363985.052    771 192.168.9.1 TCP_TUNNEL/200 115190 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.73.208 -
	1638363985.052    278 192.168.9.1 TCP_TUNNEL/200 4885 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

4. basic + user auth
   export https_proxy=http://user1:user1@localhost:3128

   pubsub Y
   auth Y
   gcs Y
	1638364205.519    338 192.168.9.1 TCP_TUNNEL/200 7240 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -
	1638364205.521    884 192.168.9.1 TCP_TUNNEL/200 7660 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
	1638364205.521    805 192.168.9.1 TCP_TUNNEL/200 115189 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -

5. basic service account auth
   export https_proxy=http://user1:user1@localhost:3128

   pubsub Y
   auth Y
   gcs Y

	1638364340.203    155 192.168.9.1 TCP_TUNNEL/200 4886 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -
	1638364340.203    680 192.168.9.1 TCP_TUNNEL/200 5810 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
	1638364340.203    623 192.168.9.1 TCP_TUNNEL/200 115191 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -
*/
func main() {

	ctx := context.Background()

	gcs, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatal(err)
	}

	b := gcs.Buckets(ctx, "your-project")
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

	pub, err := pubsub.NewClient(ctx, "your-project")
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
