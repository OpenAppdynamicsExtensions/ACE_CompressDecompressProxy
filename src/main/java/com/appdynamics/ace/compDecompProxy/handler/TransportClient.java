package com.appdynamics.ace.compDecompProxy.handler;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by stefan.marx on 12.10.16.
 */
public class TransportClient {

    protected static Logger _log = Logger.getLogger("TransportClient");


    private final CloseableHttpClient client;

    public TransportClient () {
        client = HttpClients.createDefault();


    }
    public CloseableHttpResponse execute(String verb, String url, Header[] headers, byte[] payload, String contentEncoding) throws IOException {
        //TODO :


        HttpVerbs method = null;

        try {
            method = HttpVerbs.valueOf(verb.toUpperCase());
        } catch (Throwable t) {
          _log.log(Level.SEVERE,"Unsupported HTTP Verb : "+verb);
            return null;
        }


        switch (method) {
            case GET:

                HttpGet get = new HttpGet(url);
                get.setHeaders(headers);

                return executeMethod (get);

            case POST:
                HttpPost post = new HttpPost(url);
                post.setHeaders(headers);


                if (payload != null) {
                    _log.log(Level.INFO,"Append Payload : "+contentEncoding+"  ::"+payload.length);
                    ByteArrayEntity e = new ByteArrayEntity(payload, ContentType.create(contentEncoding));
                    post.setEntity(e);
                }

                return executeMethod(post);


            case HEAD:
                HttpHead head = new HttpHead(url);
                head.setHeaders(headers);

                return executeMethod (head);

            case PUT:
                HttpPut put = new HttpPut(url);
                put.setHeaders(headers);

                if (payload != null) {
                    ByteArrayEntity e = new ByteArrayEntity(payload, ContentType.create(contentEncoding));
                    put.setEntity(e);
                }

                return executeMethod(put);
             default:
                return null;


        }


    }

    private CloseableHttpResponse executeMethod(HttpUriRequest method) throws IOException {
        try {
            return client.execute(method);
        } catch (IOException e) {
            _log.log(Level.SEVERE,"Error while executing");
            throw new IOException("Error while executing http Call:",e);
        }
    }

    public CloseableHttpResponse execute(String method, String target, Header[] headers) throws IOException {
        return execute(method,target,headers,null,null);
    }
}
