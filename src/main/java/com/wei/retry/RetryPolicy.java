package com.wei.retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

// 重试策略配置
public class RetryPolicy {
    private int maxAttempts = 3;
    private Duration delay = Duration.ofSeconds(1);
    private final Set<Class<? extends Throwable>> retryExceptions = new HashSet<>();
    private final Set<Class<? extends Throwable>> abortExceptions = new HashSet<>();
    private BackoffStrategy backoffStrategy = BackoffStrategy.FIXED;

    public enum BackoffStrategy {
        FIXED, EXPONENTIAL
    }

    // Builder模式配置
    public static class Builder {
        private final RetryPolicy policy = new RetryPolicy();

        public Builder maxAttempts(int maxAttempts) {
            policy.maxAttempts = maxAttempts;
            return this;
        }

        public Builder delay(Duration delay) {
            policy.delay = delay;
            return this;
        }

        @SafeVarargs
        public final Builder retryOn(Class<? extends Throwable>... exceptions) {
            policy.retryExceptions.addAll(Arrays.asList(exceptions));
            return this;
        }

        @SafeVarargs
        public final Builder abortOn(Class<? extends Throwable>... exceptions) {
            policy.abortExceptions.addAll(Arrays.asList(exceptions));
            return this;
        }

        public Builder backoffStrategy(BackoffStrategy strategy) {
            policy.backoffStrategy = strategy;
            return this;
        }

        public RetryPolicy build() {
            return policy;
        }
    }

    // 执行重试逻辑
    public <T> T execute(Callable<T> task) throws Exception {
        int attempt = 0;
        long currentDelay = delay.toMillis();

        while (attempt < maxAttempts) {

            try {
                return task.call();
            } catch (Throwable e) {
                if (shouldAbort(e)) {
                    throw e;
                }
                System.out.println("第一次重试 = " + attempt);
                if (attempt++ >= maxAttempts - 1 || !shouldRetry(e)) {
                    throw e;
                }

                Thread.sleep(currentDelay);

                if (backoffStrategy == BackoffStrategy.EXPONENTIAL) {
                    currentDelay *= 2;
                }
            }
        }
        throw new RetryException("Exceeded max retry attempts: " + maxAttempts);
    }

    private boolean shouldRetry(Throwable e) {
        return retryExceptions.stream().anyMatch(clazz -> clazz.isInstance(e));
    }

    private boolean shouldAbort(Throwable e) {
        return abortExceptions.stream().anyMatch(clazz -> clazz.isInstance(e));
    }
}