var log4js = require("log4js");
var logger = log4js.getLogger();

const Pubsub = require('@google-cloud/pubsub');
const Storage = require('@google-cloud/storage');

var gcs = new Storage();
gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});
  }
});

const pubsub = Pubsub({
  projectId: 'your_project'
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
