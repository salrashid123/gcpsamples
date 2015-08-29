#!/usr/bin/python

import httplib2
from apiclient.discovery import build
from oauth2client.client import SignedJwtAssertionCredentials
from oauth2client.client import GoogleCredential

scope='https://www.googleapis.com/auth/userinfo.email'

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
