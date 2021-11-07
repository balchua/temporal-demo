package com.github.balchua.temporaldemostarter.configuration;

import brave.baggage.BaggageFields;
import brave.baggage.CorrelationScopeConfig;
import brave.baggage.CorrelationScopeCustomizer;
import com.github.balchua.temporaldemocommon.context.TracingContextPropagator;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.context.ContextPropagator;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class StarterConfiguration {

    @Value("${temporal.host}")
    private String temporalHost;

    @Value("${temporal.port}")
    private int temporalPort;

    @Value("${temporal.namespace}")
    private String temporalNamespace;

    @Bean
    WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubs service =
                WorkflowServiceStubs.newInstance(
                        WorkflowServiceStubsOptions.newBuilder().setTarget(this.temporalHost + ":" + this.temporalPort).build());
        return service;
    }

    @Bean
    public ContextPropagator contextPropagator() {
        return new TracingContextPropagator();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(this.temporalNamespace)
                        .setContextPropagators(Collections.singletonList(contextPropagator()))
                        .build());
    }

    @Bean
    public CorrelationScopeCustomizer createCorrelationSpanCustomizer() {
        return b -> b.add(CorrelationScopeConfig.SingleCorrelationField.create(BaggageFields.SAMPLED))
                .add(CorrelationScopeConfig.SingleCorrelationField.create(BaggageFields.PARENT_ID));
    }

}
