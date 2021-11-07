package com.github.balchua.temporaldemoworker.configuration;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.http.HttpTracing;
import brave.okhttp3.TracingCallFactory;
import com.github.balchua.protos.GreeterGrpc;
import com.github.balchua.temporaldemocommon.common.Shared;
import com.github.balchua.temporaldemocommon.context.TracingContextPropagator;
import com.github.balchua.temporaldemoworker.activity.SimpleActivity;
import com.github.balchua.temporaldemoworker.activity.SimpleActivityImpl;
import com.github.balchua.temporaldemoworker.workflow.SimpleWorkflowImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.context.ContextPropagator;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.Collections;

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
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(this.temporalNamespace)
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
                .build();
        WorkerFactory factory = WorkerFactory.newInstance(client, options);
        this.factory = factory;
        Worker worker = factory.newWorker(Shared.DEMO_TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(SimpleWorkflowImpl.class);
        worker.registerActivitiesImplementations(simpleActivity);
        return factory;
    }

    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    @Bean
    public Reporter<Span> spanReporter() {
        return AsyncReporter.builder(sender()).build();
    }


    /** Controls aspects of tracing such as the service name that shows up in the UI */
    @Bean Tracing tracing() {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .currentTraceContext(new TestCurrentTraceContext())
                .spanReporter(spanReporter()).build();
    }

    // decides how to name and tag spans. By default they are named the same as the http method.
    @Bean
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }

    /**
     * Configuration for how to send spans to Zipkin
     */
    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://localhost:31941/api/v2/spans");
    }

    @Bean
    public Call.Factory callFactory (Tracing tracing, OkHttpClient okHttpClient) {
        return TracingCallFactory.create(tracing, okHttpClient);
    }

    @Bean
    public OkHttpClient okhttp () {
        return new OkHttpClient();
    }

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 50051).intercept(grpcTracing().newClientInterceptor())
                .usePlaintext().build();
    }

    @Bean
    public GrpcTracing grpcTracing() {
        return GrpcTracing.create(tracing());
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
