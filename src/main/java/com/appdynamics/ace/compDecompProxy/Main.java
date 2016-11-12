package com.appdynamics.ace.compDecompProxy;

import com.appdynamics.ace.compDecompProxy.command.*;
import com.appdynamics.ace.util.cli.api.api.CommandlineExecution;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Created by stefan.marx on 28.09.16.
 */
public class Main {

    public static void main(String[] args) {


        CommandlineExecution cli = new CommandlineExecution("compDecompProxy");

        cli.setHelpVerboseEnabled(false);

        cli.addCommand(new DebugFileCommand());
        cli.addCommand(new CompressingProxyCommand());
        cli.addCommand(new ExpandingProxyCommand());
        cli.addCommand(new TestloadCommand());
        cli.addCommand(new StraightCompressingProxyCommand());


        cli.execute(args);

    }
}
