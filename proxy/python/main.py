#!/usr/bin/python


# 1. user auth
#    export http_proxy=http://localhost:3128
#    auth N 
#    gcs N 
#    pubub Y

#    1638366068.078    261 192.168.9.1 TCP_TUNNEL/200 7876 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

# 2. user auth
#    export https_proxy=http://localhost:3128
#    auth Y
#    gcs Y 
#    pubub Y

   # 1638366275.669    367 192.168.9.1 TCP_TUNNEL/200 7876 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
   # 1638366275.669    324 192.168.9.1 TCP_TUNNEL/200 7183 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/142.250.73.234 -
   # 1638366275.692   1147 192.168.9.1 TCP_TUNNEL/200 34961 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.81.208 -
   # 1638366275.692   1219 192.168.9.1 TCP_TUNNEL/200 7030 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/142.250.73.234 -


# 3.  service account
#    export https_proxy=http://localhost:3128
#    auth Y
#    gcs Y 
#    pubub Y

   # 1638366614.398    201 192.168.9.1 TCP_TUNNEL/200 7838 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.251.33.202 -
   # 1638366614.418    643 192.168.9.1 TCP_TUNNEL/200 6418 CONNECT oauth2.googleapis.com:443 - HIER_DIRECT/172.217.1.202 -
   # 1638366614.418    563 192.168.9.1 TCP_TUNNEL/200 34953 CONNECT storage.googleapis.com:443 - HIER_DIRECT/142.250.81.208 -


# 4. basic + user auth
# export https_proxy=http://user1:user1@localhost:3128
#    auth Y
#    gcs Y 
#    pubub Y
   # 1638366799.680    438 192.168.9.1 TCP_TUNNEL/200 7877 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
   # 1638366799.680    404 192.168.9.1 TCP_TUNNEL/200 7010 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -
   # 1638366799.701   1450 192.168.9.1 TCP_TUNNEL/200 34954 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
   # 1638366799.701   1549 192.168.9.1 TCP_TUNNEL/200 7155 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -

# 5.  basic +service account
# export https_proxy=http://user1:user1@localhost:3128
#    auth Y
#    gcs Y 
#    pubub Y
# 1638366879.245    284 192.168.9.1 TCP_TUNNEL/200 7877 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
# 1638366879.264    582 192.168.9.1 TCP_TUNNEL/200 6408 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/142.250.188.42 -
# 1638366879.264    541 192.168.9.1 TCP_TUNNEL/200 34953 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -


project='your-project'

from google.cloud import storage
client = storage.Client(project=project)
for b in client.list_buckets():
   print(b.name)

from google.cloud import pubsub_v1
publisher = pubsub_v1.PublisherClient()
project_path = f"projects/{project}"
for topic in publisher.list_topics(request={"project": project_path}):
  print(topic)