package com.appdynamics.ace.compDecompProxy.handler;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.ByteBufferContentProvider;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.servlet.Source;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class CompressProxyServlet extends ProxyServlet {
    private String _targetUrl;
    private String _compressHeader;


    protected static Logger _log = Logger.getLogger("UncompressServlet");

    public CompressProxyServlet(String targetUrl, String compressHeader) {
        super();
        _targetUrl = targetUrl;
        _compressHeader = compressHeader;
    }


    protected ContentProvider proxyRequestContent(HttpServletRequest request, HttpServletResponse response, Request proxyRequest) throws IOException
    {

        _log.log(Level.INFO,"Processing Content: Length="+ request.getContentLength());

        if ("gzip".equals(request.getHeader(_compressHeader))) {


            _log.log(Level.INFO,"Compressing Request ("+request.getContentLength()+")");



            ByteArrayOutputStream os = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(os);

            InputStream is = request.getInputStream() ;

            byte[] buffer = new byte[1024];

            int len;
            int size = 0;
            while ((len = is.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
                size += len;
            }

            is.close();
            os.close();
            gzos.close();


            byte[] dataBuffer = os.toByteArray();

            _log.log(Level.INFO,"new Size : "+dataBuffer.length);


            proxyRequest.getHeaders().add("gzip","true");
            proxyRequest.getHeaders().remove(_compressHeader);


            return new ByteBufferContentProvider(request.getContentType(),ByteBuffer.wrap(dataBuffer));


        }  else return super.proxyRequestContent(request,response,proxyRequest);
    }

    public void init() throws ServletException {
        super.init();
        ServletConfig config = getServletConfig();

    }

    protected HttpClient newHttpClient() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        return httpClient;
    }



    protected String rewriteTarget(HttpServletRequest request) {
        String path = request.getRequestURI();


        StringBuilder uri = new StringBuilder(_targetUrl);
        if (_targetUrl.endsWith("/"))
            uri.setLength(uri.length() - 1);


        String rest = path.substring(0);
        if (!rest.isEmpty()) {
            if (!rest.startsWith("/"))
                uri.append("/");
            uri.append(rest);
        }


        String query = request.getQueryString();

        if (query != null) {
            // Is there at least one path segment ?
            String separator = "://";
            if (uri.indexOf("/", uri.indexOf(separator) + separator.length()) < 0)
                uri.append("/");
            uri.append("?").append(query);
        }
        URI rewrittenURI = URI.create(uri.toString()).normalize();

        if (!validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
            return null;

        return rewrittenURI.toString();
    }


}
