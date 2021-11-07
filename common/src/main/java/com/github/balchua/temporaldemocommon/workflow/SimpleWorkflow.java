package com.github.balchua.temporaldemocommon.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SimpleWorkflow {
    @WorkflowMethod
    String doSomething(String name);
}
