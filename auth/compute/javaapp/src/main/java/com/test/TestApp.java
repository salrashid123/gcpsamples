package com.test;

import java.util.Arrays;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;

import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.api.gax.rpc.FixedTransportChannelProvider;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

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

/*
// for Google APIs, uncomment the following section and comment out the Cloud API section in pom.xml and above
import java.util.Collection;
import java.util.Iterator;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;
*/

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{
			/*
			// For GoogleAPIs
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
			*/

          // Using Google Cloud APIs
		  Storage storage_service = StorageOptions.newBuilder()
			.build()
			.getService();	
		  for (Bucket b : storage_service.list().iterateAll()){
			  System.out.println(b);
		  }

          // String cred_file = "/path/to/cred.json";
		  //GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(cred_file));	
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
