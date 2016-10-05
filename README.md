# ACE_CompressDecompressProxy

This command line utillity allows decompressing and compressing of mobile beacon payloads send by the AppDynamics mobile eum Agent. 
This is usefull in case a content inspecting firewall needs to access the beacon payload for security.

# Operation :

## Debug Proxy
This will open a http socket and accept all incomming http traffic. It will then print some debug stats on the standart output and optional save 
the payload as single files. This is usefull to test incomming content or to save test data for dummy test loads.

#### Usage
```
startDebugEndpoint

       ---  Description
       Starts a generic endpoint that accepts all data and prints debug information.
       ---  Options
       port         : (required) portnumber
       verbose      : (optional) verbose output
       dir          : (optional) Directory to save content payload !
```


#### Examples

Start a simple proxy with verbose logging and allow it to store all payloads to /tmp/data
```bash
proxy startDebugEndpoint -port 9098 -verbose -dir /tmp/data
```

**Output**

```bash
POST /eumcollector/mobileMetrics?version=2 HTTP/1.0
   X-Real-IP: 93.216.45.249
   Host: smarxdocker2.ddns.net:9990
   Connection: close
   Content-Length: 539
   an: org.codingfragments.eumtest2
   ky: AD-AAB-AAC-PWJ
   cap: s:1
   bid: 24d47a653e302977a60f61e559ba1346
   mat: 1472756400000
   osn: Android
   di: cb519cff-79e2-4c4d-bd42-1732c8cba736
   Content-Type: application/x-www-form-urlencoded
   User-Agent: Dalvik/2.1.0 (Linux; U; Android 6.0; Android SDK built for x86_64 Build/MASTER)
   Accept-Encoding: gzip
   X-Should-Compress: gzip

   [{"type":"network-request","ec":935,"eid":"ed393e41-a779-495f-b294-e822bfb4be1d","st":1475066662019,"sut":5557198,"et":1475066664141,"eut":5559320,"bkgd":false,"url":"http://httpbin.org/delay/2","pcl":376,"hrc":200,"crg":"47f6e7b4-9b71-47b8-8343-dfc8cd5cea1d","bts":[],"see":false,"avi":1,"av":"1.1","agv":"4.2.6.0","ab":"95e7ed66fef8c92bb89a540629a74c15be05c484","dm":"unknown","dmo":"Android SDK built for x86_64","ds":1983,"tm":"1498","cf":"Unknown","cc":2,"osv":"6.0","ca":"Android","ct":"3g","bid":"24d47a653e302977a60f61e559ba1346"}]
```

Start a simple Proxy without verbose logging and don't store the payload data.

```bash
proxy startDebugEndpoint -port 9099
```


## Compressing or decompressing Body

This will start a decompressing (expanding) or compressing body. The Expanding Proxy will listen to any content that has the gzip
header field set to true and decompress it's payload before forwarding. It will also set a custom header (compress Header option) field to indicate
it'S content for further processing.

The Compressing proxy will search for this custom header and re-compress the content using gzip. it will restore the gzip header before forwarding.

If you chain a expanding and a compressing proxy you can make the whole process transparent to the EUM Collector while making s
sure there is no gzip compressed content between the proxy chain.




#### Usage

``` 
==  Command

    expandAndForward

    ---  Description
    Expand the request if needed and forward !
    ---  Options
    port         : (required) portnumber
    verbose      : (optional) verbose output
    target       : (required) TargetURL To Forward
    compressHeader   : (optional) Header that signals request compression needed (X-should-compress)


    ==  Command

    compressAndForward

    ---  Description
    Compress the request if needed and forward !
    ---  Options
    port         : (required) portnumber
    verbose      : (optional) verbose output
    target       : (required) TargetURL To Forward
    compressHeader   : (optional) Header that signals request compression needed (X-should-compress)
```



#### Examples

```bash
 proxy expandAndForward  -port 9099 -target http://localhost:9097/
 proxy compressAndForward -port 9097 -target https://mobile.eum-appdynamics.com/
```


## Configuration

Memory tuning might be necessary in high load environments. This can be achieved by changing JVM_OPTS in the proxy command file or by using the JAVA_OPTS environment variable when starting the proxy

```bash
JAVA_OPTS=-Xmx1g -Xms1g
# run both commands with 1GB heapsize

proxy expandAndForward  -port 9099 -target http://localhost:9097/
proxy compressAndForward -port 9097 -target https://mobile.eum-appdynamics.com/

```
