package com.wei.flowControl.aop;


import com.wei.flowControl.FlowControlHandler;
import com.wei.flowControl.InitMetaData;
import com.wei.flowControl.exception.FlowControlException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
@Aspect
public class FlowControlAspect {

    @Resource
    private Set<String> resourceKeys = new ConcurrentSkipListSet<>();

    /**
     * 拦截Limit注解的请求
     */
    @Around("@annotation(flowControl)")
    public Object restriction(ProceedingJoinPoint joinPoint, FlowControl flowControl) throws Throwable {
        String resourceKey = getLimiterKey(joinPoint, flowControl.key());
        int limit = flowControl.count();
        int interval = flowControl.interval();
        String type = flowControl.type();
        boolean contains = resourceKeys.contains(resourceKey);
        if (!contains) {
            InitMetaData initMetaData = new InitMetaData(resourceKey,
                    type, limit,
                    interval);
            FlowControlHandler.init(initMetaData);
            resourceKeys.add(resourceKey);
        }
        boolean result = FlowControlHandler.canPass(resourceKey);
        if (result) {
            return joinPoint.proceed();
        } else {
            String callback = flowControl.callback();
            if (null == callback || callback.isEmpty()) {
                throw new FlowControlException(flowControl.limitMsg());
            }
            // 限流失败，执行指定的回调方法
            Object target = joinPoint.getTarget();
            try {
                // 使用反射调用指定的回调方法
                return target.getClass()
                        .getMethod(callback)
                        .invoke(target);
            } catch (Exception e) {
                // 处理反射调用异常
                log.error("流控回调反射异常！", e);
                // 返回一个适当的响应，例如限流失败的提示
                return "Rate limit exceeded. Callback invoked.";
            }
        }

    }

    private String getLimiterKey(ProceedingJoinPoint joinPoint, String key) {
        Signature signature = joinPoint.getSignature();
        if (null == key || key.isEmpty()) {
            return signature.getDeclaringType().getName()
                    + "."
                    + signature.getName();
        }
        return key;
    }
}
