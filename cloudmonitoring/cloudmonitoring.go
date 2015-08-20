/*
	Package main demonstrates various Cloud Monitoring API operations.
	Specifically, it lists all MetricDescriptors, then displays the timeseries
	for the metric compute.googleapis.com/instance/uptime.  It inserts a custom
	metric and finally recalls timeseries values for that custom metric.

	To use, specify the project and a corresponding JSON service account file
	with permissions on the target project.  If running from a
	Google Compute Engine (GCE) VM, uncomment the corresponding section below
	and ensure the GCE instance is created with a scope that is authorized for
	the monitoring API.  For Google App Engine (GAE), add in the additional
	imports and initialize the context using the request handler.

	https://cloud.google.com/monitoring/docs
	https://godoc.org/google.golang.org/api/cloudmonitoring
	https://cloud.google.com/storage/docs/authentication#service_accounts
	https://godoc.org/golang.org/x/oauth2/google
*/
package main

import (
	"io/ioutil"
	"log"
	//"net/http"     // import for GAE and GCE
	"strings"
	"time"

	// import for GAE
	/*
		"google.golang.org/appengine"
		"google.golang.org/appengine/urlfetch"
	*/

	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/cloudmonitoring/v2beta2"
)

const (
	project                = "YOUR_PROJECT"
	serviceAccountJSONFile = "YOUR_SERVICE_ACCOUNT_JSON_FILE.json"
	customMetric           = "custom.cloudmonitoring.googleapis.com/yourmetric"
	standardMetric         = "compute.googleapis.com/instance/uptime"
)

func displayTimeseriesResponse(metric *cloudmonitoring.ListTimeseriesResponse) {
	for _, timeseries := range metric.Timeseries {
		log.Printf("TimeSeries for  %v", timeseries.TimeseriesDesc.Metric)
		for _, label := range timeseries.TimeseriesDesc.Labels {
			log.Printf("Label: %v", label)
		}
		for _, point := range timeseries.Points {
			log.Println(strings.Repeat("-", 50))
			log.Printf("Start/End  %v -->  %v", point.Start, point.End)
			log.Printf("BoolValue %v", point.BoolValue)
			log.Printf("DoubleValue %v", point.DoubleValue)
			log.Printf("Int64Value %v", point.Int64Value)
			log.Printf("StringValue %v", point.StringValue)
			if point.DistributionValue != nil {
				log.Printf("DistributionValue: ")
				for _, bkt := range point.DistributionValue.Buckets {
					log.Printf("[%v...%v]  --->  %v", bkt.LowerBound, bkt.UpperBound, bkt.Count)
				}
			}
		}
	}
}

func main() {

	// Initialize a client with service account JSON files.
	// Comment out this section if running from within GCE or GAE.
	fileBytes, err := ioutil.ReadFile(serviceAccountJSONFile)
	if err != nil {
		log.Fatalf("Unable to read JSON service account file: %v", err)
	}

	conf, err := google.JWTConfigFromJSON(fileBytes, cloudmonitoring.MonitoringScope)
	if err != nil {
		log.Fatalf("Unable to parse JSON service account file: %v", err)
	}

	client := conf.Client(oauth2.NoContext)

	// Use when running in GCE
	// The GCE instance must have the monitoring scope already enabled.
	// (--scope "https://www.googleapis.com/auth/monitoring")
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
				Source: google.AppEngineTokenSource(ctx, cloudmonitoring.MonitoringScope),
				Base:   &urlfetch.Transport{Context: ctx},
			},
		}
	*/

	service, err := cloudmonitoring.New(client)
	if err != nil {
		log.Fatalf("Unable to create cloudmonitoring client: %v", err)
	}

	// List all the MetricDescriptors. Use nextPageToken for pagination.
	nextPageToken := ""
	for {
		resp, err := service.MetricDescriptors.List(project,
			&cloudmonitoring.ListMetricDescriptorsRequest{}).PageToken(nextPageToken).Do()
		if err != nil {
			log.Fatalf("Unable to list MetricDescriptors: %s", err)
		}
		for _, metric := range resp.Metrics {
			log.Printf("MetricDescriptor Name %v", metric.Name)
		}
		nextPageToken = resp.NextPageToken
		if nextPageToken == "" {
			break
		}
	}

	// List the standard timeseries values for the current timestamp
	now := time.Now().Format(time.RFC3339)
	metric, err := service.Timeseries.List(project, standardMetric, now,
		&cloudmonitoring.ListTimeseriesRequest{}).Do()
	if err != nil {
		log.Fatalf("Unable to list Timeseries: %v", err)
	}
	displayTimeseriesResponse(metric)

	// Now setup a custom metric with a single point with value of 10
	timeseriesReq := cloudmonitoring.WriteTimeseriesRequest{
		Timeseries: []*cloudmonitoring.TimeseriesPoint{
			&cloudmonitoring.TimeseriesPoint{
				Point: &cloudmonitoring.Point{
					Start:      now,
					End:        now,
					Int64Value: 10, // insert a value of 10
				},
				TimeseriesDesc: &cloudmonitoring.TimeseriesDescriptor{
					Project: project,
					Metric:  customMetric,
				},
			},
		},
	}

	// Write the custom metric
	writeResp, err := service.Timeseries.Write(project, &timeseriesReq).Do()
	if err != nil {
		log.Fatalf("Unable to write custom Metric: %v", err)
	}
	log.Printf("Custom metric timeseries written %v", writeResp.Kind)

	// Read the custom metric series
	metric, err = service.Timeseries.List(project, customMetric, now,
		&cloudmonitoring.ListTimeseriesRequest{}).Do()
	if err != nil {
		log.Fatalf("Unable to list timeseries: %v", err)
	}
	displayTimeseriesResponse(metric)
}
