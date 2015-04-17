package com.kenzan.bowtie;

import com.kenzan.bowtie.annotation.Encoding;
import com.kenzan.bowtie.cache.RestCache;
import com.kenzan.bowtie.serializer.JacksonMessageSerializer;
import com.kenzan.bowtie.serializer.MessageSerializer;

public class RestAdapterConfig {

    private MessageSerializer messageSerializer;
    private Encoding encoding;
    private RestCache restCache;

    private RestAdapterConfig() {

    }

    public MessageSerializer getMessageSerializer() {
        return this.messageSerializer;
    }

    public Encoding getEncoding() {
        return this.encoding;
    }

    public RestCache getRestCache() {
        return this.restCache;
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
        private RestCache restCache;

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

        public Builder withRestCache(RestCache restCache) {
            this.restCache = restCache;
            return this;
        }

        public RestAdapterConfig build() {
            final RestAdapterConfig restAdapterConfig = new RestAdapterConfig();

            restAdapterConfig.messageSerializer = messageSerializer;
            restAdapterConfig.encoding = encoding;
            restAdapterConfig.restCache = restCache;

            return restAdapterConfig;
        }
    }
}
