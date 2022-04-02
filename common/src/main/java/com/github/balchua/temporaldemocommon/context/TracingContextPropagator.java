package com.github.balchua.temporaldemocommon.context;

import com.google.common.reflect.TypeToken;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.context.ContextPropagator;
import io.temporal.common.converter.DataConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TracingContextPropagator implements ContextPropagator {

    private static final String TRACING_KEY = "tracingContext";
    private static final String TRACER_HEADER_KEY = "_tracer-data";
    private static final Type HASH_MAP_STRING_STRING_TYPE =
            new TypeToken<HashMap<String, String>>() {
            }.getType();

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
        Payload payload = context.get(TRACER_HEADER_KEY);
        if (payload == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> serializedSpanContext =
                DataConverter.getDefaultInstance()
                        .fromPayload(payload, HashMap.class, HASH_MAP_STRING_STRING_TYPE);

        if (serializedSpanContext != null) {
            log.info("{}", serializedSpanContext.get("uber-trace-id"));
            TraceContext tracingContext = new TraceContext();
            String[] traceContextInStr = serializedSpanContext.get("uber-trace-id").split(":");
            tracingContext.setTraceId(traceContextInStr[0]);
            tracingContext.setSpanId(traceContextInStr[1]);
            tracingContext.setParentId(traceContextInStr[2]);
            tracingContext.setSampled(traceContextInStr[3]);
            return tracingContext;
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


