package iMovie.coreCA.utility;

import iMovie.coreCA.exception.CertificateNotGeneratedException;
import iMovie.coreCA.model.UserData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DBInterfaceTest {

    private static DBInterface testInterface = null;

    @Before
    public void setUp() throws Exception {
        if (testInterface == null) {
            testInterface = new DBInterface("172.16.129.130");
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Test of a valid entry
     * @throws Exception
     */
    @Test
    public void testGetUserDataValid() throws Exception {
        UserData david = testInterface.getUserData("db", "D15Licz6");
        assertNotNull(david);
        assertTrue("db".equals(david.getUid()));
        assertTrue("David".equals(david.getFirstName()));
        assertTrue("Basin".equals(david.getLastName()));
        assertTrue("db@imovies.ch".equals(david.getEmail()));
        assertTrue("8d0547d4b27b689c3a3299635d859f7d50a2b805".equals(david.getPwd()));
    }

    /**
     * Test against common sql injections
     * @throws Exception
     */
    @Test(expected=CertificateNotGeneratedException.class)
    public void testGetUserDataInjection() throws Exception {
        testInterface.getUserData("db' -- ","as");
        assertTrue(false);
    }

    @Test(expected=CertificateNotGeneratedException.class)
    public void testGetUserDataInjection1() throws Exception {
        testInterface.getUserData("db","’ or ’1’ = ’1");
        assertTrue(false);
    }

}