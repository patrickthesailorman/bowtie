/**
 * Copyright (C) 2015 Kenzan (labs@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.bowtie;

import com.kenzan.bowtie.annotation.Encoding;
import com.kenzan.bowtie.cache.RestCache;
import com.kenzan.bowtie.serializer.JacksonMessageSerializer;
import com.kenzan.bowtie.serializer.MessageSerializer;

/***
 * <p>
 * Configuration object and builder for creating clients using the {@link RestAdapter}
 * </p>
 */
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
