## ID Token


The following section details how to create and verify various id_tokens.  These tokens comes in various flavors:

* google service account id_tokens
* [OpenIDConnect ID token](https://developers.google.com/actions/reference/rest/Shared.Types/AssertionType)


The samples here outline how to get an id_token using your existing credentials and ones based off a service_account_json file.

The service_account based id_tokens maybe exchanged with google for an OpenIDConnect token.

To use, download a service_account json certificate file, then edit the sample in question and replace the certificate path within the code
(sorry, i didn't make it a arg)


The output of each script displays an id_token for the current user's gcloud login, then an id_token for the service account, and finally the same id_token
for a service account exchanged for a google-id-token.  In each case, the token is verified against the public certificates:

eg verified against public certs in x509 format:

```
const service_account_certs = 'https://www.googleapis.com/service_accounts/v1/jwk/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com';
const google_certs = 'https://www.googleapis.com/oauth2/v3/certs';
```

- golang:  see [main.go](golang/main.go)
  ```
  go get github.com/coreos/go-oidc golang.org/x/net/context golang.org/x/oauth2 golang.org/x/oauth2/google golang.org/x/oauth2/jws google.golang.org/api/oauth2/v2 google.golang.org/grpc/credentials/oauth

  go run src/main.go
  ```

- python:  see [main.py](python/main.py)
  ```
  virtualenv env
  source env/bin/activate
  pip install -r requirements.txt

  python main.py
  ```

- java:  see [TestApp.java](java/src/main/java/com/test/TestApp.java)
  ```
  mvn clean install exec:java -q
  ```

- node:  see [app.js](node/app.js)
  ```
  npm i

  node user.js   (gets a users gcloud id_token and verifies it)
  node service.js   (gets a service accounts id_token and verifies it with the service accounts public key)
  node google-id-token.js   (gets a service accounts id_token and exchanges it for a google-id-token...then verifies it)    
  ```  

- dotnet:  see [Program.cs](dotnet/Program.cs)
  Unimplimented

Please also see [Using Service Account Actor for Account Impersonation](../auth/tokens)


>>Update 7/1/18: GCP IAM now allows for [IAMCredentials.generateAccessToken()](https://cloud.google.com/iam/docs/creating-short-lived-service-account-credentials). This feature allows you to directly genreate an ID or AccessToken and place conditionals on delegation too. While the samples below will work, I’ll rework the sample in this repo to use that API as shown in this example. In several ways, this article is obsolete given the direct API is now available. I’ll keep it posted as a reference. The full code sample for iamcredentials API is shows at the end of the article

see

```python
import logging
import os
import sys
import json
import time
import pprint

from apiclient.discovery import build
import httplib2
from oauth2client.service_account import ServiceAccountCredentials
from oauth2client.client import GoogleCredentials
from apiclient import discovery

custom_claim = "some custom_claim"
audience = 'api.endpoints.YOUR_PROJECT.cloud.goog'
svc_account_A = 'svc-2-429@mineral-minutia-820.iam.gserviceaccount.com'
svc_account_B = 'service-account-b@mineral-minutia-820.iam.gserviceaccount.com'
svc_account_C = 'id-service-account-c@mineral-minutia-820.iam.gserviceaccount.com'


# initialize root creds for A
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/path/to/svc_account.json"

project_id ='-'

cc = GoogleCredentials.get_application_default()
iam_scopes = 'https://www.googleapis.com/auth/iam https://www.googleapis.com/auth/cloud-platform'
if cc.create_scoped_required():
  cc = cc.create_scoped(iam_scopes)
http = cc.authorize(httplib2.Http())
service = build(serviceName='iam', version= 'v1',http=http)
resource = service.projects()
now = int(time.time())
exptime = now + 3600
claim =('{"iss":"%s",'
  '"aud":"%s",'
  '"sub":"%s",'
  '"X-Goog-Authenticated-User-ID":"%s",'
  '"exp":%s,'
  '"iat":%s}') %(svc_account_B,audience,svc_account_B,custom_claim,exptime,now)
slist = resource.serviceAccounts().signJwt(name='projects/' + project_id + '/serviceAccounts/' + svc_account_B, body={'payload': claim })
resp = slist.execute()
signed_jwt = resp['signedJwt']

print 'iam.signJwt() for A:  --------------------- '
print signed_jwt



iamcredentials = build(serviceName='iamcredentials', version= 'v1',http=http)


print '=========================== no delegation =================================='

body={
  "delegates": [],
  "scope": [
      "https://www.googleapis.com/auth/cloud-platform"
  ],
  "lifetime": "300s"
}

req = iamcredentials.projects().serviceAccounts().generateAccessToken(name='projects/' + project_id + '/serviceAccounts/' + svc_account_B, body=body )
resp = req.execute()

print 'iamcredentials.generateAccessToken():  --------------------- '

print resp

body = {
  "delegates": [],
  "audience": svc_account_B,
  "includeEmail": "true"
}

req = iamcredentials.projects().serviceAccounts().generateIdToken(name='projects/' + project_id + '/serviceAccounts/' + svc_account_B, body=body )
resp = req.execute()

print 'iamcredentials.generateIdToken():  --------------------- '

print resp


print '=========================== delegation =================================='

body={
  "delegates": [
    'projects/-/serviceAccounts/' + svc_account_B
  ],
  "scope": [
      "https://www.googleapis.com/auth/cloud-platform"
  ],
  "lifetime": "300s"
}

req = iamcredentials.projects().serviceAccounts().generateAccessToken(name='projects/' + project_id + '/serviceAccounts/' + svc_account_C, body=body )
resp = req.execute()

print 'iamcredentials.generateAccessToken():  --------------------- '

print resp



body = {
  "delegates": [
    'projects/-/serviceAccounts/' + svc_account_B  
  ],
  "audience": svc_account_B,
  "includeEmail": "true"
}

req = iamcredentials.projects().serviceAccounts().generateIdToken(name='projects/' + project_id + '/serviceAccounts/' + svc_account_C, body=body )
resp = req.execute()

print 'iamcredentials.generateIdToken():  --------------------- '

print resp

```
