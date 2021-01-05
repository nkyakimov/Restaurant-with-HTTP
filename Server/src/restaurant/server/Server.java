package restaurant.server;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import restaurant.accounts.AccountDataBase;
import restaurant.server.handlers.AdminHandler;
import restaurant.server.handlers.DeviceHandler;
import restaurant.server.handlers.Intercom;
import restaurant.server.handlers.TableHandler;
import restaurant.server.handlers.TypeHandler;
import restaurant.storage.ProductDataBase;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class Server {
    private static final String RESTAURANT_NAME = "NM";
    private static final String PRODUCTS_DB = System.getProperty("user.dir") + File.separator + "products.json";
    private static final String ACCOUNTS_DB = System.getProperty("user.dir") + File.separator + "accounts.sg";
    private static final String BACKUP = System.getProperty("user.dir") + File.separator + "backup.sg";
    private static final String BILL_PATH = System.getProperty("user.dir") + File.separator + "bills";
    private static final String HOST = "192.168.1.245";
    private static final Integer PORT = 2506;

    private final Intercom intercom;
    private final ProductDataBase pdb;
    private final AccountDataBase adb;

    public Server() {
        pdb = new ProductDataBase(PRODUCTS_DB);
        intercom = new Intercom(pdb, RESTAURANT_NAME, BILL_PATH, BACKUP);
        adb = new AccountDataBase(ACCOUNTS_DB);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        createClientContext(server);
        createDeviceContext(server);
        createTypeContext(server);
        createAdminContext(server);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private void createTypeContext(HttpServer server) {
        var context = server.createContext("/types", new TypeHandler(pdb));
    }

    private void createDeviceContext(HttpServer server) {
        var context = server.createContext("/device");
        context.setAuthenticator(new BasicAuthenticator("device") {
            @Override
            public boolean checkCredentials(String username, String password) {
                return username.equals("device") && pdb.getAllTypes().containsAll(Arrays.asList(password.split(" *: " +
                        "*")));
            }
        });
        context.setHandler(new DeviceHandler(intercom));
    }

    private void createClientContext(HttpServer server) {
        var context = server.createContext("/table");
        context.setAuthenticator(new BasicAuthenticator("table") {
            @Override
            public boolean checkCredentials(String username, String password) {
                return adb.verify(username, password) >= 0;
            }
        });
        context.setHandler(new TableHandler(intercom, adb));
    }

    private void createAdminContext(HttpServer server) {
        var context = server.createContext("/admin");
        context.setAuthenticator(new BasicAuthenticator("admin") {
            @Override
            public boolean checkCredentials(String username, String password) {
                return adb.verify(username, password) == 1;
            }
        });
        context.setHandler(new AdminHandler(intercom, adb, pdb,BILL_PATH));
    }

}
