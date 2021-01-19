package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Request {
    private String requestParam;
    private String requestArgs;

    public Request() {
        requestParam = "";
        requestArgs = "";
    }

    public Request(String requestParam, String requestArgs) {
        this.requestParam = requestParam;
        this.requestArgs = requestArgs;
    }

    public Request(String info) {
        info = info.strip();
        var split = info.split(" *= *", 2);
        if (split.length == 1) {
            requestParam = info;
            requestArgs = "";
        } else if (split.length == 2) {
            requestParam = split[0];
            requestArgs = split[1];
        } else {
            requestParam = "";
            requestArgs = "";
        }
    }

    public static Request getRequest(HttpExchange exchange) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (exchange.getRequestURI().getQuery() == null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            return mapper.readValue(reader, Request.class);
        } else {
            return new Request(exchange.getRequestURI().getQuery());
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestParam='" + requestParam + '\'' +
                ", requestArgs='" + requestArgs + '\'' +
                '}';
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public String getRequestArgs() {
        return requestArgs;
    }

    public void setRequestArgs(String requestArgs) {
        this.requestArgs = requestArgs;
    }

}
