#!/usr/bin/python

import httplib2
from apiclient.discovery import build
from oauth2client.client import GoogleCredentials
#from oauth2client.contrib.gce import AppAssertionCredentials

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


# Using Google Cloud APIs
from google.cloud import storage
import google.auth
from google.oauth2 import service_account

credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt