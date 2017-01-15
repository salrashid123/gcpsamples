## Google Cloud Platform Samples

####  samples provided as-is without warranty

Sample code demonstrating various Auth mechanism for Google Cloud Platform APIs.

Please refer to official documentation for usage and additional samples/usage.

[Google Authentication Samples](https://cloud.google.com/docs/authentication)

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
* [Node](#nodejs)
    - Appengine
    - ComputeEngine
    - Service Account File
    - Userflow    
* [C#](#c)
    - Appengine
    - ComputeEngine
    - Service Account File
    - Userflow


For more inforamtion, see:
* [oauth2 protocol](https://developers.google.com/identity/protocols/OAuth2)
* [oauth2 service](https://developers.google.com/apis-explorer/#p/oauth2/v2/)
* [Service Accounts](https://developers.google.com/identity/protocols/OAuth2ServiceAccount#overview)

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

If running on the dev_appserver, you will need to set the local service account id and certificate first:
```bash
mkdir lib
pip install --target=lib  requests google-api-python-client httplib2 oauth2client

cat your_svc_account.p12 | openssl pkcs12 -nodes -nocerts -passin pass:notasecret | openssl rsa > key.pem

gcloud preview app run app.yaml --appidentity-email-address=YOUR_SERVICE_ACCOUNT_ID@developer.gserviceaccount.com --appidentity-private-key-path=key.pem

```

For info on ```--appidentity-email-address``` and ```--appidentity-private-key-path```, see documentation on [gcloud dev_appserver](https://cloud.google.com/sdk/gcloud/reference/preview/app/run).

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


#####ID Token from Service Account JSON Signed by Google
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
#####Returns JSON with a JWT signed by Google:
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

###Java

[Java API Client Library](https://developers.google.com/api-client-library/java/).  Most of the samples below uses gradle to build and deploy.

####Appengine
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
####ComputeEngine
Under [auth/compute/javaapp](auth/compute/javaapp).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

```bash
gradle task
gradle run
```

```bash
mvn exec:java
```
####Service Account File
Under [auth/service/javaapp](auth/service/javaapp).  Runs a simple application using both *Application DefaultCredentials* and by directly reading in the JSON certificate file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the JSON file, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials)

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/you/json/file.json
gradle task
gradle run
```

```bash
mvn exec:java
```
####UserFlow
Under [auth/userflow/javaapp](auth/userflow/javaapp).  Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a browser and listener.

```bash
gradle task
gradle run
```

```bash
mvn exec:java
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


#####Exponential Backoff

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

***  

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

# vm: false
google-cloud-sdk/go_appengine/goapp serve src/app.yaml
google-cloud-sdk/go_appengine/goapp deploy src/app.yaml

# vm: true
uncomment appengine.Main in func main
gcloud app run src/app.yaml
gcloud app deploy src/app.yaml --version 1 --set-default
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
Under [auth/userflow/goapp](auth/userflow/goapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches a link URL and expects the authorization token to get entered (installed application).

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

#####Validating id_token

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

#####Logging

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

###NodeJS
[google.auth.getApplicationDefault](https://developers.google.com/identity/protocols/application-default-credentials#callingnode)  

####Appengine
Under [auth/gae/nodeapp](auth/gae/nodeapp).  Runs a simple GAE application using *Application DefaultCredentials*.  To deploy:
```
gcloud app deploy app.yaml
```

####ComputeEngine
Runs sample on ComputeEngine.  Requires the userinfo scope enabled on the compute engine instance.
```bash
npm install
npm start
```

####Service Account JSON File
Under [auth/service/nodeapp](auth/service/nodeapp).  Runs a simple application using both *Application DefaultCredentials* and directly reading *JSON KEY file*. 

####UserFlow
Under [auth/userflow/nodeapp](auth/userflow/nodeapp).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow provides a link URL and expects the authorization token to get entered (installed application).


####Misc

#####Setting API Key

```node
var service = google.oauth2({ 
      version: 'v2', 
      auth: authClient, 
      params: { key: 'YOUR_API_KEY'}
});
```

#####Logging

```bash
export NODE_DEBUG=request
```

***

###C&#35;
.NET packages downloadable from [NuGet](https://www.nuget.org/packages/Google.Apis/).  Full end-to-end example of all the auth modes available here for CloudStorage

* [Google API .NET Library](https://developers.google.com/api-client-library/dotnet/get_started)

####Appengine
GAE Standard does not support .NET as a runtime.  However, you can deploy your application to GAE Flex if you run .NET Core on Linux.  See the following sample that runs a .NET
webapp in Flex:  [.NET on GCP](https://github.com/salrashid123/gcpdotnet).
Note: Google APIs do not support .NET Core (coreCLR) yet.  At the time of writing, they only supports upto [.NET Framework 4.5.1](https://www.nuget.org/packages/Google.Apis/).  This
means you cannot use Google APIs from within a Container.   There are some [ports](https://www.nuget.org/packages/GoogleApis.Core.vNext/) to coreCLR but they are not officially supported.

####ComputeEngine
Under [auth/compute/dotnet](auth/compute/dotnet).  Runs a simple application using both *Application DefaultCredentials* and *ComputeCredential*. 

####Service Account JSON File
Under [auth/service/dotnet](auth/service/dotnet).  Runs a simple application using both *Application DefaultCredentials* using a **JSON Certificate** and by directly reading in the **PKCS12 Certificate** file.  If the *GOOGLE_APPLICATION_CREDENTIALS* variable is set to point to the **JSON file**, the applicationDefault profile will also read the JSON file (otherwise, it will attempt to pick up the gcloud credentials).

####UserFlow
Under [auth/userflow/dotnet](auth/userflow/dotnet).   Runs a simple webflow application to acquire user consent for GoogleAPIs.  This particular userflow launches provides a link URL and expects user consent on the browser.

#####Credential store
Credentials from the GoogleAPIs userflow is usually stored at

```
Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
or c:\Users\%USER%\AppData\Roaming\Google.Apis.Auth
```