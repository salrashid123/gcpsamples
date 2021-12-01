module main

go 1.13

require (
	echo v0.0.0
	github.com/coreos/go-oidc v2.2.1+incompatible
	github.com/golang-jwt/jwt

	github.com/google/go-tpm v0.2.0 // indirect
	github.com/googleapis/gax-go v2.0.2+incompatible // indirect
	github.com/gorilla/mux v1.7.4
	github.com/hashicorp/vault/api v1.0.4 // indirect
	github.com/lestrrat/go-jwx v0.0.0-20180221005942-b7d4802280ae
	github.com/lestrrat/go-pdebug v0.0.0-20180220043741-569c97477ae8 // indirect
	github.com/pkg/errors v0.9.1 // indirect
	github.com/pquerna/cachecontrol v0.0.0-20180517163645-1555304b9b35 // indirect
	github.com/salrashid123/oauth2 v0.0.0-20200306182411-2f0ea7dcf344
	golang.org/x/net v0.0.0-20200324143707-d3edc9973b7e
	golang.org/x/oauth2 v0.0.0-20200107190931-bf48bf16ab8d
	google.golang.org/api v0.21.0 // indirect
	google.golang.org/grpc v1.28.1
	gopkg.in/square/go-jose.v2 v2.4.1 // indirect
)

replace echo => ./src/echo
