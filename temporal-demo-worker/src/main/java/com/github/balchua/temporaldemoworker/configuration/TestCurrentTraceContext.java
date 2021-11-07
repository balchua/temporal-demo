package com.github.balchua.temporaldemoworker.configuration;

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.math.BigInteger;

public class TestCurrentTraceContext extends CurrentTraceContext {
    @Override
    public TraceContext get() {
        String traceId = MDC.get("traceId");
        String parentId = MDC.get("parentId");
        String spanId = MDC.get("spanId");
        TraceContext.Builder builder = TraceContext.newBuilder();
        if (!StringUtils.isEmpty(traceId)) {
            builder.traceId(new BigInteger(traceId, 16).longValue());
        }
        if (!StringUtils.isEmpty(parentId)) {
            builder.parentId(new BigInteger(parentId, 16).longValue());
        }
        if (!StringUtils.isEmpty(spanId)) {
            builder.spanId(new BigInteger(spanId, 16).longValue());
        }

        return builder.build();
    }

    @Override
    public Scope newScope(TraceContext traceContext) {
        return null;
    }
}
