/*
 * Copyright (c) 2014 Google Inc.
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
 * Sample code to issue several basic Google Cloud Store (GCS) operations
 * using the Google Client Libraries.
 * For more information, see documentation for Compute Storage .NET client
 * https://developers.google.com/api-client-library/dotnet/apis/storage/v1
 *
 * Usage:
 * Add projects references using NuGet to
 * "Google.Apis.Storage.v1"   (other dependencies will get added automatically)
 * http://www.nuget.org/packages/Google.Apis.Storage.v1/
 * Generate and download service account JSON from place it to c:\
 * specify the bucketName, projectId, serviceAccountEmail and certificateFile below.
 * authorize serviceAccountEmail for your target bucket.
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
using Google.Apis.Download;
using Google.Apis.Services;
using Google.Apis.Storage.v1;
using Google.Apis.Storage.v1.Data;
using Google.Apis.Util.Store;

namespace GCSSample
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

        private const int KB = 0x400;
        private const int DownloadChunkSize = 256 * KB;

        #endregion

        private async Task Run()
        {
            string projectId = "YOUR_PROJECT";
            string bucketName = "YOUR_BUCKET";

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
                credential = credential.CreateScoped(new string[] { StorageService.Scope.DevstorageFullControl });

            //Authentication Option 4: non-gcloud User interactive webflow.
            /*
            UserCredential credential;
            string CLIENTSECRETS_LOCATION = "c:\\client_secrets.json";
            using (var stream = new FileStream(CLIENTSECRETS_LOCATION, FileMode.Open, FileAccess.Read))
            {
                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { StorageService.Scope.DevstorageFullControl }, Environment.UserName, CancellationToken.None);
            }
            */
            //string clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
            //string clientSecret = "YOUR_CLIENT_SECRET";
            //credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets { ClientId = clientId, ClientSecret = clientSecret },
            //    new[] { StorageService.Scope.DevstorageFullControl }, Environment.UserName, CancellationToken.None);
            //Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));  


            var service = new StorageService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "GCS Sample",
            });

            Console.WriteLine("List of buckets in current project");
            var nextPageToken = "";
            while (nextPageToken != null)
            {
                BucketsResource.ListRequest lreq = service.Buckets.List(projectId);
                lreq.PageToken = nextPageToken;

                Buckets lresp = await lreq.ExecuteAsync();
                foreach (var ls in lresp.Items)
                {
                    Console.WriteLine(ls.Name);
                }
                nextPageToken = lresp.NextPageToken;
            }

            Console.WriteLine("=============================");

            // using  Google.Apis.Storage.v1.Data.Object to disambiguate from System.Object
            Google.Apis.Storage.v1.Data.Object fileobj = new Google.Apis.Storage.v1.Data.Object() { Name = "somefile.txt" };

            Console.WriteLine("Creating " + fileobj.Name + " in bucket " + bucketName);
            byte[] msgtxt = Encoding.UTF8.GetBytes("Lorem Ipsum");

            using (var streamOut = new MemoryStream(msgtxt))
            {
                await service.Objects.Insert(fileobj, bucketName, streamOut, "text/plain").UploadAsync();
            }

            Console.WriteLine("Object created: " + fileobj.Name);

            Console.WriteLine("=============================");

            Console.WriteLine("Reading object " + fileobj.Name + " in bucket: " + bucketName);
            var req = service.Objects.Get(bucketName, fileobj.Name);
            Google.Apis.Storage.v1.Data.Object readobj = await req.ExecuteAsync();

            Console.WriteLine("Object MediaLink: " + readobj.MediaLink);

            // download using Google.Apis.Download and display the progress
            string pathUser = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            var fileName = Path.Combine(pathUser, "Downloads") + "\\" + readobj.Name;
            Console.WriteLine("Starting download to " + fileName);
            var downloader = new MediaDownloader(service)
            {
                ChunkSize = DownloadChunkSize
            };
            // add a delegate for the progress changed event for writing to console on changes
            downloader.ProgressChanged += Download_ProgressChanged;

            using (var fileStream = new System.IO.FileStream(fileName,
                System.IO.FileMode.Create, System.IO.FileAccess.Write))
            {
                var progress = await downloader.DownloadAsync(readobj.MediaLink, fileStream);
                if (progress.Status == DownloadStatus.Completed)
                {
                    Console.WriteLine(readobj.Name + " was downloaded successfully");
                }
                else
                {
                    Console.WriteLine("Download {0} was interpreted in the middle. Only {1} were downloaded. ",
                        readobj.Name, progress.BytesDownloaded);
                }
            }

            /*
            // or download as a stream
            Stream getStream = await service.HttpClient.GetStreamAsync(readobj.MediaLink);
            Console.WriteLine("Object Content: ");
            using (var reader = new StreamReader(getStream))
            {
                Console.WriteLine(reader.ReadToEnd());
            }
            */
            Console.WriteLine("=============================");
        }

        #region Progress and Response changes

        static void Download_ProgressChanged(IDownloadProgress progress)
        {
            Console.WriteLine(progress.Status + " " + progress.BytesDownloaded + " bytes");
        }

        #endregion
    }
}