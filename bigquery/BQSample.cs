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
 * Example of authorizing with BigQuery and reading from a public dataset
 * using the Google Client Libraries.
 * 
 * Usage:
 * Add projects references using NuGet
 * "Google.Apis.Bigquery.v2"   (other dependencies will get added automatically)
 * Generate and download service account .p12 from place it to c:\
 * specify the projectId, serviceAccountEmail and certificateFile below.
 */

using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.Bigquery.v2;
using Google.Apis.Bigquery.v2.Data;
using Google.Apis.Services;

namespace BQSample
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

        private const string projectId = "YOUR_PROJECT_ID";  
        #endregion

        private async Task Run()
        {
            //Authentication Options:  1. JSON ServiceAccountCredential file, 2. ComputeCredential, 3. UserCredential (gcloud application credentials)

            //Set environment variable for 1.JSON ServiceAccountsCredential
            //string CREDENTIAL_FILE = "C:\\YOUR_CRED_FILE.json";

            // 2. ComputeCredentials will get used automatically use while running on GCE 

            //Set environment variable for 3. UserCredentials acquired using gcloud.
            //by default the gcloud credential file can be found at  https://cloud.google.com/sdk/gcloud/#gcloud.auth 
            //to override it, set
            //string CREDENTIAL_FILE = "c:\\application_default_credentials.json"
            //If GOOGLE_APPLICATION_CREDENTIALS is not set, GoogleCredential will attempt to automaticallyg find the gcloud credentials.

            //Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE);
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] {BigqueryService.Scope.Bigquery});
            
            //Authentication Option 4: non-gcloud User interactive webflow.
            /*
            UserCredential credential;
            string CLIENTSECRETS_LOCATION = "c:\\client_secrets.json";
            using (var stream = new FileStream(CLIENTSECRETS_LOCATION, FileMode.Open, FileAccess.Read))
            {
                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { BigqueryService.Scope.Bigquery }, Environment.UserName, CancellationToken.None);
            }
            */
            //string clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
            //string clientSecret = "YOUR_CLIENT_SECRET";
            //credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets { ClientId = clientId, ClientSecret = clientSecret },
            //    new[] { BigqueryService.Scope.Bigquery }, Environment.UserName, CancellationToken.None);
            //Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));            
            
            var service = new BigqueryService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "BigQuery Sample",
            });

            Console.WriteLine("Listing Available Datasets on " + projectId);
            listDatasets(service,projectId);
            Console.WriteLine("=============================");

            Console.WriteLine("Running sample query on publicdata");
            string querySql = "SELECT TOP(word, 50), COUNT(*) FROM publicdata:samples.shakespeare";
            Console.WriteLine(querySql);
            JobReference jobId = startQuery(service, projectId, querySql);
            Console.WriteLine("=============================");

            // Poll for Query Results, return result output
            Job completedJob = checkQueryResults(service, projectId, jobId);

            // Return and display the results of the Query Job
            displayQueryResults(service, projectId, completedJob);

        }

        // Display all BigQuery datasets associated with a project.
        private void listDatasets(BigqueryService bigquery, string projectId)
        {
            DatasetList datasetRequest = bigquery.Datasets.List(projectId).Execute();
            string nextPageToken = datasetRequest.NextPageToken;
            do
            { 
                if (datasetRequest.Datasets != null)
                    foreach (var dataset in datasetRequest.Datasets)
                    {
                         Console.WriteLine(dataset.Id);
                    }
            } while (!String.IsNullOrEmpty(nextPageToken));
        }

        // Creates a Query Job for a particular query on a dataset.
        private JobReference startQuery(BigqueryService bigquery, string projectId, string querySql)
        {
            Console.WriteLine("Inserting Query Job: " + querySql);            
            JobConfigurationQuery queryConfig = new JobConfigurationQuery{ Query = querySql };
            JobConfiguration config = new JobConfiguration{ Query = queryConfig };
            Job job = new Job { Configuration = config };
            JobsResource.InsertRequest insert = bigquery.Jobs.Insert(job, projectId);
            JobReference jobId = insert.Execute().JobReference;
            Console.WriteLine("Job ID of Query Job is: " + jobId.JobId);
            return jobId;
        }

      // Polls the status of a BigQuery job, returns Job reference if "Done".
      private Job checkQueryResults(BigqueryService bigquery, String projectId, JobReference jobId) {
        // Variables to keep track of total query time
        double startTime = DateTime.Now.Millisecond;
        while (true) {
          Job pollJob = bigquery.Jobs.Get(projectId, jobId.JobId).Execute();
          var duration = DateTime.Now.Millisecond - startTime;
          Console.WriteLine("Job status: ElapsedTime [" +  duration + "ms]  JobID: [" +
              jobId.JobId + "]  JobState: [" + pollJob.Status.State + "]");
          if (pollJob.Status.State.Equals("DONE")) {
            return pollJob;
          }
          // Pause execution for one second before polling job status again, to
          // reduce unnecessary calls to the BigQUery API and lower overall
          // application bandwidth.
          Thread.Sleep(1000);
        }
      }

      private void displayQueryResults(BigqueryService bigquery, String projectId, Job completedJob) 
      {
        GetQueryResultsResponse queryResult = bigquery.Jobs.GetQueryResults(projectId, completedJob.JobReference.JobId).Execute();
        Console.WriteLine("Query Results:");
        foreach (var row in queryResult.Rows) {
          foreach (var field in row.F) {
              Console.WriteLine(field.V);
           }
          Console.WriteLine();
        }
      }
    }
}
