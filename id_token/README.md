## ID Token


The following seciton details how to create an verify various id_tokens.  These tokens comes in various flavors:

* google service account id_tokens
* end user id_tokens
* google-issued id_tokens

and to mention (jwt access_tokens):

- golang:  [https://godoc.org/golang.org/x/oauth2/google#JWTAccessTokenSourceFromJSON](https://godoc.org/golang.org/x/oauth2/google#JWTAccessTokenSourceFromJSON)


Please also see [Using Service Account Actor for Account Impersonation](../auth/tokens)


## Service Account id_token

- python:see:  [service.py](../auth/tokens/gcs_auth.py) as well as [google.auth](https://google-auth.readthedocs.io/en/latest/reference/google.oauth2.service_account.html#module-google.oauth2.service_account)


- golang:  see [main.go](golang/main.go)


## Google-id-token

- golang:  see [main.go](golang/main.go)


## Verify tokens

 - [https://developers.google.com/identity/sign-in/web/backend-auth#verify-the-integrity-of-the-id-token](https://developers.google.com/identity/sign-in/web/backend-auth#verify-the-integrity-of-the-id-token) 


### Google Id Tokens
 You can verify google issued id tokens by checking the 


### Service account JWT
