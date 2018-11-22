package com.test;

import java.util.Arrays;
import java.util.Collections;
import java.io.*;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.api.gax.paging.Page;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;

import java.util.Collection;
import java.util.Iterator;
import java.io.FileInputStream;

import com.google.api.client.json.*;
import com.google.api.client.json.JsonString;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson.JacksonFactory;
import java.nio.charset.Charset;

public class TestApp {
	public static void main(String[] args) {
		TestApp tc = new TestApp();
	}

	public TestApp() {
		try {

			String cred_env = "svc-src.json";
			ServiceAccountCredentials source_credentials = ServiceAccountCredentials
					.fromStream(new FileInputStream(cred_env));
			source_credentials = (ServiceAccountCredentials) source_credentials
					.createScoped(Arrays.asList("https://www.googleapis.com/auth/iam"));

			ImpersonatedCredentials target_credentials = ImpersonatedCredentials.create(source_credentials,
							"impersonated-account@PROJECT_ID.iam.gserviceaccount.com", null,
							Arrays.asList("https://www.googleapis.com/auth/devstorage.read_only"), 3600);


			Storage storage_service = StorageOptions.newBuilder().setProjectId("fabled-ray-104117")
					.setCredentials(target_credentials).build().getService();

			for (Bucket b : storage_service.list().iterateAll()) 
				System.out.println(b.getName());
			

		} catch (Exception ex) {
			System.out.println("Error:  " + ex.getMessage());
		}
	}

}
