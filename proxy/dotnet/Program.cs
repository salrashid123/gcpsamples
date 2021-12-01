using System;

using Google.Cloud.Storage.V1;
using Google.Cloud.PubSub.V1;

using Google.Apis.Auth.OAuth2;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using Google.Apis.Http;
using Google.Apis.Services;
using Google.Apis.Storage.v1;

using Google.Api.Gax.ResourceNames;


namespace main
{
    class Program
    {

        const string projectID = "your-project";

        [STAThread]
        static void Main(string[] args)
        {
            new Program().Run().Wait();
        }

        private async Task Run()
        {

// 1. no basic auth, with usercredentials
// need to set export http_proxy=http://localhost:3128 for Pubsub
// need to set ProxySupportedHttpClientFactory for GCS and oauth2

//  auth Y
//  gcs Y
//  pubsub Y


// 1638323879.659    693 192.168.9.1 TCP_TUNNEL/200 45147 CONNECT storage.googleapis.com:443 - HIER_DIRECT/172.253.63.128 -
// 1638323879.659    884 192.168.9.1 TCP_TUNNEL/200 7349 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/142.251.45.10 -
// 1638323879.659    372 192.168.9.1 TCP_TUNNEL/200 7878 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/172.217.13.234 -


// 2. no basic auth, with service account credentials
// export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc_account.json
//  auth N
//  gcs N
//  pubsub Y

// 3. no basicauth, with ServiceAccountCredential
            //var stream = new FileStream("/path/to/svc_account.json", FileMode.Open, FileAccess.Read);
            //ServiceAccountCredential sacredential = ServiceAccountCredential.FromServiceAccountData(stream);
            GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
            credential = credential.CreateWithHttpClientFactory(new ProxySupportedHttpClientFactory());
            
            StorageService service = new StorageService(new BaseClientService.Initializer
            {
                HttpClientInitializer = credential,
                ApplicationName = StorageClientImpl.ApplicationName,
                HttpClientFactory = new ProxySupportedHttpClientFactory(),
            });
           var client = new StorageClientImpl(service, null);

            foreach (var b in client.ListBuckets(projectID))
                Console.WriteLine(b.Name);

            PublisherServiceApiClient publisher = PublisherServiceApiClient.Create();
            ProjectName projectName = ProjectName.FromProject(projectID);
            foreach (Topic t in publisher.ListTopics(projectName))
                Console.WriteLine(t.Name);
        }

    }

    public class ProxySupportedHttpClientFactory : HttpClientFactory
    {
        protected override HttpMessageHandler CreateHandler(CreateHttpClientArgs args)
        {
            var proxy = new WebProxy("http://127.0.0.1:3128", true, null, null);
            var webRequestHandler = new HttpClientHandler()
            {
                UseProxy = true,
                Proxy = proxy,
                UseCookies = false
            };
            return webRequestHandler;
        }
    }

}