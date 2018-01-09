

const fs = require('fs')
var jws = require('jws');
const axios = require('axios');
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
authFactory.getApplicationDefault((err, authClient) => {
  if (err) {
    console.log('Authentication failed because of ', err);
    return;
  }
  if (authClient.createScopedRequired && authClient.createScopedRequired()) {
    const scopes = ['https://www.googleapis.com/auth/userinfo.email'];
    authClient = authClient.createScoped(scopes);
  }
  authClient.refreshAccessToken(async (err, creds) => {
    if (err) {
      console.log(err);
      return;
    }
    console.log('***************** UserCredentials JSON *****************');
    console.log(creds.id_token);
    console.log('Verifying Signature:');
    try {
      const response = await axios.get(google_certs);
      const result = await jose.JWK.asKeyStore(response.data);
      const dec = jws.decode(creds.id_token);
      const key_id = dec.header.kid;
      await jose.JWS.createVerify(result).verify(creds.id_token);
      console.log('>>>>>>>>>> signature verified  <<<<<<<<<<<<<<<<');
    } catch (e) {
      console.log('Unable to Verify JWT');
      console.error(e);
    }
  });
})