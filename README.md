## Google Cloud Platform API hello world samples

####  samples provided as-is without warranty

Sample code demonstrating various Auth mechanism for Google Cloud Platform APIs.

Please refer to official documentation for usage and additional samples/usage.

[Google Authentication Samples](https://cloud.google.com/docs/authentication)

***  

### Application Default Credentials

The samples use [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials) which uses credentials in the following order as described in the link.  Set the environment variable to override.  

You can always specify the target source to acquire credentials by using intent specific targets such as:  ComputeCredentials, UserCredentials or ServiceAccountCredential.

There are two types of client libraries you can use to connect to Google APIs:  
* Google Cloud Client Libraries
* Gooogle API Client Libraries

The basic differences is the Cloud Client libraries are idomatic, has [gcloud-based emulators](https://cloud.google.com/sdk/gcloud/reference/beta/emulators/) and much eaiser to use.  

It is recommended to use Cloud Client Libraries whereever possible. Although this article primarily describes the API Client libraries, the python code section describes uses of Cloud Client libraries with Google Cloud Storage.

For more information, see

* [Client Libraries Explained](https://cloud.google.com/apis/docs/client-libraries-explained).
* [Google Cloud Platform Authenticatio Guide](https://cloud.google.com/docs/authentication)

This article also describes how to use IAM's serviceAccountActor role to issue access_tokens, id_tokens and JWT.  For more information on that, see [auth/tokens/](auth/tokens).

The following examples use the Oauth2 *service* to demonstrate the initialized client using Google API Client Libraries.  The first section is about the different client libraries you can use.

* [Cloud Client Libraries and API Client Libraries](#googlelibraries)
    - Cloud Client Libraries
    - API Client Libraries
* Cloud Client Libraries
    - [Python](#cloud-python)
    - [Java](#cloud-java)
    - [Go](#cloud-go)
    - [Node](#cloud-node)
    - [c#](#cloud-c)
    - [gRPC Environment Variables](#grpc-environment-variables)
    - [GCS SignedURL with Customer Supplied Encryption Keys](gcs_csek_signedurl)
    - [JWT Access Token](#jwt-access-token)
* API Client Library
    - [Python](#google-api-python)
    - [Java](#google-api-java)
    - [Go](#google-api-go)
    - [Node](#google-api-nodejs)
    - [C#](#google-api-C&#35)
* [serviceAccountActor role for impersonation](auth/tokens)
    - access_token
    - id_token
    - JWT
* [Impersonated Credentials](impersonated_credentials/)
* [Accessing Google APIs through proxies](proxy/)
* [Issue and Verify id_tokens](id_token/)
* [GCS SignedURL with HMAC](gcs_hmac_signedurl)
* [GCS keyless SignedURL](gcs_keyless_signedurl)
* [Accessing Google APIs through proxies](proxy/)

For more inforamtion, see:
* [oauth2 protocol](https://developers.google.com/identity/protocols/OAuth2)
* [oauth2 service](https://developers.google.com/apis-explorer/#p/oauth2/v2/)
* [Service Accounts](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#overview)

### GoogleLibraries
As described in the introduciton, this section details the two types of libraries you can use to access Google Services:

#### Google Cloud Client Libraries
These libraries are idomatic, easy to use and even support the [gcloud-based emulator framework](https://cloud.google.com/sdk/gcloud/reference/beta/emulators/).  This is the
recommended library set to use to access Google Cloud APIs.

For more information, see:

* [Application DefaultCredentials](https://developers.google.com/identity/protocols/application-default-credentials)
* [google-cloud](https://googlecloudplatform.github.io/google-cloud-python/)
* [google-auth package](https://google-auth.readthedocs.io/en/latest/)
* [google-auth Users Guide](https://google-auth.readthedocs.io/en/latest/user-guide.html)

The following example describes various ways to initialize a service account to list the Google Cloud Storage buckets the account has access to.  It also shows listing the buckets
using the default account currently initialized by gcloud.

To use the mechanisms here, you need to initialize gcloud's application defaults:

```bash
gcloud auth application-default login

```

#### Cloud Python

The following uses the google-storage client described here: [Storage Client](http://gcloud-python.readthedocs.io/en/latest/storage-client.html)


```
virtualenv env
source env/bin/activate
pip install google-cloud-storage
```

The following lists some of the various mechanisms to acquire credentials:

List buckets using the default account on the current gcloud cli (preferred)

```python
from google.cloud import storage

client = storage.Client()
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

List buckts using gcloud cli explicit credential and project

```python
from google.cloud import storage
import google.auth

credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

List buckets using an environment variable and then google.auth.default() credentials.

```python
from google.cloud import storage
import google.auth
impot os

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_CERT.json"
credentials, project = google.auth.default()
if credentials.requires_scopes:
  credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

List buckets using a service_account oauth2 object directly

```python
from google.cloud import storage
import google.auth
from google.oauth2 import service_account

credentials = service_account.Credentials.from_service_account_file('YOUR_JSON_CERT.json')
if credentials.requires_scopes:
  credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])

client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

List buckets using the storage client directly loading the certificate:

```python
from google.cloud import storage

client = storage.Client.from_service_account_json("YOUR_JSON_CERT.json")
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

##### Iterators

see
 - [google cloud python iterators](https://gcloud-python.readthedocs.io/en/stable/core/iterators.html)
 - [page iterators](https://googlecloudplatform.github.io/google-cloud-python/latest/core/page_iterator.html)


* Logging:

```python
import os
import pprint
from google.cloud import logging

from google.cloud.logging import ASCENDING
from google.cloud.logging import DESCENDING

pp = pprint.PrettyPrinter(indent=1)

FILTER = 'resource.type="gae_app" AND logName="projects/mineral-minutia-820/logs/appengine.googleapis.com%2Frequest_log" AND protoPayload.resource="/"'

client = logging.Client()

iterator = client.list_entries(filter_=FILTER, order_by=DESCENDING)
for page in iterator.pages:
  print('    Page number: %d' % (iterator.page_number,))
  print('  Items in page: %d' % (page.num_items,))
  print('Items remaining: %d' % (page.remaining,))
  print('Next page token: %s' % (iterator.next_page_token,))  
  print('----------------------------')
  for entry in page:
      print(entry.timestamp)
```

* Monitoring:

```python

# virtualenv env
# source env/bin/activate
# pip install google-cloud-monitoring==0.30.0

import datetime, time
import pprint
from google.cloud import monitoring_v3
from google.cloud.monitoring_v3.query import Query

client = monitoring_v3.MetricServiceClient()

metric_type = 'serviceruntime.googleapis.com/api/request_count'
resource_type = 'consumed_api'
service = 'logging.googleapis.com'

now = datetime.datetime.utcnow()
fifteen_mins_ago =  now - datetime.timedelta(minutes=15)

q = Query(client, project='YOUR_PROJECT', metric_type=metric_type, minutes=10)
q.select_interval(end_time=now,start_time=fifteen_mins_ago)
q.select_resources(resource_type=resource_type, service=service)

for timeseries in q.iter():
  print '========== Metric: '
  #pprint.pprint(timeseries)
  print '========== Points: '
  for p in timeseries.points:
   print repr(p)
   print str(p.start_time) + ' --> ' + str(p.end_time) + '  : [' +  str(p.value.get('bucketCounts')) + ']'
  print('-----------------')

```

##### Using google.auth for GoogleAPIs


The following shows transport authorization for the original Google APIs

```python
import oauth2client
from oauth2client.client import GoogleCredentials
import httplib2

http = httplib2.Http()
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scopes)
http = credentials.authorize(http)
```

If you need to use the more recent Google Cloud Auth library, you need to cast the transport:

-  [http://google-auth.readthedocs.io/en/latest/reference/google.auth.html](http://google-auth.readthedocs.io/en/latest/reference/google.auth.html)
-  [ https://github.com/GoogleCloudPlatform/google-auth-library-python-httplib2]( https://github.com/GoogleCloudPlatform/google-auth-library-python-httplib2)


```python
import google.auth
import google_auth_httplib2

scopes = ['https://www.googleapis.com/auth/devstorage.read_write']
credentials, project = google.auth.default(scopes=scopes)
http =  google_auth_httplib2.AuthorizedHttp(credentials)
```

or preferably init a cloud API:

```python
from google.cloud import storage
import google.auth
from google.oauth2 import service_account
import os

#credentials = service_account.Credentials.from_service_account_file('YOUR_JSON_CERT.json')
#if credentials.requires_scopes:
#  credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])
#client = storage.Client(credentials=credentials)

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/srashid/gcp_misc/certs/mineral-minutia-820-83b3ce7dcddb.json"
credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
```

##### Logging_Cloud_python

If you want to enable trace logging with `google-cloud-python*` library set,

```python
#!/usr/bin/python

from google.cloud import bigquery
from six.moves import http_client

http_client.HTTPConnection.debuglevel = 5

client = bigquery.Client()
query_job = client.query("""
        SELECT timestamp
FROM
 `mineral-minutia-820.gae_request_logs.appengine_googleapis_com_request_log_20161119`
ORDER BY timestamp DESC
LIMIT
 4;""")

results = query_job.result()
for row in results:
        print(row)
```

#### Cloud Java

* [http://googlecloudplatform.github.io/google-cloud-java/0.8.0/index.html](http://googlecloudplatform.github.io/google-cloud-java/0.8.0/index.html)
* [StorageExample](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/master/google-cloud-examples/src/main/java/com/google/cloud/examples/storage/StorageExample.java)
* [Java Cloud Examples](https://github.com/GoogleCloudPlatform/java-docs-samples)
* [Google Extensions for Java (GAX)](http://googleapis.github.io/gax-java/)

The following describes using java default credentials.  You can explictly _setCredentials()_ while initializing a service but that is not recommended as the code is not portable

The various credential types can be found here:
* [com.google.auth.oauth2](https://github.com/google/google-auth-library-java/tree/master/oauth2_http/java/com/google/auth/oauth2)
* [Credential Types](https://github.com/google/google-auth-library-java#google-auth-library-oauth2-http)

The samples conained within

- [auth/service/javaapp](auth/service/javaapp)
- [auth/compute/javaapp](auth/compute/javaapp)

Shows one sample app that uses both library types.  At the time of writing (7/7/18), there is a conflict between the grpc dependencies and google apis.

WHich means, if you use GoogleAPIs, comment out the sections for Cloud API in the pom and .java files:

for Cloud APIs, use:
- pom.xml
```xml
  <dependency>
      <groupId>com.google.cloud</groupId>
      <artifactId>google-cloud-storage</artifactId>
      <version>1.35.0</version>
  </dependency>

  <dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-pubsub</artifactId>
    <version>1.35.0</version>
  </dependency>
```

```java
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

import com.google.cloud.storage.Storage.SignUrlOption;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.util.Iterator;
import java.io.FileInputStream;


//import com.google.auth.oauth2.ServiceAccountCredentials;

// Using Google Cloud APIs with service account file
// You can also just export an export GOOGLE_APPLICATION_CREDENTIALS and use StorageOptions.defaultInstance().service()
// see https://github.com/google/google-auth-library-java#google-auth-library-oauth2-http
/*
Storage storage_service = StorageOptions.newBuilder()
			.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/path/to/your/certificate.json")))
			.build()
			.getService();			
*/

Storage storage_service = StorageOptions.newBuilder()
	.build()
	.getService();
for (Bucket b : storage_service.list().iterateAll()){
  System.out.println(b);
}

// You can also use the client to generate a signed URL:
URL signedUrl = storage_service.signUrl(BlobInfo.newBuilder("your_project", "a.txt").build(), 60,  TimeUnit.SECONDS);
System.out.println(signedUrl);
```

##### Proxy Server Settings

see [proxy/README.md](proxy/README.md)

```  
export  https_proxy=proxy_server:3128
```

#### Credential/Channel Providers

```java

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.api.gax.rpc.FixedTransportChannelProvider;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicSubscriptionsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.Topic;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

    // or set ADC
    //export GOOGLE_APPLICATION_CREDENTIALS="/path/to/keyfile.json"
    String cred_env = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
	GoogleCredentials creds = GoogleCredentials.getApplicationDefault();	  	  
    //String cert_file = "keyfile.json";    
	//GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(cred_env));
	FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(creds);

	///ManagedChannel channel = ManagedChannelBuilder.forTarget("pubsub.googleapis.com:443").build();
    //TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

	TransportChannelProvider channelProvider = TopicAdminSettings.defaultTransportChannelProvider();

	TopicAdminClient topicClient =
	  TopicAdminClient.create(
		  TopicAdminSettings.newBuilder()
			  .setTransportChannelProvider(channelProvider)
			  .setCredentialsProvider(credentialsProvider)
			  .build());
```

##### Async Futures

see [Example](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/pubsub/cloud-client/src/main/java/com/example/pubsub/PublisherExample.java#L40)

```java

import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.iam.v1.Binding;
import com.google.cloud.Role;

    // setup topicadmin client using the bit above
	TopicAdminClient topicClient =
	  TopicAdminClient.create(
		  TopicAdminSettings.newBuilder()
			  .setTransportChannelProvider(channelProvider)
			  .setCredentialsProvider(credentialsProvider)
              .build());

      String formattedResource = TopicName.create("mineral-minutia-820", "saltopic2").toString();

      GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
        .setResource(formattedResource)
        .build();
      ApiFuture<Policy> future = topicAdminClient.getIamPolicyCallable().futureCall(request);
      Policy response = future.get();
      System.out.println(response);

```



#### Cloud Go

Package [cloud.google.com/go/storage](https://godoc.org/cloud.google.com/go/storage)

* [https://github.com/GoogleCloudPlatform/google-cloud-go](https://github.com/GoogleCloudPlatform/google-cloud-go)
* [https://godoc.org/google.golang.org/api/storage/v1](https://godoc.org/google.golang.org/api/storage/v1)

The following shows google.DefaultTokenSource as well as gcloud's Application Default Credentials
```golang
import (
        "golang.org/x/net/context"
        "cloud.google.com/go/storage"
        "google.golang.org/api/iterator"
        "google.golang.org/api/option"
        "log"  
)

	ctx := context.Background()
	/*
		tokenSource, err := google.DefaultTokenSource(oauth2.NoContext, storage.ScopeReadOnly)
		if err != nil {
			log.Fatalf("Unable to acquire token source: %v", err)
		}
		storeageClient, err := storage.NewClient(ctx, option.WithTokenSource(tokenSource))
	*/

	storeageClient, err := storage.NewClient(ctx)
	if err != nil {
		log.Fatalf("Unable to acquire storage Client: %v", err)
	}

	it := storeageClient.Buckets(ctx, "your_project")
	for {
		bucketAttrs, err := it.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Fatalf("Unable to acquire storage Client: %v", err)
		}
		log.Printf(bucketAttrs.Name)
	}

```

##### Adding custom headers

```golang
package main


import (
  "context"
  "io/ioutil"
  "log"
  "net/http"

  "cloud.google.com/go/storage"
  "google.golang.org/api/option"
  raw "google.golang.org/api/storage/v1"
  htransport "google.golang.org/api/transport/http"
)

func main() {

  ctx := context.Background()

  // Standard way to initialize client:
  // client, err := storage.NewClient(ctx)
  // if err != nil {
  //      // handle error
  // }

  // Instead, create a custom http.Client.
  base := http.DefaultTransport
  trans, err := htransport.NewTransport(ctx, base, option.WithScopes(raw.DevstorageFullControlScope),
            option.WithUserAgent("custom-user-agent"))
  if err != nil {
            // Handle error.
  }
  c := http.Client{Transport:trans}

  // Add RoundTripper to the created HTTP client.
  c.Transport = withDebugHeader{c.Transport}

  // Supply this client to storage.NewClient
  client, err := storage.NewClient(ctx, option.WithHTTPClient(&c))
  if err != nil {
              // Handle error.
  }

  // Use client...
 }

type withDebugHeader struct {
  rt http.RoundTripper
}

func (wdh withDebugHeader) RoundTrip(r *http.Request) (*http.Response, error) {
  headerName := "X-Custom-Header"
  r.Header.Add(headerName, "value")
  resp, err := wdh.rt.RoundTrip(r)
  if err == nil {
    log.Printf("Resp Header: %+v, ", resp.Header.Get(headerName))
  } else {
    log.Printf("Error: %+v", err)
  }
  return resp, err
}
```


##### Exponential Backoff

Cloud libraries implement backoff automatically per service.

- GCS
  - [https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/storage/invoke.go#L26](https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/storage/invoke.go#L26)

- BQ
  - [https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/bigquery/service.go#L705](https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/bigquery/service.go#L705)
  - [https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/bigquery/service.go#L529](https://github.com/GoogleCloudPlatform/google-cloud-go/blob/master/bigquery/service.go#L529)


#### Cloud Node

The _google-cloud_ node package initalizes the Cloud API library set:

* [https://github.com/GoogleCloudPlatform/google-cloud-node](https://github.com/GoogleCloudPlatform/google-cloud-node)
* [https://googlecloudplatform.github.io/google-cloud-node/#/](https://googlecloudplatform.github.io/google-cloud-node/#/)

The sample under [auth/compute/nodeapp](auth/compute/nodeapp) shows both Cloud APIs and Google APIs

```node
const Storage = require('@google-cloud/storage');
const storage = new Storage({
    projectId: 'your-project',
  });

storage.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});
  }
});

const Pubsub = require('@google-cloud/pubsub');
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

#### Cloud C#

Use [Google.Cloud.Storage.V1](https://www.nuget.org/packages/Google.Cloud.Storage.V1/) package for Google Cloud API access

* [google-cloud-dotnet](https://github.com/GoogleCloudPlatform/google-cloud-dotnet)
* [API Documentation](http://googlecloudplatform.github.io/google-cloud-dotnet/docs/Google.Cloud.Storage.V1/)

See [auth/service/dotnet](auth/service/dotnet) for sample for both Cloud APIs and Google APIs libraries.

```csharp
using Google.Cloud.Storage.V1;

namespace CloudStorageAppGcloud
{
    class Program
    {
        static void Main(string[] args)
        {
            var client = StorageClient.Create();

            foreach (var obj in client.ListObjects("your_project", ""))
            {
                Console.WriteLine(obj.Name);
            }
            Console.ReadLine();
        }
    }
}

```

#### gRPC Environment Variables

- [https://github.com/grpc/grpc/blob/master/doc/environment_variables.md](https://github.com/grpc/grpc/blob/master/doc/environment_variables.md)


#### JWT Access Token

-  - [auth/service/jwt_access_token](auth/service/jwt_access_token)

JWT access tokens are efficient way to access certain google apis without the extra round trip to get an ```access_token```.  Unlike the normal Oauth service account flow where you
1. use a local service account to sign a JWT,
2. Exchange that JWT with google to get an ```access_token```
3. Use that ```access_token``` to make an API call to google

with JWT Access Tokens, all you do is sign a JWT locally with a service account with the intended Service you want to access and then simply send it to the service.

The following links describes this flow:
- [JWT Auth](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#jwt-auth)

where these Google APIs will support this:
- [https://github.com/googleapis/googleapis/tree/master/google](https://github.com/googleapis/googleapis/tree/master/google)


eg. for PubSub
```golang
	// https://github.com/googleapis/googleapis/blob/master/google/pubsub/pubsub.yaml#L6

	ctx := context.Background()
	projectID := "YOUR_PROJECT"
	keyfile := "service_account.json"

	audience := "https://pubsub.googleapis.com/google.pubsub.v1.Publisher"

	keyBytes, err := ioutil.ReadFile(keyfile)
	if err != nil {
		log.Fatalf("Unable to read service account key file  %v", err)
	}
	tokenSource, err := google.JWTAccessTokenSourceFromJSON(keyBytes, audience)
	if err != nil {
		log.Fatalf("Error building JWT access token source: %v", err)
	}
	jwt, err := tokenSource.Token()
	if err != nil {
		log.Fatalf("Unable to generate JWT token: %v", err)
	}
	fmt.Println(jwt.AccessToken)

	pubsubClient, err := pubsub.NewClient(ctx, projectID, option.WithTokenSource(tokenSource))
	if err != nil {
		log.Fatalf("Could not create pubsub Client: %v", err)
	}
```

#### Google API Client Library for Python

The following describes the older, non-idomatic libraries.  As you can see, its much easier using the idomatic library set.
* [GCS JSON API](https://cloud.google.com/storage/docs/json_api/v1/buckets/list)

```
virtualenv env
source env/bin/activate
pip install --upgrade requests google-api-python-client httplib2 oauth2client

```

```python
import os
import httplib2
from apiclient.discovery import build
from oauth2client.service_account import ServiceAccountCredentials
from oauth2client.client import GoogleCredentials

scope='https://www.googleapis.com/auth/devstorage.read_only'

#os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_CERT.json"
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scope)
http = httplib2.Http()
credentials.authorize(http)

service = build(serviceName='storage', version= 'v1',http=http)
resp = service.buckets().list(project='YOUR_PROJECT').execute()
for i in resp['items']:
  print i['name']
```

You can also initialize an `AuthorizedHttp` artifact from `google-auth` library with discovery:

```python
# http://google-auth.readthedocs.io/en/latest/reference/google.auth.html
# https://github.com/GoogleCloudPlatform/google-auth-library-python-httplib2
import google.auth
import google_auth_httplib2
credentials, project = google.auth.default(scopes=scopes)
http =  google_auth_httplib2.AuthorizedHttp(credentials)

service = build(serviceName='cloudtasks',
  discoveryServiceUrl='https://cloudtasks.googleapis.com/%24discovery/rest?version=v2beta2',
  version= 'v2beta2',http=http)
```

### Google API Python

* [Google API Client Library for Python](https://developers.google.com/api-client-library/python/)

```
apt-get install curl python2.7 python-pip
pip install requests google-api-python-client httplib2 oauth2client
```

#### Appengine

Under [auth/gae/pyapp/](auth/gae/pyapp/)  Deploys an application to appengine that uses *Application Default Credentials*.  

*AppAssertionCredentials*  is also shown but commented.

Remember to edit app.yaml file with your appID.  

If running on the dev_appserver, you will need to set the local service account id and certificate first:
```bash
cd auth/gae/pyapp

virtualenv env
source env/bin/activate
pip install -t lib  -r requirements.txt
deactvate && rm -rf env

# To run with your own gcloud credentials
dev_appserver.py app.yaml

# For service account credentials
cat your_svc_account.p12 | openssl pkcs12 -nodes -nocerts -passin pass:notasecret | openssl rsa > key.pem
dev_appserver.py app.yaml --appidentity-email-address=YOUR_SERVICE_ACCOUNT_ID@developer.gserviceaccount.com --appidentity-private-key-path=key.pem

```

For info on ```--appidentity-email-address``` and ```--appidentity-private-key-path```, see documentation on [gcloud dev_appserver](https://cloud.google.com/sdk/gcloud/reference/preview/app/run).

#### ComputeEngine

Under [auth/compute/pyapp](auth/compute/pyapp)  Runs a simple application on compute engine using *Application Default Credentials*.

*AppAssertionCredentials* is also shown but commented

or

```
cd auth/compute/pyapp
virtualenv env
source env/bin/activate
pip install -r requirements.txt

python compute.py
```


#### Service Account File

Under [auth/service/pyapp](auth/service/pyapp/)  Runs a simple application that uses the service account credential from both a PKCS12 file and a JSON keyfile.  Application Default Credentials uses the JSON keyfile only if the *GOOGLE_APPLICATION_CREDENTIALS* variable isset

For more details, goto [Service Accounts](https://developers.google.com/api-client-library/python/auth/service-accounts)

#### Userflow

Under [auth/userflow/pyapp](auth/userflow/pyapp)  Runs a simple application that performs user-interactive webflow and propmpts the user for consent.  Download an *installed* app client_secrets.json and reference it for the 'flow_from_clientsecrets()' method.

For more deails, goto [flow_from_clientsecrets](https://developers.google.com/api-client-library/python/guide/aaa_oauth#flow_from_clientsecrets)

The sample also shows the simplified flow with a browser listener (so that you dont' have to type in the code manually):

```python
from google_auth_oauthlib.flow import InstalledAppFlow
flow = InstalledAppFlow.from_client_secrets_file(
    'client_secrets.json',
    scopes=['profile', 'email'])

flow.run_local_server()

client = photos_v1.PhotoServiceClient(credentials=flow.credentials)
```

#### Misc

##### Setting API Key

Example showing how to set the [API_KEY](https://developers.google.com/api-client-library/python/guide/aaa_apikeys).
```python
service = build(serviceName='oauth2', version= 'v2',http=http, developerKey='YOUR_API_KEY')
```

##### Logging

Enable verbose wire tracing.
```python
import logging
import httplib2
import sys

logFormatter = logging.Formatter('%(asctime)s - %(name)s - %(message)s')
root = logging.getLogger()
root.setLevel(logging.INFO)           
ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.INFO)    
ch.setFormatter(logFormatter)
root.addHandler(ch)
logging.getLogger('oauth2client.client').setLevel(logging.DEBUG)
logging.getLogger('apiclient.discovery').setLevel(logging.DEBUG)

httplib2.debuglevel=3
```

##### Appengine Cloud Endpoints Framework

Sample discovery for Appengine Cloud Enpoints.

>> Note: this is for use with Endpoints Framework running on GAE python27 and java7 (not OpenAPI)

```python
service = build(serviceName='myendpoint', discoveryServiceUrl='https://yourappid.appspot.com/_ah/api/discovery/v1/apis/yourendpoint/v1/rest',version= 'v1',http=http)
resource = service.yourAPI()
resp = resource.get(parameter='value').execute()
```

##### Credential store

See [credential store](https://developers.google.com/api-client-library/python/guide/aaa_oauth#storage) documentation.


##### ID Token from Service Account JSON Signed by Google

If you need an id_token issued by Google using your JSON certificate:

THe old way was to run through the full flow _manually_:

```python
from oauth2client.service_account import ServiceAccountCredentials
credentials = ServiceAccountCredentials.from_json_keyfile_name('YOUR_SERVICE_AcCOUNT.json')
now = int(time.time())
payload = {
        'iat': now,
        'exp': now + credentials.MAX_TOKEN_LIFETIME_SECS,
        'aud': 'https://www.googleapis.com/oauth2/v4/token',
        'iss': 'svc1-001@YOUR_PROJECT.iam.gserviceaccount.com',
        'scope': 'svc1-001@YOUR_PROJECT.iam.gserviceaccount.com'
}
signed_jwt = oauth2client.crypt.make_signed_jwt(credentials._signer, payload, key_id=credentials._private_key_id)
params = urllib.urlencode({
      'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      'assertion': signed_jwt })
headers = {"Content-Type": "application/x-www-form-urlencoded"}
conn = httplib.HTTPSConnection("www.googleapis.com")
conn.request("POST", "/oauth2/v4/token", params, headers)
res = json.loads(conn.getresponse().read())
print res
```

The new (preferred) way is to use the new ```iamcredentials`` API directly.  See [id_tokens/README.md](id_tokens/README.md)

- [id_tokens/iam_svc_tokens/main.py](id_tokens/iam_svc_tokens/main.py).  


both which Returns JSON with a JWT signed by Google:

```json
{"id_token": "YOUR_ID_TOKEN_SIGNED_BY_GOOGLE"}

```
Decoded JWT id_token:
```json
{
  "iss": "https://accounts.google.com",
  "aud": "svc1-001@YOUR_PROJECT.iam.gserviceaccount.com",
  "sub": "111402810199779215722",
  "email_verified": true,
  "azp": "svc1-001@YOUR_PROJECT.iam.gserviceaccount.com",
  "email": "svc1-001@YOUR_PROJECT.iam.gserviceaccount.com",
  "iat": 1468897846,
  "exp": 1468901446
}
```

If you are running the flow directly, if you used *'scope': 'https://www.googleapis.com/auth/userinfo.email'*, the return fields would include an access_token scoped to userinfo.email for the service account.  

***  

### Google API Java

[Java API Client Library](https://developers.google.com/api-client-library/java/).  Most of the samples below uses gradle to build and deploy.

#### Appengine

Under [auth/gae/javaapp](auth/gae/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *AppIdentityService*.  To deploy, edit the *build.gradle* file and enter the username of an administrator on the GAE application.

Sample shows both Cloud Client and Google API library set

```bash
mvn appengine:run
mvn appengine:deploy
```
#### ComputeEngine

Under [auth/compute/javaapp](auth/compute/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*.


```bash
mvn exec:java
```
#### Service Account File

Under [auth/service/javaapp](auth/service/javaapp).  Runs a simple application using both *Application DefaultCredentials* and by directly reading in the JSON certificate file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the JSON file, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials)


```bash
mvn exec:java
```
#### UserFlow

Under [auth/userflow/javaapp](auth/userflow/javaapp).  Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a browser and listener.

```bash
mvn exec:java
```
#### Misc

##### Logging

```java
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

ConsoleHandler consoleHandler = new ConsoleHandler();
consoleHandler.setLevel(Level.ALL);
consoleHandler.setFormatter(new SimpleFormatter());

Logger logger = Logger.getLogger("com.google.api.client");
logger.setLevel(Level.ALL);
logger.addHandler(consoleHandler);  

Logger lh = Logger.getLogger("httpclient.wire.header");
lh.setLevel(Level.ALL);
lh.addHandler(consoleHandler);

Logger lc = Logger.getLogger("httpclient.wire.content");
lc.setLevel(Level.ALL);
lc.addHandler(consoleHandler);

Logger gl = Logger.getLogger("io.grpc");
gl.setLevel(Level.FINE);
gl.addHandler(consoleHandler);
```

##### Setting API Key

```java
String API_KEY = "...";
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName("oauth client")
    .setOauth2RequestInitializer(new Oauth2RequestInitializer(API_KEY))    
    .build();
```

##### Setting Request Parameter

```java
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName("oauth client")
    .setOauth2RequestInitializer(new Oauth2RequestInitializer(){
        @Override
        public void initializeOauth2Request(Oauth2Request<?> request) {
            request.setPrettyPrint(true);
        }
    })      
    .build();
```

##### Credential store

See documentatin on [Drive](https://developers.google.com/drive/web/credentials?hl=en)


##### Exponential Backoff

See [ExponentialBackOff](https://developers.google.com/api-client-library/java/google-http-java-client/backoff)


Usign GoogleAPIs:

```java
import com.google.api.client.util.ExponentialBackOff;

final GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory).createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));

Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                    request.setContentLoggingLimit(0);
                    request.setCurlLoggingEnabled(false);
                    credential.initialize(request);
                    ExponentialBackOff backoff = new ExponentialBackOff.Builder()
                    .setInitialIntervalMillis(500)
                    .setMaxElapsedTimeMillis(900000)
                    .setMaxIntervalMillis(6000)
                    .setMultiplier(1.5)
                    .setRandomizationFactor(0.5)
                    .build();
                  request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(backoff));
                }
            })                  
            .setApplicationName("oauth client")
            .build();
```

or using Cloud Libraries

```java
import com.google.api.gax.retrying.RetrySettings;

        Storage storage = StorageOptions.newBuilder()
            .setCredentials(myprovider.getCredentials())
            .setRetrySettings(ServiceOptions.getDefaultRetrySettings())
            .build()
            .getService();
```

***  

###  Google API Go
[DefaultTokenSource](https://godoc.org/golang.org/x/oauth2/google#DefaultTokenSource)  

#### Appengine
Under [auth/gae/goapp](auth/gae/goapp).  Runs a simple GAE application using both *Application DefaultCredentials* and *AppEngineTokenSource*.  To deploy:

> Note: for use with Appengine Standard:

```bash
mkdir extra
export GOPATH=`pwd`/extra

go get golang.org/x/oauth2
go get google.golang.org/appengine/...
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go get cloud.google.com/go/compute/metadata


run locally:
  dev_appserver.py src/app.yaml

deploy:
  gcloud app deploy deploy src/app.yaml

```

#### ComputeEngine

Under [auth/compute/goapp](auth/compute/goapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeTokenSource*.  To deploy:

> Make sure you create a GCE instance with the ```userinfo.email``` scope.  That will allow the token to be used against the oauth2 endpoint

```bash
export GOPATH=`pwd`

go get golang.org/x/oauth2
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go get cloud.google.com/go/compute/metadata


go run src/main.go
```

#### Service Account JSON File

Under [auth/service/goapp](auth/service/goapp).  Runs a simple application using both *Application DefaultCredentials* and directly reading *JWTConfigFromJSON*.  

The sample also demonstrates _both_ google api clients and google cloud client libraries.

To use:

edit
```	serviceAccountJSONFile := "YOUR_SERVICE_ACCOUNT_JSON_FILE"```   and set the path to your service account JSON file.
After that, you can either explictly use the credential type for service account (```JWTConfigFromJSON```, or set the environment variable ause ADC)

THis sample also uses the service account to iterate over the Cloud Storage buckets.  Make sure the service account has that permissoin

```bash
export GOPATH=`pwd`

go get golang.org/x/oauth2
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go get cloud.google.com/go/compute/metadata
go get github.com/googleapis/gax-go
go get o.opencensus.io/trace
go get go.opencensus.io/plugin/ochttp
go get go.opencensus.io/exporter/stackdriver/propagation
go get google.golang.org/grpc

go run src/main.go
```

#### UserFlow

Under [auth/userflow/goapp](auth/userflow/goapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a link URL and expects the authorization token to get entered (installed application).

To use, go to the cloud console, "API & Credentials > Credentials", then "Create Credentials > Oauth2 Credentials > Other".  Copy the clientID and secret into main.go:

```
        ClientID:     "YOUR_CLIENT_ID",
        ClientSecret: "YOUR_CLIENT_SECRET",
```

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2

go run src/main.go
```

You will see a URL.  Copy that URL to a browser, login and then enter the code.  This sample runs the "installed application" flow

#### Misc

##### Setting API Key

```go
import "google.golang.org/api/googleapi/transport"
apiKey :="YOUR_API_KEY"
client.Transport = &transport.APIKey{
    Key: apiKey,
}
```

##### ID Token Signed by Google

The following only works with your local (user) gcloud credentials.

Also see

- [id_token/README.md](id_token/README.md)

```golang
import (
	"log"

	"golang.org/x/net/context"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
	oauthsvc "google.golang.org/api/oauth2/v2"

	"google.golang.org/grpc/credentials/oauth"
)

func main() {

	src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}

	creds := oauth.TokenSource{src}
	tok, err := creds.Token()
	if err != nil {
		log.Fatalf("Unable to acquire token source: %v", err)
	}
	if (tok.Extra("id_token") != nil){
		log.Printf("id_token: " , tok.Extra("id_token").(string))
	}
}
```

[Validating id_token](https://developers.google.com/identity/protocols/OpenIDConnect?hl=en#validatinganidtoken)

```go
src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
if err != nil {
    log.Fatalf("Unable to acquire token source: %v", err)
}
tok, err := src.Token()
if err != nil {
    log.Fatalf("Unable to acquire token: %v", err)
}
log.Printf("id_token: " , tok.Extra("id_token").(string))
```

Also see  
* [Golang Token verificaiton](http://stackoverflow.com/questions/26159658/golang-token-validation-error/26287613#26287613)
* [JWT debugger](http://jwt.io/)

##### Credential store

See [oauth2.ReuseTokenSource](https://www.godoc.org/golang.org/x/oauth2#ReuseTokenSource)

```go

src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
if err != nil {
   log.Fatalf("Unable to acquire token source: %v", err)
}

tok, err := tokenFromFile("credential.token")
src = oauth2.ReuseTokenSource(tok,src)
tokenval, err := src2.Token()
if err != nil {
    log.Fatalf("Token can't be read")
} else {
    log.Printf("token %v\n", tokenval.AccessToken)
}

client := oauth2.NewClient(context.Background(), src)
svc, err := oauthsvc.New(client)
if err != nil {
    log.Fatalf("ERROR: ", err)
}
...
...
func saveToken(file string, token *oauth2.Token) {
    f, err := os.Create(file)
    if err != nil {
        log.Printf("Warning: failed to cache oauth token: %v", err)
        return
    }
    defer f.Close()
    json.NewEncoder(f).Encode(token)
}

func tokenFromFile(file string) (*oauth2.Token, error) {
    f, err := os.Open(file)
    if err != nil {
        return nil, err
    }
    defer f.Close()
    t := new(oauth2.Token)
    err = json.NewDecoder(f).Decode(t)
    return t, err
}
```

##### Logging

The follwoing example of trace http logging wraps the Transport around a logging version:
[LogTransport](https://code.google.com/p/google-api-go-client/source/browse/examples/debug.go).  

This example also shows how the *API_KEY* could get constructed although this particular API (oauth2/v2) does not need or expect an api_key.

```go
package main
import (
    "golang.org/x/oauth2"
    "golang.org/x/oauth2/google"    
    "log"   
    oauthsvc "google.golang.org/api/oauth2/v2"
    "google.golang.org/api/googleapi/transport"
    "net/http"
)
const (
    api_key   = "YOUR_API_KEY"
)
func Auth() {
    src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
    if err != nil {
        log.Fatalf("Unable to acquire token source: %v", err)
    }
    transport := &transport.APIKey{
    //  Key:       api_key,
        Transport: &logTransport{http.DefaultTransport},
    }
    client := &http.Client{
        Transport: &oauth2.Transport{
            Source: src,
            Base:   transport,
        },
    }           
    service, err := oauthsvc.New(client)
    if err != nil {
        log.Fatalf("Unable to create oauth2 service client: %v", err)
    }
    ui, err := service.Userinfo.Get().Do()
    if err != nil {
        log.Fatalf("ERROR: ", err)
    }   
    log.Printf("UserInfo: %v", ui.Email)
}
```


***  

###  Google API NodeJS
[google.auth.getApplicationDefault](https://developers.google.com/identity/protocols/application-default-credentials#callingnode)  

#### Appengine

Under [auth/gae/nodeapp](auth/gae/nodeapp).  Runs a simple GAE application using *Application DefaultCredentials*.  To deploy:
```
gcloud app deploy app.yaml
```

#### ComputeEngine

Runs sample on ComputeEngine.  Requires the userinfo scope enabled on the compute engine instance.

```bash
npm install
npm start
```

#### Service Account JSON File

Under [auth/service/nodeapp](auth/service/nodeapp).  Runs a simple application using both *Application DefaultCredentials* and directly reading *JSON KEY file*.

#### UserFlow

Under [auth/userflow/nodeapp](auth/userflow/nodeapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow provides a link URL and expects the authorization token to get entered (installed application).


#### Misc

##### Setting API Key

```node
var service = google.oauth2({
      version: 'v2',
      auth: authClient,
      params: { key: 'YOUR_API_KEY'}
});
```

##### Logging

```bash
export NODE_DEBUG=request
```

***

###  Google API C&#35;
.NET packages downloadable from [NuGet](https://www.nuget.org/packages/Google.Apis/).  Full end-to-end example of all the auth modes available here for CloudStorage

* [Google API .NET Library](https://developers.google.com/api-client-library/dotnet/get_started)


The following code snippet demonstrates *both* google apis and google cloud libraries all in one [auth/compute/dotnet](auth/compute/dotnet):

```
cd auth/compute/dotnet
dotnet restore
dotnet run
```


#### Appengine

GAE Standard does not support .NET as a runtime.  However, you can deploy your application to GAE Flex if you run .NET Core on Linux.  See the following sample that runs a .NET
webapp in Flex:  [.NET on GCP](https://github.com/salrashid123/gcpdotnet).
Note: Google APIs do not support .NET Core (coreCLR) yet.  At the time of writing, they only supports upto [.NET Framework 4.5.1](https://www.nuget.org/packages/Google.Apis/).  This
means you cannot use Google APIs from within a Container.   There are some [ports](https://www.nuget.org/packages/GoogleApis.Core.vNext/) to coreCLR but they are not officially supported.

#### ComputeEngine

Under [auth/compute/dotnet](auth/compute/dotnet).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*.

```
dotnet restore
dotnet run
```

#### Service Account JSON File

Under [auth/service/dotnet](auth/service/dotnet).  Runs a simple application using both *Application DefaultCredentials* using a **JSON Certificate** and by directly reading in the **PKCS12 Certificate** file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the **JSON file**, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials).

Edit [service/dotnet/ServiceAuth.cs](service/dotnet/ServiceAuth.cs) file and set the path to the service account key file.  Then,

```
dotnet restore
dotnet run
```

#### UserFlow

Under [auth/userflow/dotnet](auth/userflow/dotnet).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches provides a link URL and expects user consent on the browser.

##### Credential store

Credentials from the GoogleAPIs userflow is usually stored at

```
Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
or c:\Users\%USER%\AppData\Roaming\Google.Apis.Auth
```

#### Using API through Proxy

gRPC clients support http_proxy parameter:
- [https://github.com/grpc/grpc/blob/master/doc/environment_variables.md](https://github.com/grpc/grpc/blob/master/doc/environment_variables.md)


* The following is curated from:
[https://kzhendev.wordpress.com/2015/04/28/accessing-google-apis-through-a-proxy-with-net/](https://kzhendev.wordpress.com/2015/04/28/accessing-google-apis-through-a-proxy-with-net/)


See [proxy](proxy) folder for detailed usage.

to use:

```
docker run  -p 3128:3128 -ti docker.io/salrashid123/squidproxy /bin/bash

then when inside the container:
/apps/squid/sbin/squid -NsY -f /apps/squid.conf.transparent &

tail -f /apps/squid/var/logs/access.log
```
