package com.github.balchua.temporaldemocommon.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TraceContext {

    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("spanId")
    private String spanId;
    @JsonProperty("parentId")
    private String parentId;
    @JsonProperty("sampled")
    private String sampled;

    public static TraceContext toTraceContext(String uberTraceId) {
        String[] traceContextInStr = uberTraceId.split(":");
        TraceContext tracingContext = new TraceContext(traceContextInStr[0], traceContextInStr[1], traceContextInStr[2], traceContextInStr[3]);
        return tracingContext;
    }

    public static TraceContext fromMDC(){
        String traceId = MDC.get("traceId");
        String parentId = MDC.get("parentId");
        String spanId = MDC.get("spanId");
        String sampled = MDC.get("sampled");
        TraceContext traceContext = TraceContext.builder().traceId(traceId).spanId(spanId).parentId(parentId).sampled(sampled).build();
        return traceContext;
    }

    public void populateMDC() {
        MDC.put("traceId", String.valueOf(this.getTraceId()));
        MDC.put("parentId", String.valueOf(this.getParentId()));
        MDC.put("spanId", String.valueOf(this.getSpanId()));
        MDC.put("sampled", String.valueOf(this.getSampled()));
    }
}
