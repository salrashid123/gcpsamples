#!/usr/bin/python
from google.cloud import storage
import google.auth
from google.oauth2 import credentials
from google.oauth2 import service_account

from google.auth import impersonated_credentials
import google.api_core.exceptions
import os, time

# For service account credentials
svc_account_file = 'svc-src.json'

target_scopes = ['https://www.googleapis.com/auth/devstorage.read_only']
source_credentials = service_account.Credentials.from_service_account_file(
    svc_account_file,
    scopes=target_scopes)

# For ComputeCredentials
# source_credentials, project = google.auth.default()

try:
  client = storage.Client(credentials=source_credentials)
  buckets = client.list_buckets(project='fabled-ray-104117')
  for bucket in buckets:
    print bucket.name
except google.api_core.exceptions.Forbidden:
  print ">>>>> Forbidden"
  pass

# now try delegation
target_credentials = impersonated_credentials.Credentials(
    source_credentials = source_credentials,
    target_principal='impersonated-account@fabled-ray-104117.iam.gserviceaccount.com',
    target_scopes = target_scopes,
    delegates=[],
    lifetime=500)
client = storage.Client(credentials=target_credentials)
buckets = client.list_buckets(project='fabled-ray-104117')
for bucket in buckets:
    print bucket.name