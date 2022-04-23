# Temporal Demo

This repository is a demonstration of how to use Temporal using JavaSDK and Go SDK.
The `Document` workflow has 3 flows:
* Creation
* Submission
* Validation
* Closing

M
## Install temporal

**Dont use this in production**

``` shell
helm upgrade --install --namespace temporal -f values/values.postgresql.yaml temporal \
  --set web.service.type=NodePort \
  --set server.frontend.service.type=NodePort \
  --set elasticsearch.enabled=false \
  --set server.config.persistence.default.sql.user=amazinguser \
  --set server.config.persistence.default.sql.password=perfectpassword \
  --set server.config.persistence.visibility.sql.user=amazinguser \
  --set server.config.persistence.visibility.sql.password=perfectpassword \
  --set server.config.persistence.default.sql.host=postgres-service.postgres \
  --set server.config.persistence.default.sql.port=5432 \
  --set server.config.persistence.visibility.sql.port=5432 \
  --set server.config.persistence.visibility.sql.host=postgres-service.postgres . 

```
## Add opentelemetry java agent

JVM arguments, example

``` 
-javaagent:/home/thor/Downloads/opentelemetry-javaagent.jar
-Dotel.javaagent.configuration-file=/home/thor/workspace/temporal-stuffs/demo/temporal-demo-starter/src/main/resources/otel-agent.properties
```

## Run parallel test

``` shell
k6 run --vus 10 --duration 30s k6-test/test.js
```

