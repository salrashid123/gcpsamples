

var log4js = require("log4js");
var GoogleAuth = require('google-auth-library');
var google = require('googleapis');
var fs = require('fs');
var readline = require('readline');

var logger = log4js.getLogger();

var SCOPES = ['https://www.googleapis.com/auth/userinfo.email'];

// Taken from: https://developers.google.com/drive/v3/web/quickstart/nodejs
fs.readFile('/path/to/client_secret_installed.json', function processClientSecrets(err, content) {
  if (err) {
    logger.error('Error loading client secret file: ' + err);
    return;
  }
  credentials = JSON.parse(content);
  var clientSecret = credentials.installed.client_secret;
  var clientId = credentials.installed.client_id;
  var redirectUrl = credentials.installed.redirect_uris[0];
  var auth = new GoogleAuth();
  var authClient = new auth.OAuth2(clientId, clientSecret, redirectUrl);

  logger.info(clientId);

  var authUrl = authClient.generateAuthUrl({
    access_type: 'offline',
    scope: SCOPES
  });
  console.log('Authorize this app by visiting this url: ', authUrl);
  var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  rl.question('Enter the code from that page here: ', function(code) {
    rl.close();
    authClient.getToken(code, function(err, token) {
      if (err) {
        console.log('Error while trying to retrieve access token', err);
        return;
      }
      authClient.credentials = token;

      var service = google.oauth2('v2');

      service.userinfo.get({auth: authClient}, function(err, info) {
			logger.info(info.email);      
		});
    });
  });
});