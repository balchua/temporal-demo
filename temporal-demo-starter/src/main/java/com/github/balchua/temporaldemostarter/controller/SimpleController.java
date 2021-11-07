package com.github.balchua.temporaldemostarter.controller;

import com.github.balchua.temporaldemocommon.common.Shared;
import com.github.balchua.temporaldemocommon.workflow.SimpleWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
@Slf4j
public class SimpleController {

    private WorkflowClient client;

    public SimpleController(WorkflowClient client) {
        this.client = client;
    }

    @PostMapping("/doAction")
    public String doAction() {
        log.info("Action from rest endpoint /doAction");
        String traceId = MDC.get("traceId");
        String parentId = MDC.get("parentId");
        String spanId = MDC.get("spanId");
        String sampled = MDC.get("sampled");
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(Shared.DEMO_TASK_QUEUE)
                .build();

        SimpleWorkflow workflow = client.newWorkflowStub(SimpleWorkflow.class, options);
        log.info("Starting workflow");
        String response = workflow.doSomething("World");

        return response;
    }

}
