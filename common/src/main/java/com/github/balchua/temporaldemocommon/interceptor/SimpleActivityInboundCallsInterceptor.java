package com.github.balchua.temporaldemocommon.interceptor;

import com.github.balchua.temporaldemocommon.context.TraceContext;
import com.google.common.reflect.TypeToken;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SimpleActivityInboundCallsInterceptor extends ActivityInboundCallsInterceptorBase {
    private ActivityExecutionContext activityExecutionContext;
    private static final Type HASH_MAP_STRING_STRING_TYPE =
            new TypeToken<HashMap<String, String>>() {
            }.getType();
    public SimpleActivityInboundCallsInterceptor(ActivityInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(ActivityExecutionContext context) {
        this.activityExecutionContext = context;
        super.init(context);
    }

    @Override
    public ActivityOutput execute(ActivityInput input) {
        var context = input.getHeader().getValues();
        Payload payload = context.get("_tracer-data");
        Map<String, String> serializedSpanContext =
                DataConverter.getDefaultInstance()
                        .fromPayload(payload, HashMap.class, HASH_MAP_STRING_STRING_TYPE);

        if (serializedSpanContext != null) {
            var uberTraceId = serializedSpanContext.get("uber-trace-id");
            log.info("####### interceptor trace id: {}", uberTraceId);
            var tracingContext = TraceContext.toTraceContext(uberTraceId);
            if (tracingContext != null) {
                tracingContext.populateMDC();
            }
        }
        log.info("workflowId: {}", this.activityExecutionContext.getInfo().getWorkflowId());
        var output = super.execute(input);
        log.info("output {}", output);
        return output;
    }
}
