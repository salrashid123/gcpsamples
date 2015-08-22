package com.test;

import java.util.Arrays;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.*;
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

			GoogleClientSecrets clientSecrets =  new GoogleClientSecrets();
			GoogleClientSecrets.Details det = new GoogleClientSecrets.Details();
			det.setClientId("YOUR_CLIENT_ID");
			det.setClientSecret("YOUR_CLIENT_SECRET");
			det.setRedirectUris(Arrays.asList("urn:ietf:wg:oauth:2.0:oob"));
			clientSecrets.setInstalled(det);

			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
			    httpTransport, jsonFactory, clientSecrets,
			    Arrays.asList(Oauth2Scopes.USERINFO_EMAIL)).build();
			Credential credential = new AuthorizationCodeInstalledApp(flow,
			    new LocalServerReceiver()).authorize("user");

			Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
			    .setApplicationName("oauth client").build();

			Userinfoplus ui = service.userinfo().get().execute();
			System.out.println(ui.getEmail());
		} 
		catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}
	    
}
