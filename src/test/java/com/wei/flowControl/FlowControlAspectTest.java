package com.wei.flowControl;


import com.wei.flowControl.aop.FlowControl;
import com.wei.flowControl.aop.FlowControlAspect;
import com.wei.flowControl.exception.FlowControlException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FlowControlAspectTest {

    private FlowControlAspect flowControlAspect;
    private ProceedingJoinPoint mockJoinPoint;

    @BeforeEach
    void setUp() {
        flowControlAspect = new FlowControlAspect();
        mockJoinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    void testAllowRequest() throws Throwable {
        // 模拟 FlowControl 注解
        FlowControl mockAnnotation = mock(FlowControl.class);
        when(mockAnnotation.key()).thenReturn("testKey");
        when(mockAnnotation.count()).thenReturn(5);
        when(mockAnnotation.interval()).thenReturn(1);
        when(mockAnnotation.type()).thenReturn("TOKEN_BUCKET");
        when(mockAnnotation.callback()).thenReturn("");
        when(mockAnnotation.limitMsg()).thenReturn("Request limited!");

        // 模拟方法签名
        MethodSignature mockSignature = mock(MethodSignature.class);
        when(mockSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("testAllowRequest"));
        when(mockJoinPoint.getSignature()).thenReturn(mockSignature);

        // 模拟方法执行
        when(mockJoinPoint.proceed()).thenReturn("Success!");

        Object result = flowControlAspect.restriction(mockJoinPoint, mockAnnotation);

        assertEquals("Success!", result, "Method should pass under rate limit");
    }

    @Test
    void testRejectRequestWithoutCallback() throws Throwable {
        // 模拟 FlowControl 注解
        FlowControl mockAnnotation = mock(FlowControl.class);
        when(mockAnnotation.key()).thenReturn("testKey");
        when(mockAnnotation.count()).thenReturn(1);
        when(mockAnnotation.interval()).thenReturn(1);
        when(mockAnnotation.type()).thenReturn("TOKEN_BUCKET");
        when(mockAnnotation.callback()).thenReturn("");
        when(mockAnnotation.limitMsg()).thenReturn("Request limited!");

        // 模拟方法签名
        MethodSignature mockSignature = mock(MethodSignature.class);
        when(mockSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("testRejectRequestWithoutCallback"));
        when(mockJoinPoint.getSignature()).thenReturn(mockSignature);

        // 模拟限流逻辑
        FlowControlHandler.init(new InitMetaData("testKey", "TOKEN_BUCKET", 1, 1));
        FlowControlHandler.canPass("testKey"); // 第一次请求放行
        assertThrows(FlowControlException.class, () -> flowControlAspect.restriction(mockJoinPoint, mockAnnotation));
    }

    @Test
    void testRejectRequestWithCallback() throws Throwable {
        // 模拟 FlowControl 注解
        FlowControl mockAnnotation = mock(FlowControl.class);
        when(mockAnnotation.key()).thenReturn("testKey");
        when(mockAnnotation.count()).thenReturn(1);
        when(mockAnnotation.interval()).thenReturn(1);
        when(mockAnnotation.type()).thenReturn("TOKEN_BUCKET");
        when(mockAnnotation.callback()).thenReturn("callbackMethod");
        when(mockAnnotation.limitMsg()).thenReturn("Request limited!");

        // 模拟方法签名
        MethodSignature mockSignature = mock(MethodSignature.class);
        when(mockSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("callbackMethod"));
        when(mockJoinPoint.getSignature()).thenReturn(mockSignature);

        // 模拟限流逻辑
        FlowControlHandler.init(new InitMetaData("testKey", "TOKEN_BUCKET", 1, 1));
        FlowControlHandler.canPass("testKey"); // 第一次请求放行

        // 模拟目标对象
        FlowControlAspectTest mockTarget = spy(new FlowControlAspectTest());
        when(mockJoinPoint.getTarget()).thenReturn(mockTarget);

        Object result = flowControlAspect.restriction(mockJoinPoint, mockAnnotation);

        assertEquals("Callback executed!", result, "Callback method should be executed on limit");
        verify(mockTarget, times(1)).callbackMethod();
    }

    // 模拟的回调方法
    public String callbackMethod() {
        return "Callback executed!";
    }
}