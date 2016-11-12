package com.appdynamics.ace.compDecompProxy.command;

import com.appdynamics.ace.compDecompProxy.handler.CompressProxyServlet;
import com.appdynamics.ace.util.cli.api.api.CommandException;
import com.appdynamics.ace.util.cli.api.api.OptionWrapper;
import org.apache.commons.cli.Option;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class CompressingProxyCommand extends JettyCommand{

    public static final String TARGET = "target";
    public static final String COMPRESS_HEADER = "compressHeader";
    public static final String X_SHOULD_COMPRESS = "X-should-compress";
    private String _targetUrl;
    private String _compressHeader;

    @Override
    protected List<Option> getCLIOptionsImpl() {
        List<Option> options = super.getCLIOptionsImpl();
        Option o = null;
        options.add(o = new Option(TARGET,true,"TargetURL To Forward"));
        o.setRequired(true);

        options.add(o = new Option(COMPRESS_HEADER,true,
                "Header that signals request compression needed (X-should-compress)"));
        o.setRequired(false);


        return options;
    }
    @Override
    protected void addConfig(Server http) {

    }

    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {
        _targetUrl = options.getOptionValue(TARGET);
        _compressHeader = options.getOptionValue(COMPRESS_HEADER, X_SHOULD_COMPRESS);

        return super.executeImpl(options);
    }

    @Override
    protected void addHandler(ServletHandler handler) {

        ServletHolder holder = new ServletHolder(new CompressProxyServlet(_targetUrl, _compressHeader));


       // holder.setAsyncSupported(true);
        holder.setInitParameter("maxThreads", "20");

        handler.addServletWithMapping(holder,"/");
    }

    @Override
    public String getName() {
        return "compressAndForward";
    }

    @Override
    public String getDescription() {
        return "Compress the request if needed and forward !";
    }
}
