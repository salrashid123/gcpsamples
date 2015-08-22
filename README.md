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

* [Python](#python)
    - Appengine
    - ComputeEngine
    - Service Account File
    - Userflow
* [Java](#java)
    - Appengine
    - ComputeEngine
    - Service Account File
    - Userflow
* [Go](#go)
    - Appengine
    - ComputeEngine
    - Service Account File
    - Userflow
* [C#](#c)
    - ComputeEngine
    - Service Account File
    - Userflow


For more inforamtion, see:
* [oauth2 protocol](https://developers.google.com/identity/protocols/OAuth2)
* [oauth2 service](https://developers.google.com/apis-explorer/#p/oauth2/v2/)

###Python
* [Google API Client Library for Python](https://developers.google.com/api-client-library/python/)

```
apt-get install curl python2.7 python-pip
pip install requests google-api-python-client httplib2 oauth2client
```

####Appengine
Under [auth/gae/pyapp/](auth/gae/pyapp/)  Deploys an application to appengine that uses *Application Default Credentials*.  

*AppAssertionCredentials*  is also shown but commented.

Remember to edit app.yaml file with your appID.

####ComputeEngine
Under [auth/compute/pyapp](auth/compute/pyapp)  Runs a simple application on compute engine using *Application Default Credentials*.

*AppAssertionCredentials* is also shown but commented

####Service Account File
Under [auth/service/pyapp](auth/service/pyapp/)  Runs a simple application that uses the service account credential from both a PKCS12 file and a JSON keyfile.  Application Default Credentials uses the JSON keyfile only if the *GOOGLE_APPLICATION_CREDENTIALS* variable isset

For more details, goto [Service Accounts](https://developers.google.com/api-client-library/python/auth/service-accounts)

####Userflow
Under [auth/userflow/pyapp](auth/userflow/pyapp)  Runs a simple application that performs user-interactive webflow and propmpts the user for consent.  Download an *installed* app client_secrets.json and reference it for the 'flow_from_clientsecrets()' method.

For more deails, goto [flow_from_clientsecrets](https://developers.google.com/api-client-library/python/guide/aaa_oauth#flow_from_clientsecrets)

####Misc

#####Setting API Key 
Example showing how to set the [API_KEY](https://developers.google.com/api-client-library/python/guide/aaa_apikeys).
```python
service = build(serviceName='oauth2', version= 'v2',http=http, developerKey='YOUR_API_KEY')
```

#####Logging 
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

#####Appengine Cloud Endpoints
Sample discovery for Appengine Cloud Enpoints
```python
service = build(serviceName='myendpoint', discoveryServiceUrl='https://yourappid.appspot.com/_ah/api/discovery/v1/apis/yourendpoint/v1/rest',version= 'v1',http=http)
resource = service.yourAPI()
resp = resource.get(parameter='value').execute()
```

#####Credential store
See [credential store](https://developers.google.com/api-client-library/python/guide/aaa_oauth#storage) documentation.

###Java

[Java API Client Library](https://developers.google.com/api-client-library/java/).  Most of the samples below uses gradle to build and deploy.

####Appengine
Under [auth/gae/javaapp](auth/gae/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *AppIdentityService*.  To deploy, edit the *build.gradle* file and enter the username of an administrator on the GAE application.

```bash
gradle task
gradle war
gradle gaeUpdate
```

####ComputeEngine
Under [auth/compute/javaapp](auth/compute/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

```bash
gradle task
gradle run
```

####Service Account File
Under [auth/service/javaapp](auth/service/javaapp).  Runs a simple application using both *Application DefaultCredentials* and by directly reading in the JSON certificate file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the JSON file, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials)

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/you/json/file.json
gradle task
gradle run
```

####UserFlow
Under [auth/userflow/javaapp](auth/userflow/javaapp).  Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a browser and listener.

```bash
gradle task
gradle run
```


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
See documentatin on [Drive](https://developers.google.com/drive/web/credentials?hl=en)

###Go
[DefaultTokenSource](https://godoc.org/golang.org/x/oauth2/google#DefaultTokenSource)  

####Appengine
Under [auth/gae/goapp](auth/gae/goapp).  Runs a simple GAE application using both *Application DefaultCredentials* and *AppEngineTokenSource*.  To deploy:

```bash
mkdir extra
export GOPATH=/path/to/where/the/extra/folder/is
go get golang.org/x/oauth2
go get google.golang.org/appengine/...
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
google-cloud-sdk/go_appengine/goapp deploy src/app.yaml
```

####ComputeEngine
Under [auth/compute/goapp](auth/compute/goapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeTokenSource*.  To deploy:

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

####Service Account JSON File
Under [auth/service/goapp](auth/service/goapp).  Runs a simple application using both *Application DefaultCredentials* and directly reading *JWTConfigFromJSON*.  To deploy:

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

####UserFlow
Under [auth/userflow/goapp](auth/userflow/goapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches provides a link URL and expects the authorization token to get entered (installed application).

```bash
go get golang.org/x/net/context
go get golang.org/x/oauth2/google
go get google.golang.org/cloud/compute/...
go get google.golang.org/api/oauth2/v2
go run src/main.go
```

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


###C&#35;
.NET packages downloadable from [NuGet](https://www.nuget.org/packages/Google.Apis/).  Full end-to-end example of all the auth modes available here for CloudStorage

* [Google API .NET Library](https://developers.google.com/api-client-library/dotnet/get_started)

####ComputeEngine
Under [auth/compute/dotnet] (auth/compute/dotnet).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

####Service Account JSON File
Under [auth/service/dotnet](auth/service/dotnet).  Runs a simple application using both *Application DefaultCredentials* using a **JSON Certificate** and by directly reading in the **PKCS12 Certificate** file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the **JSON file**, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials).

####UserFlow
Under [auth/userflow/dotnet](auth/userflow/dotnet).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches provides a link URL and expects user consent on the browser.

#####Credential store
Credentials are usually stored in
gcloud
```
Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
or c:\Users\%USER%\AppData\Roaming\gcloud
```
