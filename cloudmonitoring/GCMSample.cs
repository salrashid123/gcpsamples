/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Sample code to issue several basic Google Cloud Monitoring (GCM) operations
 * using the Google Client Libraries.
 *
 * Usage:
 * Add projects references using NuGet
 * "Google.Apis.CloudMonitoring.v2beta2"   (other dependencies will get added automatically)
 * https://developers.google.com/api-client-library/dotnet/apis/cloudmonitoring/v2beta2
 * http://www.nuget.org/packages/Google.Apis.CloudMonitoring.v2beta2/
 * Generate and download service account .p12 from place it to c:\
 * specify the customMetric, projectId, serviceAccountEmail and certificateFile below.
 * authorize serviceAccountEmail for your target project.
 * ComputeCredentials requires Google API CLient library >=1.9.1
 * https://developers.google.com/api-client-library/dotnet/release_notes#version_191
 */

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.CloudMonitoring.v2beta2;
using Google.Apis.CloudMonitoring.v2beta2.Data;
using Google.Apis.Services;

namespace GCMSample
{
    internal class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                new Program().Run().Wait();
            }
            catch (AggregateException ex)
            {
                foreach (var err in ex.InnerExceptions)
                {
                    Console.WriteLine("ERROR: " + err.Message);
                }
            }
            Console.ReadKey();
        }

        #region Consts

        private const string projectId = "YOUR_PROJECTID";

        private const string customMetric = "custom.cloudmonitoring.googleapis.com/yourmetric";
        private const string standardMetric = "compute.googleapis.com/instance/uptime";
        private const string RFC3339Format = "yyyy-MM-ddTHH:mm:ssK";

        #endregion

        // Displays the entire time series including metrics, labels and any primitive or range values.
        private void displayTimeSeries(ListTimeseriesResponse timeseriesResp)
        {
            if (timeseriesResp.Timeseries != null)
            {
                foreach (var timeseries in timeseriesResp.Timeseries)
                {
                    if (timeseries.TimeseriesDesc.Metric != null)
                    {
                        Console.WriteLine(timeseries.TimeseriesDesc.Metric);
                    }
                    if (timeseries.TimeseriesDesc.Labels != null)
                    {
                        foreach (var label in timeseries.TimeseriesDesc.Labels)
                        {
                            Console.WriteLine("Label: " + label.Key + " --> " + label.Value);
                        }
                    }
                    foreach (var point in timeseries.Points)
                    {
                        Console.WriteLine(point.Start + " --> " + point.End);
                        Console.WriteLine("BoolValue: " + point.BoolValue);
                        Console.WriteLine("DoubleValue: " + point.DoubleValue);
                        Console.WriteLine("Int64Value: " + point.Int64Value);
                        Console.WriteLine("StringValue: " + point.StringValue);
                        if (point.DistributionValue != null)
                        {
                            foreach (var bkt in point.DistributionValue.Buckets)
                            {
                                Console.WriteLine(bkt.LowerBound + "..." + bkt.UpperBound + " Count " + bkt.Count);
                            }
                        }
                    }
                }
            }
        }

        private async Task Run()
        {

            //Authentication Options:  1. JSON ServiceAccountCredential file, 2. ComputeCredential, 3. UserCredential (gcloud application credentials)

            //Set environment variable for 1.JSON ServiceAccountsCredential
            //string CREDENTIAL_FILE = "C:\\YOUR_SERVICE_ACCOUNT.json";

            // 2. ComputeCredentials will get used automatically use while running on GCE 

            //Set environment variable for 3. UserCredentials acquired using gcloud.
            //by default the gcloud credential file can be found at  https://cloud.google.com/sdk/gcloud/#gcloud.auth 
            //to override it, set
            //string CREDENTIAL_FILE = "c:\\application_default_credentials.json"
            //If GOOGLE_APPLICATION_CREDENTIALS is not set, GoogleCredential will attempt to automaticallyg find the gcloud credentials.

            //Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE);
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] { CloudMonitoringService.Scope.Monitoring });

            //Authentication Option 4: non-gcloud User interactive webflow.
            /*
            UserCredential credential;
            string CLIENTSECRETS_LOCATION = "c:\\client_secrets.json";
            using (var stream = new FileStream(CLIENTSECRETS_LOCATION, FileMode.Open, FileAccess.Read))
            {
                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] {CloudMonitoringService.Scope.Monitoring }, Environment.UserName, CancellationToken.None);
            }
            */
            //string clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
            //string clientSecret = "YOUR_CLIENT_SECRET";
            //credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets { ClientId = clientId, ClientSecret = clientSecret },
            //    new[] { CloudMonitoringService.Scope.Monitoring }, Environment.UserName, CancellationToken.None);
            //Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));  


            var service = new CloudMonitoringService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "GCM Sample",
            });

            // List all GCM Metrics.
            var nextPageToken = "";
            while (nextPageToken != null)
            {
                MetricDescriptorsResource.ListRequest lreq = service.MetricDescriptors.List(projectId);
                lreq.PageToken = nextPageToken;
                ListMetricDescriptorsResponse lresp = await lreq.ExecuteAsync();
                foreach (var metric in lresp.Metrics)
                {
                    Console.WriteLine(metric.Name);
                }
                nextPageToken = lresp.NextPageToken;
            }

            DateTime now = DateTime.Now;
            ListTimeseriesResponse timeseriesResp = await service.Timeseries.List(projectId,
                standardMetric, now.ToString(RFC3339Format)).ExecuteAsync();
            displayTimeSeries(timeseriesResp);

            Console.WriteLine("Writing Custom Metric " + customMetric);

            // Construct the custom metric point and timeseries
            TimeseriesDescriptor timeseriesDescriptor = new TimeseriesDescriptor { Project = projectId, Metric = customMetric };
            // Write a custom metric point occuring now with a value of 10
            Point point = new Point { Start = now, End = now, Int64Value = 10 };
            TimeseriesPoint tsPoint = new TimeseriesPoint { TimeseriesDesc = timeseriesDescriptor, Point = point };

            WriteTimeseriesRequest wr = new WriteTimeseriesRequest();
            wr.Timeseries = new List<TimeseriesPoint>() { tsPoint };

            WriteTimeseriesResponse ws = await service.Timeseries.Write(wr, projectId).ExecuteAsync();
            // A successful Timeseries.Write responds with an empty response object.
            // Console.WriteLine("Timeseries.Write Response: " + ws.ToString());

            Console.WriteLine("Reading Custom Metric");

            ListTimeseriesResponse customTimeseriesResp = await service.Timeseries.List(projectId,
                customMetric, now.ToString(RFC3339Format)).ExecuteAsync();
            displayTimeSeries(customTimeseriesResp);

            Console.WriteLine("============================= ");
        }

    }
}