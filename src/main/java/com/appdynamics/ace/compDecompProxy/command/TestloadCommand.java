package com.appdynamics.ace.compDecompProxy.command;

import com.appdynamics.ace.compDecompProxy.handler.TransportClient;
import com.appdynamics.ace.util.cli.api.api.AbstractCommand;
import com.appdynamics.ace.util.cli.api.api.Command;
import com.appdynamics.ace.util.cli.api.api.CommandException;
import com.appdynamics.ace.util.cli.api.api.OptionWrapper;
import org.apache.commons.cli.Option;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by stefan.marx on 12.10.16.
 */
public class TestloadCommand extends AbstractCommand {
    @Override
    protected List<Option> getCLIOptionsImpl() {
        List<Option> opts = new ArrayList<>();

        Option o;

        opts.add(o=new Option("target",true,"Target URL")) ;
        o.setRequired(true);

        return opts;
    }

    @Override
    protected int executeImpl(OptionWrapper options) throws CommandException {
        TransportClient client = new TransportClient();


        String payloadString = "H4sIAAAAAAAAAE1RyW7bMBD9FYNnSyap4TK6Fegtx6KnICi4DGXBjuTIUtMgyL936A" +
                "RJTrPgbUPev4r15UKiFxOtz/NyahZ62ui6ir2gJHrlTMfdmBnhM3qjo2osOtNA1KXxWtomWkdRdSHYJJnG" +
                "3F6BMxINKPCd5dVWd8qBQytZ7jvCokFefSCMlF7vRTwN7FjC+Up7sS1ndj+u66U/HGqJ49TOy3DIdA4vB82e" +
                "l8SQzrHVceHUWrJLWgamAXXK29I1mYBDK7BNCCk0FKUOiCQzGhaI61X09w8clejTOPwdOVStLKRaxbgw1B5a" +
                "3dq2Hhsij2jIUba2UPEJdYwegwFpNQYHSZlI0iTwwPj8yPhtOk3z83QbZ55/THmZx7z79fNuF7fxvO7KvOz+e" +
                "fvH3jgcTaHnf1grWw" +
                "F63qbC/e9PpVTP3ov5WvO9Z0vhS7uO/MKiG+qxt+/EInNgWZ9IFXTFIhZIWqKGAJhJvD38B2ImiO8eAgAA";

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("kkk","kkkk"));
        headers.add(new BasicHeader("kkk","kkkk"));
        headers.add(new BasicHeader("kkk","kkkk"));
        headers.add(new BasicHeader("kkk","kkkk"));
        headers.add(new BasicHeader("kkk","kkkk"));
        headers.add(new BasicHeader("kkk","kkkk"));


        Header [] h = headers.toArray(new Header[]{});

        byte[] buffer = payloadString.getBytes();

        try {
            HttpResponse resp = client.execute("post",options.getOptionValue("target"),
                            h,
                            buffer,"application/json");

            System.out.println(resp.getStatusLine());

        } catch (IOException e) {
            e.printStackTrace();
        }


        return 0;

    }

    @Override
    public String getName() {
        return "sendTestload";
    }

    @Override
    public String getDescription() {
        return "Sends artificial testload to test stack";
    }
}
