

const fs = require('fs')
var jws = require('jws');
const request = require('request');
var jose = require('node-jose');

// https://github.com/google/google-auth-library-nodejs/blob/next/examples/verifyIdToken.js

var GoogleAuth = require('google-auth-library');
var authFactory = new GoogleAuth();
var jwtClient = new authFactory.JWT();

const service_account_certs = 'https://www.googleapis.com/service_accounts/v1/jwk/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com';
const google_certs = 'https://www.googleapis.com/oauth2/v3/certs';

const service_account_json = '/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json';

/*
  "aud": "764086051850-6qr4p6gpi6hn506pt8ejuq83di341hur.apps.googleusercontent.com",
  "iss": "accounts.google.com",
*/
authFactory.getApplicationDefault(function(err, authClient) {
    if (err) {
      console.log('Authentication failed because of ', err);
      return;
    }
    if (authClient.createScopedRequired && authClient.createScopedRequired()) {
      var scopes = ['https://www.googleapis.com/auth/userinfo.email'];
      authClient = authClient.createScoped(scopes);
    }
    console.log("***************** UserCredentials JSON *****************");
    authClient.refreshAccessToken(function (err, creds) {
      if ( err ) {
        console.log(err);
        return;
      }
      console.log(creds.id_token);

      // need to verify the creds.id_token  here
      // i don't know if https://github.com/google/google-auth-library-nodejs/blob/master/ts/lib/auth/oauth2client.ts#L450
      // would help

      console.log("Verifying Signature:");
      request(google_certs, { json: true }, (err, res, body) => {
        if (err) { return console.log(err); }        
        jose.JWK.asKeyStore(body).then(function(result) {	       
          var dec = jws.decode(creds.id_token);
          var key_id = dec.header.kid;
          jose.JWS.createVerify(result).
                  verify(creds.id_token).
                  then(function(result) {
                    console.log('>>>>>>>>>> signature verified  <<<<<<<<<<<<<<<<')
                  }).catch(function(e) {
                  console.log("Uable to Verify JWT");
                  console.log(e);
              });
            });  
      });
    });

});  
    


/*
fs.readFile(service_account_json, "utf-8", function(err, data) {  
    if (err) throw err;
    var parsed_json = JSON.parse(data);
    jwtClient.fromJSON(parsed_json, (err) => {
      if ( err ) {
        console.log(err);
        return;
      }
    });
    console.log("*****************  Service Account JSON  (default) *****************");

    if (jwtClient.createScopedRequired && jwtClient.createScopedRequired()) {
      var scopes = ['https://www.googleapis.com/auth/userinfo.email'];
      jwtClient = jwtClient.createScoped(scopes);
    }    

    jwtClient.authorize(function (err, creds) {
      if ( err ) {
        console.log(err);
        return;
      }
      // Does not return id_token
      console.log(creds)

    });

});
*/

console.log("*****************  Service Account JSON (manual sign) *****************");
/*
  "iss": "svc-2-429@mineral-minutia-820.iam.gserviceaccount.com",
  "aud": "https://www.googleapis.com/oauth2/v4/token",
  "target_audience": "https://yourapp.appspot.com/"
*/

fs.readFile(service_account_json, "utf-8", function(err, data) { 
    var parsed_json = JSON.parse(data); 

    const iat = Math.floor(new Date().getTime() / 1000);
    const exp = iat + 3600;  // 3600 seconds = 1 hour

    var payload = {
      iss: parsed_json.client_email,
      aud: "https://www.googleapis.com/oauth2/v4/token",
      exp: exp,
      iat: iat,
      target_audience: "https://yourapp.appspot.com/"
    }

    const signature = jws.sign({
      header:  {alg: 'RS256', typ: 'JWT', kid: parsed_json.private_key_id},
      payload:  payload,
      privateKey: parsed_json.private_key
    });

    console.log(signature);
    console.log("Verifying Signature:");
    request(service_account_certs, { json: true }, (err, res, body) => {
        if (err) { return console.log(err); }        
        jose.JWK.asKeyStore(body).then(function(result) {	       
          var dec = jws.decode(signature);
          var key_id = dec.header.kid;
          jose.JWS.createVerify(result).
                  verify(signature).
                  then(function(result) {
                    console.log('>>>>>>>>>> signature verified  <<<<<<<<<<<<<<<<')
                  }).catch(function(e) {
                  console.log("Uable to Verify JWT");
                  console.log(e);
              });
            });  
      });

      console.log("Exchanging JWT for Google ID Token");
      data = {
        'grant_type' : 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' : signature 
      }
      headers = {"Content-type": "application/x-www-form-urlencoded"}

      request.post({url:'https://www.googleapis.com/oauth2/v4/token', form: data, headers: headers, json: true }, function(err,httpResponse,r){ 
        if (err) { return console.log(err); }

        console.log(r.id_token);

        console.log("Verifying Signature against: [" + google_certs + ']');
        request(google_certs, { json: true }, (err, res, body) => {
            if (err) { return console.log(err); }        
            jose.JWK.asKeyStore(body).then(function(result) {	       
              var dec = jws.decode(r.id_token);
              var key_id = dec.header.kid;
              jose.JWS.createVerify(result).
                      verify(r.id_token).
                      then(function(result) {
                        console.log('>>>>>>>>>> signature verified  <<<<<<<<<<<<<<<<')
                      }).catch(function(e) {
                      console.log("Uable to Verify JWT");
                      console.log(e);
                  });
                });  
          });
      })
});
