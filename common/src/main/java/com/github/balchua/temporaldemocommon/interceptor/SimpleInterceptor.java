package com.github.balchua.temporaldemocommon.interceptor;

import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;

public class SimpleInterceptor implements WorkerInterceptor {
    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor workflowInboundCallsInterceptor) {
        return new SimpleWorkflowInboundCallsInterceptor(workflowInboundCallsInterceptor);
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor activityInboundCallsInterceptor) {
        return new SimpleActivityInboundCallsInterceptor(activityInboundCallsInterceptor);
    }
}
