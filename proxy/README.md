
# Accessing Google Cloud APIs though a Proxy


Proxy servers are not uncommon and I found some of my customers accessing Google Cloud APIs thorough them.

Most of the time, its a simple forward proxy with no authentication and never with SSL interception (which IMHO, is a really questionable, see [link](https://github.com/salrashid123/squid_proxy#https-intercept))

Fortunately, many of our APIs piggyback off of the native language proxy configuration settings (eg. ```https_proxy``` env variable) so that part
makes it easier.

This article describes the various proxy configurations you can use while accessing google APIs and I thought i'd consolidate some of my findings and issues with configuring google libraries into a doc to share.

For background, please see the two types of libraries available to access Google APIs though in this article, the focus is google cloud client libraries.

- [Client Libraries Explained](https://cloud.google.com/apis/docs/client-libraries-explained)

The recommended library set to use is the idiomatic "Cloud Client Library" which itself comes in two different transport mechanisms:  HTTP and gRPC.  At the time of writing (10/17), some APIs support both transports, some HTTP only (GCS, BigQiery), while other gRPC (PubSub).  How users enable and configure proxy support is slightly different for these transports.

One other complication to account for is that the authentication step (i.,e getting a GoogleCredential()) uses HTTP even if the underlying RPC is using gRPC.  For example, with java, you have to account for the credential via HTTP and the rpc call via gRPC.


The following lists the tests i ran using a squid proxy below.   Each tests verifies two modes of for the proxy


1. Proxy without authentication
2. Proxy that requires basic authentication


within those, i tested Application Default Credentials while a user was active and ADC when an service account was active



As you'll see, some languages uses the proxy for all traffic, some partial traffic (ie., auth or GCS is omitted), while some reuire a lot of fiddling

The outcome of each test and permutation is described in the comments in code

>my ask: If you have any updates or fixes, please file an bug or better yet, a PR.

# Python


```python
#!/usr/bin/python


# 1. user auth
#    export http_proxy=http://localhost:3128
#    auth N
#    gcs N
#    pubub Y

#    1638366068.078    261 192.168.9.1 TCP_TUNNEL/200 7876 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

# 2. user auth
#    export https_proxy=http://localhost:3128
#    auth Y
#    gcs Y
#    pubub Y

   # 1638366275.669    367 192.168.9.1 TCP_TUNNEL/200 7876 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
   # 1638366275.669    324 192.168.9.1 TCP_TUNNEL/200 7183 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/142.250.73.234 -
   # 1638366275.692   1147 192.168.9.1 TCP_TUNNEL/200 34961 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.81.208 -
   # 1638366275.692   1219 192.168.9.1 TCP_TUNNEL/200 7030 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/142.250.73.234 -


# 3.  service account
#    export https_proxy=http://localhost:3128
#    auth Y
#    gcs Y
#    pubub Y

   # 1638366614.398    201 192.168.9.1 TCP_TUNNEL/200 7838 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.251.33.202 -
   # 1638366614.418    643 192.168.9.1 TCP_TUNNEL/200 6418 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.1.202 -
   # 1638366614.418    563 192.168.9.1 TCP_TUNNEL/200 34953 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.81.208 -


# 4. basic + user auth
# export https_proxy=http://user1:user1@localhost:3128
#    auth Y
#    gcs Y
#    pubub Y
   # 1638366799.680    438 192.168.9.1 TCP_TUNNEL/200 7877 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
   # 1638366799.680    404 192.168.9.1 TCP_TUNNEL/200 7010 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -
   # 1638366799.701   1450 192.168.9.1 TCP_TUNNEL/200 34954 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
   # 1638366799.701   1549 192.168.9.1 TCP_TUNNEL/200 7155 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -

# 5.  basic +service account
# export https_proxy=http://user1:user1@localhost:3128
#    auth Y
#    gcs Y
#    pubub Y
# 1638366879.245    284 192.168.9.1 TCP_TUNNEL/200 7877 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
# 1638366879.264    582 192.168.9.1 TCP_TUNNEL/200 6408 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -
# 1638366879.264    541 192.168.9.1 TCP_TUNNEL/200 34953 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -


project='your-project'

from google.cloud import storage
client = storage.Client(project=project)
for b in client.list_buckets():
   print(b.name)

from google.cloud import pubsub_v1
publisher = pubsub_v1.PublisherClient()
project_path = f"projects/{project}"
for topic in publisher.list_topics(request={"project": project_path}):
  print(topic)
```


# JAVA [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-python)

```java
package com.test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.SocketAddress;

import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.core.ApiFunction;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.TransportOptions;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.w3c.dom.UserDataHandler;

import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ProxiedSocketAddress;
import io.grpc.ProxyDetector;

/*

1. user

mvn -DproxySet=true -Dhttp.proxyHost=localhost -Dhttp.proxyPort=3128 clean install exec:java -q

auth N
pubsub N
gcs N
1638367655.406      1 192.168.9.1 TCP_MISS/503 4414 GET http://metadata.google.internal/computeMetadata/v1/project/project-id - HIER_NONE/- text/html

2. user

mvn -DproxySet=true -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128 clean install exec:java -q

auth Y
pubub Y
gcs Y

1638367713.920   2290 192.168.9.1 TCP_TUNNEL/200 7008 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.9.202 -
1638367713.920   2073 192.168.9.1 TCP_TUNNEL/200 162229 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.188.48 -
1638367713.920    808 192.168.9.1 TCP_TUNNEL/200 7826 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.251.33.202 -

3. service account

export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc.json

auth N
pubsub N
gcs N
1638367846.791      8 192.168.9.1 TCP_MISS/503 4414 GET http://metadata.google.internal/computeMetadata/v1/project/project-id - HIER_NONE/- text/html

4. service account
  mvn -DproxySet=true -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128 clean install exec:java -q

auth Y
pubsub Y
gcs Y

1638367902.271   2702 192.168.9.1 TCP_TUNNEL/200 6328 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.15.106 -
1638367902.271   2234 192.168.9.1 TCP_TUNNEL/200 162251 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.188.48 -
1638367902.271    844 192.168.9.1 TCP_TUNNEL/200 7826 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.251.33.202 -

5.  any cred

mvn -DproxySet=true -Dhttps.proxyUser=user1 -Dhttps.proxyPassword=user1 -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128 clean install exec:java -q

auth N
pubsub N
gcs N

1638368288.849      0 192.168.9.1 TCP_DENIED/407 4105 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html

*/

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}

	private ApacheHttpTransport mHttpTransport;
	private GoogleCredentials credential;

	public TestApp() {
		try {

			credential = GoogleCredentials.getApplicationDefault();
			Storage storage_service = StorageOptions.newBuilder()
					.build()
					.getService();
			for (Bucket b : storage_service.list().iterateAll()) {
				System.out.println(b);
			}

			GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(creds);

			TopicAdminClient topicClient = TopicAdminClient.create(
					TopicAdminSettings.newBuilder()
							.setCredentialsProvider(credentialsProvider)
							.build());

			ListTopicsRequest listTopicsRequest = ListTopicsRequest.newBuilder()
					.setProject(ProjectName.format("your-project"))
					.build();
			ListTopicsPagedResponse response = topicClient.listTopics(listTopicsRequest);
			Iterable<Topic> topics = response.iterateAll();
			for (Topic topic : topics)
				System.out.println(topic);

		} catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}

}
```


```java
package com.test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.SocketAddress;

import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.core.ApiFunction;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.TransportOptions;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.w3c.dom.UserDataHandler;

import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ProxiedSocketAddress;
import io.grpc.ProxyDetector;


/*

unsure why the first set is denied

1.user
  mvn clean install exec:java -q

auth Y
gcs Y
pubsub Y


1638368638.824      0 192.168.9.1 TCP_DENIED/407 4000 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html
1638368639.059      0 192.168.9.1 TCP_DENIED/407 4004 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
1638368640.852   1792 192.168.9.1 TCP_TUNNEL/200 162251 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
1638368640.852    711 192.168.9.1 TCP_TUNNEL/200 7827 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
1638368640.853   2021 192.168.9.1 TCP_TUNNEL/200 7064 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/172.217.9.202 -

2. service account
 mvn clean install exec:java -q

auth Y
gcs Y
pubsub Y


1638368545.910      0 192.168.9.1 TCP_DENIED/407 4000 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html
1638368546.080      0 192.168.9.1 TCP_DENIED/407 4004 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
1638368547.913   1996 192.168.9.1 TCP_TUNNEL/200 6282 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/172.217.9.202 -
1638368547.913   1832 192.168.9.1 TCP_TUNNEL/200 162251 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
1638368547.913    763 192.168.9.1 TCP_TUNNEL/200 7826 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -

*/


public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}

	private ApacheHttpTransport mHttpTransport;
	private GoogleCredentials credential;

	public TestApp() {
		try {

			HttpHost proxy = new HttpHost("127.0.0.1", 3128);

			org.apache.http.client.CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
					new UsernamePasswordCredentials("user1", "user1"));
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			clientBuilder.useSystemProperties();
			clientBuilder.setProxy(proxy);
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

			CloseableHttpClient httpClient = clientBuilder.build();

			mHttpTransport = new ApacheHttpTransport(httpClient);

			HttpTransportFactory hf = new HttpTransportFactory() {
				@Override
				public HttpTransport create() {
					return mHttpTransport;
				}
			};

			credential = GoogleCredentials.getApplicationDefault(hf);

			TransportOptions options = HttpTransportOptions.newBuilder().setHttpTransportFactory(hf).build();
			Storage storage_service = StorageOptions.newBuilder().setCredentials(credential)
					.setTransportOptions(options)
					.build()
					.getService();
			for (Bucket b : storage_service.list().iterateAll()) {
				System.out.println(b);
			}

			GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(creds);

			TransportChannelProvider channelProvider = TopicAdminSettings.defaultGrpcTransportProviderBuilder()
					.setChannelConfigurator(new ApiFunction<ManagedChannelBuilder, ManagedChannelBuilder>() {
						@Override
						public ManagedChannelBuilder apply(ManagedChannelBuilder managedChannelBuilder) {
							return managedChannelBuilder.proxyDetector(
									new ProxyDetector() {
										@Override
										public ProxiedSocketAddress proxyFor(SocketAddress socketAddress)
												throws IOException {
											return HttpConnectProxiedSocketAddress.newBuilder()
													.setUsername("user1")
													.setPassword("user1")
													.setProxyAddress(new InetSocketAddress("localhost", 3128))
													.setTargetAddress((InetSocketAddress) socketAddress)
													.build();
										}
									});
						}
					})
					.build();

			TopicAdminClient topicClient = TopicAdminClient.create(
					TopicAdminSettings.newBuilder()
							.setTransportChannelProvider(channelProvider)
							.setCredentialsProvider(credentialsProvider)
							.build());

			ListTopicsRequest listTopicsRequest = ListTopicsRequest.newBuilder()
					.setProject(ProjectName.format("your-project"))
					.build();
			ListTopicsPagedResponse response = topicClient.listTopics(listTopicsRequest);
			Iterable<Topic> topics = response.iterateAll();
			for (Topic topic : topics)
				System.out.println(topic);

		} catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}

}
```

# golang [google-cloud-go](https://github.com/GoogleCloudPlatform/google-cloud-go)


### Forward

```golang
package main

import (
	"log"

	"cloud.google.com/go/pubsub"
	"cloud.google.com/go/storage"
	"golang.org/x/net/context"
	"google.golang.org/api/iterator"
)

/*

1. user auth
   export http_proxy=http://localhost:3128

   no traffic

2. user auth
    export https_proxy=http://localhost:3128

	pubsub Y
	auth Y
	gcs Y

	1638363908.054   1043 192.168.9.1 TCP_TUNNEL/200 7779 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.15.74 -
	1638363908.054    930 192.168.9.1 TCP_TUNNEL/200 115190 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.73.208 -
	1638363908.054    390 192.168.9.1 TCP_TUNNEL/200 4886 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

3. service account auth
   export https_proxy=http://localhost:3128

   pubsub Y
   auth Y
   gcs Y

	1638363985.052    837 192.168.9.1 TCP_TUNNEL/200 5610 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.15.74 -
	1638363985.052    771 192.168.9.1 TCP_TUNNEL/200 115190 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.73.208 -
	1638363985.052    278 192.168.9.1 TCP_TUNNEL/200 4885 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

4. basic + user auth
   export https_proxy=http://user1:user1@localhost:3128

   pubsub Y
   auth Y
   gcs Y
	1638364205.519    338 192.168.9.1 TCP_TUNNEL/200 7240 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -
	1638364205.521    884 192.168.9.1 TCP_TUNNEL/200 7660 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
	1638364205.521    805 192.168.9.1 TCP_TUNNEL/200 115189 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -

5. basic service account auth
   export https_proxy=http://user1:user1@localhost:3128

   pubsub Y
   auth Y
   gcs Y

	1638364340.203    155 192.168.9.1 TCP_TUNNEL/200 4886 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -
	1638364340.203    680 192.168.9.1 TCP_TUNNEL/200 5810 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
	1638364340.203    623 192.168.9.1 TCP_TUNNEL/200 115191 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -
*/
func main() {

	ctx := context.Background()

	gcs, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatal(err)
	}

	b := gcs.Buckets(ctx, "your-project")
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

	pub, err := pubsub.NewClient(ctx, "your-project")
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
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf("Topic: %q\n", t)
	}
}

```

# C# [google-cloud-dotnet](https://github.com/GoogleCloudPlatform/google-cloud-dotnet)


```csharp
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
```


```csharp
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

// 1. basic auth, with usercredentials
// need export http_proxy=http://user1:user1@localhost:3128 for pubsub
// configure ProxySupportedHttpClientFactory with auth for gcs, oauth2

//  auth Y
//  gcs Y
//  pubsub Y

// 1638363563.445      0 192.168.9.1 TCP_DENIED/407 3999 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html
// 1638363563.662      0 192.168.9.1 TCP_DENIED/407 4068 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
// 1638363564.438    776 192.168.9.1 TCP_TUNNEL/200 45147 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -
// 1638363564.438    345 192.168.9.1 TCP_TUNNEL/200 7877 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -
// 1638363564.438    987 192.168.9.1 TCP_TUNNEL/200 7348 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.73.234 -

// NOTE, see deny first

// 2. no basic auth, with service account credentials
// export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc_account.json
// Invalid Credentials [401]
// Errors [
// 	Message[Invalid Credentials] Location[Authorization - header] Reason[authError] Domain[global]
// ]

// 1638363615.081      0 192.168.9.1 TCP_DENIED/407 4068 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
// 1638363615.287    201 192.168.9.1 TCP_TUNNEL/200 8242 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.73.208 -

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
            ICredentials credentials = new NetworkCredential("user1", "user1");
            var proxy = new WebProxy("http://127.0.0.1:3128", true, null, credentials);            
            //var proxy = new WebProxy("http://127.0.0.1:3128", true, null, null);
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

# NodeJS [google-cloud-node](https://github.com/GoogleCloudPlatform/google-cloud-node)

```javascript
var log4js = require("log4js");
var logger = log4js.getLogger();

const Pubsub = require('@google-cloud/pubsub');
const Storage = require('@google-cloud/storage');


/*

1. User auth
   export https_proxy=http://localhost:3128

   auth N
   gcs Y
   pubsub Y

   1638364764.530    205 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
   1638364764.939    694 192.168.9.1 TCP_TUNNEL/200 7595 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

2. user auth
   export https_proxy=http://localhost:3128

	Auth error:Error: write EPROTO 139715611573120:error:1408F10B:SSL routines:ssl3_get_record:wrong version number:../deps/openssl/openssl/ssl/record/ssl3_record.c:332:

    (which is fine)

3. svc account
   export https_proxy=http://localhost:3128

   auth Y
   gcs Y
   pubsub Y

    1638364926.866     60 192.168.9.1 TCP_TUNNEL/200 5866 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364926.899     50 192.168.9.1 TCP_TUNNEL/200 6048 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364927.030    149 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364927.082    277 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

4. basic user auth
   export http_proxy=http://user1:user1@localhost:3128

   auth N
   gcs Y
   pubsub Y

	1638365107.917    152 192.168.9.1 TCP_TUNNEL/200 44961 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365107.938    509 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -

5. basic service account

   auth Y
   gcs Y
   pubsub Y

	1638365190.001     53 192.168.9.1 TCP_TUNNEL/200 6027 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.017     44 192.168.9.1 TCP_TUNNEL/200 5829 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.156    142 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.192    244 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -

*/
var gcs = new Storage();
gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});
  }
});

const pubsub = Pubsub({
  projectId: 'your-project'
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
