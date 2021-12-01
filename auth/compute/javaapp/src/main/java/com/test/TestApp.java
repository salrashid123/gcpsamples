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

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	public TestApp() {
		try
		{

          // Using Google Cloud APIs
		  Storage storage_service = StorageOptions.newBuilder()
			.build()
			.getService();	
		  for (Bucket b : storage_service.list().iterateAll()){
			  System.out.println(b);
		  }

		  TopicAdminClient topicClient =
			  TopicAdminClient.create(TopicAdminSettings.newBuilder().build());

		  ListTopicsRequest listTopicsRequest =
							ListTopicsRequest.newBuilder().build();
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
