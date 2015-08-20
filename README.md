## Google Cloud Platform Samples

#### all samples here are provided as-is without warranty

Sample code demonstrating various Google Cloud Platform APIs.

Please refer to official documentation for usage and additional samples/usage.

Code samples contained in this repo contain:

  * Google Cloud Storage  
    * Basic CRUD in C# and Go  
    * SignedURL in Java, C# and Go  
  * BigQuery
    * Basic query against public dataset in C# and Go.  
  * CloudLogging
    * Listing and inserting custom log messages in C# and Go.  
  * CloudMonitoring
    * Listing MetricsDescriptors and inserting custom metric in C# and Go.  

***  

### Application Default Credentials

The samples use [Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials) which uses credentials in the following order as described in the link.  Set the environment variable to override.  

You can always specify the target source to acquire credentials by using intent specific targets such as:  ComputeCredentials, UserCredentials or ServiceAccountCredential.