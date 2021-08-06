import datetime

from google.cloud import storage
from google.auth import impersonated_credentials

import google.auth
import google.auth.transport.requests

bucket_name = "fabled-ray-104117"
blob_name = "foo.txt"

source_credentials, project = google.auth.default()

target_credentials = impersonated_credentials.Credentials(
    source_credentials=source_credentials,
    target_principal="impersonated-account@fabled-ray-104117.iam.gserviceaccount.com",
    target_scopes=["https://www.googleapis.com/auth/cloud-platform"],
    lifetime=500,
)


storage_client = storage.Client(credentials=target_credentials)
bucket = storage_client.bucket(bucket_name)
blob = bucket.blob(blob_name)

url = blob.generate_signed_url(
    version="v4",
    expiration=datetime.timedelta(minutes=10),
    method="GET",
)

print("Generated GET signed URL:")
print(url)
