package iMovie.coreCA.utility;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import iMovie.coreCA.exception.CertificateNotGeneratedException;
import iMovie.coreCA.model.UserData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Objects of this class can communicate with the database and retrieve data from it
 */
public class DBInterface {

    private static final Logger LOGGER = LogManager.getLogger(DBInterface.class.getName());

    private static final String USER_QUERY = "SELECT * FROM users WHERE uid=? AND pwd=?";

    private Connection connection = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    public DBInterface(String ipAddress) throws SQLException {
        LOGGER.info("Connecting to the database..");
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ipAddress + "/imovies?" + "user=coreCA&password=imovie");
        } catch (SQLException ex) {
            LOGGER.error(ex);
            LOGGER.trace("SQLException: ", ex.getMessage());
            LOGGER.trace("SQLState: " + ex.getSQLState());
            LOGGER.trace("VendorError: " + ex.getErrorCode());
            throw ex;
        }
        LOGGER.info("Connection success.");
    }

    public UserData getUserData(String username, String password) throws CertificateNotGeneratedException {
        LOGGER.trace("Requested user data for user: " + username);
        MessageDigest messageDigest;
        UserData userData = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("algorithm not supported...", e);
            return null;
        }
        String hashPassword = HexBin.encode(messageDigest.digest(password.getBytes())).toLowerCase();
        try {
            stmt = connection.prepareStatement(USER_QUERY);
            stmt.setString(1, username);
            stmt.setString(2, hashPassword);
            rs = stmt.executeQuery();
            if (rs.next()) {
                userData = new UserData(rs);
            } else {
                LOGGER.trace("Wrong username/password.");
                throw new CertificateNotGeneratedException(new boolean[]{true, false, false, false});
            }
        } catch (SQLException ex) {
            // handle any errors
            LOGGER.error(ex);
            LOGGER.trace("SQLException: " + ex.getMessage());
            LOGGER.trace("SQLState: " + ex.getSQLState());
            LOGGER.trace("VendorError: " + ex.getErrorCode());
        } finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    //ignore
                }

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                stmt = null;
            }
        }
        LOGGER.trace("Data for user: " + username +" fetched.");
        return userData;
    }

    protected void closeConnection() {
        LOGGER.trace("Closing the DB connection..");
        if (!Thread.currentThread().getClass().isAssignableFrom(CleanerHook.class)) {
            return;
        }
        try {
            this.connection.close();
        } catch (SQLException e) {
            LOGGER.trace("Error while closing the connection.. Continuing anyway", e);
        }
        LOGGER.trace("Connection closed.");
    }
}
