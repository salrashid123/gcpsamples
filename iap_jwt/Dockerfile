FROM golang:1.14 as build

RUN apt-get update -y && apt-get install -y build-essential wget unzip curl


RUN curl -OL https://github.com/google/protobuf/releases/download/v3.2.0/protoc-3.2.0-linux-x86_64.zip && \
    unzip protoc-3.2.0-linux-x86_64.zip -d protoc3 && \
    mv protoc3/bin/* /usr/local/bin/ && \
    mv protoc3/include/* /usr/local/include/


ENV GO111MODULE=on
RUN go get -u github.com/golang/protobuf/protoc-gen-go   

WORKDIR /app

ADD . /app

RUN go mod download


RUN /usr/local/bin/protoc -I src/ --include_imports --include_source_info --descriptor_set_out=src/echo/echo.proto.pb  --go_out=plugins=grpc:src/ src/echo/echo.proto


#RUN GRPC_HEALTH_PROBE_VERSION=v0.2.0 && \
#    wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
#    chmod +x /bin/grpc_health_probe

RUN export GOBIN=/app/bin && go install src/grpc_server.go
RUN export GOBIN=/app/bin && go install src/grpc_client.go
RUN export GOBIN=/app/bin && go install src/http_server.go

FROM gcr.io/distroless/base
COPY --from=build /app/server_crt.pem /
COPY --from=build /app/server_key.pem /
COPY --from=build /app/CA_crt.pem /
COPY --from=build /app/bin /

EXPOSE 50051

#ENTRYPOINT ["grpc_server", "--grpcport", ":50051"]
#ENTRYPOINT ["grpc_client", "--host",  "server.domain.com:50051"]
