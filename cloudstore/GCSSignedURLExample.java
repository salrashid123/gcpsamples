/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
*/


/*
Demonstrates various Google Cloud Storage Signed URL operations
https://cloud.google.com/storage/docs/accesscontrol#Signed-URLs

Replace SERVICE_ACCOUNT_EMAIL,SERVICE_ACCOUNT_PKCS12_FILE_PATH,BUCKET_NAME incode below
*/

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

// From Apache Commons Codec.
// https://commons.apache.org/codec/
import org.apache.commons.codec.binary.Base64;


public class GCSSignedURLExample {

    final String SERVICE_ACCOUNT_EMAIL = "<YOUR_SERVICE_ACCOUNT_EMAIL>@developer.gserviceaccount.com";
    final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "YOUR_SERVICE_ACCOUNT_KEY.p12";
    final long expiration = System.currentTimeMillis()/1000 + 60;

    final String BUCKET_NAME = "YOURBUCKET";
    String OBJECT_NAME = "somerandomfile.txt";

    PrivateKey key;

    public static void main(String[] args) {
        GCSSignedURLExample tc = new GCSSignedURLExample();
    }

    public GCSSignedURLExample() {
        try {

             key = loadKeyFromPkcs12(SERVICE_ACCOUNT_PKCS12_FILE_PATH, "notasecret".toCharArray());

             System.out.println("======= PUT File =========");
             String put_url = this.getSigningURL("PUT");

             URL url = new URL(put_url);
             HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
             httpCon.setDoOutput(true);
             httpCon.setRequestMethod("PUT");
             OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
             out.write("Lorem ipsum");
             out.close();

             System.out.println("PUT Request URL: " + put_url);
             System.out.println("PUT Response code: " + httpCon.getResponseCode());
             renderResponse(httpCon.getInputStream());

             System.out.println("======= GET File =========");

             String get_url = this.getSigningURL("GET");
             url = new URL(get_url);
             httpCon = (HttpURLConnection) url.openConnection();
             httpCon.setRequestMethod("GET");

             System.out.println("GET Request URL " + get_url);
             System.out.println("GET Response code: " + httpCon.getResponseCode());
             renderResponse(httpCon.getInputStream());

             System.out.println("======= DELETE File ======");

             String delete_url = this.getSigningURL("DELETE");
             url = new URL(delete_url);
             httpCon = (HttpURLConnection) url.openConnection();
             httpCon.setRequestMethod("DELETE");

             System.out.println("DELETE Request URL " + get_url);
             System.out.println("DELETE Response code: " + httpCon.getResponseCode());
             renderResponse(httpCon.getInputStream());

        }
        catch (Exception ex) {
            System.out.println("Error : " + ex);
        }
}
    private void renderResponse(InputStream is) throws Exception {
         BufferedReader in = new BufferedReader(new InputStreamReader(is));
         String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
         in.close();
    }
    private String getSigningURL(String verb) throws Exception {
         String url_signature = this.signString(verb + "\n\n\n" + expiration + "\n" + "/" + BUCKET_NAME + "/" + OBJECT_NAME  );
         String signed_url = "https://storage.googleapis.com/" + BUCKET_NAME + "/" + OBJECT_NAME +
                    "?GoogleAccessId=" + SERVICE_ACCOUNT_EMAIL +
                    "&Expires=" + expiration +
                    "&Signature=" + URLEncoder.encode(url_signature, "UTF-8");
         return signed_url;
    }

    private static PrivateKey loadKeyFromPkcs12(String filename, char[] password) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(fis, password);
        return (PrivateKey) ks.getKey("privatekey", password);
    }

    private String signString(String stringToSign) throws Exception {
        if (key == null)
          throw new Exception("Private Key not initalized");
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(key);
        signer.update(stringToSign.getBytes("UTF-8"));
        byte[] rawSignature = signer.sign();
        return new String(Base64.encodeBase64(rawSignature, false), "UTF-8");
    }
}
