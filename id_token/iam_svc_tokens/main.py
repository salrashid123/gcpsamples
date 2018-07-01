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
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/srashid/gcp_misc/certs/mineral-minutia-820-83b3ce7dcddb.json"

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
