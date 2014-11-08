package iMovie.coreCA.control;

import iMovie.coreCA.exception.CertificateNotGeneratedException;
import iMovie.coreCA.model.Certificate;
import iMovie.coreCA.model.UserData;
import iMovie.coreCA.utility.CertStatus;
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
    private static final String PWD = "s(aCT*2{XNvFKZeaTdhW";


    private DBInterface dbInterface;

    public CoreCA (DBInterface dbInterface) {
        this.dbInterface = dbInterface;
    }

    /**
     * Creates a certificate for the user specified as argument.
     * If a valid certificate for the user specified already exist, no certificate is generated.
     * @return the descriptor of the file containing the certificate in the PKCS#12 format
     */
    public File generateCertificate(String userName, String password) throws CertificateNotGeneratedException {


        UserData userData = dbInterface.getUserData(userName, password);

        if (userData == null) {
            //Exception not generated from user data , but still anything has been retrieved.
            //Aborting with unknown error.
            throw new CertificateNotGeneratedException(new boolean[]{false, false, false, false});
        }

        File confFile;
        Certificate certificate = new Certificate(userData);

        try {
            if (certificate.getStatus().equals(CertStatus.VALID)) {
                LOGGER.trace("Creating a certificate when it already exist.");
                return null;
            }
        } catch (IOException e) {
            LOGGER.trace("Unable to read the index file.");
            return null;
        }

        try {
            confFile = certificate.writeConfFile();
        } catch (IOException e) {
            throw new CertificateNotGeneratedException(new boolean[]{false, true, false, false});
        }

        try {
            if (!certificate.getStatus().equals(CertStatus.VALID)) {
                LOGGER.trace("Failed to generate the certificate.");
                return null;
            }
        } catch (IOException e) {
            LOGGER.trace("Unable to read the index file.");
            return null;
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

        File cert = new File("/root/pkcs12/" + userName + ".p12");
        CertInserter certInserter = new CertInserter(dbInterface, certificate.getCertificateSerial(), password, cert);
        certInserter.run();

        return cert;
    }


     /** Revoke the last valid certificate for the user provided as Input

     * @return
     * @throws CertificateNotGeneratedException
     */
    public boolean revokeCertificate(String userName, String password) throws CertificateNotGeneratedException {

        UserData userData = dbInterface.getUserData(userName, password);

        File cert;
        Certificate certificate = new Certificate(userData);
        try {
            cert = certificate.getCertificateFile();
            if (!certificate.getStatus().equals(CertStatus.VALID)) {
                LOGGER.trace("Requested to revoke certificate for user " + userName + " but there are not valid certificates for the user to revoke.");
                return false;
            }
        } catch (IOException e) {
            LOGGER.trace("IO Error prevent from retrieving user certificate file.", e);
            //Consider throwing an error here instead of a normal exit
            return false;
        }



        Process certRev;
        try {
            certRev = Runtime.getRuntime().exec("/etc/ssl/CA/cert_gen.sh " + cert.getAbsolutePath() + " " + PWD);
        } catch (IOException e) {
            LOGGER.trace("IO Error prevent from running the remove cert script.", e);
            //Consider throwing an error here instead of a normal exit
            return false;
        }

        while (certRev.isAlive()) {
            try {
                certRev.waitFor();
            } catch (InterruptedException e) {
                //Ignore and continue waiting
            }
        }

        try {
            if (!certificate.getStatus().equals(CertStatus.REVOKED)) {
                //TODO: check also the certificate revocation list
                LOGGER.info("Unable to revoke the user certificate.. Sorry");
                return false;
            }
        } catch (IOException e) {
            LOGGER.trace("Unable to read the index file.");
            return false;
        }

        return true;
    }

    public DBInterface getDBInterface() {
        return this.dbInterface;
    }
}
