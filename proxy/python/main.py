#!/usr/bin/python

# virtualenv env
# source env/bin/activate
# pip install google-cloud-storage google-cloud-pubsub --upgrade

project='your_project'

from google.cloud import storage
client = storage.Client(project=project)
for b in client.list_buckets():
   print(b.name)

from google.cloud import pubsub
client = pubsub.PublisherClient()
project_path = client.project_path(project)
for topic in client.list_topics(project_path):
  print(topic)