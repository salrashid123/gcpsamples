## Google Cloud Platform Samples

#### all samples here are provided as-is without warranty

Sample code demonstrating various Google Cloud Platform APIs.

Please refer to official documentation for usage and additional samples/usage.

Code samples contained in this repo contain:

  * BigQuery
    * Basic query against public dataset in C# and Go.  
  * CloudLogging
    * Listing and inserting custom log messages in C# and Go.  
  * CloudMonitoring
    * Listing MetricsDescriptors and inserting custom metric in C# and Go.  

***  

### Application Default Credentials

The samples use [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials) which uses credentials in the following order as described in the link.  Set the environment variable to override.  

You can always specify the target source to acquire credentials by using intent specific targets such as:  ComputeCredentials, UserCredentials or ServiceAccountCredential.

The following examples use the Oauth2 *service* to demonstrate the initialized client.  


* [oauth2 protocol](https://developers.google.com/identity/protocols/OAuth2)
* [oauth2 service](https://developers.google.com/apis-explorer/#p/oauth2/v2/)

###Python
[Google API Client Library for Python](https://developers.google.com/api-client-library/python/)  

```
apt-get install curl python2.7 python-pip
pip install requests google-api-python-client httplib2 oauth2client
```

####Appengine
Under [auth/gae/pyapp/](auth/gae/pyapp/).  Deploys an application to appengine that uses *Application Default Credentials*.  

*AppAssertionCredentials*  is also shown but commented.

Remember to edit app.yaml file with your appID.

####ComputeEngine
Under [auth/compute/pyapp](auth/compute/pyapp).  Runs a simple application on compute engine using *Application Default Credentials*.

*AppAssertionCredentials* is also shown but commented

####Service Account File
Under [auth/service/pyapp](auth/service/pyapp/).  Runs a simple application that uses the service account credential from both a PKCS12 file and a JSON keyfile.  Application Default Credentials uses the JSON keyfile only if the *GOOGLE_APPLICATION_CREDENTIALS* variable isset

[Service Accounts](https://developers.google.com/api-client-library/python/auth/service-accounts)

####Userflow
Under [auth/userflow/pyapp](auth/userflow/pyapp).  Runs a simple application that performs user-interactive webflow and propmpts the user for consent.  Download an *installed* app client_secrets.json and reference it for the 'flow_from_clientsecrets()' method.

[flow_from_clientsecrets](https://developers.google.com/api-client-library/python/guide/aaa_oauth#flow_from_clientsecrets)

####Misc

#####Setting API Key 
```python
service = build(serviceName='oauth2', version= 'v2',http=http, developerKey='YOUR_API_KEY')
```

#####Logging 
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

#####Appengine Cloud Endpoints
```python
service = build(serviceName='myendpoint', discoveryServiceUrl='https://yourappid.appspot.com/_ah/api/discovery/v1/apis/yourendpoint/v1/rest',version= 'v1',http=http)
resource = service.yourAPI()
resp = resource.get(parameter='value').execute()
```

#####Credential store

###Java
[Java API Client Library](https://developers.google.com/api-client-library/java/)

####Appengine

####ComputeEngine

####Service Account File

####UserFlow

####Misc
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
```

#####Setting API Key 
```java
String API_KEY = "...";
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName("oauth client")
    .setOauth2RequestInitializer(new Oauth2RequestInitializer(API_KEY))    
    .build();
```

#####Setting Request Parameter
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

#####Credential store

###Go
[DefaultTokenSource](https://godoc.org/golang.org/x/oauth2/google#DefaultTokenSource)  

####Appengine

####ComputeEngine

####Service Account JSON File


####UserFlow

####Misc

#####Setting API Key
```go
import "google.golang.org/api/googleapi/transport"
apiKey :="YOUR_API_KEY"
client.Transport = &transport.APIKey{ 
    Key: apiKey, 
}
```

#####Credential store

###C&#35;
* [NuGet](https://www.nuget.org/packages/Google.Apis/)

####ComputeEngine

####Service Account JSON File

####UserFlow

#####Credential store