package com.test;

import java.util.Arrays;
import java.util.Collections;
import java.io.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Request;
import com.google.api.services.oauth2.Oauth2RequestInitializer;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.Collection;
import java.util.Iterator;
import java.io.FileInputStream;

import com.google.auth.oauth2.ServiceAccountCredentials;

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{
			HttpTransport httpTransport = new NetHttpTransport();             
			JacksonFactory jsonFactory = new JacksonFactory();

            // unset GOOGLE_APPLICATION_CREDENTIALS
            //String SERVICE_ACCOUNT_JSON_FILE = "YOUR_SERVICE_ACCOUNT_JSON_FILE.json";
            //FileInputStream inputStream = new FileInputStream(new File(SERVICE_ACCOUNT_JSON_FILE));
            //GoogleCredential credential = GoogleCredential.fromStream(inputStream, httpTransport, jsonFactory);

			// to use application default credentials and a JSON file, set the environment variable first:
            // export GOOGLE_APPLICATION_CREDENTIALS=YOUR_SERVICE_ACCOUNT_JSON_FILE.json        
            GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);

            if (credential.createScopedRequired())
                credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));

			Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
			            .setApplicationName("oauth client")   
			            .build();				            
			Userinfoplus ui = service.userinfo().get().execute();
			System.out.println(ui.getEmail());


          // Using Google Cloud APIs with service account file
		  // You can also just export an export GOOGLE_APPLICATION_CREDENTIALS and use StorageOptions.defaultInstance().service()
		  // see: https://github.com/google/google-auth-library-java#google-auth-library-oauth2-http
		  /*
		  Storage storage_service = StorageOptions.newBuilder()
			.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("/path/to/your/certificate.json")))
			.build()
			.getService();			
		  */

          // Using Google Cloud APIs

		  Storage  storage_service = StorageOptions.defaultInstance().service();
        
          Iterator<Bucket> bucketIterator = storage_service.list().iterateAll();
          while (bucketIterator.hasNext()) {
            System.out.println(bucketIterator.next());
          }		
		} 
		catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}
	    
}
