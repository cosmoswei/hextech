package com.wei.retry;

import java.util.concurrent.Callable;

// 编程式API
public class RetryOps {
    public static <T> T retry(RetryPolicy policy, Callable<T> task) throws Exception {
        return policy.execute(task);
    }

    public static RetryPolicy.Builder policyBuilder() {
        return new RetryPolicy.Builder();
    }
}