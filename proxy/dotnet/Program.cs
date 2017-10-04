using System;

using Google.Cloud.Storage.V1;
using Google.Cloud.PubSub.V1;

using Google.Apis.Auth.OAuth2;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Google.Api.Gax;
using Google.Api.Gax.Rest;
using Google.Apis.Http;
using System.Security.Cryptography.X509Certificates;
using Google.Apis.Services;
using Google.Apis.Storage.v1;

using System.Linq;
using System.IO;
using Grpc.Auth;
using Grpc.Core;

namespace main
{
    class Program
    {

        const string projectID = "mineral-minutia-820";

       [STAThread]
        static void Main(string[] args)
        { 
            new Program().Run().Wait();
        }

        private async Task Run()
        {

            string CREDENTIAL_FILE_PKCS12 = "/path/to/your/cert/file.p12"; 
            string serviceAccountEmail = "your_servce_account@yourproject.iam.gserviceaccount.com";
            var certificate = new X509Certificate2(CREDENTIAL_FILE_PKCS12, "notasecret",X509KeyStorageFlags.Exportable);
            ServiceAccountCredential credential = new ServiceAccountCredential(
               new ServiceAccountCredential.Initializer(serviceAccountEmail)
               {
                   //Scopes = new[] { StorageService.Scope.DevstorageReadOnly, PublisherClient.DefaultScopes },
                   Scopes = PublisherClient.DefaultScopes.Append(StorageService.Scope.DevstorageReadOnly),
                   HttpClientFactory = new ProxySupportedHttpClientFactory()
               }.FromCertificate(certificate));


            //GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();            
            //StorageService service = StorageClient.Create(credential);  

            StorageService service = new StorageService(new BaseClientService.Initializer
            {
                HttpClientInitializer = credential,
                ApplicationName = StorageClientImpl.ApplicationName,
                HttpClientFactory = new ProxySupportedHttpClientFactory(),
            });
            var client = new StorageClientImpl(service, null);

            foreach (var b in client.ListBuckets(projectID))
                Console.WriteLine(b.Name);

            ChannelCredentials channelCredentials = credential.ToChannelCredentials();
            Channel channel = new Channel(PublisherClient.DefaultEndpoint.ToString(), channelCredentials);
            PublisherSettings ps = new PublisherSettings();        
            PublisherClient publisher = PublisherClient.Create(channel,ps);
        
            foreach  (Topic t in publisher.ListTopics(new ProjectName(projectID)))
              Console.WriteLine(t.Name);
        }

    }

public class ProxySupportedHttpClientFactory : HttpClientFactory
{
    protected override HttpMessageHandler CreateHandler(CreateHttpClientArgs args)
    {
        //ICredentials credentials = new NetworkCredential("user1", "user1");
        //var proxy = new WebProxy("http://127.0.0.1:3128", true, null, credentials);
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