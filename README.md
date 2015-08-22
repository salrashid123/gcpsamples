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

####Appengine
```python
from oauth2client.appengine import AppAssertionCredentials
from google.appengine.api import app_identity
from apiclient.discovery import build

scope='https://www.googleapis.com/auth/userinfo.email'
#credentials = AppAssertionCredentials(scope=scope)
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scope)
http = credentials.authorize(httplib2.Http())
resp = service.userinfo().get().execute()
logging.info(resp['email'])
```

####ComputeEngine
```python
import pprint
import httplib2
from apiclient.discovery import build
from oauth2client.client import GoogleCredentials
from oauth2client.gce import AppAssertionCredentials

scope='https://www.googleapis.com/auth/userinfo.email'
#credentials = AppAssertionCredentials(scope=scope)
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scope)
http = httplib2.Http()
credentials.authorize(http)
service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print resp['email']
```

####Service Account PKCS12 File
[Service Accounts](https://developers.google.com/api-client-library/python/auth/service-accounts)
```python
from oauth2client.client import SignedJwtAssertionCredentials

f = file('YOUR_CERTIFICATE_FILE.p12', 'r').read()
key = f.read()
f.close()
credentials = SignedJwtAssertionCredentials(
        service_account_name = 'YOUR_SERIVCE_ACCOUNT@developer.gserviceaccount.com',
        private_key = key, private_key_password='notasecret',
        scope='https://www.googleapis.com/auth/userinfo.email')
```

####Userflow (installed)
[flow_from_clientsecrets](https://developers.google.com/api-client-library/python/guide/aaa_oauth#flow_from_clientsecrets)
```python
import httplib2
from apiclient import discovery
import oauth2client
from apiclient.discovery import build
from oauth2client.client import flow_from_clientsecrets

flow  = flow_from_clientsecrets('client_secrets.json',
                               scope='https://www.googleapis.com/auth/userinfo.email',
                               redirect_uri='urn:ietf:wg:oauth:2.0:oob')
auth_uri = flow.step1_get_authorize_url()
print 'goto the following url ' +  auth_uri
code = raw_input('Enter your input:')
credentials = flow.step2_exchange(code)
http = credentials.authorize(httplib2.Http())
service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print resp['email']
```

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

###Java
[Java API Client Library](https://developers.google.com/api-client-library/java/)

####Appengine

```java
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.*;
import com.google.api.services.oauth2.model.Userinfo;
/*
AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();    
AppIdentityService.GetAccessTokenResult accessToken = appIdentity.getAccessToken(Arrays.asList(StorageScopes.DEVSTORAGE_FULL_CONTROL));         
GoogleCredential credential;
    credential.setAccessToken(accessToken.getAccessToken());
*/
HttpTransport httpTransport = new UrlFetchTransport();        
JacksonFactory jsonFactory = new JacksonFactory();
    
GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);    
if (credential.createScopedRequired())
    credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));           
    
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName("oauth client").build();
    
Userinfo ui = service.userinfo().get().execute(); 
```

####ComputeEngine
```java
import java.util.Arrays;
import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Request;
import com.google.api.services.oauth2.Oauth2RequestInitializer;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfo;

HttpTransport httpTransport = new NetHttpTransport();             
JacksonFactory jsonFactory = new JacksonFactory();

//ComputeCredential credential = new ComputeCredential.Builder(httpTransport, jsonFactory).build();

GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);
            
if (credential.createScopedRequired())
    credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));           
            
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("oauth client")   
            .build();
            
Userinfo ui = service.userinfo().get().execute();
System.out.println(ui.getEmail());
```

####Service Account JSON File
```java
import java.util.Arrays;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Request;
import com.google.api.services.oauth2.Oauth2RequestInitializer;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfo;

String SERVICE_ACCOUNT_JSON_FILE = "YOUR_SERVICE_ACCOUNT_JSON_FILE.json";

HttpTransport httpTransport = new NetHttpTransport();             
JacksonFactory jsonFactory = new JacksonFactory();
        
//FileInputStream inputStream = new FileInputStream(new File(SERVICE_ACCOUNT_JSON_FILE));
//GoogleCredential credential = GoogleCredential.fromStream(inputStream, httpTransport, jsonFactory);
            
// set environment variable outside java first GOOGLE_APPLICATION_CREDENTIALS=YOUR_SERVICE_ACCOUNT_JSON_FILE.json        
GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);
            
if (credential.createScopedRequired())
    credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));           
            
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("oauth client")   
            .build();
            
Userinfo ui = service.userinfo().get().execute();
System.out.println(ui.getEmail());
```

####UserFlow (installed)
```java
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.*;
import com.google.api.services.oauth2.model.Userinfo;

HttpTransport httpTransport = new NetHttpTransport();             
JacksonFactory jsonFactory = new JacksonFactory();
            
GoogleClientSecrets clientSecrets =  new GoogleClientSecrets();
GoogleClientSecrets.Details det = new GoogleClientSecrets.Details();
det.setClientId("YOUR_CLIENT_ID");
det.setClientSecret("YOUR_CLIENT_SECRET");    
det.setRedirectUris(Arrays.asList("urn:ietf:wg:oauth:2.0:oob"));
clientSecrets.setInstalled(det);
            
GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    httpTransport, jsonFactory, clientSecrets,
    Collections.singleton(Oauth2Scopes.USERINFO_EMAIL)).build();            
Credential credential = new AuthorizationCodeInstalledApp(flow,
    new LocalServerReceiver()).authorize("user");
            
Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
    .setApplicationName("oauth client").build();
            
Userinfo ui = service.userinfo().get().execute();
```

#####Trace logging
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


###Go
[DefaultTokenSource](https://godoc.org/golang.org/x/oauth2/google#DefaultTokenSource)  

####Appengine
```go
import (
    "fmt"
    "golang.org/x/oauth2"
    "golang.org/x/oauth2/google"
    oauthsvc "google.golang.org/api/oauth2/v2"
    "google.golang.org/appengine"
    "google.golang.org/appengine/log"
    "google.golang.org/appengine/urlfetch"
    "net/http"
)
const ()
func init() {
    http.HandleFunc("/appid", appidhandler)
}
func appidhandler(w http.ResponseWriter, r *http.Request) {
    w.Header().Set("Content-Type", "text/plain")
    ctx := appengine.NewContext(r)
    //src := google.AppEngineTokenSource(ctx, oauthsvc.UserinfoEmailScope)
    src, err := google.DefaultTokenSource(ctx, oauthsvc.UserinfoEmailScope)
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
    }
    client := &http.Client{
        Transport: &oauth2.Transport{
            Source: src,
            Base:   &urlfetch.Transport{Context: ctx},
        },
    }
    client = oauth2.NewClient(ctx, src)
    service, err := oauthsvc.New(client)
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
    }
    ui, err := service.Userinfo.Get().Do()
    if err != nil {
        http.Error(w, err.Error(), http.StatusInternalServerError)
    }
    log.Infof(ctx, "UserInfo: %v", ui.Email)
    fmt.Fprintln(w, "UserInfo: ", ui.Email)
}
```

####ComputeEngine
```go
package main
import (
        "golang.org/x/net/context"
        "golang.org/x/oauth2"
        "golang.org/x/oauth2/google"
        oauthsvc "google.golang.org/api/oauth2/v2"
        "log"        
)
func main() {
        //src := google.ComputeTokenSource("")    
        src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
        if err != nil {
              log.Fatalf("Unable to acquire token source: %v", err)
        }        
        client := oauth2.NewClient(context.Background(), src)
        service, err := oauthsvc.New(client)
        if err != nil {
                log.Fatalf("Unable to create api service: %v", err)
        }
        ui, err := service.Userinfo.Get().Do()
        if err != nil {
                log.Fatalf("Unable to get userinfo: ", err)
        }
        log.Printf("UserInfo: %v", ui.Email)
}
```

####Service Account JSON File
```go
import (
        "golang.org/x/net/context"
        "golang.org/x/oauth2"
        "golang.org/x/oauth2/google"
        oauthsvc "google.golang.org/api/oauth2/v2"
        "log"
//        "io/ioutil"
        "os"
)

func main() {
        serviceAccountJSONFile := "/home/srashid/f.json"
        /*
        dat, err := ioutil.ReadFile(serviceAccountJSONFile)
        if err != nil {
              log.Fatalf("Unable to read service account file %v", err)
        }
        conf, err := google.JWTConfigFromJSON(dat, oauthsvc.UserinfoEmailScope)
        if err != nil {
              log.Fatalf("Unable to acquire generate config: %v", err)
        }
        client := conf.Client(oauth2.NoContext)
        */
        os.Setenv("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountJSONFile)
        src, err := google.DefaultTokenSource(oauth2.NoContext, oauthsvc.UserinfoEmailScope)
        if err != nil {
              log.Fatalf("Unable to acquire token source: %v", err)
        }
        client := oauth2.NewClient(context.Background(), src)

        service, err := oauthsvc.New(client)
        if err != nil {
                log.Fatalf("Unable to create api service: %v", err)
        }
        ui, err := service.Userinfo.Get().Do()
        if err != nil {
                log.Fatalf("Unable to get userinfo: ", err)
        }
        log.Printf("UserInfo: %v", ui.Email)
}
```


####UserFlow (installed)
```go
package main
import (
    "fmt"
    "golang.org/x/net/context"
    "golang.org/x/oauth2"
    "golang.org/x/oauth2/google"    
    "log"   
    oauthsvc "google.golang.org/api/oauth2/v2"
)
func main() {
    conf := &oauth2.Config{
        ClientID:     "YOUR_CLIENT_ID",
        ClientSecret: "YOUR_CLIENT_SECRET",
        RedirectURL:  "urn:ietf:wg:oauth:2.0:oob",
        Scopes: []string{
            oauthsvc.UserinfoEmailScope,
        },
        Endpoint: google.Endpoint,
    }
    url := conf.AuthCodeURL("state")
    log.Println("Visit the URL for the auth dialog: ", url)    
    var code string
    log.Print("Enter auth token: ")
    if _, err := fmt.Scan(&code); err != nil {
        log.Fatalf(err.Error())
    }
    tok, err := conf.Exchange(context.Background(), code)
    if err != nil {
        log.Fatalf(err.Error())
    }
    //client := conf.Client(context.Background(),tok)
    src := conf.TokenSource(context.Background(),tok)       
    client := oauth2.NewClient(context.Background(), src)   
    service, err := oauthsvc.New(client)
    if err != nil {
        log.Fatalf("Unable to create oauth2 client: %v", err)
    }
    ui, err := service.Userinfo.Get().Do()
    if err != nil {
        log.Fatalf("ERROR: ", err)
    }   
    log.Printf("UserInfo: %v", ui.Email)
}
```

#####APIKey
```go
import "google.golang.org/api/googleapi/transport"
apiKey :="YOUR_API_KEY"
client.Transport = &transport.APIKey{ 
    Key: apiKey, 
}
```

###C&#35;
* [NuGet](https://www.nuget.org/packages/Google.Apis/)

####ComputeEngine
```c#
using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.Oauth2.v2;
using Google.Apis.Oauth2.v2.Data;
using Google.Apis.Services;

//ComputeCredential credentials = new ComputeCredential(new ComputeCredential.Initializer());
GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
if (credential.IsCreateScopedRequired)
    credential = credential.CreateScoped(new string[] { Oauth2Service.Scope.UserinfoEmail });
var service = new Oauth2Service(new BaseClientService.Initializer()
{
    HttpClientInitializer = credential,
    ApplicationName = "Oauth2 Sample",
});
Console.WriteLine(service.Userinfo.Get().Execute().Name);
```

####Service Account JSON File
```c#
using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.Oauth2.v2;
using Google.Apis.Oauth2.v2.Data;
using Google.Apis.Services;

string CREDENTIAL_FILE = "C:\\YOUR_SERVICE_ACCOUNT.json";
Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", CREDENTIAL_FILE);
GoogleCredential credential = await GoogleCredential.GetApplicationDefaultAsync();
if (credential.IsCreateScopedRequired)
    credential = credential.CreateScoped(new string[] { Oauth2Service.Scope.UserinfoEmail });
```

####UserFlow
```c#
using Google.Apis;
using Google.Apis.Auth.OAuth2;
using Google.Apis.Oauth2.v2;
using Google.Apis.Oauth2.v2.Data;
using Google.Apis.Services;

UserCredential credential;
string clientId = "YOUR_CLIENT_ID.apps.googleusercontent.com";
string clientSecret = "YOUR_CLIENT_SECRET";
credential = await GoogleWebAuthorizationBroker.AuthorizeAsync(new ClientSecrets{ClientId= clientId,ClientSecret=clientSecret},
    new[] { Oauth2Service.Scope.UserinfoEmail }, Environment.UserName, CancellationToken.None);
//Console.WriteLine("Credential file saved at: " + Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
```
