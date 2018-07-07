
    //authClient.setCredentials({ access_token: 'abc123' });

var log4js = require("log4js");
var GoogleAuth = require('google-auth-library');
var google = require('googleapis');

var logger = log4js.getLogger();

var authClient = new google.auth.Compute();

if (authClient.createScopedRequired && authClient.createScopedRequired()) {
   authClient = authClient.createScoped(['https://www.googleapis.com/auth/userinfo.email']);
}

var service = google.oauth2({ version: 'v2', auth: authClient });
service.userinfo.get(function(err, info) {
   if (err) {
        logger.error(err);
        return;
   }
   logger.info(info.email);
});



const Storage = require('@google-cloud/storage');
const storage = new Storage({
  projectId: 'your-project',
});

storage.getBuckets(function(err, buckets) {
if (!err) {
  buckets.forEach(function(value){
      logger.info(value.id);
});
}
});


const Pubsub = require('@google-cloud/pubsub');

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

