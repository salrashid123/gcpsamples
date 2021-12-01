#!/usr/bin/python

# Using Google Cloud APIs
from google.cloud import storage
import google.auth
from google.oauth2 import service_account

# credentials = service_account.Credentials.from_service_account_file('YOUR_JSON_CERT.json')
# if credentials.requires_scopes:
#  credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])
# client = storage.Client(credentials=credentials)

credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print(bkt)