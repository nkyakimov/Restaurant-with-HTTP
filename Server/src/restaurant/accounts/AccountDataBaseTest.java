package restaurant.accounts;

import org.junit.jupiter.api.BeforeAll;
import restaurant.exceptions.UserAlreadyThereException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDataBaseTest {
    private static final String FILE = "demo.txt";
    private static AccountDataBase adb;

    @BeforeAll
    static void setup() throws IOException {
        File file = new File(FILE);
        if (file.exists() || file.createNewFile()) {
            new FileOutputStream(file).close();
            adb = new AccountDataBase(FILE);
            assertDoesNotThrow(() -> adb.addUser("nick", "2506", false));
            assertDoesNotThrow(() -> adb.addUser("admin", "admin", true));
        }
    }

    @org.junit.jupiter.api.Test
    void changePassword() {
        assertTrue(adb.changePassword("nick", "2506", "5566"));
        assertFalse(adb.changePassword("admin", "2506", "5566"));
        assertEquals(adb.verify("nick", "5566"), 0);
        assertEquals(-1, adb.verify("nick", "2506"));
        assertEquals(adb.verify("admin", "admin"), 1);
        assertDoesNotThrow(() -> adb.changePassword(null, "2222", ""));
        assertDoesNotThrow(() -> adb.changePassword("nic", "2222", ""));
    }

    @org.junit.jupiter.api.Test
    void addUser() {
        assertThrows(UserAlreadyThereException.class, () -> adb.addUser("nick", "5566", false));
    }

    @org.junit.jupiter.api.Test
    void verify() {
        assertEquals(adb.verify("nick", "2506"), 0);
        assertEquals(adb.verify("admin", "admin"), 1);
        assertEquals(-1, adb.verify("admin", "ceco"));
    }
}