'use strict';

var http = require('http');
var express = require('express')

var log4js = require("log4js");
var GoogleAuth = require('google-auth-library');
var google = require('googleapis');

var logger = log4js.getLogger();

var app = express();

app.get('/', function (req, res) {
  google.auth.getApplicationDefault(function(err, authClient) {
      if (err) {
        logger.error(err);
        res.send(err);
        return;
      }

      if (authClient.createScopedRequired && authClient.createScopedRequired()) {
        authClient = authClient.createScoped(['https://www.googleapis.com/auth/userinfo.email']);
      }
      
      var service = google.oauth2({ version: 'v2', 
        auth: authClient
      });
      
      var service = google.oauth2({ version: 'v2', auth: authClient });    
      service.userinfo.get(function(err, info) {
          if (err) {
          res.send(err);
          return;
          }      
          res.status(200).send(info.email);      
      });
  });
});

app.get('/_ah/health', function (req, res) {
  res.status(200).send('ok');
});


var server = app.listen(8080, function () {
  var host = server.address().address;
  var port = server.address().port;
  logger.info("Web Server Start " + host + ":" + port);
});

