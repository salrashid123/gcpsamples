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
 * Sample code to issue several basic Google Cloud Logging ooperations
 * using the Google Client Libraries.
 *
 * Usage:
 * Add projects references using NuGet
 * "Google.Apis.Logging.v1beta3"   (other dependencies will get added automatically)
 * https://cloud.google.com/logging/docs
 * https://cloud.google.com/logging/docs/api/ref/rest/v1beta3/projects/logs/entries/write#google.logging.v1.LogEntry
 * Generate and download service account .p12 from place it to c:\
 * specify the logID, projectId, serviceAccountEmail and certificateFile below.
 * authorize serviceAccountEmail for your target project.
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
using Google.Apis.Logging.v1beta3;
using Google.Apis.Logging.v1beta3.Data;
using Google.Apis.Services;

namespace GCLSample
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
        private const string logsId = "customLogID1";
        private const string RFC3339Format = "yyyy-MM-ddTHH:mm:ssZ";

        #endregion

        private async Task Run()
        {

            //Authentication Options:  1. JSON ServiceAccountCredential file, 2. ComputeCredential, 3. UserCredential (gcloud application credentials)

            //Set environment variable for 1.JSON ServiceAccountsCredential
            //string CREDENTIAL_FILE = "C:\\YOUR_JSON_FILE.json";

            // 2. ComputeCredentials will get used automatically use while running on GCE 

            //Set environment variable for 3. UserCredentials acquired using gcloud.
            //by default the gcloud credential file can be found at  https://cloud.google.com/sdk/gcloud/#gcloud.auth 
            //to override it, set
            //string CREDENTIAL_FILE = "c:\\application_default_credentials.json"
            //If GOOGLE_APPLICATION_CREDENTIALS is not set, GoogleCredential will attempt to automaticallyg find the gcloud credentials.

            //Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE);
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] { LoggingService.Scope.CloudPlatform });

            //Authentication Option 4: non-gcloud User interactive webflow.
            /*
            UserCredential credential;
            string CLIENTSECRETS_LOCATION = "c:\\client_secrets.json";
            using (var stream = new FileStream(CLIENTSECRETS_LOCATION, FileMode.Open, FileAccess.Read))
            {
                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { LoggingService.Scope.CloudPlatform }, Environment.UserName, CancellationToken.None);
            }
            */
            //string clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
            //string clientSecret = "YOUR_CLIENT_SECRET";
            //credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets { ClientId = clientId, ClientSecret = clientSecret },
            //    new[] { LoggingService.Scope.CloudPlatform }, Environment.UserName, CancellationToken.None);
            //Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));  

            var service = new LoggingService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "Cloud Logging Sample",
            });

            Console.WriteLine("Listing available LogServices");
            var nextPageToken = "";
            do
            {
                ProjectsResource.LogServicesResource.ListRequest lstRequest = service.Projects.LogServices.List(projectId);
                lstRequest.PageToken = nextPageToken;
                ListLogServicesResponse lstResponse = await lstRequest.ExecuteAsync();
                foreach (var ls in lstResponse.LogServices)
                {
                    Console.WriteLine(ls.Name);
                }
                nextPageToken = lstResponse.NextPageToken;
            } while (!String.IsNullOrEmpty(nextPageToken));
            Console.WriteLine("=============================");

            Console.WriteLine("Listing available Logs");
            nextPageToken = "";
            do
            {
                ProjectsResource.LogsResource.ListRequest lstRequest = service.Projects.Logs.List(projectId);
                lstRequest.PageToken = nextPageToken;
                ListLogsResponse lstResponse = await lstRequest.ExecuteAsync();
                foreach (var ls in lstResponse.Logs)
                {
                    Console.WriteLine(ls.Name);
                }
                nextPageToken = lstResponse.NextPageToken;
            } while (!String.IsNullOrEmpty(nextPageToken));
            Console.WriteLine("=============================");

            // Insert custom log messages.
            Console.WriteLine("Inserting custom log message");
            // First create two LogEntries to save.
            List<LogEntry> logEntriesList = new List<LogEntry>();

            // Define lables to apply to an individual message.
            var messageLabels = new Dictionary<string, string>();
            messageLabels.Add("localKey", "localValue");
            string rfc3339 = DateTime.Now.ToString(RFC3339Format);

            LogEntryMetadata meta = new LogEntryMetadata 
                {
                     Labels = messageLabels, 
                     ServiceName = "compute.googleapis.com", 
                     Severity = "INFO", 
                     Timestamp = rfc3339 
                };

            logEntriesList.Add(new LogEntry
                {    InsertId = "firstInsertID", 
                     Log = logsId, 
                     Metadata = meta, 
                     TextPayload = "First TextPayload Message" 
                });
            logEntriesList.Add(new LogEntry
                {    InsertId = "secondInsertID", 
                     Log = logsId,
                     Metadata = meta,
                     TextPayload = "Second TextPayload Message"
                });

            // Set some labels to apply to all the log entries:
            var globalLabels = new Dictionary<string, string>();
            // Compute labels
            globalLabels.Add("compute.googleapis.com/resource_type", "instance");
            globalLabels.Add("compute.googleapis.com/resource_id", "The C# Sample");
            // Or custom labels
            //globalLabels.Add("globalKey","globalValue");

            WriteLogEntriesRequest logsEntriesWriteReq = new WriteLogEntriesRequest 
                { 
                    CommonLabels = globalLabels,
                    Entries = logEntriesList 
                };

            // The first time this program is run, it will create logsID "customLogID1".
            // Subsequent runs will append to this log.
            var writeRequest = service.Projects.Logs.Entries.Write(logsEntriesWriteReq, projectId, logsId);
            WriteLogEntriesResponse writeResponse = await writeRequest.ExecuteAsync();
            // Successful Logs.Entries.Write will return an empty WriteLogsEntriesResponse.
            Console.WriteLine("Response: " + writeResponse.ToString());
        }
    }
}
