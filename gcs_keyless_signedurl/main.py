import os
import sys
import inspect
import json
import time

from apiclient.discovery import build
import httplib2
from oauth2client.client import GoogleCredentials
from apiclient import discovery

import base64
import datetime
from urllib.parse import quote as urlquote
import requests

GCS_API_ENDPOINT = "https://storage.googleapis.com"
SERVICE_ACCOUNT_EMAIL = "service-account-b@mineral-minutia-820.iam.gserviceaccount.com"
BUCKET_NAME = "mineral-minutia-820"
OBJECT_NAME = "somefile.txt"

expiration = int(datetime.datetime.now().timestamp()) + 60


def _Base64Sign(plaintext):

    project_id = "-"

    # use for local devlopment, comment out for deploy
    # os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/srashid/gcp_misc/certs/mineral-minutia-820-83b3ce7dcddb.json"

    cc = GoogleCredentials.get_application_default()
    iam_scopes = "https://www.googleapis.com/auth/iam"
    if cc.create_scoped_required():
        cc = cc.create_scoped(iam_scopes)
    http = cc.authorize(httplib2.Http())

    iamcredentials = build(serviceName="iamcredentials", version="v1", http=http)
    body = {
        "delegates": [],
        "payload": base64.urlsafe_b64encode(plaintext.encode()).decode(),
    }
    req = (
        iamcredentials.projects()
        .serviceAccounts()
        .signBlob(
            name="projects/" + project_id + "/serviceAccounts/" + SERVICE_ACCOUNT_EMAIL,
            body=body,
        )
    )
    resp = req.execute()
    return resp["signedBlob"]


def _MakeSignatureString(verb, path, content_md5, content_type, expiration):
    return inspect.cleandoc(
        f"""{verb}
            {content_md5}
            {content_type}
            {expiration}
            {path}"""
    )


def MakeUrl(verb, path, content_type="", content_md5=""):
    signature_string = _MakeSignatureString(verb, path, content_md5, content_type, expiration)
    signature_signed = urlquote(_Base64Sign(signature_string))

    signed_url = (
        "https://storage.googleapis.com/"
        + BUCKET_NAME
        + "/"
        + OBJECT_NAME
        + "?GoogleAccessId="
        + SERVICE_ACCOUNT_EMAIL
        + "&Expires="
        + str(expiration)
        + "&Signature="
        + signature_signed
    )

    return signed_url


file_path = "/%s/%s" % (BUCKET_NAME, OBJECT_NAME)

print("GET URL:", MakeUrl("GET", file_path))


print("PUT:")
u = MakeUrl("PUT", file_path)
print(u)
r = requests.put(u, data="lorem2 ipsum")
print(f"put status_code: {r.status_code}")

print("---------------------------------")

print("GET")
u = MakeUrl("GET", file_path)
print(u)
r = requests.get(u)
print(f"get status_code: {r.status_code}")
print("data: " + r.text)
