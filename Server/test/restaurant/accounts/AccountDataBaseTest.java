package restaurant.accounts;

import org.junit.BeforeClass;
import org.junit.Test;
import restaurant.exceptions.AccountDataBaseFileException;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AccountDataBaseTest {
    private static final String FILE = "demo.txt";
    private static AccountDataBase adb;

    @BeforeClass
    public static void setup() throws IOException, AccountDataBaseFileException {
        new FileWriter(FILE, false);
        adb = new AccountDataBase(FILE);
        assertTrue(adb.addUser("nick", "2506", false));
        assertTrue(adb.addUser("admin", "admin", true));
    }

    @Test
    public void testChangePassword() throws AccountDataBaseFileException {
        assertTrue(adb.changePassword("nick", "2506", "5566"));
        assertFalse(adb.changePassword("admin", "2506", "5566"));
        assertFalse(adb.changePassword(null, "2222", ""));
        assertFalse(adb.changePassword("nic", "2222", ""));
        assertFalse(adb.changePassword("nick", "5566", "25:06"));
        assertTrue(adb.changePassword("nick","5566","2506"));
    }

    @Test
    public void testAddUser() throws AccountDataBaseFileException {
        assertFalse(adb.addUser("nick", "55::66", false));
    }

    @Test
    public void testVerify() {
        assertEquals(0, adb.verify("nick", "2506"));
        assertEquals(1, adb.verify("admin", "admin"));
        assertEquals(-1, adb.verify("admin", "ceco"));
    }

    @Test
    public void testRemoveUser() throws AccountDataBaseFileException {
        assertTrue(adb.addUser("hey", "hey", true));
        assertTrue(adb.removeUser("hey"));
        assertEquals(-1,adb.verify("hey","hey"));
    }

    @Test
    public void testGetStatus() throws AccountDataBaseFileException {
        assertTrue(adb.changeStatus("nick"));
        assertTrue(adb.changeStatus("nick"));
        assertFalse(adb.changeStatus("demo"));
    }

    @Test
    public void testGetAllAccounts() {
        assertTrue(adb.getAllAccounts().stream().map(AccountForDevice::getUsername)
                .allMatch(i -> i.equals("nick") || i.equals("admin")));
    }
}