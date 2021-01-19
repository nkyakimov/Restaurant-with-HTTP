package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import restaurant.intercom.Intercom;
import restaurant.intercom.orders.UnsentOrder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DeviceHandler implements HttpHandler {
    private static final String SPLITTER = " *: *";
    private final Intercom intercom;
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
            String auth = exchange.getRequestHeaders().get("Authorization").get(0).split("\\s+")[1];
            String decoded = new String(Base64.getDecoder().decode(auth));
            types = decoded.split(":", 2)[1].split(SPLITTER);
            request = Request.getRequest(exchange);
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

    private void post(HttpExchange exchange) throws IOException {
        boolean result = true;
        if (request.getRequestParam().equals("message")) {
            String[] split = request.getRequestArgs().split(SPLITTER, 2);
            intercom.addMessage(split[0], split[1]);
        } else {
            result = false;
        }
        exchange.sendResponseHeaders(result ? 200 : 400, 0);
    }

    private void get(HttpExchange exchange) throws IOException {
        BufferedOutputStream writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = new byte[0];
        if (request.getRequestParam().equals("orders")) {
            List<UnsentOrder> all = new ArrayList<>();
            for (String type : types) {
                all.addAll(intercom.getOrders(type));
            }
            arr = mapper.writeValueAsBytes(all);
        }
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.close();
    }
}
