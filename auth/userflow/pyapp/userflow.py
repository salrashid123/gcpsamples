#!/usr/bin/python

import httplib2
from apiclient import discovery
import oauth2client
from apiclient.discovery import build
from oauth2client.client import flow_from_clientsecrets

flow  = flow_from_clientsecrets('client_secrets.json',
                               scope='https://www.googleapis.com/auth/userinfo.email',
                               redirect_uri='urn:ietf:wg:oauth:2.0:oob')

auth_uri = flow.step1_get_authorize_url()
print 'goto the following url ' +  auth_uri

code = raw_input('Enter token:')
credentials = flow.step2_exchange(code)

http = credentials.authorize(httplib2.Http())
service = build(serviceName='oauth2', version= 'v2',http=http)
resp = service.userinfo().get().execute()
print resp['email']
