# bowtie
Http client library using interfaces and a proxy class to execute HTTP requests and configure hystrix command objects

See the FakeClient class for sample usage.



## Tests
* HTTP tests use a mock-server for http calls.  When working on the unit tests start up the mock server in a separate shell (see instructions below).
* To test additional HTTP calls use the MockServerInitializationClass to add mocks.
* Memcache tests use jmemcached and are started/stopped in the setup/teardown methods of the test.  No need to run memcache independently.



## Mock Server
* See http://www.mock-server.com/  <- RTFM
* Uses mock-server to mock HTTP requests for testing
* Start mock server for development using
```
mvn mockserver:run
```
* Mock server is automatically started during "mvn test" and "mvn verify" lifescycles

