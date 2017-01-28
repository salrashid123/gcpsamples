#!/usr/bin/python

import httplib2
from apiclient.discovery import build
from oauth2client.service_account import ServiceAccountCredentials
from oauth2client.client import GoogleCredentials
import os

scope='https://www.googleapis.com/auth/userinfo.email'

#credentials = ServiceAccountCredentials.from_json_keyfile_name('your-service-account.json')

# for JSON_CERTIFICATE_FILES
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_KEY_FILE"
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scope)

http = httplib2.Http()
credentials.authorize(http)

service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print resp['email']



# Using Google Cloud APIs
from google.cloud import storage
import google.auth
from google.oauth2 import service_account

#credentials = service_account.Credentials.from_service_account_file('YOUR_JSON_CERT.json')
#if credentials.requires_scopes:
#  credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])
#client = storage.Client(credentials=credentials)

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_CERT.json"
credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt