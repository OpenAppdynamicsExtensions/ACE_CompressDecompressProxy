package com.appdynamics.ace.compDecompProxy.command;

import com.appdynamics.ace.compDecompProxy.handler.health.HealthDetailCallback;
import com.appdynamics.ace.util.cli.api.api.CommandException;
import com.appdynamics.ace.util.cli.api.api.OptionWrapper;
import org.apache.commons.cli.Option;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class DebugFileCommand extends JettyCommand {
    private boolean _save = false;
    private String _dir = null;
    private String _filter = null;

    @Override
    public String getName() {
        return "startDebugEndpoint";
    }

    @Override
    protected List<Option> getCLIOptionsImpl() {
        List<Option> options = super.getCLIOptionsImpl();
        Option o = null;
        options.add(o = new Option("dir",true,"Directory to save content payload !"));
        o.setRequired(false);

        options.add(o = new Option("filter",true,"Filter to apply !"));
        o.setRequired(false);

        return options;
    }

    @Override
    protected void addConfig(Server http) {

    }

    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {
        _dir = options.getOptionValue("dir",null) ;
        _save = options.hasOption("dir");
        _filter = options.getOptionValue("filter","") ;


        return super.executeImpl(options);
    }

    @Override
    protected void addHandler(ServletHandler handler) {

        DebugServlet servlet;
        handler.addServletWithMapping( new ServletHolder(servlet =new DebugServlet()),"/");

        getHealthServlet().addHealthDetailCallback(servlet);

    }

    @Override
    public String getDescription() {
        return "Starts a generic endpoint that accepts all data and prints debug information.";
    }

    private class DebugServlet extends HttpServlet implements HealthDetailCallback{


        private long _totalCharsRead;
        private long _totalInputRequested;


        @Override
        public JSONObject getDetailJson() {
            JSONObject erg = new JSONObject();
            erg.put("totalBytes",_totalCharsRead);
            erg.put("totalRequests",_totalInputRequested);
            return erg;
        }


        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Date time = new Date();


            _totalInputRequested++;


            System.out.println("\n-----------------\nDebug : "+req.getMethod().toUpperCase()+"  "+req.getRequestURI());

            System.out.println(new Date());

            if (!_filter.equals("") && !req.getRequestURI().contains(_filter)){
                return;
            }

            // Header
            Enumeration<String> names = req.getHeaderNames();
            while(names.hasMoreElements()) {
                String hn = names.nextElement() ;
                Enumeration<String> values = req.getHeaders(hn);
                while (values.hasMoreElements())  {
                    System.out.println(hn+": "+values.nextElement());
                }

            }

            int len = req.getContentLength();
            System.out.println(":: Content Size ="+len);

            if (len > 0) {
                System.out.println(":: Content Type :"+req.getContentType());

                InputStream r = req.getInputStream();




                byte[] buffer = new byte[80];

                int rlen = -1;

                OutputStream output = null;
                if (_save ) {
                    File dir = new File(_dir);
                    dir = new File(dir,req.getRequestURI().replaceAll("/","_"));

                    dir.mkdirs();

                    output =new FileOutputStream(
                                        new File(dir,System.currentTimeMillis()+"_data.dat"));


                }

                int charsRead = 0;
                System.out.println("Available chars: "+r.available() );

                while ( (rlen = r.read(buffer)) != -1) {

                    charsRead += rlen;
                    String s = new String(buffer,0,rlen);
                    String printS  = s.replaceAll("[^\\p{Graph}]",".");
                    System.out.println(printS);

                     if (output != null) {
                        output.write(buffer,0,rlen);
                    }

                }
                System.out.println("\n----");

                _totalCharsRead += charsRead;


                System.out.println("Char readed: "+charsRead);
                if (output != null) {
                    output.flush();
                    output.close();

                }
            }


            resp.setStatus(200);
            ServletOutputStream sos = resp.getOutputStream();

            PrintWriter pw = new PrintWriter(sos);


            pw.close();
            sos.close();




        }

    }
}
