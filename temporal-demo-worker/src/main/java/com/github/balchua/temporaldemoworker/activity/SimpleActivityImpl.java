package com.github.balchua.temporaldemoworker.activity;

import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.protos.HelloReply;
import com.github.balchua.protos.HelloRequest;
import com.github.balchua.temporaldemocommon.context.TraceContext;
import com.github.balchua.temporaldemoworker.wrapper.WrapInSpan;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.temporal.activity.Activity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SimpleActivityImpl implements SimpleActivity {

    @Value("${sink-service.host}")
    private String sinkServiceHost;

    @Value("${sink-service.port}")
    private int sinkServicePort;

    @Autowired
    private Call.Factory callFactory;

    @Autowired
    private GreeterGrpc.GreeterBlockingStub blockingStub;

    private Request setupRequest() {
        var sinkService = "http://" + sinkServiceHost + ":" + sinkServicePort + "/sink/api/v1/finalAction";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(sinkService).newBuilder();
        String url = urlBuilder.build().toString();
        Request.Builder builder = new Request.Builder()
                .url(url);

        Request request = builder.build();
        return request;
    }

    @Override
    @WrapInSpan
    public String simpleAct(String name) {
        try {
            Call call = callFactory.newCall(setupRequest());
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            log.error("Unable to access sink endpoint {}", e);
            throw Activity.wrap(e);
        }
    }

    @Override
    @WrapInSpan
    public String grpcCall(String arg) {
        HelloReply reply = blockingStub.sayHello(HelloRequest.newBuilder().setName(arg).build());
        log.info("done calling grpccall");
        var response = reply.getMessage();
        return response;
    }

}
