package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.client.http.HttpRequest.Verb;


public class VerbAnnotationFunctionTest {
    
    
    private final class AssertInvocationHandler implements InvocationHandler {
        
        
        private Verb verb;

        public AssertInvocationHandler(Verb verb) {
            this.verb = verb;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            
            
            Assert.assertThat(function.apply(method.getAnnotations()), IsEqual.equalTo(verb));
//            if(function.apply(annotation) != verb){
//                throw new IllegalStateException("Test failed");
//            }
            return null;
   }
    }

    final VerbAnnotationFunction function = new VerbAnnotationFunction();
    
    private static FakeClient create(InvocationHandler invocationHandler){
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{FakeClient.class}, invocationHandler);
        return (FakeClient) proxyInstance;
    }
    
    @Test
    public void testGET() throws NoSuchMethodException, SecurityException {
        create(new AssertInvocationHandler(Verb.GET)).getUser("jdoe");
        
//        create(new AssertInvocationHandler(Verb.POST)).getUser("jdoe");
    }

}
