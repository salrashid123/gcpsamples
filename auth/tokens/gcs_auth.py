#!/usr/bin/python

# virtualenv env  
# source env/bin/activate
# pip install requests google-api-python-client httplib2 oauth2client

import os, sys
import getopt
import datetime, time, base64
import logging
import urllib,urllib2, httplib
from urllib2 import URLError, HTTPError
import json, random

import httplib2
from apiclient.discovery import build
from apiclient.errors import HttpError
from oauth2client.client import AccessTokenCredentials
from oauth2client.client import GoogleCredentials
from oauth2client.client import verify_id_token
from oauth2client.crypt import AppIdentityError

class gcs_auth(object):

  
  def __init__(self, client_id):

    ## First initialize IAM for the 'Master Service Account '

    scope = 'https://www.googleapis.com/auth/iam https://www.googleapis.com/auth/cloud-platform'
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "GCPNETAppID-345d845b3936.json"
    credentials = GoogleCredentials.get_application_default()
    if credentials.create_scoped_required():
      credentials = credentials.create_scoped(scope)
    http = credentials.authorize(httplib2.Http())

    service = build(serviceName='iam', version= 'v1',http=http)
    resource = service.projects()

    # ----------------------------------------------------  access_token -----------------------------------   

    # THen create a JWT for the 'Secondary svc account'
   
    jwt_scope = 'https://www.googleapis.com/auth/userinfo.email' 
    iss = client_id
    now = int(time.time())
    exptime = now + 3600
    claim =('{"iss":"%s",'
            '"scope":"%s",'
            '"aud":"https://accounts.google.com/o/oauth2/token",'
            '"exp":%s,'
            '"iat":%s}') %(iss,jwt_scope,exptime,now)    


    # now that we have a JWT, we use the master svc account to get a jwt
    slist = resource.serviceAccounts().signJwt(name='projects/your-project/serviceAccounts/' + client_id, 
                                                  body={'payload': claim })

    resp = slist.execute()   

    signed_jwt = resp['signedJwt']
  
    # finally, send the signed jwt to the google endpoint to acquire the access_token

    url = 'https://accounts.google.com/o/oauth2/token'
    data = {'grant_type' : 'assertion',
            'assertion_type' : 'http://oauth.net/grant_type/jwt/1.0/bearer',
            'assertion' : signed_jwt }
    headers = {"Content-type": "application/x-www-form-urlencoded"}
     
    data = urllib.urlencode(data)
    req = urllib2.Request(url, data, headers)

    try:
      resp = urllib2.urlopen(req).read()
      parsed = json.loads(resp)
      self.access_token = parsed.get('access_token')
      self.log('access_token: ' + self.access_token,  logging.INFO)
    except HTTPError, e:
      self.log('Error code: ' + str(e.code),logging.ERROR)
      self.log(e.read(),logging.ERROR)
    except URLError, e:
      self.log( 'Reason: ' + str(e.reason),logging.ERROR)
      self.log(e.read(),logging.ERROR)      
      sys.exit(1)

 
   # ----------------------------------------------------  id_token google_signed -----------------------------------   
      
    now = int(time.time())
    exptime = now + 3600
    id_token_claim =('{"iss":"%s",'
            '"scope":"%s",'
            '"aud":"https://www.googleapis.com/oauth2/v4/token",'
            '"exp":%s,'
            '"iat":%s}') %(client_id,client_id,exptime,now)    

    slist = resource.serviceAccounts().signJwt(name='projects/your-project/serviceAccounts/' + client_id, 
                                                  body={'payload': id_token_claim })
    resp = slist.execute()     
    signed_jwt = resp['signedJwt']    

    url = 'https://www.googleapis.com/oauth2/v4/token'
    data = {'grant_type' : 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' : signed_jwt }
    headers = {"Content-type": "application/x-www-form-urlencoded"}
     
    data = urllib.urlencode(data)
    req = urllib2.Request(url, data, headers)

    try:
      resp = urllib2.urlopen(req).read()
      parsed = json.loads(resp)
      self.id_token = parsed.get('id_token')
      self.log('id_token: ' + self.id_token,  logging.INFO)

      jwt = verify_id_token(self.id_token, client_id)     
      self.log('\n ID_TOKEN Validation: \n ' + json.dumps(jwt,sort_keys = False, indent = 4)  +' \n', logging.INFO)
    except AppIdentityError, e:
      self.log('Payload: ' + str(e.read), logging.ERROR)      
    except HTTPError, e:
      self.log('Error code: ' + str(e.code),logging.ERROR)
      self.log(e.read(),logging.ERROR)
    except URLError, e:
      self.log( 'Reason: ' + str(e.reason),logging.ERROR)
      self.log(e.read(),logging.ERROR)      
      sys.exit(1)


   # ----------------------------------------------------  id_token self_signed -----------------------------------   
      
    audience = 'SystemC'
    id_scope='scope1 scope2'
    now = int(time.time())
    exptime = now + 3600
    id_token_claim =('{"iss":"%s","scope":"%s", "aud":"%s","exp":%s,"iat":%s}') %(client_id,id_scope,audience,exptime,now)   

    slist = resource.serviceAccounts().signJwt(name='projects/your-project/serviceAccounts/' + client_id, 
                                                  body={'payload': id_token_claim })
    resp = slist.execute()     
    signed_jwt = resp['signedJwt']


    self.log('Self-signed id_token:: ' + signed_jwt,  logging.INFO)

  # taken from /oauth2client/crypt.py   
  def _urlsafe_b64encode(self,raw_bytes):
    return base64.urlsafe_b64encode(raw_bytes).rstrip('=')
   
  def _urlsafe_b64decode(self,b64string):
    # Guard against unicode strings, which base64 can't handle.
    b64string = b64string.encode('ascii')
    padded = b64string + '=' * (4 - len(b64string) % 4)
    return base64.urlsafe_b64decode(padded)
      

  def log(self,msg, loglevel):
    #LOG_FILENAME = 'gcsoauth.log'
    #logging.basicConfig(filename=LOG_FILENAME,level=logging.INFO)
    m = ('[%s] %s') % (datetime.datetime.now(), msg)
    print m
    if (loglevel == logging.DEBUG):
      logging.debug(m)   
    else:
      logging.info(m)

  def getrandom_jti(self):
      return random.choice('abcdefghijklmnopqrstuvwxyz') + hex(random.getrandbits(160))[2:-1]


if __name__ == '__main__':
  client_id = None

  try:
    opts, args = getopt.getopt(sys.argv[1:], None, ["client_id="])
  except getopt.GetoptError:
    print 'errror parsing options'
    sys.exit(1)

  for opt, arg in opts:
    if opt == "--client_id":
      client_id = arg    
      
  if (client_id is not None):
    gcs_auth(client_id)
  else:
    print 'please specify client_id='
