package com.wei.retry;

public class PaymentServiceImpl implements PaymentService {
    @Override
    public void processPayment(PaymentRequest request) {
        if (System.currentTimeMillis() % 2 != 0) {
            throw new RuntimeException("ok");
        }
        System.out.println(" PaymentServiceImpl request = " + request);
    }
}
