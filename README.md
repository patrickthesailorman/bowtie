# bowtie
A REST client library providing a declarative API to create common REST clients using [Hystrix](https://github.com/Netflix/Hystrix) and [Ribbon](https://github.com/Netflix/ribbon).

It provides the following features:
* Can be used in a Netflix-OSS platform or standalone.
* Annotation based declarative clients
* Consistent configuration using [Archaius](https://github.com/Netflix/Archaius)
* Supports JSON serialization via [Jackson](http://jackson.codehaus.org/)
* Caching using Guava Cache or Memcache


## Getting started
*NOTE:  Assumes understanding of Archaius*

Create a client as an interface

    public interface UserClient {
    
	    @Http(
	        method = Verb.GET,
	        uriTemplate = "/user/{username}"
	    )
	    public User getUser(@Path("username") String name);
    }
    

Use the builder to configure the instance

    final RestAdapter restAdapter = RestAdapter.getNamedAdapter("user-client");
    

Create an instance and start using

    final UserClient userClient = restAdapter.create(UserClient.class);
    final User user = fakeClient.getUser("jdoe");

See the FakeClient class in the tests for sample calls.


If method and parameter annotations are used, runtime annotations override the static values. 

Example:

    public interface AnotherClient {

	    @Http(
	        method = Verb.GET,
	        uriTemplate = "/pet/{guid}",
	        headers = {
	            @Header(name="X-SESSION-ID", value="55892d6d-77df-4617-b728-6f5de97f5752")
	        }
	    )
	    public User getPet(@Header(name="X-SESSION-ID") String sessionId, @Path("guid") String guid);
    }

In the above example the value of sessionId passed into the request will be used, not the value from the method annoation.




#Configuration
Use the RestAdapterConfig to configure the RestAdapter.

     final RestCache cache = GuavaRestCache.newDefaultCache();
     final RestAdapter restAdapter = RestAdapter.getNamedAdapter("user-client", RestAdapterConfig.custom()
       .withMessageSerializer(new JacksonMessageSerializer())
       .withRestCache(cache)
       .withEncoding(Encoding.gzip)
       .build());


## MessageSerializers
Use MessageSerializers to control how the request and responses are serialized.

See:  https://github.com/kenzanmedia/bowtie/issues/15 (May be changed later).

## Encoding
Adding gzip encoding will tell Jersey to use the GZIPContentEncodingFilter for the request.  Adding the Accept-Encoding 
header and returning the response with a GZIPInputStream.   

##Caching
Requests are cached using the Cache-Control header.  No other caching mechanism is currently supported.

See:  "<Insert link to ticket for -> Add support for Max-Age header"

Caching is done using key/value where:
* key:  <CacheKeyGroup>:<Request Path>
* value:  CachedResponse

Supported caches:
* GuavaRestCache:  Pass in a Guava Cache and caching will be performed in memory
* MemcacheRestCache:  Cache the response in Memcache





# Tests
## HTTP
* HTTP tests use a mock-server for http calls.  When working on the unit tests start up the mock server in a separate shell (see instructions below).
* To test additional HTTP calls use the MockServerInitializationClass to add mocks.

## Memcache
* Memcache tests use jmemcached and are started/stopped in the setup/teardown methods of the test.
* Unfortunately jmemcached does not didn't seem to honor the TTL calls, so those are not tested.



# Mock Server
* See http://www.mock-server.com/  <- RTFM
* Uses mock-server to mock HTTP requests for testing
* Start mock server for development using
```
mvn mockserver:run
```
* Mock server is automatically started during "mvn test" and "mvn verify" lifescycles



#Backlog
* Add parameter support for cookie
* Add support for type annotations
* Add validation for uriTemplates with variables (check to make sure there is a corresponding path parameter)