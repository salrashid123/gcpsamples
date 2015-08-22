package main
import (
    "fmt"
    "golang.org/x/net/context"
    "golang.org/x/oauth2"
    "golang.org/x/oauth2/google"
    "log"
    oauthsvc "google.golang.org/api/oauth2/v2"
)
func main() {
    conf := &oauth2.Config{
        ClientID:     "YOUR_CLIENT_ID",
        ClientSecret: "YOUR_CLIENT_SECRET",
        RedirectURL:  "urn:ietf:wg:oauth:2.0:oob",
        Scopes: []string{
            oauthsvc.UserinfoEmailScope,
        },
        Endpoint: google.Endpoint,
    }
    url := conf.AuthCodeURL("state")
    log.Println("Visit the URL for the auth dialog: ", url)
    var code string
    log.Print("Enter auth token: ")
    if _, err := fmt.Scan(&code); err != nil {
        log.Fatalf(err.Error())
    }
    tok, err := conf.Exchange(context.Background(), code)
    if err != nil {
        log.Fatalf(err.Error())
    }
    //client := conf.Client(context.Background(),tok)
    src := conf.TokenSource(context.Background(),tok)
    client := oauth2.NewClient(context.Background(), src)
    service, err := oauthsvc.New(client)
    if err != nil {
        log.Fatalf("Unable to create oauth2 client: %v", err)
    }
    ui, err := service.Userinfo.Get().Do()
    if err != nil {
        log.Fatalf("ERROR: ", err)
    }
    log.Printf("UserInfo: %v", ui.Email)
}

