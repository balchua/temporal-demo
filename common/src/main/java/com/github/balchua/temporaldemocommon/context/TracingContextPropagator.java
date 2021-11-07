package com.github.balchua.temporaldemocommon.context;

import io.temporal.api.common.v1.Payload;
import io.temporal.common.context.ContextPropagator;
import io.temporal.common.converter.DataConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

@Slf4j
public class TracingContextPropagator implements ContextPropagator {

    private static final String TRACING_KEY = "tracingContext";

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Map<String, Payload> serializeContext(Object context) {
        TraceContext testKey = (TraceContext) context;
        if (testKey != null) {
            return Collections.singletonMap(
                    TRACING_KEY, DataConverter.getDefaultInstance().toPayload(testKey).get());
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Object deserializeContext(Map<String, Payload> context) {
        if (context.containsKey(TRACING_KEY)) {
            return DataConverter.getDefaultInstance()
                    .fromPayload(context.get(TRACING_KEY), String.class, TraceContext.class);

        } else {
            return null;
        }
    }

    @Override
    public Object getCurrentContext() {
        String traceId = MDC.get("traceId");
        String parentId = MDC.get("parentId");
        String spanId = MDC.get("spanId");
        String sampled = MDC.get("sampled");
        if (StringUtils.isEmpty(parentId)) {
            parentId = spanId;
        }
        TraceContext traceContext = new TraceContext(traceId, parentId, spanId, sampled);
        return traceContext;
    }

    @Override
    public void setCurrentContext(Object context) {
        TraceContext traceContext = (TraceContext) context;
        if (traceContext != null) {
            MDC.put("traceId", String.valueOf(traceContext.getTraceId()));
            MDC.put("parentId", String.valueOf(traceContext.getParentId()));
            MDC.put("spanId", String.valueOf(traceContext.getSpanId()));
            MDC.put("sampled", String.valueOf(traceContext.getSampled()));
        }
    }
}


