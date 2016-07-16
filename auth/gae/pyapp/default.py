#!/usr/bin/python

# Edit app.yaml with your APPID

#mkdir lib
#pip install -t lib google-api-python-client httplib2 oauth2client
#gcloud preview app deploy app.yaml --set-default

import os
import sys
import httplib2

from google.appengine.ext import webapp
import logging

#from oauth2client.contrib.appengine import AppAssertionCredentials
from oauth2client.client import GoogleCredentials
from apiclient.discovery import build

class MainPageTemplate(webapp.RequestHandler):

    def get(self):
      scope='https://www.googleapis.com/auth/userinfo.email'
      #credentials = AppAssertionCredentials(scope=scope)
      credentials = GoogleCredentials.get_application_default()
      if credentials.create_scoped_required():
        credentials = credentials.create_scoped(scope)
      http = credentials.authorize(httplib2.Http())

      service = build(serviceName='oauth2', version= 'v2',http=http)
      resp = service.userinfo().get().execute()
      logging.info(resp['email'])
      self.response.headers["Content-Type"] = "text/plain"
      self.response.set_status(200)
      self.response.out.write(resp['email'])
      
class HealthCheck(webapp.RequestHandler):
    def get(self):
      self.response.set_status(200)
      self.response.out.write('ok')

application = webapp.WSGIApplication([('/', MainPageTemplate),('/_ah/health',HealthCheck)], debug=True)

