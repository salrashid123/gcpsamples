/*
Package main demonstrates various Bigquery API operations.

To use, specify the project and a corresponding JSON service account file
with permissions on the target project.  If running from a
Google Compute Engine (GCE) VM, uncomment the corresponding section below
and ensure the GCE instance is created with a scope that is authorized for
the logging API.  For Google App Engine (GAE), add in the additional
imports and initialize the context using the request handler.

https://godoc.org/golang.org/x/oauth2/google
*/
package main

import (
	//"fmt"
	"log"
	"time"
	//"net/http"     // import for GAE and GCE

	// import for GAE
	/*
	   "google.golang.org/appengine"
	   "google.golang.org/appengine/urlfetch"
	*/

	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/bigquery/v2"
)

const (
	projectId              = "YOUR_PROJECT_ID"
	serviceAccountJSONFile = "YOUR_SERVICE_ACCOUNT_JSON_FILE"
)

func listDatasets(service *bigquery.Service, projectId string) {
	nextPageToken := ""
	for {
		resp, err := service.Datasets.List(projectId).PageToken(nextPageToken).Do()
		if err != nil {
			log.Fatalf("Unable to list Datasets: %s", err)
		}
		for _, ds := range resp.Datasets {
			log.Printf("Datasets %v", ds.Id)
		}
		nextPageToken = resp.NextPageToken
		if nextPageToken == "" {
			break
		}
	}
}

func startQuery(service *bigquery.Service, projectId string, querySQL string) (jobId *bigquery.JobReference, err error) {
	log.Printf("Inserting Query Job: %v", querySQL)
	bqjob := bigquery.Job{
		Configuration: &bigquery.JobConfiguration{
			Query: &bigquery.JobConfigurationQuery{
				Query: querySQL,
			},
		},
	}
	ins, err := service.Jobs.Insert(projectId, &bqjob).Do()
	if err != nil {
		log.Printf("Unable to acquire token source: %v", err)
		return nil, err
	}
	return ins.JobReference, nil
}

func checkQueryResults(service *bigquery.Service, projectId string, jobId *bigquery.JobReference) (job *bigquery.Job, err error) {
	startTime := time.Now().UnixNano() / 1000000
	var elapsedTime int64 = 0
	for {
		var pollJob, err = service.Jobs.Get(projectId, jobId.JobId).Do()
		if err != nil {
			log.Printf("Unable to acquire jobID: %v", err)
			return nil, err
		}
		elapsedTime = (time.Now().UnixNano() / 1000000) - startTime
		log.Printf("Job status (%vms) %v: %v\n", elapsedTime,
			jobId.JobId, pollJob.Status.State)
		if pollJob.Status.State == "DONE" {
			return pollJob, nil
		}
		// Pause execution for one second before polling job status again, to
		// reduce unnecessary calls to the BigQUery API and lower overall
		// application bandwidth.
		time.Sleep(1000 * time.Microsecond)
	}
}

func displayQueryResults(service *bigquery.Service, projectId string, completedJob *bigquery.Job) {
	queryResult, err := service.Jobs.GetQueryResults(projectId, completedJob.JobReference.JobId).Do()
	if err != nil {
		log.Fatalf("Unable to render query results: %v", err)
	}
	log.Printf("Query Results:------------")
	for _, row := range queryResult.Rows {
		for _, field := range row.F {
			log.Printf("%v", field.V)
		}
	}
}

func Main() {

	// Comment out setting the environment variable while running on GCE or GAE or if using
	// credentials from gcloud
	//os.Setenv("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountJSONFile)
	// End comment

	src, err := google.DefaultTokenSource(oauth2.NoContext, bigquery.BigqueryScope)
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}
	tok, err := src.Token()
	if err != nil {
		log.Fatalf("Credentials Token file can't be read")
	}

	// Use when running in GAE
	// r is the current *http.Request from the handler
	// https://cloud.google.com/appengine/docs/go/requests#Go_Requests_and_HTTP
	/*
	   ctx := appengine.NewContext(r)
	   src, err := google.DefaultTokenSource(ctx, bigquery.BigqueryScope)
	   if err != nil {
	           log.Infof(ctx, "Unable to acquire token source: %v", err)
	   }
	   client := &http.Client{
	           Transport: &oauth2.Transport{
	                   Source: src),
	                   Base:   &urlfetch.Transport{ Context: ctx },
	           },
	   }
	*/

	// For user interactive webflow
	/*
	   conf := &oauth2.Config{
	           ClientID:     "YOUR_CLIENT_ID",
	           ClientSecret: "YOUR_CLIENT_SECRET",
	           RedirectURL:  "urn:ietf:wg:oauth:2.0:oob",
	           Scopes: []string{
	                   bigquery.BigqueryScope,
	           },
	           Endpoint: google.Endpoint,
	   }

	   url := conf.AuthCodeURL("state")
	   log.Printf("Visit the URL for the auth dialog and: %v", url)

	   var code string
	   fmt.Println("Enter code: ")
	   if _, err = fmt.Scan(&code); err != nil {
	           log.Fatalf(err.Error())
	   }
	   tok, err = conf.Exchange(context.Background(), code)
	   if err != nil {
	           log.Fatalf(err.Error())
	   }
	*/
	src = oauth2.ReuseTokenSource(tok, src)

	client := oauth2.NewClient(context.Background(), src)
	service, err := bigquery.New(client)
	if err != nil {
		log.Fatalf("Unable to create logging client: %v", err)
	}

	// First list out all the available datasets
	listDatasets(service, projectId)

	// Start a Query Job
	querySql := "SELECT TOP(word, 50), COUNT(*) FROM publicdata:samples.shakespeare"
	jobId, err := startQuery(service, projectId, querySql)
	if err != nil {
		log.Fatalf("Unable to get submit Query: %v", err)
	}
	completedJob, err := checkQueryResults(service, projectId, jobId)
	if err != nil {
		log.Fatalf("Unable to get submit Query: %v", err)
	}
	log.Printf(completedJob.JobReference.JobId)

	// Return and display the results of the Query Job
	displayQueryResults(service, projectId, completedJob)

}
