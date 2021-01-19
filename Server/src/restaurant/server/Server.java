package restaurant.server;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import restaurant.accounts.AccountDataBase;
import restaurant.exceptions.AccountDataBaseFileException;
import restaurant.intercom.Intercom;
import restaurant.server.handlers.AdminHandler;
import restaurant.server.handlers.DeviceHandler;
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
    private static final String HOST = "192.168.43.245";
    private static final Integer PORT = 2506;
    private static final String SPLITTER = " *: *";
    private static final String DEVICE_ADDRESS = "/device";
    private static final String TABLE_ADDRESS = "/table";
    private static final String ADMIN_ADDRESS = "/admin";
    private static final String TYPE_ADDRESS = "/type";

    private final Intercom intercom;
    private final ProductDataBase pdb;
    private final AccountDataBase adb;
    private final HttpServer server;

    public Server(String productDBFile, String restaurantName, String billPath, String backupFilePath,
                  String accountsFilePath, String hostAddress, int port) throws IOException,
            AccountDataBaseFileException {
        pdb = new ProductDataBase(productDBFile);
        intercom = new Intercom(pdb, restaurantName, billPath, backupFilePath);
        adb = new AccountDataBase(accountsFilePath);
        server = HttpServer.create(new InetSocketAddress(hostAddress, port), 0);
    }

    public static void main(String[] args) throws IOException, AccountDataBaseFileException {
        Server server = new Server(PRODUCTS_DB, RESTAURANT_NAME, BILL_PATH, BACKUP, ACCOUNTS_DB, HOST, PORT);
        server.start();
    }

    private void start() {
        createClientContext(server);
        createDeviceContext(server);
        createTypeContext(server);
        createAdminContext(server);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private void stop() {
        server.stop(10);
    }

    private void createTypeContext(HttpServer server) {
        server.createContext(TYPE_ADDRESS, new TypeHandler(pdb));
    }

    private void createDeviceContext(HttpServer server) {
        HttpContext context = server.createContext(DEVICE_ADDRESS);
        context.setAuthenticator(new BasicAuthenticator(DEVICE_ADDRESS) {
            @Override
            public boolean checkCredentials(String username, String password) {
                return username.equals("device") &&
                        pdb.getAllTypes().containsAll(Arrays.asList(password.split(SPLITTER)));
            }
        });
        context.setHandler(new DeviceHandler(intercom));
    }

    private void createClientContext(HttpServer server) {
        HttpContext context = server.createContext(TABLE_ADDRESS);
        context.setAuthenticator(new BasicAuthenticator(TABLE_ADDRESS) {
            @Override
            public boolean checkCredentials(String username, String password) {
                return adb.verify(username, password) >= 0;
            }
        });
        context.setHandler(new TableHandler(intercom, adb));
    }

    private void createAdminContext(HttpServer server) {
        HttpContext context = server.createContext(ADMIN_ADDRESS);
        context.setAuthenticator(new BasicAuthenticator(ADMIN_ADDRESS) {
            @Override
            public boolean checkCredentials(String username, String password) {
                return adb.verify(username, password) == 1;
            }
        });
        context.setHandler(new AdminHandler(intercom, adb, pdb, BILL_PATH));
    }

}
