package com.appdynamics.ace.compDecompProxy.handler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.ByteBufferContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class StraightCompressProxyServlet extends HttpServlet {
    private String _targetUrl;
    private String _compressHeader;

    protected static final Set<String> HOP_HEADERS;
    static
    {
        Set<String> hopHeaders = new HashSet<>();
        hopHeaders.add("connection");
        hopHeaders.add("keep-alive");
        hopHeaders.add("proxy-authorization");
        hopHeaders.add("proxy-authenticate");
        hopHeaders.add("proxy-connection");
        hopHeaders.add("transfer-encoding");
        hopHeaders.add("te");
        hopHeaders.add("trailer");
        hopHeaders.add("upgrade");
        hopHeaders.add("content-length");
        hopHeaders.add("content-type");
        hopHeaders.add("host");

        HOP_HEADERS = Collections.unmodifiableSet(hopHeaders);
    }


    protected static Logger _log = Logger.getLogger(StraightCompressProxyServlet.class.getName());
    private TransportClient client;
    private ServletConfig config;

    public StraightCompressProxyServlet(String targetUrl, String compressHeader) {
        super();
        _targetUrl = targetUrl;
        _compressHeader = compressHeader;
    }

    @Override
    public void init() throws ServletException {
        config = getServletConfig();
        client = new TransportClient();
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        _log.info( "Work on incoming Request " + req.getRequestURI());

        List<Header> headers = new ArrayList<Header>();

        Set<String> removeHeaders = new HashSet<>();
        removeHeaders.add(_compressHeader);



        // Need to fill Fields
        headers = readHeaders(req);

        headers = filterHeaders(headers,removeHeaders);
        //headers = addForwardHeaders(req,headers);

        // need to add Forwarder


        String targetURI = rewriteTarget(req);
        _log.info( "Target to :" + targetURI);


        byte[] buffer = null;

        boolean shouldCompress = ("gzip".equals(req.getHeader(_compressHeader)) );

        if (req.getContentLength() > -1) {
            buffer = readContent(req, shouldCompress);

            _log.info("forwarding Request with content size: " + buffer.length);

        }

        CloseableHttpResponse forwardResp = null;

        if (buffer == null) {
            forwardResp = client.execute(req.getMethod(), targetURI, headers.toArray(new Header[]{}));
        } else {
            forwardResp = client.execute(req.getMethod(), targetURI, headers.toArray(new Header[]{}), buffer, req.getContentType());
        }


        _log.info("Result from forwarding endpoint :" + forwardResp.getStatusLine());

        byte[] respBody = readResponseContent(forwardResp);
        _log.info( "Response Size :" + respBody.length);

        resp.setStatus(forwardResp.getStatusLine().getStatusCode());

        // copy Headers
        Header[] forwardHeaders = forwardResp.getAllHeaders();
        for (Header h: forwardHeaders) {
            resp.setHeader(h.getName(),h.getValue());
        }

        if (respBody.length > 0) {
            ServletOutputStream os = resp.getOutputStream();

            os.write(respBody);
            os.flush();
            os.close();
        }



        resp.flushBuffer();
    }

    private List<Header> addForwardHeaders(HttpServletRequest clientRequest, List<Header> headers) {
        ArrayList<Header> result = new ArrayList<Header>(headers);

        result.add(new BasicHeader(HttpHeader.X_FORWARDED_FOR.name(), clientRequest.getRemoteAddr()));
        result.add(new BasicHeader(HttpHeader.X_FORWARDED_PROTO.name(), clientRequest.getScheme()));
        result.add(new BasicHeader(HttpHeader.X_FORWARDED_HOST.name(), clientRequest.getHeader(HttpHeader.HOST.asString())));

        result.add(new BasicHeader(HttpHeader.X_FORWARDED_SERVER.name(), clientRequest.getLocalName()));

        return result;
    }

    private List<Header> filterHeaders(List<Header> headers) {
        return filterHeaders(headers,new HashSet<String> ());
    }

    private List<Header> filterHeaders(List<Header> headers, Set<String> headersToRemove) {

        List<Header> result = new ArrayList<>();

        for (Header he : headers) {
            if (HttpHeader.HOST.equals(he.getName()) )
              continue;

            // Remove hop-by-hop headers.
            if (HOP_HEADERS.contains(he.getName().toLowerCase()))
                continue;
            if (headersToRemove != null && headersToRemove.contains(he.getName().toLowerCase()))
                continue;

            result.add(he);

        }


        return result;

    }

    private List<Header> readHeaders(HttpServletRequest req) {

        List<Header> result = new ArrayList<>();

        Enumeration<String> hnames = req.getHeaderNames();

        while (hnames.hasMoreElements()){
            String key = hnames.nextElement();
            Enumeration<String> values = req.getHeaders(key);

            while (values.hasMoreElements()) {
                 result.add(new BasicHeader(key,values.nextElement()));
            }

        }

        return result;

    }

    private byte[] readResponseContent(CloseableHttpResponse response) {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];

                int len;
                int size = 0;
                while ((len = instream.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                    size += len;
                }
                instream.close();
                bos.close();

                return bos.toByteArray();

            }
        } catch (IOException e) {
            _log.info("Problem reading response, will discard response",e);
            return new byte[]{};
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                _log.info("Close Failed on Entity read",e);
                return new byte []{};
            }
        }

        return new byte []{};
    }

    private byte[] readContent(HttpServletRequest request, boolean compress) throws IOException {

        if (compress) _log.info("Compressing Request (" + request.getContentLength() + ")");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStream wrk  = compress?new GZIPOutputStream(os):os;


        InputStream is = request.getInputStream();

        byte[] buffer = new byte[1024];

        int len;
        int size = 0;
        while ((len = is.read(buffer)) > 0) {
            wrk.write(buffer, 0, len);
            size += len;
        }

        is.close();
        os.close();
        wrk.close();


        byte[] dataBuffer = os.toByteArray();

        if (compress) _log.info( "new Size : " + dataBuffer.length);

        return dataBuffer;
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

        return rewrittenURI.toString();
    }


}
