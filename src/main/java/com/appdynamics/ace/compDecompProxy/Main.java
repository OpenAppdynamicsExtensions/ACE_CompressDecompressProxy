package com.appdynamics.ace.compDecompProxy;

import com.appdynamics.ace.compDecompProxy.command.DebugFileCommand;
import com.appdynamics.ace.util.cli.api.api.CommandlineExecution;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class Main {

    public static void main(String[] args) {


        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);

        CommandlineExecution cli = new CommandlineExecution("compDecompProxy");

        cli.setHelpVerboseEnabled(true);

        cli.addCommand(new DebugFileCommand());
        cli.addCommand(new CompressingProxyCommand());
        cli.addCommand(new ExpandingProxyCommand());


        cli.execute(args);

    }
}
