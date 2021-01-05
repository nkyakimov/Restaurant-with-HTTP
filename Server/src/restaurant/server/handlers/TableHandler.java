package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import restaurant.accounts.AccountDataBase;
import restaurant.table.Table;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TableHandler implements HttpHandler {
    private static final String splitter = " *: *";
    private final AccountDataBase adb;
    private final Intercom intercom;
    private final ObjectMapper mapper;
    private String username;
    private boolean admin;
    private Request request;

    public TableHandler(Intercom intercom, AccountDataBase adb) {
        this.adb = adb;
        this.intercom = intercom;
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var auth = exchange.getRequestHeaders().get("Authorization").get(0).split("\\s+")[1];
            var decoded = new String(Base64.getDecoder().decode(auth));
            username = decoded.split(":")[0];
            admin = adb.verify(username, decoded.split(splitter)[1]) == 1;
            getRequest(exchange);
            //System.out.println(request.toString());
            switch (exchange.getRequestMethod()) {
                case "GET" -> get(exchange);
                case "POST" -> post(exchange);
                case "DELETE" -> delete(exchange);
                default -> exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

    private void getRequest(HttpExchange exchange) throws Exception {
        if (exchange.getRequestURI().getQuery() == null) {
            var reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            request = mapper.readValue(reader, Request.class);
        } else {
            request = new Request(exchange.getRequestURI().getQuery());
        }
    }

    private void delete(HttpExchange exchange) throws IOException {
        boolean result = false;
        try {
            if (request.getRequestParam().equals("bill")) { // bill : 5
                result = intercom.bill(Integer.parseInt(request.getRequestArgs()), username);
            }
            /*
            else if (request.getRequestParam().equals("delete") && admin) { // delete 5
                result = intercom.removeTable(Integer.parseInt(request.getRequestArgs()), admin);
            }
            else if (request.getRequestParam().equals("remove") && admin) { // remove : 5:35 removes from table 5
                // product with id 35
                var split = request.getRequestArgs().split(splitter);
                result = intercom.removeFromTable(Integer.parseInt(split[0]), Integer.parseInt(split[1]), admin);
            }
            */
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            result = false;
        }
        exchange.sendResponseHeaders(result ? 200 : 400, String.valueOf(result).length());
        var writer = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
        writer.write(String.valueOf(result));
        writer.flush();
    }

    private void post(HttpExchange exchange) throws IOException {
        boolean result = false;
        try {
            switch (request.getRequestParam()) {
                case "order" -> result = getOrder();
                case "create" -> result = intercom.createTable(Integer.parseInt(request.getRequestArgs()), username);
                case "newPassword" -> {
                    var split = request.getRequestArgs().split(splitter, 2);
                    result = adb.changePassword(username, split[0], split[1]);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            result = false;
        }
        exchange.sendResponseHeaders(result ? 200 : 400, String.valueOf(result).length());
        var writer = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody()));
        writer.write(String.valueOf(result));
        writer.flush();
    }

    private boolean getOrder() throws NumberFormatException {
        var split = request.getRequestArgs().split(splitter, 3);
        if (split.length == 2) {
            return intercom.order(Integer.parseInt(split[0]), split[1], username, "");
        } else if (split.length == 3) {
            return intercom.order(Integer.parseInt(split[0]), split[1], username, split[2]);
        } else {
            return false;
        }
    }

    private void get(HttpExchange exchange) throws IOException {
        var writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = new byte[0];
        switch (request.getRequestParam()) {
            case "tables" -> arr = mapper.writeValueAsBytes(getTables());
            case "products" -> arr = mapper.writeValueAsBytes(intercom.getProducts(request.getRequestArgs()));
            case "notifications" -> arr = mapper.writeValueAsBytes(intercom.getMessages(username));
        }
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.flush();
        writer.close();
    }

    private List<Table> getTables() throws NumberFormatException {
        if (request.getRequestArgs().equals("ALL")) {
            return new ArrayList<>(intercom.getTables(username, admin).values());
        } else {
            return Arrays.stream(request.getRequestArgs().split(splitter))
                    .mapToInt(Integer::parseInt)
                    .mapToObj(tableNumber -> intercom.getTable(username, tableNumber, admin))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}
