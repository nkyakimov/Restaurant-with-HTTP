package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import restaurant.accounts.AccountDataBase;
import restaurant.storage.Product;
import restaurant.storage.ProductDataBase;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class AdminHandler implements HttpHandler {
    private final Intercom intercom;
    private final AccountDataBase adb;
    private final ProductDataBase pdb;
    private final ObjectMapper mapper;
    private final String billPath;
    private final String splitter = " *: *";
    private Request request;
    private String username;


    public AdminHandler(Intercom intercom, AccountDataBase adb, ProductDataBase pdb, String billPath) {
        this.intercom = intercom;
        this.adb = adb;
        this.pdb = pdb;
        this.billPath = billPath;
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var auth = exchange.getRequestHeaders().get("Authorization").get(0).split("\\s+")[1];
        var decoded = new String(Base64.getDecoder().decode(auth));
        username = decoded.split(":")[0];
        try {
            getRequest(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> get(exchange);
                case "POST" -> post(exchange);
                case "DELETE" -> delete(exchange);
                default -> exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

    private void post(HttpExchange exchange) throws Exception {
        boolean result = false;
        switch (request.getRequestParam()) {
            case "changePassword" -> {
                var split = request.getRequestArgs().split(splitter, 3);
                if (split.length == 3) {
                    result = adb.changePassword(split[0], split[1], split[2]);
                }
            }
            case "newUser" -> {
                var split = request.getRequestArgs().split(splitter, 3);
                if (split.length == 3) {
                    adb.addUser(split[0], split[1], Boolean.parseBoolean(split[2]));
                    result = true;
                }
            }
            case "changeProduct" -> result =
                    pdb.changeProduct(mapper.readValue(request.getRequestArgs(), Product.class));
            case "newProduct" -> result = pdb.addProduct(mapper.readValue(request.getRequestArgs(), Product.class));
            case "switchUser" -> {
                if (!username.equals(request.getRequestArgs())) {
                    result = adb.changeStatus(request.getRequestArgs());
                }
            }
        }
        exchange.sendResponseHeaders(result ? 200 : 400, 0);
    }

    private void delete(HttpExchange exchange) throws IOException {
        boolean result = false;
        try {
            switch (request.getRequestParam()) {
                case "deleteTable" -> result = intercom.removeTable(Integer.parseInt(request.getRequestArgs()), true);
                case "removeFromTable" -> {
                    var split = request.getRequestArgs().split(splitter);
                    result = intercom.removeFromTable(Integer.parseInt(split[0]), split[1], true);
                }
                case "removeProduct" -> result = pdb.removeProduct(request.getRequestArgs());
                case "removeAccount" -> {
                    intercom.removeUser(request.getRequestArgs());
                    result = adb.removeUser(request.getRequestArgs());
                }
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            result = false;
        }
        exchange.sendResponseHeaders(result ? 200 : 400, String.valueOf(result).length());
    }

    private void get(HttpExchange exchange) throws IOException {
        var writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = new byte[0];
        switch (request.getRequestParam()) {
            case "tables" -> arr = mapper.writeValueAsBytes(intercom.getTables("", true));
            case "products" -> arr = mapper.writeValueAsBytes(pdb.getAllProducts());
            case "accounts" -> arr = mapper.writeValueAsBytes(adb.getAllAccounts());
            case "bills" -> arr = getBills(request.getRequestArgs());
        }
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.flush();
        writer.close();
    }

    private byte[] getBills(String args) throws IOException {
        if (args.equals("ALL") || args.isEmpty()) {
            var files = new File(billPath).listFiles();
            ArrayList<String> bills = null;
            if (files != null) {
                bills = Arrays.stream(files).map(File::getName).collect(Collectors.toCollection(ArrayList::new));
            }
            return mapper.writeValueAsBytes(bills);
        } else {
            File file = new File(billPath + File.separator + args);
            if (file.isFile()) {
                var reader = new BufferedReader(new FileReader(file));
                return mapper.writeValueAsBytes(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            } else {
                return new byte[0];
            }
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
}
