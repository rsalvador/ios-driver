package org.uiautomation.ios.server.command;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.Response;
import org.uiautomation.ios.communication.WebDriverLikeRequest;
import org.uiautomation.ios.server.IOSServerManager;
import org.uiautomation.ios.utils.Command;

import com.google.common.collect.Lists;

public final class ExecuteHostHandler extends BaseCommandHandler {

    public ExecuteHostHandler(IOSServerManager server, WebDriverLikeRequest request) {
        super(server, request);
    }

    @Override
    public Response handle() throws Exception {
        String script = getRequest().getPayload().getString("script");
        JSONArray args = getRequest().getPayload().getJSONArray("args");
        if (args.length() > 0) {
            throw new UnsupportedOperationException(script + ", " + args);
        }
        
        // create response with exitCode/stdout/stderr
        List<String> cmd = Lists.newArrayList();
        script = script.substring(6).trim();
        cmd.add("/bin/sh");
        cmd.add("-c");
        cmd.add(script);
        for (int i = 0; i < args.length(); i++) {
            cmd.add(args.getString(i));
        }
        Command command = new Command(cmd, true);
        command.start();
        int exitCode = command.waitFor(60 * 1000);
        JSONObject jo = new JSONObject();
        jo.put("exitCode", exitCode);
        jo.put("stdout", toOutput(command.getStdOut()));
        jo.put("stderr", toOutput(command.getErr()));

        Response resp = new Response();
        resp.setSessionId(getSession().getSessionId());
        resp.setStatus(0);
        resp.setValue(jo);

        return resp;
    }

    @Override
    public JSONObject configurationDescription() throws JSONException {
        return noConfigDefined();
    }
    
    private static String toOutput(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line: lines) {
            if (sb.length() > 0)
                sb.append('\n');
            sb.append(line);
        }
        return sb.toString();
    }
}
