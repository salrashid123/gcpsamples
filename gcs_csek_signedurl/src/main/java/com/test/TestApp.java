/*
Copyright [2018] [Google]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.test;

import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobInfo;

import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.io.OutputStreamWriter;
import javax.net.ssl.HttpsURLConnection;
import java.security.MessageDigest;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

//import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;


public class TestApp {

	private final String keyFile = "/home/srashid/gcp_misc/certs/your-project-e9a7c8665867.json";
	private final String BUCKET_NAME = "your-project";
	private final String BLOB_NAME = "encrypted.txt";
	

	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{

			Storage storage_service = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(keyFile)))
				.build()
				.getService();

			BlobInfo BLOB_INFO1 = BlobInfo.newBuilder(BUCKET_NAME, BLOB_NAME).build();

			Map<String, String> extHeaders = new HashMap<String, String>();
			extHeaders.put("x-goog-encryption-algorithm", "AES256");
			extHeaders.put("x-goog-meta-icecreamflavor", "vanilla");
			URL url =
			storage_service.signUrl(
					BLOB_INFO1,
					60,
					TimeUnit.SECONDS,
					Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
					Storage.SignUrlOption.withExtHeaders(extHeaders));

			System.out.println(url);



			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey skey = keyGen.generateKey();
			String encryption_key = Base64.getEncoder().encodeToString(skey.getEncoded());

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String encryption_key_sha256 = Base64.getEncoder().encodeToString(digest.digest(skey.getEncoded()));	

			String postData = "lorem ipsum";
			
			HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty("x-goog-encryption-algorithm", "AES256");
			httpCon.setRequestProperty("x-goog-encryption-key", encryption_key);
			httpCon.setRequestProperty("x-goog-encryption-key-sha256", encryption_key_sha256);
			httpCon.setRequestProperty("x-goog-meta-icecreamflavor", "vanilla");	
			httpCon.setRequestProperty("Content-Length", "" + postData.getBytes().length);
			
			OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
			out.write(postData);
			out.close();
			httpCon.getInputStream();

			System.out.println("Response Code : " + httpCon.getResponseCode());
			System.out.println("Response Message : " + httpCon.getResponseMessage());


		} 
		catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}
	    
}
