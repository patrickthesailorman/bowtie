package com.netflix.niws.client.http;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.config.IClientConfig;
import com.netflix.client.http.HttpResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;

/***
 * <p>
 * POJO class for caching response objects. Used by Kryo (requires
 * getters/setters). Also used to create re-create {@link HttpResponse}, which
 * is why it has to be in the com.netflix.niws.client.http package.
 * </p>
 *
 */
public class CachedResponse implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CachedResponse.class);

    private static final long serialVersionUID = -6367516151816285192L;

    public static CachedResponse createResponse(int status,
            Map<String, Collection<String>> headers, byte[] cachedBytes) {

        return new CachedResponse(status, headers, cachedBytes);
    }

    private Map<String, Collection<String>> headers;
    private int status;
    private byte[] cachedBytes;
    private long ttl;

    public CachedResponse() {

    }

    public CachedResponse(int status, Map<String, Collection<String>> headers,
            byte[] cachedBytes) {

        this.status = status;
        this.headers = headers;
        this.cachedBytes = cachedBytes;
        this.ttl = parseTTL(headers);

    }

    public byte[] getCachedBytes() {
        return cachedBytes;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }

    public long getTTL() {
        return ttl;
    }

    private long parseTTL(Map<String, Collection<String>> headers) {

        long ttl = 0;

        if (headers != null) {
            Optional<Entry<String, Collection<String>>> cacheControlEntry = headers
                    .entrySet().stream()
                    .filter(t -> "Cache-Control".equals(t.getKey()))
                    .findFirst();

            if (cacheControlEntry.isPresent()) {
                Optional<String> cacheValue = cacheControlEntry.get()
                        .getValue().stream().findFirst();
                if (cacheValue.isPresent()) {
                    LOGGER.debug("cacheValue: {}", cacheValue.get());
                    Pattern pattern = Pattern.compile("^.*max-age=(\\d+).*$");
                    Matcher matcher = pattern.matcher(cacheValue.get());
                    if (matcher.matches()) {
                        ttl = Long.parseLong(matcher.group(1));
                        LOGGER.debug("Matches: {}", ttl);
                    }
                }
            }
        }

        return ttl;
    }

    public void setCachedBytes(byte[] cachedBytes) {

        this.cachedBytes = cachedBytes;
    }

    public void setHeaders(Map<String, Collection<String>> headers) {

        this.headers = headers;
    }

    public void setStatus(int status) {

        this.status = status;
    }

    public void setTtl(long ttl) {

        this.ttl = ttl;
    }

    public HttpResponse toHttpResponse(MessageBodyWorkers workers) {

        final InBoundHeaders inBoundHeaders = new InBoundHeaders();
        headers.forEach((k, v) -> {
            inBoundHeaders.put(k, new ArrayList<String>(v));
        });

        ClientResponse clientResponse = new ClientResponse(status,
                inBoundHeaders, new ByteArrayInputStream(cachedBytes), workers);

        URI requestedURI = null;
        IClientConfig config = null;

        return new HttpClientResponse(clientResponse, requestedURI, config);
    }
}