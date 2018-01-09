#!/usr/bin/python

# http://google-auth.readthedocs.io/en/latest/index.html
# http://google-auth.readthedocs.io/en/latest/reference/google.auth.transport.requests.html
import os
import google.auth
from google.auth import exceptions
from google.auth import transport
from google.oauth2 import id_token
import google_auth_httplib2

import google.auth.transport.requests
import requests


print "++++++++++++++  1. google auth to list gcs buckets ++++++++++++++++"
# using google apis and access_tokens
from google.cloud import storage
credentials, project = google.auth.default()
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt
print '--------------------------------------------------------------------------'


# how to initialize a cred with scope and authorize a transport
scopes=['https://www.googleapis.com/auth/userinfo.email']
credentials, project = google.auth.default(scopes=scopes)
authed_http = google_auth_httplib2.AuthorizedHttp(credentials)


print "++++++++++++++ 2. mint and verify id_token for current user ++++++++++++++++"
# "aud": "764086051850-6qr4p6gpi6hn506pt8ejuq83di341hur.apps.googleusercontent.com",
# "iss": "accounts.google.com",
credentials, project = google.auth.default()
request = google.auth.transport.requests.Request()
credentials.refresh(request)
idt = credentials.id_token
print 'id_token: ' + idt
# verify the id_token
print 'id_token verification:'
print id_token.verify_oauth2_token(idt,request)

print '--------------------------------------------------------------------------'

print "++++++++++++++  3. mint and verify id_token for a service_account ++++++++++++++++"
# "iss": "svc-2-429@mineral-minutia-820.iam.gserviceaccount.com",
# "aud": "https://yourapp.appspot.com"

import google.auth
from google.auth import jwt
from google.oauth2 import service_account
from google.oauth2 import id_token

audience = 'https://yourapp.appspot.com'
additional_claims = {'target_audience': 'http://yourappspot.com'}

svc_creds = service_account.Credentials.from_service_account_file(
    '/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json')
jwt_creds = jwt.Credentials.from_signing_credentials(
    svc_creds, audience=audience, additional_claims=additional_claims )

request = google.auth.transport.requests.Request()
jwt_creds.refresh(request)
idt = jwt_creds.token
print 'id_token: ' + idt
print id_token.verify_token(idt,request, certs_url='https://www.googleapis.com/service_accounts/v1/metadata/x509/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com')


print '--------------------------------------------------------------------------'

print "++++++++++++++  3. mint and verify google_id_token for a service_account ++++++++++++++++"
# "aud": "http://yourappspot.com",
# "iss": "https://accounts.google.com",

from google.oauth2 import id_token
from google.oauth2 import service_account
import json

audience = "https://www.googleapis.com/oauth2/v4/token"
additional_claims = {'target_audience': 'http://yourappspot.com'}

svc_creds = service_account.Credentials.from_service_account_file(
    '/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json')
jwt_creds = jwt.Credentials.from_signing_credentials(
    svc_creds, audience=audience, additional_claims=additional_claims )

request = google.auth.transport.requests.Request()
jwt_creds.refresh(request)
signed_jwt = jwt_creds.token


url = 'https://www.googleapis.com/oauth2/v4/token'
data = {'grant_type' : 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' : signed_jwt }
headers = {"Content-type": "application/x-www-form-urlencoded"}
resp = requests.post(url, data = data, headers=headers)
idt = json.loads(resp.text)['id_token']
print idt

request = google.auth.transport.requests.Request()
print id_token.verify_token(idt,request)
print '--------------------------------------------------------------------------'
