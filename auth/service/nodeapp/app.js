

var log4js = require("log4js");
var GoogleAuth = require('google-auth-library');

var logger = log4js.getLogger();

// either set the env variable or keyFilename
//process.env.GOOGLE_APPLICATION_DEFAULT='/path/to/keyfile.json';

const Storage = require('@google-cloud/storage');
const storage = new Storage({
  projectId: 'project_id',
  //keyFilename: '/path/to/keyfile.json',
});

storage.getBuckets(function (err, buckets) {
  if (err) {
    console.log(err);
  }
  if (!err) {
    buckets.forEach(function (value) {
      logger.info(value.id);
    });
  }
});

