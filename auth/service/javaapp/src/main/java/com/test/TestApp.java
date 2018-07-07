package com.test;

import java.util.Arrays;
import java.util.Collections;
import java.io.*;
/*
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
*/
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.api.gax.paging.Page;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.api.gax.rpc.FixedTransportChannelProvider;


import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicSubscriptionsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.Topic;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;

import java.util.Collection;
import java.util.Iterator;
import java.io.FileInputStream;

import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.google.iam.v1.SetIamPolicyRequest;
import com.google.iam.v1.Binding;
import com.google.cloud.Role;

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{

			// use env or set the path directly
			String cred_env = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
			cred_env = "/path/to/your/cert.json";
			
/*
			<!--use:
				<dependency>
				<groupId>com.google.api-client</groupId>
				<artifactId>google-api-client</artifactId>
				<version>1.23.0</version>
				</dependency>
				<dependency>
				<groupId>com.google.apis</groupId>
				<artifactId>google-api-services-oauth2</artifactId>
				<version>v2-rev114-1.22.0</version>
				</dependency>
			--> 
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
*/
/* 
          Using Google Cloud APIs with service account file
		  // You can also just export an export GOOGLE_APPLICATION_CREDENTIALS and use StorageOptions.defaultInstance().service()
		  // see: https://github.com/google/google-auth-library-java#google-auth-library-oauth2-http
		  uncomment the dependencies for google-api-client
		  
			<dependency>
				<groupId>com.google.cloud</groupId>
				<artifactId>google-cloud-storage</artifactId>
				<version>1.35.0</version>
			</dependency>

			<dependency>
				<groupId>com.google.cloud</groupId>
				<artifactId>google-cloud-pubsub</artifactId>
				<version>1.35.0</version>
			</dependency>
*/
		  
		  
		  Storage storage_service = StorageOptions.newBuilder()
			.build()
			.getService();	
		  for (Bucket b : storage_service.list().iterateAll()){
			  System.out.println(b);
		  }

		  //GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(cred_env));	
		  GoogleCredentials creds = GoogleCredentials.getApplicationDefault();	  	  
		  FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(creds);
		  
		  ///ManagedChannel channel = ManagedChannelBuilder.forTarget("pubsub.googleapis.com:443").build();
          //TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));	
		  
		  TransportChannelProvider channelProvider = TopicAdminSettings.defaultTransportChannelProvider();

		  TopicAdminClient topicClient =
			  TopicAdminClient.create(
				  TopicAdminSettings.newBuilder()
					  .setTransportChannelProvider(channelProvider)
					  .setCredentialsProvider(credentialsProvider)
					  .build());

		  ListTopicsRequest listTopicsRequest =
							ListTopicsRequest.newBuilder()
								.setProject(ProjectName.format("your_project"))
								.build();
		  ListTopicsPagedResponse response = topicClient.listTopics(listTopicsRequest);
		  Iterable<Topic> topics = response.iterateAll();
		  for (Topic topic : topics) 
			 System.out.println(topic);
		 

		} 
		catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}
	    
}
