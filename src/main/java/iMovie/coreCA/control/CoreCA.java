package iMovie.coreCA.control;

import iMovie.coreCA.exception.CertificateNotGeneratedException;
import iMovie.coreCA.model.Certificate;
import iMovie.coreCA.model.UserData;
import iMovie.coreCA.utility.DBInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ivy on 02/11/14.
 */
public class CoreCA {

    private static final Logger LOGGER = LogManager.getLogger(CoreCA.class.getName());
    private DBInterface dbInterface;

    public CoreCA (DBInterface dbInterface) {
        this.dbInterface = dbInterface;
    }

    /**
     * Creates a certificate for the user specified as argument
     * @return the descriptor of the file containing the certificate in the PKCS#12 format
     */
    public File generateCertificate(String userName, String password) throws CertificateNotGeneratedException {


        UserData userData = dbInterface.getUserData(userName, password);

        if (userData == null) {
            //Exception not generated from user data , but still anything has been retrieved.
            //Aborting with unknown error.
            throw new CertificateNotGeneratedException(new boolean[]{false, false, false, false});
        }

        File confFile = null;
        try {
            confFile = new Certificate(userData).writeConfFile();
        } catch (IOException e) {
            throw new CertificateNotGeneratedException(new boolean[]{false, true, false, false});
        }


        Process certGen;
        try {
            certGen = Runtime.getRuntime().exec("/etc/ssl/CA/cert_gen.sh " + confFile.getAbsolutePath() + " /root/pkcs12/" + userName + ".p12" + " " + password);
        } catch (IOException e) {
            throw new CertificateNotGeneratedException(new boolean[]{false, false, true, false});
        }



        while (certGen.isAlive()) {
            try {
                certGen.waitFor();
            } catch (InterruptedException e) {
                //Ignore and continue waiting
            }
        }

        LOGGER.trace("Certificate for user: " + userName + " generated correctly.");

        return new File("/root/pkcs12/" + userName + ".p12");
    }

    public DBInterface getDBInterface() {
        return this.dbInterface;
    }
}
