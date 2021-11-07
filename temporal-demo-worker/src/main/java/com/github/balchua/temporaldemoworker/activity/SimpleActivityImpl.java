package com.github.balchua.temporaldemoworker.activity;

import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.protos.HelloReply;
import com.github.balchua.protos.HelloRequest;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SimpleActivityImpl implements SimpleActivity {

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private Call.Factory callFactory;

    @Autowired
    private GreeterGrpc.GreeterBlockingStub blockingStub;

    private static final Metadata.Key<String> X_B3_TRACEID =
            Metadata.Key.of("X-B3-TraceId", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> X_B3_PARENTID =
            Metadata.Key.of("X-B3-ParentSpanId", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> X_B3_SPANID =
            Metadata.Key.of("X-B3-SpanId", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> X_B3_SAMPLED =
            Metadata.Key.of("X-B3-sampled", Metadata.ASCII_STRING_MARSHALLER);

    private static final String BASE_URL = "http://localhost:8099/sink/api/v1/";

    private Request setupRequest() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/finalAction").newBuilder();
        String url = urlBuilder.build().toString();
        Request.Builder builder = new Request.Builder()
                .url(url);
        if (!StringUtils.isEmpty(MDC.get("traceId"))) {
            builder.addHeader("X-B3-TraceId", MDC.get("traceId"));
        }
        if (!StringUtils.isEmpty(MDC.get("parentId"))) {
            builder.addHeader("X-B3-ParentSpanId", MDC.get("parentId"));
        }
        if (!StringUtils.isEmpty(MDC.get("spanId"))) {
            builder.addHeader("X-B3-SpanId", MDC.get("spanId"));
        }
        if (!StringUtils.isEmpty(MDC.get("sampled"))) {
            builder.addHeader("X-B3-sampled", MDC.get("sampled"));
        }

        Request request = builder.build();
        return request;
    }

    @Override
    public String simpleAct(String name) {
        log.info("This is doing something simple.");

        Call call = callFactory.newCall(setupRequest());
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            log.error("Unable to access sink endpoint {}", e);
        }

        return "response not ok";
    }

    @Override
    public String grpcCall(String arg) {
        log.info("calling grpc");
        Metadata b3Headers = addB3GrpcHeaders();
        blockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(b3Headers));
        HelloReply reply = blockingStub.sayHello(HelloRequest.newBuilder().setName(arg).build());
        log.info("done calling grpccall");
        return reply.getMessage();
    }

    public Metadata addB3GrpcHeaders() {
        Metadata headers = new Metadata();
        String traceId = MDC.get("traceId");
        String parentId = MDC.get("parentId");
        String spanId = MDC.get("spanId");
        String sampled = MDC.get("sampled");

        if (!StringUtils.isEmpty(traceId)) {
            headers.put(X_B3_TRACEID, traceId);
        }
        if (!StringUtils.isEmpty(parentId)) {
            headers.put(X_B3_PARENTID, parentId);
        }
        if (!StringUtils.isEmpty(spanId)) {
            headers.put(X_B3_SPANID, spanId);
        }
        if (!StringUtils.isEmpty(sampled)) {
            headers.put(X_B3_SAMPLED, sampled);
        }
        return headers;
    }


}
