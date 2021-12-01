#!/usr/bin/python

import httplib2
from apiclient import discovery
import oauth2client
from apiclient.discovery import build
from oauth2client.client import flow_from_clientsecrets
from oauth2client.file import Storage

flow  = flow_from_clientsecrets('/tmp/client_secrets.json',
                               scope='https://www.googleapis.com/auth/userinfo.email',
                               redirect_uri='urn:ietf:wg:oauth:2.0:oob')

auth_uri = flow.step1_get_authorize_url()
print('goto the following url ' +  auth_uri)

code = input('Enter token:')
credentials = flow.step2_exchange(code)

http = credentials.authorize(httplib2.Http())
storage = Storage('user_creds.json')
storage.put(credentials)
service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print(resp['email'])


# -------------------------------------

#from google_auth_oauthlib.flow import InstalledAppFlow
#flow = InstalledAppFlow.from_client_secrets_file(
#    'client_secrets.json',
#    scopes=['profile', 'email'])

#flow.run_local_server()

#client = photos_v1.PhotoServiceClient(credentials=flow.credentials)
# credentials = google.oauth2.credentials.Credentials.from_authorized_user_file('user_creds.json')
#client = storage.Client(credentials=credentials, project=project)
#buckets = client.list_buckets(project=project)
#for bucket in buckets:
# print bucket.name