package com.test;

import java.util.Arrays;

import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.Collection;
import java.util.Iterator;

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{
			HttpTransport httpTransport = new NetHttpTransport();             
			JacksonFactory jsonFactory = new JacksonFactory();
			//ComputeCredential credential = new ComputeCredential.Builder(httpTransport, jsonFactory).build();	
			GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);				            
			if (credential.createScopedRequired())
			    credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));           				            
			Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
			            .setApplicationName("oauth client")   
			            .build();				            
			Userinfoplus ui = service.userinfo().get().execute();
			System.out.println(ui.getEmail());



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
