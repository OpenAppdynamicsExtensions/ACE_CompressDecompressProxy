#!/bin/sh
./build/install/proxy/bin/proxy startDebugEndpoint --port 9009 --verbose >/tmp/proxyDebug.log &
./build/install/proxy/bin/proxy compressAndForward --port 9008 --compressHeader X-beacon-should-compress --verbose --target http://localhost:9009 >/tmp/proxyCompress.log &
./build/install/proxy/bin/proxy expandAndForward --port 9007 --compressHeader X-beacon-should-compress --verbose --target http://localhost:9008 >/tmp/proxyExpand.log &
