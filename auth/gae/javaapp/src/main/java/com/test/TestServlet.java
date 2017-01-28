package com.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.*;
import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.*;
import com.google.api.services.oauth2.model.Userinfoplus;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.Collection;
import java.util.Iterator;

/* 
   Edit WEB-INF/lib/appengine-web.xml  with  your APPID 
   Set appengien administrator/deployes email in build.gradle
*/


public class TestServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(TestServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
				
		HttpTransport httpTransport = new UrlFetchTransport();        
		JacksonFactory jsonFactory = new JacksonFactory();

		/*
		AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();    
		AppIdentityService.GetAccessTokenResult accessToken = appIdentity.getAccessToken(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));         
		GoogleCredential credential = new GoogleCredential.Builder()
			.setTransport(httpTransport)
			.setJsonFactory(jsonFactory).build();
	    credential.setAccessToken(accessToken.getAccessToken());
		*/

		GoogleCredential credential = GoogleCredential.getApplicationDefault(httpTransport,jsonFactory);    
		if (credential.createScopedRequired())
		    credential = credential.createScoped(Arrays.asList(Oauth2Scopes.USERINFO_EMAIL));           

		Oauth2 service = new Oauth2.Builder(httpTransport, jsonFactory, credential)
		    .setApplicationName("oauth client").build();

		Userinfoplus ui = service.userinfo().get().execute(); 
		resp.getWriter().println(ui.getEmail());
	    

          // Using Google Cloud APIs
		  Storage  storage_service = StorageOptions.defaultInstance().service();
        
          Iterator<Bucket> bucketIterator = storage_service.list().iterateAll();
          while (bucketIterator.hasNext()) {
            System.out.println(bucketIterator.next());
          }		

	}
}
	

