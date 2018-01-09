
package com.test;

import java.io.FileInputStream;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.net.URL;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;



public class TestApp {


     public static void main(String[] args) {
          TestApp tc = new TestApp();
     }


     public TestApp() {
          try
          {
            System.out.println("UserCredentials id_token:");
            /*
              "aud": "764086051850-6qr4p6gpi6hn506pt8ejuq83di341hur.apps.googleusercontent.com",
              "iss": "accounts.google.com",            
            */

            /* 
            https://github.com/google/google-auth-library-java/blob/master/oauth2_http/java/com/google/auth/oauth2/UserCredentials.java#L229
            String idToken = OAuth2Utils.validateString(responseData, "id_token", PARSE_ERROR_PREFIX);
            System.out.println(idToken);
            */
            UserCredentials user_credentials =   (UserCredentials)GoogleCredentials.getApplicationDefault();
            user_credentials.refresh();

            System.out.println(user_credentials.getAccessToken().getTokenValue());

            // https://developers.google.com/identity/sign-in/web/backend-auth#verify-the-integrity-of-the-id-token


            System.out.println("2. Issue JWT with Service Account JSON file");
            /*
              "aud": "https://www.googleapis.com/oauth2/v4/token",
              "target_audience": "https://yourappid.appspot.com/",
              "iss": "svc-2-429@mineral-minutia-820.iam.gserviceaccount.com",
            */

            // Taken from:
            // https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/iap/src/main/java/com/example/iap/BuildIapRequest.java#L65
            // https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
            ServiceAccountCredentials service_credentials =   ServiceAccountCredentials.fromStream(new FileInputStream("/home/srashid/gcp_misc/certs/GCPNETAppID-e65deccae47b.json"));

            Clock c =  Clock.systemUTC();
            Instant now = Instant.now(c);
            long expirationTime = now.getEpochSecond() + 3600;

            JWSHeader jwsHeader =
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(service_credentials.getPrivateKeyId()).build();
    
            JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                    .audience("https://www.googleapis.com/oauth2/v4/token")
                    .issuer(service_credentials.getClientEmail())
                    .subject(service_credentials.getClientEmail())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(Instant.ofEpochSecond(expirationTime)))
                    .claim("target_audience", "https://yourappid.appspot.com/" )
                    .build();
        
            JWSSigner signer = new RSASSASigner(service_credentials.getPrivateKey());
            SignedJWT signedJwt = new SignedJWT(jwsHeader, claims);
            signedJwt.sign(signer);
            System.out.println("Service Account issued JWT: " + signedJwt.serialize());


            System.out.println("Verifying JWT for Service Account");
            ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();            
            JWKSource keySource = new RemoteJWKSet(new URL("https://www.googleapis.com/service_accounts/v1/jwk/svc-2-429@mineral-minutia-820.iam.gserviceaccount.com"));            
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
            JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);            
            try {
            JWTClaimsSet claimsSet = jwtProcessor.process(signedJwt.serialize(), null);            
            System.out.println(claimsSet.toJSONObject());
            } catch (com.nimbusds.jose.proc.BadJWSException e) {
              System.out.println("Unable to verify service acount JWT: " + e.getMessage());
            }

            System.out.println("Exchange Service Account JWT for GoogleID Token");
            /*
            {
              "aud": "https://yourappid.appspot.com/",
              "iss": "https://accounts.google.com",
            }
            */
            HttpTransport httpTransport = new NetHttpTransport();
            GenericData tokenRequest =
            new GenericData().set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer").set("assertion", signedJwt.serialize());
            UrlEncodedContent content = new UrlEncodedContent(tokenRequest);
        
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        
            HttpRequest request =
                requestFactory
                    .buildPostRequest(new GenericUrl("https://www.googleapis.com/oauth2/v4/token"), content)
                    .setParser(new JsonObjectParser(JacksonFactory.getDefaultInstance()));
        
            HttpResponse response;
            String idToken = null;
            response = request.execute();
            GenericData responseData = response.parseAs(GenericData.class);
            idToken = (String) responseData.get("id_token");
            
            System.out.println("Google ID Token: " + idToken);

            System.out.println("Verifying JWT for Service Account");
            jwtProcessor = new DefaultJWTProcessor();            
            keySource = new RemoteJWKSet(new URL("https://www.googleapis.com/oauth2/v3/certs"));            
            expectedJWSAlg = JWSAlgorithm.RS256;
            keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);            
            try {
            JWTClaimsSet claimsSet = jwtProcessor.process(idToken, null);            
            System.out.println(claimsSet.toJSONObject());
            } catch (com.nimbusds.jose.proc.BadJWSException e) {
              System.out.println("Unable to verify service acount JWT: " + e.getMessage());
            }

          }
          catch (Exception ex) {
               System.out.println("Error:  " + ex);
          }

     }

}
