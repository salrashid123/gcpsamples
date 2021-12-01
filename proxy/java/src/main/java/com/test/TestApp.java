package com.test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.SocketAddress;

import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.core.ApiFunction;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.TransportOptions;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient.ListTopicsPagedResponse;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ListTopicsRequest;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.w3c.dom.UserDataHandler;

import io.grpc.HttpConnectProxiedSocketAddress;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ProxiedSocketAddress;
import io.grpc.ProxyDetector;


/*

unsure why the first set is denied

1.user
  mvn clean install exec:java -q

auth Y
gcs Y
pubsub Y


1638368638.824      0 192.168.9.1 TCP_DENIED/407 4000 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html
1638368639.059      0 192.168.9.1 TCP_DENIED/407 4004 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
1638368640.852   1792 192.168.9.1 TCP_TUNNEL/200 162251 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
1638368640.852    711 192.168.9.1 TCP_TUNNEL/200 7827 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -
1638368640.853   2021 192.168.9.1 TCP_TUNNEL/200 7064 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/172.217.9.202 -

2. service account
 mvn clean install exec:java -q

auth Y
gcs Y
pubsub Y


1638368545.910      0 192.168.9.1 TCP_DENIED/407 4000 CONNECT oauth2.googleapis.com:443 - HIER_NONE/- text/html
1638368546.080      0 192.168.9.1 TCP_DENIED/407 4004 CONNECT storage.googleapis.com:443 - HIER_NONE/- text/html
1638368547.913   1996 192.168.9.1 TCP_TUNNEL/200 6282 CONNECT oauth2.googleapis.com:443 user1 HIER_DIRECT/172.217.9.202 -
1638368547.913   1832 192.168.9.1 TCP_TUNNEL/200 162251 CONNECT storage.googleapis.com:443 user1 HIER_DIRECT/142.250.81.208 -
1638368547.913    763 192.168.9.1 TCP_TUNNEL/200 7826 CONNECT pubsub.googleapis.com:443 user1 HIER_DIRECT/142.251.33.202 -

*/


public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}

	private ApacheHttpTransport mHttpTransport;
	private GoogleCredentials credential;

	public TestApp() {
		try {


			HttpHost proxy = new HttpHost("127.0.0.1", 3128);

			org.apache.http.client.CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
					new UsernamePasswordCredentials("user1", "user1"));
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			clientBuilder.useSystemProperties();
			clientBuilder.setProxy(proxy);
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

			CloseableHttpClient httpClient = clientBuilder.build();

			mHttpTransport = new ApacheHttpTransport(httpClient);

			HttpTransportFactory hf = new HttpTransportFactory() {
				@Override
				public HttpTransport create() {
					return mHttpTransport;
				}
			};

			credential = GoogleCredentials.getApplicationDefault(hf);

			TransportOptions options = HttpTransportOptions.newBuilder().setHttpTransportFactory(hf).build();
			Storage storage_service = StorageOptions.newBuilder().setCredentials(credential)
					.setTransportOptions(options)
					.build()
					.getService();
			for (Bucket b : storage_service.list().iterateAll()) {
				System.out.println(b);
			}

			GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(creds);

			TransportChannelProvider channelProvider = TopicAdminSettings.defaultGrpcTransportProviderBuilder()
					.setChannelConfigurator(new ApiFunction<ManagedChannelBuilder, ManagedChannelBuilder>() {
						@Override
						public ManagedChannelBuilder apply(ManagedChannelBuilder managedChannelBuilder) {
							return managedChannelBuilder.proxyDetector(
									new ProxyDetector() {
										@Override
										public ProxiedSocketAddress proxyFor(SocketAddress socketAddress)
												throws IOException {
											return HttpConnectProxiedSocketAddress.newBuilder()
													.setUsername("user1")
													.setPassword("user1")
													.setProxyAddress(new InetSocketAddress("localhost", 3128))
													.setTargetAddress((InetSocketAddress) socketAddress)
													.build();
										}
									});
						}
					})
					.build();

			TopicAdminClient topicClient = TopicAdminClient.create(
					TopicAdminSettings.newBuilder()
							.setTransportChannelProvider(channelProvider)
							.setCredentialsProvider(credentialsProvider)
							.build());

			ListTopicsRequest listTopicsRequest = ListTopicsRequest.newBuilder()
					.setProject(ProjectName.format("your-project"))
					.build();
			ListTopicsPagedResponse response = topicClient.listTopics(listTopicsRequest);
			Iterable<Topic> topics = response.iterateAll();
			for (Topic topic : topics)
				System.out.println(topic);

		} catch (Exception ex) {
			System.out.println("Error:  " + ex);
		}
	}

}