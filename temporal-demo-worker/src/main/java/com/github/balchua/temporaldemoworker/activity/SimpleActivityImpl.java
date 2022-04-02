package com.github.balchua.temporaldemoworker.activity;

import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.protos.HelloReply;
import com.github.balchua.protos.HelloRequest;
import com.github.balchua.temporaldemocommon.context.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SimpleActivityImpl implements SimpleActivity {

    @Autowired
    private Call.Factory callFactory;

    @Autowired
    private GreeterGrpc.GreeterBlockingStub blockingStub;

    @Autowired
    private OpenTelemetry otel;

    private static final String BASE_URL = "http://localhost:8099/sink/api/v1/";

    private Request setupRequest() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/finalAction").newBuilder();
        String url = urlBuilder.build().toString();
        Request.Builder builder = new Request.Builder()
                .url(url);

        Request request = builder.build();
        return request;
    }

    @Override
    public String simpleAct(String name) {
        var tracer = otel.getTracer("simple-activity");
        log.info("This is doing something simple.");
        var traceContext = TraceContext.fromMDC();
        byte sampled = 1;
        SpanContext parentContext =
                SpanContext.createFromRemoteParent(
                        TraceId.fromBytes(traceContext.getTraceId().getBytes(StandardCharsets.UTF_8)),
                        SpanId.fromBytes(traceContext.getSpanId().getBytes(StandardCharsets.UTF_8)),
                        TraceFlags.fromByte(sampled),
                        TraceState.builder().build());

        var parentSpan =
                tracer.spanBuilder("simpleAct").setNoParent().addLink(parentContext).startSpan();

        try {
            Call call = callFactory.newCall(setupRequest());
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            log.error("Unable to access sink endpoint {}", e);
        } finally {
            parentSpan.end();
        }

        return "response not ok";
    }

    @Override
    public String grpcCall(String arg) {
        var tracer = otel.getTracer("simple-activity");
        log.info("calling grpc");
        String response = "";
        byte sampled = 1;

        var traceContext = TraceContext.fromMDC();
        SpanContext parentContext =
                SpanContext.createFromRemoteParent(
                        TraceId.fromBytes(traceContext.getTraceId().getBytes(StandardCharsets.UTF_8)),
                        SpanId.fromBytes(traceContext.getSpanId().getBytes(StandardCharsets.UTF_8)),
                        TraceFlags.fromByte(sampled),
                        TraceState.builder().build());

        var parentSpan =
                tracer.spanBuilder("grpcCall").setNoParent().addLink(parentContext).startSpan();
        HelloReply reply = blockingStub.sayHello(HelloRequest.newBuilder().setName(arg).build());
        log.info("done calling grpccall");
        response = reply.getMessage();
        parentSpan.end();
        return response;
    }

}
