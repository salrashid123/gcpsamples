

const fs = require('fs')
var jws = require('jws');
const axios = require('axios');
var jose = require('node-jose');
var querystring = require('querystring');

// https://github.com/google/google-auth-library-nodejs/blob/next/examples/verifyIdToken.js

var GoogleAuth = require('google-auth-library');
var authFactory = new GoogleAuth();
var jwtClient = new authFactory.JWT();

const service_account_certs = 'https://www.googleapis.com/service_accounts/v1/jwk/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com';
const google_certs = 'https://www.googleapis.com/oauth2/v3/certs';

const service_account_json = '/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json';



/*
  "iss": "svc-2-429@mineral-minutia-820.iam.gserviceaccount.com",
  "aud": "https://www.googleapis.com/oauth2/v4/token",
  "target_audience": "https://yourapp.appspot.com/"
*/

fs.readFile(service_account_json, "utf-8",  async (err, data) => {
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

    console.log("*****************  Google Id Token *****************");
    console.log(signature);
    console.log("Exchanging JWT for Google ID Token");

    data = {
      'grant_type' : 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      'assertion' : signature
    }

    var config = {
      headers : {"Content-type": "application/x-www-form-urlencoded"}
    };

    // cant' get axis post working :(
    const post_response = await axios.post('https://www.googleapis.com/oauth2/v4/token',  querystring.stringify(data),  config);

      console.log('Got GoogleID Token:');
      console.log(post_response.data.id_token);
      console.log('Verifying Signature:');
      try {
        const response = await axios.get(google_certs);
        const result = await jose.JWK.asKeyStore(response.data);
        const dec = jws.decode(post_response.data.id_token);
        const key_id = dec.header.kid;
        await jose.JWS.createVerify(result).verify(post_response.data.id_token);
        console.log('>>>>>>>>>> signature verified  <<<<<<<<<<<<<<<<');
      } catch (e) {
        console.log('Unable to Verify JWT');
        console.error(e);
      }
  

});
