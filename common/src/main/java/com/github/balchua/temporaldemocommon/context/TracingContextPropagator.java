package com.github.balchua.temporaldemocommon.context;

import com.google.common.reflect.TypeToken;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.context.ContextPropagator;
import io.temporal.common.converter.DataConverter;
import lombok.extern.slf4j.Slf4j;

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
            var uberTraceId = serializedSpanContext.get("uber-trace-id");
            log.info("{}", uberTraceId);
            var tracingContext = TraceContext.toTraceContext(uberTraceId);
            return tracingContext;
        } else {
            return null;
        }
    }

    @Override
    public Object getCurrentContext() {
        return TraceContext.fromMDC();
    }

    @Override
    public void setCurrentContext(Object context) {
        TraceContext traceContext = (TraceContext) context;
        if (traceContext != null) {
            traceContext.populateMDC();
        }
    }
}


