## Add opentelemetry java agent

JVM arguments, example

``` 
-javaagent:/home/thor/Downloads/opentelemetry-javaagent.jar
-Dotel.traces.exporter=jaeger
-Dotel.exporter.jaeger.endpoint=http://localhost:32473
-Dotel.service.name=otel-starter
```