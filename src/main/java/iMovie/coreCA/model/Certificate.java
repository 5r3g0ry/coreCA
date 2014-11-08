package iMovie.coreCA.model;

import iMovie.coreCA.utility.CertStatus;
import iMovie.coreCA.utility.RRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Model of a Certificate configuration. Objects of this class will contain all the information that will identify the subject.
 * This information can be used to configure the certificate
 */
public class Certificate {

    private static final Logger LOGGER = LogManager.getLogger(Certificate.class.getName());
    private static final List<String> TEMPLATE = new ArrayList<String>();
    private static final String INDEX_PATH = "/etc/ssl/CA/index.txt";

    //Patterns in the template file to be substituted with the actual value
    private static final String COUNTRY = "C";
    private static final String STATE = "ST";
    private static final String LOCALITY = "LC";
    private static final String ORGANIZATION = "O";
    private static final String ORGANIZATION_UNIT = "OU";
    private static final String COMMON_NAME = "CN";
    private static final String EMAIL = "E";

    //Actual values that will be used to create the certificate
    private String country = "CH";
    private String state = "Zurich";
    private String locality = "";
    private String organization = "iMovie";
    private String organizationUnit = "";
    private String commonName = "";
    private String email = "";

    //Keeps track of the last status of the certificate
    private CertStatus status = null;

    public Certificate(UserData userData) {
        if (TEMPLATE.isEmpty()) {
            initClass();
        }
        this.commonName = userData.getFirstName() + " " + userData.getLastName();
        this.email = userData.getEmail();
    }

    /**
     * Writes on disk a configuration file that can be used with openssl to generate the certificate.
     * The file just created is then returned to the caller.
     *
     * --Security concerns:
     * Modifying this file after creation and before the related certificate has been created
     * allow an attacker to create a certificate with arbitrary content.
     * Randomizing the name of the file will make this attack less likely to be successful
     * @return the configuration file
     */
    public File writeConfFile() throws IOException {
        File confFile = File.createTempFile(RRandom.getInstance().nextNonNegative() + "", ".cnf");
        PrintWriter confWriter = new PrintWriter(confFile, "UTF-8");
        for (String line: TEMPLATE) {
            confWriter.write(parseLine(line) + "\n");
        }
        confWriter.close();
        return confFile;
    }

    /**
     * Gives the last valid certificate for the user selected.
     * If there are no valid certificates it returns null
     * @return the descriptor of the user certificate, or null if it does not exist
     */
    public File getCertificateFile() throws FileNotFoundException {

        String fileNumber = getCertificateSerial();

        if (fileNumber == null) {
            return null;
        }

        return new File("/etc/ssl/CA/newcerts/" + fileNumber + ".pem");
    }

    /**
     * Get the current status of the certificate for this user.
     * The current status can be one of the values of CertStatus: Valid, Revocated,
     * @return
     * @throws IOException
     */
    public CertStatus getStatus() throws IOException {
        getCertificateSerial();
        return this.status;
    }

    /**
     * Obtain the serial number of the last certificate issued with the information contained in this certificate class.
     * As a side effect the status of the certificate is updated.
     * @return the serial of the last certificate issued with the information contained in the instance of this class
     */
    private String getCertificateSerial() throws FileNotFoundException {
        Scanner indexScanner = new Scanner(new File(INDEX_PATH));
        String lookUpString = indexString();
        String fileNumber = null;

        while (indexScanner.hasNextLine()) {
            String line = indexScanner.nextLine();
            String[] parts = line.split("\\t");
            if (lookUpString.equals(parts[parts.length - 1])) {
                status = CertStatus.stringToCertStatus(parts[0]);
                fileNumber = parts[3];
                if (status.equals(CertStatus.VALID)) {
                    break;
                }
            }
        }

        if (fileNumber == null) {
            status = CertStatus.stringToCertStatus(null);
        }

        return fileNumber;
    }

    private String parseLine(String line) {
        if (line.contains(STATE)) {
            return ((state.isEmpty()) ? "#" : "") + line.replace(STATE, state);
        }
        if (line.contains(LOCALITY)) {
            return ((locality.isEmpty()) ? "#" : "") + line.replace(LOCALITY, locality);
        }
        if (line.contains(ORGANIZATION_UNIT)) {
            return ((organizationUnit.isEmpty()) ? "#" : "") + line.replace(ORGANIZATION_UNIT, organizationUnit);
        }
        if (line.contains(ORGANIZATION)) {
            return ((organization.isEmpty()) ? "#" : "") + line.replace(ORGANIZATION, organization);
        }
        if (line.contains(COMMON_NAME)) {
            return ((commonName.isEmpty()) ? "#" : "") + line.replace(COMMON_NAME, commonName);
        }
        if (line.contains(COUNTRY)) {
            return ((country.isEmpty()) ? "#" : "") + line.replace(COUNTRY, country);
        }
        if (line.contains(EMAIL)) {
            return ((email.isEmpty()) ? "#" : "") + line.replace(EMAIL, email);
        }
        return line;
    }

    private String indexString() {
        String div = "/";
        String eq = "=";
        String iS = div + COUNTRY + eq + country;
        iS += div + STATE + eq + state;
        iS += div + ORGANIZATION + eq + organization;
        iS += div + COMMON_NAME + eq + commonName;
        iS += div + "emailAddress" + eq + email;
        return iS;
    }


    private synchronized void initClass() {
        if (!TEMPLATE.isEmpty()) {
            return;
        }
        Scanner templateScanner;
        try {
            templateScanner = new Scanner(new File("resources" + File.separator + "template.cnf"));
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to load configuration file template. Terminating...", e);
            throw new IllegalStateException("Resources files not loaded.");
        }
        while (templateScanner.hasNextLine()) {
            TEMPLATE.add(templateScanner.nextLine());
        }
    }
}
