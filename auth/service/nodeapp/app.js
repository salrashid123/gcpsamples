

var log4js = require("log4js");
var GoogleAuth = require('google-auth-library');
var google = require('googleapis');

var logger = log4js.getLogger();


var key = require('/path/to/keyfile.json');
var authClient = new google.auth.JWT(key.client_email, null, key.private_key, ['https://www.googleapis.com/auth/userinfo.email'], null);

authClient.authorize(function(err, tokens) {
  if (err) {
    console.log(err);
    return;
  }    
  var service = google.oauth2({ version: 'v2', auth: authClient });    

  service.userinfo.get(function(err, info) {
   if (err) {
      logger.error(err);
      return;
   }      
   logger.info(info.email);
  });

});


var gcloud = require('google-cloud')({
  keyFilename: '/path/to/keyfile.json'
});

var gcs = gcloud.storage();

gcs.getBuckets(function(err, buckets) {
  if (!err) {
  	buckets.forEach(function(value){
  			logger.info(value.id);
	});    
  }
});


