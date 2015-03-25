# bowtie



## Mock Server
* See http://www.mock-server.com/  <- RTFM
* Uses mock-server to mock HTTP requests for testing
* Start mock server for development using
```
mvn mockserver:run
```
* Mock server is automatically started during "mvn test" and "mvn verify" lifescycles


##Backlog
* Add interface for pre/post classes to allow manipulating request/response during executions
* Add caching support (only honor max-age for now)
** http://hc.apache.org/httpcomponents-client-ga/tutorial/html/caching.html
** http://hc.apache.org/httpcomponents-client-ga/httpclient-cache/apidocs/org/apache/http/impl/client/cache/memcached/MemcachedHttpCacheStorage.html