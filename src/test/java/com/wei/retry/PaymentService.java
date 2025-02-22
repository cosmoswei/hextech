package com.wei.retry;

public interface PaymentService {
    @Retryable(
            maxAttempts = 5,
            delay = 2000
    )
    void processPayment(PaymentRequest request);
}