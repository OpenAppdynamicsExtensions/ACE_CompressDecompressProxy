package com.appdynamics.ace.compDecompProxy.command;

import com.appdynamics.ace.util.cli.api.api.AbstractCommand;
import com.appdynamics.ace.util.cli.api.api.Command;
import com.appdynamics.ace.util.cli.api.api.CommandException;
import com.appdynamics.ace.util.cli.api.api.OptionWrapper;
import org.apache.commons.cli.Option;
import org.apache.log4j.spi.RootLogger;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by stefan.marx on 28.09.16.
 */
public abstract class JettyCommand extends AbstractCommand {

    public static final String VERBOSE = "verbose";
    public static final String PORT = "port";
    public static final String DEBUG = "debug";
    private boolean _verbose;

    protected static Logger _log = Logger.getLogger("Jetty");
    private boolean _debug;

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

        return options;
    }


    public boolean isVerboseLogging() {
        return _verbose;
    }
    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {
        _verbose = options.hasOption(VERBOSE) ;
         _debug =  options.hasOption(DEBUG) ;
        if (_verbose) RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);
        if (_debug) RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.DEBUG);


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

        addHandler(http);

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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,null,e);
        }
        return 0;
    }

    protected abstract void addConfig(Server http);

    protected abstract void addHandler(Server http);


}
