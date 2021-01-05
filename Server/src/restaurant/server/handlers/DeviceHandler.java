package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import restaurant.server.handlers.orders.UnsentOrder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Base64;

public class DeviceHandler implements HttpHandler {
    private final Intercom intercom;
    private final String splitter = " *: *";
    private final ObjectMapper mapper;
    private Request request;
    private String[] types;

    public DeviceHandler(Intercom intercom) {
        this.intercom = intercom;
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var auth = exchange.getRequestHeaders().get("Authorization").get(0).split("\\s+")[1];
            var decoded = new String(Base64.getDecoder().decode(auth));
            types = decoded.split(":", 2)[1].split(" *: *");
            getRequest(exchange);
            //System.out.println(request.toString());
            switch (exchange.getRequestMethod()) {
                case "GET" -> get(exchange);
                case "POST" -> post(exchange);
                default -> exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

    private void getRequest(HttpExchange exchange) throws Exception {
        if (exchange.getRequestURI().getQuery() != null) {
            request = new Request(exchange.getRequestURI().getQuery());
        } else {
            var reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            request = mapper.readValue(reader, Request.class);
        }
    }

    private void post(HttpExchange exchange) throws IOException {
        boolean result = true;
        var writer = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
        if (request.getRequestParam().equals("message")) {
            var split = request.getRequestArgs().split(splitter, 2);
            intercom.addMessage(split[0], split[1]);
        } else {
            result = false;
        }
        exchange.sendResponseHeaders(result ? 200 : 400, String.valueOf(result).length());
        writer.write(String.valueOf(result));
        writer.flush();
    }

    private void get(HttpExchange exchange) throws IOException {
        var writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = new byte[0];
        if (request.getRequestParam().equals("orders")) {
            var all = new ArrayList<UnsentOrder>();
            for (String type : types) {
                all.addAll(intercom.getOrders(type));
            }
            arr = mapper.writeValueAsBytes(all);
        }
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.flush();
        writer.close();
    }
}
