package iMovie.coreCA.model;

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
