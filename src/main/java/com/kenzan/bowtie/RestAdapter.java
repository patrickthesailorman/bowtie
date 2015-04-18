package com.kenzan.bowtie;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.kenzan.bowtie.annotation.Encoding;
import com.netflix.client.ClientFactory;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;

/***
 * <p>Main class for instantiating client instances.</p>
 * 
 * Use the default config for most cases (REST/JSON):
 * <pre>
 *      final RestAdapter restAdapter = RestAdapter.getNamedAdapter("sample-client");
 *      fakeClient = restAdapter.create(FakeClient.class);
 * </pre>
 * 
 * Or use a custom config:
 * <pre>
 *  final RestAdapter restAdapter2 = RestAdapter.getNamedAdapter("sample-client", RestAdapterConfig.custom()
 *      .withMessageSerializer(new JacksonMessageSerializer())
 *      .withEncoding(Encoding.gzip)
 *      .build());
 *
 *      fakeClient2 = restAdapter2.create(FakeClient.class);
 * </pre>
 *
 */
public class RestAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAdapter.class);
    
    private String namedClient;
    private RestAdapterConfig restAdapterConfig;

    private RestAdapter(String namedClient, RestAdapterConfig restAdapterConfig) {
        this.namedClient = namedClient;
        this.restAdapterConfig = restAdapterConfig;
    }

    public static RestAdapter getNamedAdapter(String namedAdapter) {
        return new RestAdapter(namedAdapter, RestAdapterConfig.createDefault());
    }

    public static RestAdapter getNamedAdapter(String namedAdapter, RestAdapterConfig restAdapterConfig) {
        return new RestAdapter(namedAdapter, restAdapterConfig);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientClass) {

        Preconditions.checkNotNull("NamedClient required", this.namedClient);
        Preconditions.checkNotNull("MessageSerializer required", this.restAdapterConfig.getMessageSerializer());
        
        LOGGER.info("Using NamedClient {}", this.namedClient);
        LOGGER.info("Using MessageSerializer {}", this.restAdapterConfig.getMessageSerializer());
        
        final RestClient restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
        restClient.getJerseyClient().addFilter(new LoggerFilter());
        
        if(this.restAdapterConfig.getEncoding() == Encoding.gzip){
            restClient.getJerseyClient().addFilter(new GZIPContentEncodingFilter());
        }
        
        InvocationHandler invocationHandler = new JerseyInvocationHandler(restClient, this.restAdapterConfig);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
