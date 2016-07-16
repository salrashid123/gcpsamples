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
		} 
		catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}
	    
}
