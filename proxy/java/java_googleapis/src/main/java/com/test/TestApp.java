package com.test;

import java.io.IOException;
import java.util.logging.Logger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;



import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;

/*
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.StorageScopes;
*/

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.google.auth.http.HttpTransportFactory;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.auth.AuthScope;

import org.apache.http.message.BasicHeader;
import java.net.Proxy;
import java.net.InetSocketAddress;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.TransportOptions;
import com.google.cloud.http.HttpTransportOptions;
import com.google.cloud.grpc.GrpcTransportOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import java.util.Arrays;

public class TestApp {
    public static void main(String[] args) {
        TestApp tc = new TestApp();
    }

    private ApacheHttpTransport mHttpTransport;

    public TestApp() {
        try
        {
            
            /*
          JacksonFactory jsonFactory = new JacksonFactory(); 
              
            Authenticator.setDefault(
                new Authenticator() {
                   @Override
                   public PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(
                            "user1", "user1".toCharArray());
                   }
                }
             );                          
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 3128));
            NetHttpTransport mHttpTransport = new NetHttpTransport.Builder().setProxy(proxy).build();
            */
           

            HttpHost proxy = new HttpHost("127.0.0.1",3128);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            
            httpClient.addRequestInterceptor(new HttpRequestInterceptor(){            
                @Override
                public void process(org.apache.http.HttpRequest request, HttpContext context) throws HttpException, IOException {
                    //if (request.getRequestLine().getMethod().equals("CONNECT"))                 
                    //  request.addHeader(new BasicHeader("Proxy-Authorization","Basic dXNlcjE6dXNlcjE="));
                }
            });

           mHttpTransport =  new ApacheHttpTransport(httpClient);



/*
            com.google.api.client.googleapis.auth.oauth2.GoogleCredential credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential.getApplicationDefault(mHttpTransport,jsonFactory);
            if (credential.createScopedRequired())
                credential = credential.createScoped(Arrays.asList(StorageScopes.DEVSTORAGE_READ_ONLY));

            com.google.api.services.storage.Storage service = new com.google.api.services.storage.Storage.Builder(mHttpTransport, jsonFactory, credential)
                .setApplicationName("oauth client")   
                .build(); 
                
            com.google.api.services.storage.model.Buckets dl = service.buckets().list("mineral-minutia-820").execute();
            for (com.google.api.services.storage.model.Bucket bucket: dl.getItems()) 
                System.out.println(bucket.getName());
*/

            
           // System.setProperty("https.proxyHost", "localhost");
           // System.setProperty("https.proxyPort", "3128");
/*
            Authenticator.setDefault(
                new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            "user1", "user1".toCharArray());
                }
                }
            );
*/


            HttpTransportFactory hf = new HttpTransportFactory(){
                @Override
                public HttpTransport create() {
                    return mHttpTransport;
                }
            };            

            com.google.auth.oauth2.GoogleCredentials credential = com.google.auth.oauth2.GoogleCredentials.getApplicationDefault(hf);
            if (credential.createScopedRequired())
                credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/devstorage.read_write"));

            TransportOptions options = HttpTransportOptions.newBuilder().setHttpTransportFactory(hf).build();            
            com.google.cloud.storage.Storage storage = com.google.cloud.storage.StorageOptions.newBuilder()
                .setCredentials(credential)
                .setProjectId("mineral-minutia-820")
                .setTransportOptions(options)
                .build().getService();
            
            System.out.println("My buckets:");        
            for (com.google.cloud.storage.Bucket bucket : storage.list().iterateAll()) 
              System.out.println(bucket);              
          
        } 
        catch (Exception ex) {
            System.out.println("Error:  " + ex);
        }
    
    }
        
}