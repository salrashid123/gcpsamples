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
            UserCredential credential;
            string CLIENTSECRETS_LOCATION = "c:\\client_secrets.json";
            using (var stream = new FileStream(CLIENTSECRETS_LOCATION, FileMode.Open, FileAccess.Read))
            {
                credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(
                    GoogleClientSecrets.Load(stream).Secrets,
                    new[] { Oauth2Service.Scope.UserinfoEmail }, Environment.UserName, CancellationToken.None);
            }
            /*
            string clientId = "YOUR_CLIENT_ID";
            string clientSecret = "YOUR_CLIENT_SECRET";
            credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets { ClientId = clientId, ClientSecret = clientSecret },
                new[] { Oauth2Service.Scope.UserinfoEmail }, Environment.UserName, CancellationToken.None);
            Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
            */
            var service = new Oauth2Service(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "Oauth2 Sample",
            });
            Console.WriteLine(service.Userinfo.Get().Execute().Email);
        }

    }
}

