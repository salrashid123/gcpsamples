var log4js = require("log4js");
var logger = log4js.getLogger();

const Pubsub = require('@google-cloud/pubsub');
const Storage = require('@google-cloud/storage');


/*

1. User auth
   export https_proxy=http://localhost:3128
   
   auth N
   gcs Y
   pubsub Y

   1638364764.530    205 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
   1638364764.939    694 192.168.9.1 TCP_TUNNEL/200 7595 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	
2. user auth
   export https_proxy=http://localhost:3128

	Auth error:Error: write EPROTO 139715611573120:error:1408F10B:SSL routines:ssl3_get_record:wrong version number:../deps/openssl/openssl/ssl/record/ssl3_record.c:332:

    (which is fine)

3. svc account
   export https_proxy=http://localhost:3128

   auth Y
   gcs Y
   pubsub Y

    1638364926.866     60 192.168.9.1 TCP_TUNNEL/200 5866 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364926.899     50 192.168.9.1 TCP_TUNNEL/200 6048 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364927.030    149 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -
	1638364927.082    277 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 - HIER_DIRECT/142.250.73.202 -

4. basic user auth 
   export http_proxy=http://user1:user1@localhost:3128

   auth N
   gcs Y
   pubsub Y

	1638365107.917    152 192.168.9.1 TCP_TUNNEL/200 44961 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365107.938    509 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -

5. basic service account

   auth Y
   gcs Y
   pubsub Y

	1638365190.001     53 192.168.9.1 TCP_TUNNEL/200 6027 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.017     44 192.168.9.1 TCP_TUNNEL/200 5829 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.156    142 192.168.9.1 TCP_TUNNEL/200 44960 CONNECT www.googleapis.com:443 user1 HIER_DIRECT/172.217.164.138 -
	1638365190.192    244 192.168.9.1 TCP_TUNNEL/200 7596 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.250.73.202 -

*/
var gcs = new Storage();
gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});
  }
});

const pubsub = Pubsub({
  projectId: 'your-project'
});
pubsub.getTopics((err, topic) => {
	if (err) {
		logger.error(err);
		return;
	}
	topic.forEach(function(entry) {
    logger.info(entry.name);
	});
});


