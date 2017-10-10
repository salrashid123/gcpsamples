
# Accessing Google Cloud APIs though a Proxy


Proxy servers are not uncommon and I found some of my customers accessing Google Cloud APIs thorough them.

Most of the time, its a simple forward proxy with no authentication and never with SSL interception (which IMHO, is a really questionable, see [link](https://github.com/salrashid123/squid_proxy#https-intercept))

Fortunately, many of our APIs piggyback off of the native language proxy configuration settings (eg. ```http_proxy``` env variable) so that part
makes it easier.

This article describes the various proxy configurations you can use while accessing google APIs and I thought i'd consolidate some of my findings and issues with configuring google libraries into a doc to share.

For background, please see the two types of libraries available to access Google APIs though in this article, the focus is google cloud client libraries.

- [Client Libraries Explained](https://cloud.google.com/apis/docs/client-libraries-explained)

The recommended library set to use is the idiomatic "Cloud Client Library" which itself comes in two different transport mechanisms:  HTTP and gRPC.  At the time of writing (10/17), some APIs support both transports, some HTTP only (GCS, BigQiery), while other gRPC (PubSub).  How users enable and configure proxy support is slightly different for these transports.

One other complication to account for is that the authentication step (i.,e getting a GoogleCredential()) uses HTTP even if the underlying RPC is using gRPC.  For example, with java, you have to account for the credential via HTTP and the rpc call via gRPC.


# Python

## Cloud Client Library    ([google-cloud-python](https://github.com/GoogleCloudPlatform/google-cloud-python))

### HTTP

The GCS and BQ libraries currently use HTTP Transports.  To enable proxy support, use the env_variable:

```
export  https_proxy=http://user1:user1@127.0.0.1:3128
```

Once that is set, the sample application will contact the proxy for both the authentication and api call:

```python
from google.cloud import storage

client = storage.Client(project="your_project")
for b in client.list_buckets():
   print(b.name)
```

Meaning in the proxy access logs, you would see:
```
1506979122.959   1516 172.17.0.1 TCP_MISS/200 5384 CONNECT accounts.google.com:443 - HIER_DIRECT/172.217.27.237 -
1506979122.959    741 172.17.0.1 TCP_MISS/200 11476 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.31.42 -
```

### GRPC

The PubSub library uses gRPC so you need to enable _both_ environment variables:
```
export http_proxy=http://localhost:3128
export https_proxy=http://localhost:3128
```

Then with the sample API call for pubsub:

```python
from google.cloud import pubsub

client = pubsub.PublisherClient()
project_path = client.project_path('your_project')
for topic in client.list_topics(project_path):
  print(topic)
```

Once the environment variables are set, you will see the authentication and PubSub RPC call in the squid proxy access logs (described below)
```
1506979250.018   1870 172.17.0.1 TCP_MISS/200 5134 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/172.217.26.138 -
1506979250.021   1768 172.17.0.1 TCP_MISS/200 5438 CONNECT accounts.google.com:443 - HIER_DIRECT/172.217.27.237 -
```

>> Note:
http\_proxy= variable only passes through the API call via the proxy while https\_proxy covers the authentication call.
Which means you need to set both environment variables.


# JAVA [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-python)

For java, you can set the well known environment variables as described here:

[https://github.com/GoogleCloudPlatform/google-cloud-java#using-a-proxy](https://github.com/GoogleCloudPlatform/google-cloud-java#using-a-proxy)



## HTTP

For HTTP-only requests, there are several options depending on your needs,

### Default proxy

The following env variables will force all calls via the proxy

```java
System.setProperty("https.proxyHost", "localhost");
System.setProperty("https.proxyPort", "3128");
```

### Default proxy with Basic Authenticator

If your proxy requires BASIC auth, you may want to use an
[Authenticator](https://docs.oracle.com/javase/7/docs/api/java/net/Authenticator.html) to handle proxy negotiations.

```java
System.setProperty("https.proxyHost", "localhost");
System.setProperty("https.proxyPort", "3128");

Authenticator.setDefault(
    new Authenticator() {
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
                "user1", "user1".toCharArray());
    }
    }
);
```

### ApacheHttpTransport

 [ApacheHttpTransport()](https://developers.google.com/api-client-library/java/google-http-java-client/reference/1.20.0/com/google/api/client/http/apache/ApacheHttpTransport) provides an override mechanism to set custom headers which you can use later.
What this allows users to do is to set custom Proxy-Authorization:  or even Basic as shown below

The following shows overrides the transport

```java
JacksonFactory jsonFactory = new JacksonFactory();

HttpHost proxy = new HttpHost("127.0.0.1",3128);
DefaultHttpClient httpClient = new DefaultHttpClient();
httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

httpClient.addRequestInterceptor(new HttpRequestInterceptor(){            
    @Override
    public void process(org.apache.http.HttpRequest request, HttpContext context) throws HttpException, IOException {
            if (request.getRequestLine().getMethod().equals("CONNECT"))                 
               request.addHeader(new BasicHeader("Proxy-Authorization","Basic dXNlcjE6dXNlcjE="));
        }
    });

mHttpTransport =  new ApacheHttpTransport(httpClient);
```

#### For Proxy-Authentication:  Basic with GoogleAPIs

You can then use the transport in even in Traditional GoogleAPIs or with the Cloud libraries.

With GoogleAPIs, you can simply set the ```https.proxyHost`` env variable set or use the full override as shown below:

```java
com.google.api.client.googleapis.auth.oauth2.GoogleCredential credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential.getApplicationDefault(mHttpTransport,jsonFactory);
if (credential.createScopedRequired())
    credential = credential.createScoped(Arrays.asList(StorageScopes.DEVSTORAGE_READ_ONLY));

com.google.api.services.storage.Storage service = new com.google.api.services.storage.Storage.Builder(mHttpTransport, jsonFactory, credential)
                .setApplicationName("oauth client")   
                .build();
```

#### For Proxy-Authentication:  Basic with Google Cloud Client Libraries

For Google Client Libraries that use HTTP, the you can also use the env variables or the full transport override:

```java

HttpTransportFactory hf = new HttpTransportFactory(){
    @Override
    public HttpTransport create() {
        return mHttpTransport;
    }
};            

com.google.auth.oauth2.GoogleCredentials credential = com.google.auth.oauth2.GoogleCredentials.getApplicationDefault(hf);
    if (credential.createScopedRequired())
       credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/devstorage.read_write"));

TransportOptions options = HttpTransportOptions.newBuilder().setHttpTransportFactory(hf).build();            
com.google.cloud.storage.Storage storage = com.google.cloud.storage.StorageOptions.newBuilder()
    .setCredentials(credential)
    .setProjectId("your_project")
    .setTransportOptions(options)
    .build().getService();
```

both would show the following in the proxy access logs which indicates the authentication and API calls to GCS

```
1507030386.785   2871 172.17.0.1 TCP_MISS/200 5380 CONNECT accounts.google.com:443 user1 HIER_DIRECT/172.217.24.173 -
1507030388.004   1219 172.17.0.1 TCP_MISS/200 51450 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/74.125.68.95 -
```


## GRPC

Google-cloud-java with GRPC has experimental proxy support using
```GRPC_PROXY_EXP``` environment variable.

so first export the environment variable for the GRPC part:
```
export GRPC_PROXY_EXP=localhost:31138
```

then set the ```https.proxyHost=, https.proxyPort=``` env variables or use a GoogleCredential that is  configured to use the proxy.

In the end, your Credentials are acquired by HTTP while PubSub uses GRPC


```java
HttpTransportFactory hf = new HttpTransportFactory(){
	@Override
	public HttpTransport create() {
		return mHttpTransport;
	}
};            

credential = GoogleCredentials.getApplicationDefault(hf);
CredentialsProvider credentialsProvider =  new GoogleCredentialsProvider(){
	public List<String> getScopesToApply(){
		return Arrays.asList("https://www.googleapis.com/auth/pubsub");
	   }
	public Credentials getCredentials()  {
		return credential;
       }
};

TopicAdminSettings topicAdminSettings =
     TopicAdminSettings.newBuilder().setCredentialsProvider(credentialsProvider)
		 .build();

TopicAdminClient topicAdminClient =
     TopicAdminClient.create(topicAdminSettings);
ProjectName project = ProjectName.create(projectId);
for (Topic element : topicAdminClient.listTopics(project).iterateAll())
		System.out.println(element.getName());
```

and in the logs, you'll see
```
1507073510.386   5108 172.17.0.1 TCP_MISS/200 5466 CONNECT accounts.google.com:443 - HIER_DIRECT/216.58.203.77 -
1507073510.386   5758 172.17.0.1 TCP_MISS/200 4438 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/172.217.27.234 -
```

# golang [google-cloud-go](https://github.com/GoogleCloudPlatform/google-cloud-go)

GO also uses the standard environment variable to use the proxy:

```
export https_proxy=http://127.0.0.1:3128
```

So the following golang app will proxy both auth traffic for GCS and PubSub through:

```golang
import (
	"log"

	"cloud.google.com/go/storage"
	"cloud.google.com/go/pubsub"
	"golang.org/x/net/context"
	"google.golang.org/api/iterator"
)

const (
  projectID = "your_project"
)

func main() {
	ctx := context.Background()

	gcs, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatal(err)
	}

	b := gcs.Buckets(ctx, projectID)
	for {
		t, err := b.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf("bucket: %q\n", t.Name)
	}

	pub, err := pubsub.NewClient(ctx, projectID)
	if err != nil {
		log.Fatal(err)
	}

	topics := pub.Topics(ctx)
	for {
		t, err := topics.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire PubSub Client: %v", err)
		}
		log.Printf("topic: %q\n", t)
	}
}
```

the access log output would show both pubsub, gcs calls as well as the authentication exchange over the proxy:
```
1506982991.264   2004 172.17.0.1 TCP_MISS/200 3653 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/216.58.199.202 -
1506982991.264   2004 172.17.0.1 TCP_MISS/200 3654 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/216.58.199.202 -
1506982991.264   2004 172.17.0.1 TCP_MISS/200 3654 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/216.58.199.202 -
1506982991.264   2969 172.17.0.1 TCP_MISS/200 51391 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.31.74 -
1506982991.264   3711 172.17.0.1 TCP_MISS/200 6875 CONNECT accounts.google.com:443 - HIER_DIRECT/172.217.24.173 -
1506982991.265   2005 172.17.0.1 TCP_MISS/200 4675 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/216.58.199.202 -
```

# C# [google-cloud-dotnet](https://github.com/GoogleCloudPlatform/google-cloud-dotnet)

C# also uses the environment variables though for some services, enabling it requires some code changes:


```
export http_proxy=http://127.0.0.1:3128
```

then

```csharp
            string CREDENTIAL_FILE_PKCS12 = "/path/to/your/service0-account.p12";
            string serviceAccountEmail = "yourservice_account@project.gserviceaccount.com";
            var certificate = new X509Certificate2(CREDENTIAL_FILE_PKCS12, "notasecret",X509KeyStorageFlags.Exportable);

            ServiceAccountCredential credential = new ServiceAccountCredential(
               new ServiceAccountCredential.Initializer(serviceAccountEmail)
               {
                   //Scopes = new[] { StorageService.Scope.DevstorageReadOnly, PublisherClient.DefaultScopes },
                   Scopes = PublisherClient.DefaultScopes.Append(StorageService.Scope.DevstorageReadOnly),
                   HttpClientFactory = new ProxySupportedHttpClientFactory()
               }.FromCertificate(certificate));

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
```

Finally, the squid proxy logs will show:

```
1506991395.032    994 172.17.0.1 TCP_MISS/200 5210 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/172.217.31.42 -
1506991395.048   2015 172.17.0.1 TCP_MISS/200 10902 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.27.234 -
1506991395.049   2294 172.17.0.1 TCP_MISS/200 4100 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.27.234 -
```

# NodeJS [google-cloud-node](https://github.com/GoogleCloudPlatform/google-cloud-node)

NodeJS also uses the environment variable directly for _both_ authentication and the RPC call spanning HTTP and GRPC

so simply setting
```
export http_proxy=http://localhost:3128
```

and then running the sample

```javascript
var log4js = require("log4js");
var logger = log4js.getLogger();

const Pubsub = require('@google-cloud/pubsub');
var gcloud = require('google-cloud');

var gcs = gcloud.storage();
gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
      logger.info(value.id);
	});
  }
});

const pubsub = Pubsub({
  projectId: 'your_project'
});
pubsub.getTopics((err, topic) => {
	if (err) {
		logger.error(err);
		return;
	}
	topic.forEach(function(entry) {
    logger.info(entry.name);
	});
});
```

gives

```
1507120284.378    335 172.17.0.1 TCP_MISS/200 5632 CONNECT accounts.google.com:443 - HIER_DIRECT/216.58.203.77 -
1507120284.468    283 172.17.0.1 TCP_MISS/200 5632 CONNECT accounts.google.com:443 - HIER_DIRECT/216.58.203.77 -
1507120285.369    987 172.17.0.1 TCP_MISS/200 11167 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.31.74 -
1507120286.030   2001 172.17.0.1 TCP_MISS/200 5125 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/172.217.31.42 -
```

# Squid Proxy Dockerfile

If you need a local, containerized Squid proxy in various modes to test with, please see the following gitRepo and image

- GitRepo: [https://github.com/salrashid123/squid_proxy](https://github.com/salrashid123/squid_proxy)
- DockerHub:  github.io/salrashid123/squid_proxy

To use this image, simply enter a shell:
```
docker run  -p 3128:3128 -ti docker.io/salrashid123/squidproxy /bin/bash
```


### Forward proxy
 for a basic proxy, run from within the shell:

```
/apps/squid/sbin/squid -NsY -f /apps/squid.conf.transparent &
```

### Basic Auth  

```
/apps/squid/sbin/squid -NsY -f /apps/squid.conf.basicauth &
```
Your can verify the proxy is being used by running the following in a shell command
```
curl -x localhost:3128  --proxy-user user1:user1 -L http://www.yahoo.com
```

to view the accesslogs in the container:

```
tail -f /apps/squid/var/logs/access.log
```

### SSL interception

> Note this is only for amusement!

start the proxy server dockerfile with HTTPS intercept:
```
/apps/squid/sbin/squid -NsY -f /apps/squid.conf.https_proxy &
```

on your laptop, setup virtualenv:

```
virtualenv env --no-site-packages
source env/bin/activate
pip install google-cloud-storage
```

edit the two files and disable SSL checks ```verify=False``` (you can also set the ```REQUESTS_CA_BUNDLE``` env variable as described [here](http://docs.python-requests.org/en/master/user/advanced/#ssl-cert-verification)..)

- env/local/lib/python2.7/site-packages/google/oauth2/_client.py
(around line 103):
```python
def _token_endpoint_request(request, token_uri, body):
...
    response = request(
        method='POST', url=token_uri, headers=headers, body=body, verify=False)
```

- env/local/lib/python2.7/site-packages/google/auth/transport/requests.py
(around line 179):
```python
    def request(self, method, url, data=None, headers=None, **kwargs):
    ...
        response = super(AuthorizedSession, self).request(
            method, url, data=data, verify=False, headers=request_headers, **kwargs)
    ...    
```

Create main.py:

```python
#!/usr/bin/python

project='your_project'
from google.cloud import storage
client = storage.Client(project=project)
for b in client.list_buckets():
   print(b.name)
```

then export the proxy env var
```
export https_proxy=localhost:3128
```
and run the sample from withing the virtualenv
```
python main.py
```

the access logs now shows actual path requested (within the SSL session!)

```
1507365724.949    330 172.17.0.1 TAG_NONE/200 0 CONNECT accounts.google.com:443 - HIER_DIRECT/216.58.197.173 -
1507365725.159    175 172.17.0.1 TAG_NONE/200 0 CONNECT accounts.google.com:443 - HIER_DIRECT/216.58.197.173 -
1507365725.371    207 172.17.0.1 TCP_MISS/200 1455 POST https://accounts.google.com/o/oauth2/token - HIER_DIRECT/216.58.197.173 application/json
1507365725.719    344 172.17.0.1 TAG_NONE/200 0 CONNECT www.googleapis.com:443 - HIER_DIRECT/172.217.26.42 -
1507365726.443    721 172.17.0.1 TCP_MISS/200 7467 GET https://www.googleapis.com/storage/v1/b? - HIER_DIRECT/172.217.26.42 application/json
```
