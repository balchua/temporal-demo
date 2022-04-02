package com.github.balchua.temporaldemoworker.configuration;

import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.temporaldemocommon.common.Shared;
import com.github.balchua.temporaldemocommon.context.TracingContextPropagator;
import com.github.balchua.temporaldemoworker.activity.SimpleActivity;
import com.github.balchua.temporaldemoworker.activity.SimpleActivityImpl;
import com.github.balchua.temporaldemoworker.workflow.SimpleWorkflowImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTracing;
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTracing;
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
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfiguration implements SmartInitializingSingleton {

    private WorkerFactory factory;

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

    @Value("${grpc-service.host}")
    private String grpcServiceHost;

    @Value("${grpc-service.port}")
    private int grpcServicePort;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
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
    public OpenTelemetry openTelemetry() {
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
        return openTelemetry;
    }

    @Bean
    public OpenTracingOptions getJaegerOpenTelemetryOptions(OpenTelemetry openTelemetry) {

        OpenTracingOptions options =
                OpenTracingOptions.newBuilder()
                        .setSpanContextCodec(OpenTracingSpanContextCodec.TEXT_MAP_CODEC)
                        .setTracer(OpenTracingShim.createTracerShim(openTelemetry))
                        .build();
        return options;
    }

    @Bean
    public OpenTracingClientInterceptor clientInterceptor() {
        return new OpenTracingClientInterceptor(getJaegerOpenTelemetryOptions(openTelemetry()));
    }

    @Bean
    public OpenTracingWorkerInterceptor workerInterceptor() {
        return new OpenTracingWorkerInterceptor(getJaegerOpenTelemetryOptions(openTelemetry()));
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

    @Bean
    public SimpleActivity simpleActivity() {
        return new SimpleActivityImpl();
    }


    @Bean
    public WorkerFactory workerFactory(WorkflowClient client, SimpleActivity simpleActivity) {
        WorkerFactoryOptions options = WorkerFactoryOptions.newBuilder()
                .setWorkerInterceptors(workerInterceptor())
                .build();
        WorkerFactory factory = WorkerFactory.newInstance(client, options);
        this.factory = factory;
        WorkerOptions workerOptions = WorkerOptions.newBuilder()
                .build();
        Worker worker = factory.newWorker(Shared.DEMO_TASK_QUEUE, workerOptions);
        worker.registerWorkflowImplementationTypes(SimpleWorkflowImpl.class);
        worker.registerActivitiesImplementations(simpleActivity);


        return factory;
    }

    @Bean
    public Call.Factory createTracedClient(OpenTelemetry openTelemetry) {
        return OkHttpTracing.builder(openTelemetry).build().newCallFactory(new OkHttpClient());
    }


    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress(grpcServiceHost, grpcServicePort)
                .intercept(GrpcTracing.create(openTelemetry()).newClientInterceptor())
                .usePlaintext().build();
    }

    @Bean
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub() {
        return GreeterGrpc.newBlockingStub(managedChannel());
    }

    @Override
    public void afterSingletonsInstantiated() {
        factory.start();
    }

}
