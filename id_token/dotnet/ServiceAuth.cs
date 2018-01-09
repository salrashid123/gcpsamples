using System;
using System.IO;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using Google.Apis;
using Google.Apis.Auth.OAuth2;
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
        }

        private async Task Run()
        {
            

            
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            if (credential.IsCreateScopedRequired)
                credential = credential.CreateScoped(new string[] { "https://www.googleapis.com/auth/userinfo.email" });
            String uc = await credential.UnderlyingCredential.GetAccessTokenForRequestAsync();
            Console.WriteLine("Done UserCredential " + uc);



            string CREDENTIAL_FILE_JSON = "/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json";
            //Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE_JSON);
            using (var stream = new FileStream(CREDENTIAL_FILE_JSON, FileMode.Open, FileAccess.Read))
            {
                ServiceAccountCredential svc_credential = GoogleCredential.FromStream(stream)
                                                .CreateScoped(new string[] { "https://www.googleapis.com/auth/userinfo.email" })
                                                .UnderlyingCredential as ServiceAccountCredential;

                
                String sc = await svc_credential.GetAccessTokenForRequestAsync();
                Console.WriteLine("Done ServiceAccountCredential " + sc);

                //https://github.com/google/google-api-dotnet-client/blob/master/Src/Support/Google.Apis.Auth/GoogleJsonWebSignature.cs
                //var certs = await GoogleJsonWebSignature.GetGoogleCertsAsync(SystemClock.Default, false, null);
                //var validPayload = await GoogleJsonWebSignature.ValidateAsync(jwt);
            }
        }

    }
}

