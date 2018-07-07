

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


// either set the env variable or keyFilename
//process.env.GOOGLE_APPLICATION_DEFAULT='/path/to/keyfile.json';

const Storage = require('@google-cloud/storage');
const storage = new Storage({
  projectId: 'your_project',
  keyFilename: '/path/to/keyfile.json',
});

storage.getBuckets(function(err, buckets) {
if (err) {
  console.log(err);
}
if (!err) {
  buckets.forEach(function(value){
      logger.info(value.id);
});
}
});

