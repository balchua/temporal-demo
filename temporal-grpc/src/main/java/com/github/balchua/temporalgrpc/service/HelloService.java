package com.github.balchua.temporalgrpc.service;

import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.protos.HelloReply;
import com.github.balchua.protos.HelloRequest;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.stereotype.Component;

@GRpcService
@Component
@Slf4j
public class HelloService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

        log.info("saying hello");

        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
