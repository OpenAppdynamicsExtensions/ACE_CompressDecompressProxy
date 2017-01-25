package com.appdynamics.ace.compDecompProxy.command;

import com.appdynamics.ace.compDecompProxy.handler.HealthServlet;
import com.appdynamics.ace.util.cli.api.api.AbstractCommand;
import com.appdynamics.ace.util.cli.api.api.CommandException;
import com.appdynamics.ace.util.cli.api.api.OptionWrapper;
import org.apache.commons.cli.Option;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * Created by stefan.marx on 28.09.16.
 */
public abstract class JettyCommand extends AbstractCommand {

    public static final String VERBOSE = "verbose";
    public static final String PORT = "port";
    public static final String DEBUG = "debug";
    private static final String LOG_CONF = "log_config";
    private boolean _verbose;

    protected static Logger _log = Logger.getLogger(JettyCommand.class.getName());
    private boolean _debug;

    public HealthServlet getHealthServlet() {
        return _healthServlet;
    }

    private HealthServlet _healthServlet;

    @Override
    protected List<Option> getCLIOptionsImpl() {

        List<Option> options = new ArrayList<>();

        Option o = null;


        options.add(o = new Option(PORT,true,"portnumber"));
        o.setRequired(true);

        options.add(o = new Option(VERBOSE,false,"verbose output"));
        o.setRequired(false);

        options.add(o = new Option(DEBUG,false,"debug output"));
        o.setRequired(false);

        options.add(o = new Option(LOG_CONF,true,"log4j.xml config file."));
        o.setRequired(false);



        return options;
    }


    public boolean isVerboseLogging() {
        return _verbose;
    }
    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {

        _verbose = options.hasOption(VERBOSE) ;
        _debug =  options.hasOption(DEBUG) ;

        if (!options.hasOption(LOG_CONF)) {


            BasicConfigurator.configure();

            RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR);
            if (_verbose) RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);
            if (_debug) RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.DEBUG);

        }else {
            DOMConfigurator.configure(options.getOptionValue(LOG_CONF));
        }

        int port = Integer.parseInt(options.getOptionValue(PORT));

        InetAddress bindAddress;

        try {
            bindAddress = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }

        InetSocketAddress bind= new InetSocketAddress(bindAddress,port);

        Server http = new Server(port);


        ServletHandler servletHandler = new ServletHandler();

        addHealthHandler(servletHandler);
        addHandler(servletHandler);


        http.setHandler(servletHandler);


        addConfig(http);

        try {
            if(options.hasOption(VERBOSE))  {
                System.out.println("Starting server on port :"+ options.getOptionValue(PORT));
                for (Connector c : http.getConnectors()) {
                    System.out.println("C: "+c.getName());
                    for (EndPoint e : c.getConnectedEndPoints()) {
                        System.out.println("-->"+e.getLocalAddress());
                    }
                }
            }
            http.start();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).error("",e);
        }
        return 0;
    }

    private void addHealthHandler(ServletHandler handler) {

        ServletHolder holder = new ServletHolder(_healthServlet = new HealthServlet());



        handler.addServletWithMapping(holder,"/adProxyHealth");


    }

    protected abstract void addConfig(Server http);

    protected abstract void addHandler(ServletHandler http);


}
