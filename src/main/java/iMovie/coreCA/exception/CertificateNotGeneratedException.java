package iMovie.coreCA.exception;

import com.sun.istack.internal.NotNull;

import java.sql.SQLException;

/**
 * Created by Ivy on 02/11/14.
 */
public class CertificateNotGeneratedException extends Exception {

    private static final String[] ERROR_MSG = {
            "Unable to access the database to retrieve user data.",
            "Wrong username or password provided.",
            "IOError occur when writing the configuration file for the certificate...",
            "IOError occur when executing the script to generate the certificate...",
            "Potential SQL injection prevented."
    };

    private final boolean databaseAccess;
    private final boolean wrongUP;
    private final boolean confFileNotGen;
    private final boolean scriptError;

    private final boolean potentialSQLInjection;

    private final SQLException e;

    private String message;


    public CertificateNotGeneratedException(SQLException e) {
        this.e = e;
        databaseAccess = true;
        wrongUP = false;
        confFileNotGen = false;
        scriptError = false;
        potentialSQLInjection = false;
        message = ERROR_MSG[0];
    }

    /**
     * Constructor of the exception:
     * the errorArray parameter have the following meaning:
     * errorArray[0] --> wrong User/Password
     * errorArray[1] --> unable to create the configuration file due to a IOError
     * errorArray[2] --> error during script execution
     * errorArray[3] --> potential SQL injection
     * @param errorArray an array of booleans representing various problem scenarios
     */
    public CertificateNotGeneratedException(@NotNull boolean[] errorArray) {

        if (errorArray.length != 4) {
            throw new IllegalArgumentException("provided malformed error array");
        }

        this.databaseAccess = false;
        this.e = null;

        this.wrongUP = errorArray[0];
        this.confFileNotGen = errorArray[1];
        this.scriptError = errorArray[2];
        this.potentialSQLInjection = errorArray[3];

        for (int i = 1; i < ERROR_MSG.length; i++) {
            if (errorArray[i - 1]) {
                message = ERROR_MSG[i];
                break;
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean isDatabaseAccess() {
        return databaseAccess;
    }

    public boolean isWrongUP() {
        return wrongUP;
    }

    public boolean isConfFileNotGen() {
        return confFileNotGen;
    }

    public boolean isScriptError() {
        return scriptError;
    }

    public SQLException getE() {
        return e;
    }

    public boolean isPotentialSQLInjection() {
        return potentialSQLInjection;
    }

}
