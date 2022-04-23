package com.github.balchua.temporaldemocommon.interceptor;

import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;

public class SimpleWorkflowInboundCallsInterceptor extends WorkflowInboundCallsInterceptorBase {
    public SimpleWorkflowInboundCallsInterceptor(WorkflowInboundCallsInterceptor next) {
        super(next);
    }
}
