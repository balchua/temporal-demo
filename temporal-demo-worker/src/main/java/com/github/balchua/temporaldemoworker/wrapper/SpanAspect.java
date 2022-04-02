package com.github.balchua.temporaldemoworker.wrapper;

import com.github.balchua.temporaldemocommon.context.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Aspect
@Component
@Slf4j
public class SpanAspect {
    @Autowired
    private OpenTelemetry otel;

    @Around("@annotation(WrapInSpan)")
    public Object wrapInSpan(ProceedingJoinPoint joinPoint) throws Throwable {
        var tracer = otel.getTracer("simple-activity");
        log.info("This is doing something simple.");
        var traceContext = TraceContext.fromMDC();
        byte sampled = traceContext.isSampled() ? (byte)1 : 0;
        SpanContext parentContext =
                SpanContext.createFromRemoteParent(
                        TraceId.fromBytes(traceContext.getTraceId().getBytes(StandardCharsets.UTF_8)),
                        SpanId.fromBytes(traceContext.getSpanId().getBytes(StandardCharsets.UTF_8)),
                        TraceFlags.fromByte(sampled),
                        TraceState.builder().build());

        var parentSpan =
                tracer.spanBuilder("simpleAct").setNoParent().addLink(parentContext).startSpan();

        Object proceed = joinPoint.proceed();

        parentSpan.end();
        return proceed;
    }
}
