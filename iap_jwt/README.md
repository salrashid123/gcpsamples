## Google Identity Aware proxy IAP Token Verification in Golang

Sample http and grpc applications which validates the inbound JWT token sent by [Identity Aware Proxy](https://cloud.google.com/iap/docs/).

Included here are:

* `src/http_server.go`:  HTTP server that uses gorilla mux middleware to check and validate the header
* `src/grpc_server.go`: Simple gRPC server which uses a Unary auth Interceptor 

These applications are designed to run as the backend service after IAP's validation is done.  It is recommended to recheck the headers in all cases so this sample in golang supplements the existing language snippets here:

* [Securing your app with IAP headers](https://cloud.google.com/iap/docs/signed-headers-howto#securing_iap_headers)


To test these samples locally,

First acquire an IAP auth token and your client_id by using [Programmatic Authentication](https://cloud.google.com/iap/docs/authentication-howto#authenticating_from_a_service_account).


Once you have the token, edit `http_server.go` (for HTTP traffic) or `grpc_server.go` and modify the audience value.

For HTTP traffic, run

```bash
# server
go run src/http_server.go

# client 
export TOKEN=your_iap_token
curl -v -H "x-goog-iap-jwt-assertion: $TOKEN" http://localhost:8080/
```

for gRPC, run

```bash
# server
go run src/grpc_server.go

# client
export TOKEN=your_iap_token
go run src/grpc_client.go --token=$TOKEN
```



Notes:
* IAP's JWT header is signed `ECDSA` where the public JWK endpoint is at  `https://www.gstatic.com/iap/verify/public_key-jwk`.
* [Envoy for Google Cloud Identity Aware Proxy](https://medium.com/google-cloud/envoy-for-google-cloud-identity-aware-proxy-ee0a77200fd5)


