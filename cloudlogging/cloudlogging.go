/*
Package main demonstrates various Cloud Logging API operations.
Specifically, it lists all LogServices, then displays the available Logs, and
finally inserts a custom log line.

To use, specify the project and a corresponding JSON service account file
with permissions on the target project.  If you are running this sample from a
workstation (not GCE), you need to download the JSON file from the cloud console and set the
project and serviceAccountJSONFile below.  If running from inside a
Google Compute Engine (GCE) VM, comment the section initializing the client from JSON serviceaccount and
uncomment the corresponding section for GCE.  The GCE instance must have its scope authorized for
the logging API (i.e., --scope "https://www.googleapis.com/auth/logging").  For Google App Engine (GAE),
add in the additional imports and initialize the context using the request handler.

https://cloud.google.com/logging/docs
https://cloud.google.com/logging/docs/api/ref/rest/v1beta3/projects/logs/entries/write#google.logging.v1.LogEntry
https://godoc.org/google.golang.org/api/logging
https://cloud.google.com/storage/docs/authentication#service_accounts
https://godoc.org/golang.org/x/oauth2/google
*/
package main

import (
	"io/ioutil"
	"log"
	"time"
	//"net/http"     // import for GAE and GCE

	// import for GAE
	/*
		"google.golang.org/appengine"
		"google.golang.org/appengine/urlfetch"
	*/

	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/logging/v1beta3"
)

const (
	project                = "YOUR_PROJECT"
	serviceAccountJSONFile = "YOUR_SERVICE_ACCOUNT_JSON_FILE.json"
	logsID                 = "customLogID1"
)

func main() {

	// Initialize a client with service account JSON files.
	// Comment out this section if running from within GCE or GAE.
	fileBytes, err := ioutil.ReadFile(serviceAccountJSONFile)
	if err != nil {
		log.Fatalf("Unable to read JSON service account file: %v", err)
	}

	conf, err := google.JWTConfigFromJSON(fileBytes, "https://www.googleapis.com/auth/logging.admin")
	if err != nil {
		log.Fatalf("Unable to parse JSON service account file: %v", err)
	}

	client := conf.Client(oauth2.NoContext)

	// Use when running in GCE
	// The GCE instance must have the monitoring scope already enabled.
	// (--scope "https://www.googleapis.com/auth/logging")
	// see https://cloud.google.com/compute/docs/authentication#using
	/*
		client := &http.Client{
			Transport: &oauth2.Transport{
				Source: google.ComputeTokenSource(""),
			},
		}
	*/

	// Use when running in GAE
	// r is the current *http.Request from the handler
	// https://cloud.google.com/appengine/docs/go/requests#Go_Requests_and_HTTP
	/*
		ctx := appengine.NewContext(r)
		client := &http.Client{
			Transport: &oauth2.Transport{
				Source: google.AppEngineTokenSource(ctx, logging.CloudPlatformScope),
				Base:   &urlfetch.Transport{Context: ctx},
			},
		}
	*/

	service, err := logging.New(client)
	if err != nil {
		log.Fatalf("Unable to create logging client: %v", err)
	}

	// First list out all the available LogServices
	nextPageToken := ""
	for {
		resp, err := service.Projects.LogServices.List(project).PageToken(nextPageToken).Do()
		if err != nil {
			log.Fatalf("Unable to list LogServices: %s", err)
		}
		for _, logservice := range resp.LogServices {
			log.Printf(" Name %v", logservice.Name)
			log.Printf(" Name %v", logservice.IndexKeys)
		}
		nextPageToken = resp.NextPageToken
		if nextPageToken == "" {
			break
		}
	}

	// List out all the available Logs.
	nextPageToken = ""
	for {
		resp, err := service.Projects.Logs.List(project).PageToken(nextPageToken).Do()
		if err != nil {
			log.Fatalf("Unable to list Logs: %s", err)
		}
		for _, logobj := range resp.Logs {
			log.Printf(" Name %v", logobj.Name)
		}
		nextPageToken = resp.NextPageToken
		if nextPageToken == "" {
			break
		}
	}

	// Insert custom log messages.
	// First create two LogEntries to save.
	now := time.Now().UTC().Format(time.RFC3339)
	ent := make([]*logging.LogEntry, 2)

	// Define lables to apply to an individual message
	messageLabels := make(map[string]string)
	messageLabels["localKey"] = "localValue"

	ent[0] = &logging.LogEntry{
		InsertId: "firstInsertID",
		Log:      logsID,
		Metadata: &logging.LogEntryMetadata{
			Labels:      messageLabels,
			ServiceName: "compute.googleapis.com",
			Severity:    "INFO",
			Timestamp:   now,
		},
		TextPayload: "First TextPayload Message",
	}

	ent[1] = &logging.LogEntry{
		InsertId: "secondInsertID",
		Log:      logsID,
		Metadata: &logging.LogEntryMetadata{
			ServiceName: "compute.googleapis.com",
			Severity:    "INFO",
			Timestamp:   now,
		},
		TextPayload: "Second TextPayload Message",
	}

	// Set some labels to apply to all the log entries:
	globalLabels := make(map[string]string)
	// compute labels
	globalLabels["compute.googleapis.com/resource_type"] = "instance"
	globalLabels["compute.googleapis.com/resource_id"] = "The Go Sample"
	// or custom labels
	//globalLabels["globalKey"] = "globalValue"

	logsWriteReq := logging.WriteLogEntriesRequest{
		CommonLabels: globalLabels,
		Entries:      ent,
	}

	// The first time this program is run, it will create logsID "customLogID1".
	// Subsequent runs will append to this log.
	writeResp, err := service.Projects.Logs.Entries.Write(project, logsID, &logsWriteReq).Do()
	if err != nil {
		log.Fatalf("Unable to write custom Log: %v", err)
	}
	// Successful Logs.Entries.Write will return an empty WriteLogsEntriesResponse
	log.Printf("Logs.Entries.Write  response: %v", writeResp)
}
