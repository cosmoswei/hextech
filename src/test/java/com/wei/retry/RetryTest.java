package com.wei.retry;

import java.io.IOException;
import java.time.Duration;

public class RetryTest {
    public static void main(String[] args) throws Exception {

        // 使用代理
        PaymentService service = RetryProxy.createProxy(new PaymentServiceImpl());
        PaymentRequest request = new PaymentRequest();
        service.processPayment(request);

        System.out.println("===========================================================");

        RetryPolicy policy = RetryOps.policyBuilder()
                .maxAttempts(3)
                .delay(Duration.ofSeconds(1))
                .retryOn(Exception.class)
                .backoffStrategy(RetryPolicy.BackoffStrategy.EXPONENTIAL)
                .build();

        String result = RetryOps.retry(policy, () -> {
            return "httpClient.get(";
        });

        System.out.println("result = " + result);
    }
}
