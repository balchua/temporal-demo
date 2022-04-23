package com.github.balchua.temporaldemoworker.workflow;

import com.github.balchua.temporaldemocommon.workflow.SimpleWorkflow;
import com.github.balchua.temporaldemoworker.activity.SimpleActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class SimpleWorkflowImpl implements SimpleWorkflow {

    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(300))
            .build();

    // ActivityStubs enable calls to Activities as if they are local methods, but actually perform an RPC.
    private final SimpleActivity activity = Workflow.newActivityStub(SimpleActivity.class, options);

    @Override
    public String doSomething(String name) {
        log.info("starting workflow work....");
        String result = activity.simpleAct(name);
        log.info("done doing some work..");
        String reply = activity.grpcCall(result);

        return reply;
    }
}
