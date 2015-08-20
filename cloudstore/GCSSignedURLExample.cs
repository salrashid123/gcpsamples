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

/*
Demonstrates various Google Cloud Storage Signed URL operations
https://cloud.google.com/storage/docs/accesscontrol#Signed-URLs

Replace SERVICE_ACCOUNT_EMAIL,SERVICE_ACCOUNT_PKCS12_FILE_PATH,BUCKET_NAME incode below

"Microsoft Enhanced RSA and AES Cryptographic Provider"
Requires:  Windows Vista+,Windows Server 2008+
http://msdn.microsoft.com/en-us/library/windows/desktop/bb931357(v=vs.85).aspx
HKEY_LOCAL_MACHINE\Software\Microsoft\Cryptography\Defaults\Providers 
MS_ENH_RSA_AES_PROV
Windows XP:  "Microsoft Enhanced RSA and AES Cryptographic Provider (Prototype)"

*/

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Collections.Specialized;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Security;
using System.Net;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace GCSSignedURLExample
{
    internal class GCSSignedURLExample
    {

        String SERVICE_ACCOUNT_EMAIL = "<SERVICE_ACCOUNT_EMAIL>@developer.gserviceaccount.com";
        String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "c:\\SERVICE_ACCOUNT_PKCS12_FILE_PATH";
        long expiration = Convert.ToInt64((DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalSeconds) + 60;

        String BUCKET_NAME = "YOUR_BUCKET_NAME";
        String OBJECT_NAME = "somerandomfile.txt";

        X509Certificate2 key;

        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                new GCSSignedURLExample().Run();
            }
            catch (AggregateException ex)
            {
                foreach (var e in ex.InnerExceptions)
                    Console.WriteLine("Error: " + e.Message);
            }
            Console.ReadLine();
        }

        private void Run()
        {
             try
             {
                 key = new X509Certificate2(SERVICE_ACCOUNT_PKCS12_FILE_PATH, "notasecret");
                 Console.WriteLine("======= PUT File =========");
                 String put_url = this.getSigningURL("PUT");
                 string payload = "Lorem ipsum";

                 HttpWebRequest request = (HttpWebRequest)HttpWebRequest.Create(put_url);
                 request.Method = "PUT";
                 byte[] byte1 = new UTF8Encoding().GetBytes(payload);
                 using (Stream reqStream = request.GetRequestStream())
                 {
                     reqStream.Write(byte1, 0, byte1.Length);
                     Console.WriteLine(request.Method + " " + request.Host + request.RequestUri.PathAndQuery);
                     renderResponse((HttpWebResponse)request.GetResponse());
                 }

                 Console.WriteLine("======= GET File =========");
                 String get_url = this.getSigningURL("GET");
                 request = (HttpWebRequest)HttpWebRequest.Create(get_url);
                 request.Method = "GET";
                 Console.WriteLine(request.Method + " " + request.Host + request.RequestUri.PathAndQuery);
                 Console.WriteLine(renderResponse((HttpWebResponse)request.GetResponse()));

                 Console.WriteLine("======= DELETE File =========");
                 String delete_url = this.getSigningURL("DELETE");
                 request = (HttpWebRequest)HttpWebRequest.Create(delete_url);
                 request.Method = "DELETE";
                 Console.WriteLine(request.Method + " " + request.Host + request.RequestUri.PathAndQuery);
                 Console.WriteLine(renderResponse((HttpWebResponse)request.GetResponse()));
             }
             catch (WebException ex)
             {
                 if (ex.Status == WebExceptionStatus.ProtocolError)
                 {
                     HttpStatusCode statusCode = ((HttpWebResponse)ex.Response).StatusCode;
                     string statusDescription = ((HttpWebResponse)ex.Response).StatusDescription;
                     Console.WriteLine("HTTP Error: " + statusCode + " " + statusDescription);
                 }
             }
             catch (Exception ex)
             {
                 Console.WriteLine("Exception " + ex);
             }

        }

        private String renderResponse(HttpWebResponse response)
        {
            try
            {
                String responseText = String.Empty;
                Console.WriteLine("Response Code: " + response.StatusCode);
                using (TextReader tr = new StreamReader(response.GetResponseStream()))
                {
                    responseText = tr.ReadToEnd();
                }
                return responseText;
            }
            catch (IOException ex)
            {
                Console.WriteLine("Error processing Stream " + ex);
                return null;
            }

        }

    private String getSigningURL(String verb){
        String url_signature = this.signString(
            String.Format("{0}\n\n\n{1}\n/{2}/{3}",
                verb,
                expiration,
                BUCKET_NAME,
                OBJECT_NAME
                )
            );
         String signed_url = String.Format(
             "https://storage.googleapis.com/{0}/{1}?GoogleAccessId={2}&Expires={3}&Signature={4}",
             BUCKET_NAME,
             OBJECT_NAME,
             SERVICE_ACCOUNT_EMAIL,
             expiration,
             Uri.EscapeDataString(url_signature)
             );
         return signed_url;
    }

    private String signString(String stringToSign)  {
        if (key == null)
            throw new Exception("Certificate not initialized");
        CspParameters cp = new CspParameters(24, "Microsoft Enhanced RSA and AES Cryptographic Provider",
                ((RSACryptoServiceProvider)key.PrivateKey).CspKeyContainerInfo.KeyContainerName);
        RSACryptoServiceProvider provider = new RSACryptoServiceProvider(cp);
        byte[] buffer = Encoding.UTF8.GetBytes(stringToSign);
        byte[] signature = provider.SignData(buffer, "SHA256");
        return Convert.ToBase64String(signature);
    }

    }
}
