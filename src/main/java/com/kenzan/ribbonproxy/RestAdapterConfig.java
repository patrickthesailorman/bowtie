/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.Encoding;
import com.kenzan.ribbonproxy.cache.RestCache;
import com.kenzan.ribbonproxy.serializer.JacksonMessageSerializer;
import com.kenzan.ribbonproxy.serializer.MessageSerializer;

public class RestAdapterConfig {

    private MessageSerializer messageSerializer;
    private Encoding encoding;
    private RestCache ribbonCache;

    private RestAdapterConfig() {

    }

    public MessageSerializer getMessageSerializer() {
        return this.messageSerializer;
    }

    public Encoding getEncoding() {
        return this.encoding;
    }

    public RestCache getRibbonCache() {
        return this.ribbonCache;
    }

    public static RestAdapterConfig createDefault(){
        return new Builder()
        .withMessageSerializer(new JacksonMessageSerializer())
        .build();
    }

    public static Builder custom(){
        return new Builder();
    }

    public static class Builder {

        private MessageSerializer messageSerializer;
        private Encoding encoding;
        private RestCache ribbonCache;

        private Builder() {

        }

        public Builder withMessageSerializer(MessageSerializer messageSerializer) {
            this.messageSerializer = messageSerializer;
            return this;
        }

        public Builder withEncoding(Encoding encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder withRibbonCache(RestCache ribbonCache) {
            this.ribbonCache = ribbonCache;
            return this;
        }

        public RestAdapterConfig build() {
            final RestAdapterConfig restAdapterConfig = new RestAdapterConfig();

            restAdapterConfig.messageSerializer = messageSerializer;
            restAdapterConfig.encoding = encoding;
            restAdapterConfig.ribbonCache = ribbonCache;

            return restAdapterConfig;
        }
    }
}
