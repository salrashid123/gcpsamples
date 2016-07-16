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
            /*
            string CREDENTIAL_FILE_PKCS12 = "c:\\your_pkcs_cert.p12";
            string serviceAccountEmail = "YOUR_SERVICE_ACCOUNT_EMAIL@developer.gserviceaccount.com";
            var certificate = new X509Certificate2(CREDENTIAL_FILE_PKCS12, "notasecret", X509KeyStorageFlags.Exportable);
            ServiceAccountCredential credential = new ServiceAccountCredential(
               new ServiceAccountCredential.Initializer(serviceAccountEmail)
               {
                   Scopes = new[] { Oauth2Service.Scope.UserinfoEmail }
               }.FromCertificate(certificate));
            */
            
            string CREDENTIAL_FILE_JSON = "C:\\your_json_cert.json";
            Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE_JSON);
            
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] { Oauth2Service.Scope.UserinfoEmail });
            var service = new Oauth2Service(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "Oauth2 Sample",
            });
            Console.WriteLine(service.Userinfo.Get().Execute().Email);
        }

    }
}

