package com.github.balchua.temporaldemoworker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SimpleActivity {
    @ActivityMethod
    String simpleAct(String name);

    @ActivityMethod
    String grpcCall(String arg);
}
