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
    private boolean _verbose;

    protected static Logger _log = Logger.getLogger("Jetty");

    @Override
    protected List<Option> getCLIOptionsImpl() {

        List<Option> options = new ArrayList<>();

        Option o = null;


        options.add(o = new Option(PORT,true,"portnumber"));
        o.setRequired(true);

        options.add(o = new Option(VERBOSE,false,"verbose output"));
        o.setRequired(false);

        return options;
    }


    public boolean isVerboseLogging() {
        return _verbose;
    }
    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {
        _verbose = options.hasOption(VERBOSE) ;

        if (_verbose) RootLogger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);


        Server http = new Server(Integer.parseInt(options.getOptionValue(PORT)));

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
