## Google Cloud Platform Samples

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
gcloud beta auth application-default login

```

#### Cloud Python

The following uses the google-storage client described here: [Storage Client](http://gcloud-python.readthedocs.io/en/latest/storage-client.html)


```
virtualenv env 
source env/bin/activate
pip install google-cloud

Name: google-cloud
Version: 0.22.0
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

see [google cloud python iterators](https://googlecloudplatform.github.io/google-cloud-python/latest/iterators.html)

```python
import os
#os.environ["GOOGLE_CLOUD_DISABLE_GRPC"] = "true"

from google.cloud import logging
from google.cloud.logging import DESCENDING

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
#### Cloud Java

* [http://googlecloudplatform.github.io/google-cloud-java/0.8.0/index.html](http://googlecloudplatform.github.io/google-cloud-java/0.8.0/index.html)
* [StorageExample](https://github.com/GoogleCloudPlatform/google-cloud-java/blob/master/google-cloud-examples/src/main/java/com/google/cloud/examples/storage/StorageExample.java)
* [Java Cloud Examples](https://github.com/GoogleCloudPlatform/java-docs-samples)
* [Google Extensions for Java (GAX)](http://googleapis.github.io/gax-java/)

The following describes using java default credentials.  You can explictly _setCredentials()_ while initializing a service but that is not recommended as the code is not portable

The various credential types can be found here:
* [com.google.auth.oauth2](https://github.com/google/google-auth-library-java/tree/master/oauth2_http/java/com/google/auth/oauth2)
* [Credential Types](https://github.com/google/google-auth-library-java#google-auth-library-oauth2-http)

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

Storage  storage_service = StorageOptions.defaultInstance().service();
        
Iterator<Bucket> bucketIterator = storage_service.list().iterateAll();
while (bucketIterator.hasNext()) {
  System.out.println(bucketIterator.next());
}


URL signedUrl = storage_service.signUrl(BlobInfo.newBuilder("your_project", "a.txt").build(), 60,  TimeUnit.SECONDS);
System.out.println(signedUrl);

```

##### Proxy Server Settings

- [https://github.com/grpc/grpc-java/releases/tag/v1.0.3](https://github.com/grpc/grpc-java/releases/tag/v1.0.3)
```  
export  GRPC_PROXY_EXP=proxy_server:3128
```
#### Credential Providers

```java
import com.google.api.gax.grpc.InstantiatingChannelProvider;
import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.Credentials;

	String cert_file = "GCPNETAppID-e65deccae47b.json";
    //export GOOGLE_APPLICATION_CREDENTIALS="/path/to/keyfile.json" 

    String cred_env = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    System.out.println(cred_env);
    List<String> ll = Arrays.asList("https://www.googleapis.com/auth/cloud-platform");

    //GoogleCredentialsProvider myprovider = GoogleCredentialsProvider.newBuilder().setScopesToApply(ll).build();
    ServiceAccountCredentials creds = ServiceAccountCredentials.fromStream(new FileInputStream(cert_file));
    
    FixedCredentialsProvider myprovider = FixedCredentialsProvider.create(creds);
    System.out.println(myprovider.getCredentials() );

     InstantiatingChannelProvider channelProvider = TopicAdminSettings.defaultChannelProviderBuilder()
        .setCredentialsProvider(myprovider)
        .build();
```

##### Async Futures

```java
import com.google.api.client.http.HttpTransport;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.spi.v1.Publisher;
import com.google.cloud.pubsub.spi.v1.TopicAdminClient;
import com.google.cloud.pubsub.spi.v1.TopicAdminSettings;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;

import com.google.api.gax.grpc.InstantiatingChannelProvider;
import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.iam.v1.Binding;
import com.google.cloud.Role;


    List<String> ll = Arrays.asList("https://www.googleapis.com/auth/cloud-platform");
    GoogleCredentialsProvider myprovider = GoogleCredentialsProvider.newBuilder().setScopesToApply(ll).build();
    System.out.println(myprovider.getCredentials());

     InstantiatingChannelProvider channelProvider = TopicAdminSettings.defaultChannelProviderBuilder()
        .setCredentialsProvider(myprovider)
        .build();    
        
     TopicAdminSettings topicAdminSettings =  TopicAdminSettings.defaultBuilder().setChannelProvider(channelProvider).build();
     TopicAdminClient topicAdminClient =  TopicAdminClient.create(topicAdminSettings);
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


```node
var log4js = require("log4js");
var logger = log4js.getLogger();


// var gcloud = require('google-cloud')({
//  keyFilename: '/path/to/keyfile.json'
// });

var gcloud = require('google-cloud');

var gcs = gcloud.storage();

gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});    
  }
});
```

#### Cloud C#

Use [Google.Cloud.Storage.V1](https://www.nuget.org/packages/Google.Cloud.Storage.V1/) package for Google Cloud API access

* [google-cloud-dotnet](https://github.com/GoogleCloudPlatform/google-cloud-dotnet)
* [API Documentation](http://googlecloudplatform.github.io/google-cloud-dotnet/docs/Google.Cloud.Storage.V1/)

```csharp
using Google.Storage.V1;

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
mkdir lib
pip install --target=lib  requests google-api-python-client httplib2 oauth2client

cat your_svc_account.p12 | openssl pkcs12 -nodes -nocerts -passin pass:notasecret | openssl rsa > key.pem

gcloud preview app run app.yaml --appidentity-email-address=YOUR_SERVICE_ACCOUNT_ID@developer.gserviceaccount.com --appidentity-private-key-path=key.pem

```

For info on ```--appidentity-email-address``` and ```--appidentity-private-key-path```, see documentation on [gcloud dev_appserver](https://cloud.google.com/sdk/gcloud/reference/preview/app/run).

#### ComputeEngine

Under [auth/compute/pyapp](auth/compute/pyapp)  Runs a simple application on compute engine using *Application Default Credentials*.

*AppAssertionCredentials* is also shown but commented

#### Service Account File

Under [auth/service/pyapp](auth/service/pyapp/)  Runs a simple application that uses the service account credential from both a PKCS12 file and a JSON keyfile.  Application Default Credentials uses the JSON keyfile only if the *GOOGLE_APPLICATION_CREDENTIALS* variable isset

For more details, goto [Service Accounts](https://developers.google.com/api-client-library/python/auth/service-accounts)

#### Userflow

Under [auth/userflow/pyapp](auth/userflow/pyapp)  Runs a simple application that performs user-interactive webflow and propmpts the user for consent.  Download an *installed* app client_secrets.json and reference it for the 'flow_from_clientsecrets()' method.

For more deails, goto [flow_from_clientsecrets](https://developers.google.com/api-client-library/python/guide/aaa_oauth#flow_from_clientsecrets)

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

##### Appengine Cloud Endpoints

Sample discovery for Appengine Cloud Enpoints
```python
service = build(serviceName='myendpoint', discoveryServiceUrl='https://yourappid.appspot.com/_ah/api/discovery/v1/apis/yourendpoint/v1/rest',version= 'v1',http=http)
resource = service.yourAPI()
resp = resource.get(parameter='value').execute()
```

##### Credential store

See [credential store](https://developers.google.com/api-client-library/python/guide/aaa_oauth#storage) documentation.


##### ID Token from Service Account JSON Signed by Google

If you need an id_token issued by Google using your JSON certificate:
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

##### Returns JSON with a JWT signed by Google:

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

In the same flow, if you used *'scope': 'https://www.googleapis.com/auth/userinfo.email'*, the return fields would include an access_token scoped to userinfo.email for the service account.  
You do not need to explicitly recall the access_token as that is normally used internally when a Credential is initialized for a given Google API.

***  

### Google API Java

[Java API Client Library](https://developers.google.com/api-client-library/java/).  Most of the samples below uses gradle to build and deploy.

#### Appengine

Under [auth/gae/javaapp](auth/gae/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *AppIdentityService*.  To deploy, edit the *build.gradle* file and enter the username of an administrator on the GAE application.

```bash
gradle task
gradle appengineRun
gradle appengineDeploy
```

```bash
mvn appengine:run
mvn appengine:deploy
```
#### ComputeEngine

Under [auth/compute/javaapp](auth/compute/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

```bash
gradle task
gradle run
```

```bash
mvn exec:java
```
#### Service Account File

Under [auth/service/javaapp](auth/service/javaapp).  Runs a simple application using both *Application DefaultCredentials* and by directly reading in the JSON certificate file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the JSON file, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials)

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/you/json/file.json
gradle task
gradle run
```

```bash
mvn exec:java
```
#### UserFlow

Under [auth/userflow/javaapp](auth/userflow/javaapp).  Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a browser and listener.

```bash
gradle task
gradle run
```

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


```bash
mkdir extra
export GOPATH=/path/to/where/the/extra/folder/is
go get golang.org/x/oauth2
go get google.golang.org/appengine/...
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2

# vm: false
google-cloud-sdk/go_appengine/goapp serve src/app.yaml
google-cloud-sdk/go_appengine/goapp deploy src/app.yaml

# vm: true
uncomment appengine.Main in func main
gcloud app run src/app.yaml
gcloud app deploy src/app.yaml --version 1 --set-default
```

#### ComputeEngine

Under [auth/compute/goapp](auth/compute/goapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeTokenSource*.  To deploy:

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

#### Service Account JSON File

Under [auth/service/goapp](auth/service/goapp).  Runs a simple application using both *Application DefaultCredentials* and directly reading *JWTConfigFromJSON*.  To deploy:

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

#### UserFlow

Under [auth/userflow/goapp](auth/userflow/goapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a link URL and expects the authorization token to get entered (installed application).

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

#### Misc

##### Setting API Key

```go
import "google.golang.org/api/googleapi/transport"
apiKey :="YOUR_API_KEY"
client.Transport = &transport.APIKey{ 
    Key: apiKey, 
}
```

##### Validating id_token

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

#### Appengine

GAE Standard does not support .NET as a runtime.  However, you can deploy your application to GAE Flex if you run .NET Core on Linux.  See the following sample that runs a .NET
webapp in Flex:  [.NET on GCP](https://github.com/salrashid123/gcpdotnet).
Note: Google APIs do not support .NET Core (coreCLR) yet.  At the time of writing, they only supports upto [.NET Framework 4.5.1](https://www.nuget.org/packages/Google.Apis/).  This
means you cannot use Google APIs from within a Container.   There are some [ports](https://www.nuget.org/packages/GoogleApis.Core.vNext/) to coreCLR but they are not officially supported.

#### ComputeEngine

Under [auth/compute/dotnet](auth/compute/dotnet).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

#### Service Account JSON File

Under [auth/service/dotnet](auth/service/dotnet).  Runs a simple application using both *Application DefaultCredentials* using a **JSON Certificate** and by directly reading in the **PKCS12 Certificate** file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the **JSON file**, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials).

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


```csharp
using System.Net;
using System.Net.Http;
using System.Security.Cryptography.X509Certificates;

using Google.Apis.Auth.OAuth2;
using Google.Apis.Oauth2.v2;
using Google.Apis.Services;
using Google.Apis.Http;


            string CREDENTIAL_FILE_PKCS12 = "c:\\your_cert.p12"; 
            string serviceAccountEmail = "your_svc_account@project.iam.gserviceaccount.com";
            var certificate = new X509Certificate2(CREDENTIAL_FILE_PKCS12, "notasecret",X509KeyStorageFlags.Exportable);
            ServiceAccountCredential credential = new ServiceAccountCredential(
               new ServiceAccountCredential.Initializer(serviceAccountEmail)
               {
                   Scopes = new[] { Oauth2Service.Scope.UserinfoEmail },
                   HttpClientFactory = new ProxySupportedHttpClientFactory()
               }.FromCertificate(certificate));


            var service = new Oauth2Service(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credential,
                ApplicationName = "Oauth2 Sample",
                HttpClientFactory = new ProxySupportedHttpClientFactory()
            });



public class ProxySupportedHttpClientFactory : HttpClientFactory
{
    protected override HttpMessageHandler CreateHandler(CreateHttpClientArgs args)
    {

        ICredentials credentials = new NetworkCredential("user1", "user1");

        var proxy = new WebProxy("http://192.168.1.6:3128", true, null, credentials);

        var webRequestHandler = new WebRequestHandler()
        {
            UseProxy = true,
            Proxy = proxy,
            UseCookies = false
        };

        return webRequestHandler;
    }
}

```
