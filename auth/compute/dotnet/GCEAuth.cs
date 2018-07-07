using System;
using System.IO;
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
            //ComputeCredential credentials = new ComputeCredential(new ComputeCredential.Initializer());
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] { Oauth2Service.Scope.UserinfoEmail });
            var service = new Oauth2Service(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "Oauth2 Sample",
            });
            Console.WriteLine(service.Userinfo.Get().Execute().Email);

            var client = StorageClient.Create();
            
            foreach (var obj in client.ListObjects("your-project", ""))
            {
                Console.WriteLine(obj.Name);
            }

        }

    }
}

