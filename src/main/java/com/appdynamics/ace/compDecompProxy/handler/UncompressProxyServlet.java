package com.appdynamics.ace.compDecompProxy.handler;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.ByteBufferContentProvider;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;


/**
 * Created by stefan.marx on 28.09.16.
 */
public class UncompressProxyServlet extends ProxyServlet {
    private String _targetUrl;
    private String _compressHeader;

    private static boolean _verbose;

    protected static Logger _log = Logger.getLogger("UncompressServlet");

    public UncompressProxyServlet(String targetUrl, String compressHeader) {
        super();
        _targetUrl = targetUrl;
        _compressHeader = compressHeader;
    }

    public void init() throws ServletException {
        super.init();
        ServletConfig config = getServletConfig();
        String logging = config.getInitParameter("verboseLogging");



    }

    protected HttpClient newHttpClient() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        return httpClient;
    }



    protected ContentProvider proxyRequestContent(HttpServletRequest request, HttpServletResponse response, Request proxyRequest) throws IOException
    {
        if ("true".equals(request.getHeader("gzip"))) {


         _log.log(Level.INFO,"Decompressing Request ("+request.getContentLength()+")");


            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream is = new GZIPInputStream(request.getInputStream()) ;

            byte[] buffer = new byte[1024];

            int len;
            int size = 0;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
                size += len;
            }

            is.close();
            os.close();

             _log.log(Level.INFO,"new Size : "+size);


            proxyRequest.getHeaders().remove("gzip");
            proxyRequest.getHeaders().add(_compressHeader,"gzip");

            return new BytesContentProvider(request.getContentType(),os.toByteArray());

        }  else return super.proxyRequestContent(request,response,proxyRequest);
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
