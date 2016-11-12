package com.appdynamics.ace.compDecompProxy.handler;

import com.appdynamics.ace.compDecompProxy.command.DebugFileCommand;
import com.appdynamics.ace.compDecompProxy.handler.health.HealthDetailCallback;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan.marx on 12.11.16.
 */
public class HealthServlet extends HttpServlet {
    protected static Logger _log = Logger.getLogger(HealthServlet.class.getName());
    private final long _startupTimeMS;

    private List<HealthDetailCallback> _detailCallbacks  = new ArrayList<>();

    public HealthServlet() {
        _startupTimeMS =System.currentTimeMillis();

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JSONObject obj = new JSONObject();

        obj.put("health","ok");
        obj.put("uptime",(int)((System.currentTimeMillis()-_startupTimeMS)/1000));

        JSONArray details = retrieveDetails();
        obj.put("details",details);

        ServletOutputStream os = resp.getOutputStream();
        os.print(obj.toJSONString());

        _log.info("Healthcheck Delivered :\n"+obj.toJSONString());

        os.flush();

    }

    private JSONArray retrieveDetails() {
        JSONArray erg = new JSONArray();

        for (HealthDetailCallback cb : _detailCallbacks)  {
            erg.add(cb.getDetailJson());
        }

        return erg;
    }

    public void addHealthDetailCallback(HealthDetailCallback hcDetail) {
        _detailCallbacks.add(hcDetail);

    }
}
