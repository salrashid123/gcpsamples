/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// Package main contains a program that demonstrates various Google Cloud Storage Signed URL operations.
// More information can be found at
// https://cloud.google.com/storage/docs/accesscontrol#Signed-URLs
// http://godoc.org/google.golang.org/cloud/storage#SignedURL
// Replace googleAccessID,serviceAccountPEMFilename,bucket constants
// Note: default bucketname for App Engine projects is formatted as: <app_id>.appspot.com
// Convert your PKCS12 private key file to PEM:
//
//    openssl pkcs12 -in key.p12 -passin pass:notasecret -out key.pem -nodes
//
package main

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"time"

	"google.golang.org/cloud/storage"
)

const (
	googleAccessID            = "<serviceAccountEmail>@developer.gserviceaccount.com"
	serviceAccountPEMFilename = "YOUR_SERVICE_ACCOUNT_KEY.pem"
	bucket                    = "YOURBUCKET"
	object                    = "somerandomfile.txt"
)

var (
	expiration = time.Now().Add(time.Second * 60) //expire in 60 seconds
)

func main() {
	data, err := ioutil.ReadFile(serviceAccountPEMFilename)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("Putting the object")
	opts := &storage.SignedURLOptions{
		ClientID:   googleAccessID,
		PrivateKey: data,
		Method:     "PUT",
		Expires:    expiration,
	}

	putURL, err := storage.SignedURL(bucket, object, opts)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("PUT URL : %v\n", putURL)

	client := &http.Client{}
	var payload = []byte("Lorem Ipsum")
	req, err := http.NewRequest("PUT", putURL, bytes.NewBuffer(payload))
	res, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	res.Body.Close()
	fmt.Printf("Response Code: %s\n", res.Status)

	fmt.Println("Getting the object")
	opts = &storage.SignedURLOptions{
		ClientID:   googleAccessID,
		PrivateKey: data,
		Method:     "GET",
		Expires:    expiration,
	}

	getURL, err := storage.SignedURL(bucket, object, opts)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("GET URL : %v\n", getURL)

	res, err = http.Get(getURL)
	if err != nil {
		log.Fatal(err)
	}

	body, err := ioutil.ReadAll(res.Body)
	if err != nil {
		log.Fatal(err)
	}
	res.Body.Close()
	fmt.Printf("Response Code: %s\n", res.Status)
	fmt.Printf("%s\n", body)

	fmt.Println("Deleting the object")
	opts = &storage.SignedURLOptions{
		ClientID:   googleAccessID,
		PrivateKey: data,
		Method:     "DELETE",
		Expires:    expiration,
	}

	deleteURL, err := storage.SignedURL(bucket, object, opts)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("DELETE URL : %v\n", deleteURL)
	req, err = http.NewRequest("DELETE", deleteURL, nil)
	res, err = client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	res.Body.Close()
	fmt.Printf("Response Code: %s\n", res.Status)

}
