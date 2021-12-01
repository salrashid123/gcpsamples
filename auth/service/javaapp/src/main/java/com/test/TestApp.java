package com.test;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminSettings;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;
import java.io.FileInputStream;
public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}

	public TestApp() {
		try {

			// with file
			GoogleCredentials sourceCredentials = GoogleCredentials.fromStream(new FileInputStream("/path/to/svc_account.json"));

			// with export GOOGLE_APPLICATION_CREDENTIALS=/path/to/svc_account.json
			//GoogleCredentials sourceCredentials = GoogleCredentials.getApplicationDefault();
			Storage storage_service = StorageOptions.newBuilder().setCredentials(sourceCredentials).build().getService();
			for (Bucket b : storage_service.list().iterateAll()) {
				System.out.println(b);
			}

			
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(sourceCredentials);
			/// ManagedChannel channel =
			/// ManagedChannelBuilder.forTarget("pubsub.googleapis.com:443").build();
			// TransportChannelProvider channelProvider =
			/// FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

			TransportChannelProvider channelProvider = TopicAdminSettings.defaultTransportChannelProvider();

			TopicAdminClient topicClient = TopicAdminClient.create(TopicAdminSettings.newBuilder()
					.setTransportChannelProvider(channelProvider).setCredentialsProvider(credentialsProvider).build());

			ListTopicsRequest listTopicsRequest = ListTopicsRequest.newBuilder()
					.setProject(ProjectName.format("fabled-ray-104117")).build();
			ListTopicsPagedResponse response = topicClient.listTopics(listTopicsRequest);
			Iterable<Topic> topics = response.iterateAll();
			for (Topic topic : topics)
				System.out.println(topic);

		} catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}

}
