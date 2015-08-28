package main

// mkdir extra
// export GOPATH=/path/to/where/the/extra/folder/is
// go get golang.org/x/oauth2
// go get google.golang.org/appengine/...
// go get google.golang.org/cloud/compute/...
// go get google.golang.org/api/oauth2/v2

// for vm: false
// google-cloud-sdk/go_appengine/goapp serve src/app.yaml
// google-cloud-sdk/go_appengine/goapp deploy src/app.yaml

// for vm: true
// uncomment appengine.Main() in func main()
// gcloud preview app run src/app.yaml
// gcloud preview app deploy src/app.yaml --version 1 --set-default

import (
	//"appengine"
	"fmt"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"
	"google.golang.org/appengine"
	"google.golang.org/appengine/log"
	"google.golang.org/appengine/urlfetch"
	"net/http"
)

const ()

func main() {
	// for vm: true
	// appengine.Main()
}

func init() {
	http.HandleFunc("/", mainhandler)
	http.HandleFunc("/_ah/health", healthhandler)
}

func healthhandler(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	fmt.Fprintln(w, "ok")
}

func mainhandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/plain")
	ctx := appengine.NewContext(r)
	//src := google.AppEngineTokenSource(ctx, oauthsvc.UserinfoEmailScope)
	src, err := google.DefaultTokenSource(ctx, oauthsvc.UserinfoEmailScope)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
	client := &http.Client{
		Transport: &oauth2.Transport{
			Source: src,
			Base:   &urlfetch.Transport{Context: ctx},
		},
	}
	client = oauth2.NewClient(ctx, src)
	service, err := oauthsvc.New(client)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
	ui, err := service.Userinfo.Get().Do()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
	log.Infof(ctx, "UserInfo: %v", ui.Email)
	fmt.Fprintln(w, "UserInfo: ", ui.Email)
}
