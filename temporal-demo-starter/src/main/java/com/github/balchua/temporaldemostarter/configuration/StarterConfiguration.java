package com.github.balchua.temporaldemostarter.configuration;

import com.github.balchua.temporaldemocommon.context.TracingContextPropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.context.ContextPropagator;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.opentracing.OpenTracingOptions;
import io.temporal.opentracing.OpenTracingSpanContextCodec;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class StarterConfiguration {

    @Value("${temporal.host}")
    private String temporalHost;

    @Value("${temporal.port}")
    private int temporalPort;

    @Value("${temporal.namespace}")
    private String temporalNamespace;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${jaeger.host}")
    private String jaegerHost;


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
    public OpenTracingOptions getJaegerOpenTelemetryOptions() {
        Resource serviceNameResource =
                Resource.create(
                        Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        JaegerGrpcSpanExporter jaegerExporter =
                JaegerGrpcSpanExporter.builder()
                        .setEndpoint(jaegerHost)
                        .setTimeout(1, TimeUnit.SECONDS)
                        .build();

        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();

        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder()
                        .setPropagators(
                                ContextPropagators.create(
                                        TextMapPropagator.composite(
                                                W3CTraceContextPropagator.getInstance(), JaegerPropagator.getInstance())))
                        .setTracerProvider(tracerProvider)
                        .build();

        OpenTracingOptions options =
                OpenTracingOptions.newBuilder()
                        .setSpanContextCodec(OpenTracingSpanContextCodec.TEXT_MAP_CODEC)
                        .setTracer(OpenTracingShim.createTracerShim(openTelemetry))
                        .build();
        return options;
    }

    @Bean
    public OpenTracingClientInterceptor clientInterceptor() {
        return new OpenTracingClientInterceptor(getJaegerOpenTelemetryOptions());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(this.temporalNamespace)
                        .setInterceptors(clientInterceptor())
                        .setContextPropagators(Collections.singletonList(contextPropagator()))
                        .build());
    }


}
