#!/usr/bin/python

from google.cloud import storage
import google.auth
from google.oauth2 import service_account

#credentials = service_account.Credentials.from_service_account_file('YOUR_JSON_CERT.json')
#credentials = credentials.with_scopes(['https://www.googleapis.com/auth/devstorage.read_write'])
#client = storage.Client(credentials=credentials)

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_CERT.json"
credentials, project = google.auth.default()    
client = storage.Client(credentials=credentials)
buckets = client.list_buckets()
for bkt in buckets:
  print bkt