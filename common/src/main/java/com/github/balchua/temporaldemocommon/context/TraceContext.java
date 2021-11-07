package com.github.balchua.temporaldemocommon.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraceContext {

    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("parentId")
    private String parentId;
    @JsonProperty("spanId")
    private String spanId;
    @JsonProperty("sampled")
    private String sampled;
}
