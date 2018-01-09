## ID Token


The following section details how to create an verify various id_tokens.  These tokens comes in various flavors:

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

  npm start
  ```  

- dotnet:  see [Program.cs](dotnet/Program.cs)
  Unimplimented

Please also see [Using Service Account Actor for Account Impersonation](../auth/tokens)
