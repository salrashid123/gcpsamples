package com.test;

import java.util.List;

import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.ProjectName;

import com.google.api.client.http.HttpTransport;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.google.auth.http.HttpTransportFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;

import org.apache.http.message.BasicHeader;

import com.google.auth.oauth2.GoogleCredentials;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.params.ConnRoutePNames;
import java.io.IOException;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.GoogleCredentialsProvider;
import java.util.Arrays;

import com.google.auth.Credentials;

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}
		
	private ApacheHttpTransport mHttpTransport;
	private GoogleCredentials credential;

	public TestApp()  {

    String projectId = ServiceOptions.getDefaultProjectId();
	try {

		//export GRPC_PROXY_EXP=localhost:3128
		HttpHost proxy = new HttpHost("127.0.0.1",3128);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
					
		httpClient.addRequestInterceptor(new HttpRequestInterceptor(){            
			@Override
			public void process(org.apache.http.HttpRequest request, HttpContext context) throws HttpException, IOException {
					//if (request.getRequestLine().getMethod().equals("CONNECT"))                 
					//   request.addHeader(new BasicHeader("Proxy-Authorization","Basic dXNlcjE6dXNlcjE="));
				}
			});
		
		mHttpTransport =  new ApacheHttpTransport(httpClient);		

		HttpTransportFactory hf = new HttpTransportFactory(){
			@Override
			public HttpTransport create() {
				return mHttpTransport;
			}
		};            
		
		credential = GoogleCredentials.getApplicationDefault(hf);

		CredentialsProvider credentialsProvider =  new GoogleCredentialsProvider(){
			public List<String> getScopesToApply(){
				return Arrays.asList("https://www.googleapis.com/auth/pubsub");
			   }

			public Credentials getCredentials()  {
				return credential;
			}
		};

		TopicAdminSettings topicAdminSettings =
		     TopicAdminSettings.newBuilder().setCredentialsProvider(credentialsProvider)
				 .build();
				 
		 TopicAdminClient topicAdminClient =
		     TopicAdminClient.create(topicAdminSettings);
		
		//TopicAdminClient topicAdminClient = TopicAdminClient.create();
		ProjectName project = ProjectName.create(projectId);
		for (Topic element : topicAdminClient.listTopics(project).iterateAll()) 
	  		System.out.println(element.getName());
	
	} catch (Exception ex) 
	{
		System.out.println("ERROR " + ex);
	}
  }   
		
}
