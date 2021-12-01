using System;
using System.IO;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.Oauth2.v2;
using Google.Apis.Oauth2.v2.Data;
using Google.Apis.Services;

using Google.Cloud.Storage.V1;

namespace Oauth2Harness
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

        private async Task Run()
        {

            // export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc_account.json
            
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            //var stream = new FileStream("/path/to/svc_account.json", FileMode.Open, FileAccess.Read);
            //ServiceAccountCredential credential = ServiceAccountCredential.FromServiceAccountData(stream);
            var client = StorageClient.Create();
            string project_id="your-project"; 
            foreach (var obj in client.ListObjects(project_id, ""))
            {
                Console.WriteLine(obj.Name);
            }


        }

    }
}

