package restaurant.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import restaurant.storage.ProductDataBase;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TypeHandler implements HttpHandler {
    private final ProductDataBase pdb;

    public TypeHandler(ProductDataBase pdb) {
        this.pdb = pdb;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestMethod().equals("GET") && exchange.getRequestURI().getQuery().equals("types")) {
                get(exchange);
            } else if (exchange.getRequestMethod().equals("GET") && !exchange.getRequestURI().getQuery().isEmpty()) {
                getProductsByType(exchange);
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

    private void getProductsByType(HttpExchange exchange) throws IOException {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        BufferedOutputStream writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = mapper.writeValueAsBytes(
                pdb.getProductsByTypes(Arrays.asList(exchange.getRequestURI().getQuery().split(" *: *"))));
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.close();
    }

    private void get(HttpExchange exchange) throws IOException {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        BufferedOutputStream writer = new BufferedOutputStream(exchange.getResponseBody());
        byte[] arr = mapper.writeValueAsBytes(pdb.getAllTypes());
        exchange.sendResponseHeaders(arr.length > 0 ? 200 : 400, arr.length);
        writer.write(arr);
        writer.close();
    }
}
