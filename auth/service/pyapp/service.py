#!/usr/bin/python

import httplib2
from apiclient.discovery import build
from oauth2client.service_account import ServiceAccountCredentials
from oauth2client.client import GoogleCredentials

scope='https://www.googleapis.com/auth/userinfo.email'

#credentials = ServiceAccountCredentials.from_p12_keyfile('YOUR_SERVICE_ACCOUNT_NAME',
#                                                           'your-service-account.p12',
#                                                           scopes=scope)

# for JSON_CERTIFICATE_FILES
#os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "YOUR_JSON_KEY_FILE"
credentials = GoogleCredentials.get_application_default()
if credentials.create_scoped_required():
  credentials = credentials.create_scoped(scope)

http = httplib2.Http()
credentials.authorize(http)

service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print resp['email']

