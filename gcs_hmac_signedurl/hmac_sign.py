
import base64
import datetime
import hashlib
import hmac
import time

import os
import sys
import json
import time

import base64
import datetime
import urllib
import requests


GCS_API_ENDPOINT = 'https://storage.googleapis.com'
hmac_key = 'GOOGE6ESAVCTDYJSGKXMQK6M'
hmac_secret = 'aZK6ILu4pHfFD1F8wLlhFllakLq0CqjQM9ez0pXy'
BUCKET_NAME = 'mineral-minutia-820'
OBJECT_NAME = 'somefile.txt'

expiration = datetime.datetime.now() +  datetime.timedelta(seconds=60)
expiration = int(time.mktime(expiration.timetuple()))

def _Base64Sign(url_to_sign):
        digest = hmac.new(
          hmac_secret, url_to_sign.encode('utf-8'), hashlib.sha1).digest()
        signature = base64.standard_b64encode(digest).decode('utf-8')        
        return signature


def _MakeSignatureString(verb, path, content_md5, content_type):
      signature_string = ('{verb}\n'
                          '{content_md5}\n'
                          '{content_type}\n'
                          '{expiration}\n'
                          '{resource}')
      return signature_string.format(verb=verb,
                                     content_md5=content_md5,
                                     content_type=content_type,
                                     expiration=expiration,
                                     resource=path)

def MakeUrl(verb, path, content_type='', content_md5=''):
      signature_string = _MakeSignatureString(verb, path, content_md5,
                                                   content_type)
      signature_signed = urllib.quote(_Base64Sign(signature_string))      

      signed_url = "https://storage.googleapis.com/" + \
              BUCKET_NAME + "/" + OBJECT_NAME + "?GoogleAccessId=" + \
              hmac_key + "&Expires=" + str(expiration) + \
              "&Signature=" + signature_signed

      return signed_url


file_path = '/%s/%s' % (BUCKET_NAME, OBJECT_NAME)


print "PUT:"
u =  MakeUrl("PUT",file_path)
print u
r = requests.put(u, data='lorem ipsum')
print "put status_code: " + str(r.status_code)
print 'data: ' + r.text
print "---------------------------------"

print "GET"
u =  MakeUrl("GET",file_path)
print u
r = requests.get(u)
print "get status_code: " + str(r.status_code)
print 'data; ' + r.text
