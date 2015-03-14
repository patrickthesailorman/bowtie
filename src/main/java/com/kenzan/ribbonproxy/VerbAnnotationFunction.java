package com.kenzan.ribbonproxy;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.kenzan.ribbonproxy.annotation.GET;
import com.netflix.client.http.HttpRequest.Verb;


public class VerbAnnotationFunction implements Function<Annotation[], Verb>{

    @Override
    public Verb apply(Annotation[] annotations) {

        Optional<Annotation> firstAnnotation = Arrays.stream(annotations)
                        .filter(t -> GET.class.equals(t.annotationType()))
                        .findFirst();
        
        if(!firstAnnotation.isPresent()){
            throw new IllegalStateException("Missing verb annotation.  Add GET or similar annotation");
        }
        
        
        final Verb verb;
        Annotation annotation = firstAnnotation.get();
        if(GET.class.equals(annotation.annotationType())){
            verb = Verb.GET;
        }else{
            throw new IllegalStateException("Verb not supported: " + annotation.getClass().getName());
        }
        
        return verb;
    }

}
