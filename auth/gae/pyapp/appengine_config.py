import os
import sys

from google.appengine.ext import vendor
vendor.add('lib')
sys.path.append(os.path.join(os.path.dirname(__file__), 'lib'))
