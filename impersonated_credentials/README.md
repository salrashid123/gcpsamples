# Using ImpersonatedCredentials for Google Cloud APIs

Almost two years ago I wrote an article about using the [Service Account Actor IAM role for account impersonation](
https://medium.com/google-cloud/using-serviceaccountactor-iam-role-for-account-impersonation-on-google-cloud-platform-a9e7118480ed).  Since then, there have been some API surface changes and enchanced capabilities that makes that flow a lot easier...and as noted in the article, a bit obsolete.  The ideas contained in that article are still valid though the mechanism is dated.  In the new flow, you can now 'just get' an `access_token`, `id_token`, `jwt` or sign arbitrary blobs for a target service account by directly calling the [iamcredentials API](https://cloud.google.com/iam/credentials/reference/rest/#service-iamcredentialsgoogleapiscom). 

Lets use that API and go one step further: what if we bake in that exchange directly into the varioud `google-auth-*` libraries directly?  That is, perform the exchange directly within any cloud library and use the assumed identity within any of library service set.  For example:

1. You start with a credential A initialized by any means (Service Account JSON, GCE metadata, userflow, etc)
2. You use GCP auth libraries to exchange A for credentials representing serviceAccountB
3. You use any google-cloud* library as account B 

In code, it would look like this:


```python
#!/usr/bin/python
from google.cloud import storage
import google.auth
from google.oauth2 import credentials
from google.oauth2 import service_account

from google.auth import impersonated_credentials

# For service account credentials
svc_account_file = 'svc-src.json'
target_scopes = ['https://www.googleapis.com/auth/devstorage.read_only']
source_credentials = service_account.Credentials.from_service_account_file(
    svc_account_file,
    scopes=target_scopes)

# For ComputeCredentials
# source_credentials, project = google.auth.default()

target_credentials = impersonated_credentials.Credentials(
    source_credentials = source_credentials,
    target_principal='impersonated-account@PROJECT_ID.iam.gserviceaccount.com',
    target_scopes = target_scopes,
    delegates=[],
    lifetime=500)

client = storage.Client(credentials=target_credentials)
buckets = client.list_buckets(project='PROJECT_ID')
for bucket in buckets:
    print bucket.name
```

Note we start off with serviceAccount Credentials and then use [google.auth.impersonated_credentials](https://github.com/googleapis/google-auth-library-python/blob/master/google/auth/impersonated_credentials.py) to exchange the provided credentials for another and the proceed to 'just use' any google API library

## Usage

Using this is pretty straightforward:

```bash
# Create source identity:

export YOUR_PROJECT=$(gcloud config list --format="value(core.project)")
gcloud iam service-accounts create source-serviceaccount --display-name="Source Identity"
gcloud iam service-accounts keys  create svc-src.json --iam-account=source-serviceaccount@$YOUR_PROJECT.iam.gserviceaccount.com


# Create target identity
gcloud iam service-accounts create target-serviceaccount --display-name="Target Identity"

# Allow source to impersonate target
gcloud iam service-accounts add-iam-policy-binding \
    target-serviceaccount@$YOUR_PROJECT.iam.gserviceaccount.com  \
    --member='serviceAccount:source-serviceaccount@$YOUR_PROJECT.iam.gserviceaccount.com' \
    --role='roles/iam.serviceAccountTokenCreator'

# Add resource ACL to target
gcloud projects add-iam-policy-binding $YOUR_PROJECT \
   --member='serviceAccount:target-serviceaccount@$YOUR_PROJECT.iam.gserviceaccount.com' \
   --role='roles/storage.admin'
```

then just run the the `main.py` python sample above (ofcourse replace `PROJECT_ID` vairable in code above)

You can also 'just run' the sample above in a GCE compute engine instance:

```
gcloud compute instances create impersonate-test \
   --service-account=source-serviceaccount@$PROJECT.iam.gserviceaccount.com \
   --scopes=https://www.googleapis.com/auth/iam
```

SSH to that instance and run the same sample as above (just remember to comment out the part using service accounts credentials and instead use `google.auth.default()`)

## Language bindings

At the time of writing (11/25), the 'first class' support for impersonated credentials is not available in all languages 

### Available:

- python: [google.auth.impersonated_credentials](https://github.com/googleapis/google-auth-library-python/blob/master/google/auth/impersonated_credentials.py)

- java: [com.google.auth.oauth2.ImpersonatedCredentials](https://github.com/googleapis/google-auth-library-java/blob/master/oauth2_http/java/com/google/auth/oauth2/ImpersonatedCredentials.java)

### Pending

- golang: [google.auth.ImpersonatedTokenSource](https://github.com/golang/oauth2/pull/335)

- nodejs: [{ImpersonatedClient} = require('google-auth-library');](https://github.com/googleapis/google-auth-library-nodejs/issues/535)


> If you are interested in contributing a binding in any of the languages above, please feel free :)


## AuditLogs

The following shows the audit logs that would get generated as a result of the Impersonated and Resource (GCS) access above:

- ![images/audit_log.png](images/audit_log.png)

Note that the logs span two different resource.types in Cloud logging (IAM and the resource in context).  That means to see them together, just run
a query with a filter like:

```
resource.type="service_account" OR resource.type="gcs_bucket"
```

Here is a detaled logging payload for the IAM request:

- ![images/get_credentials_request.png](images/get_credentials_request.png)

And the actual resource request.

- ![images/api_request.png](images/api_request.png)

Notice that the resource API call lacks any information that would indicate who the original caller was (i,e serviceAccount A). 


Thats it!.  its a pretty straightforward API and usage and particularly useful now since any of the google cloud libraries for those language bindings will 'just accept' impersonated credentials and refresh them if expired!

