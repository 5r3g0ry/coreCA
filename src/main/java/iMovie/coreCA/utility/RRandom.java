package iMovie.coreCA.utility;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;

/**
 * RRandom does not add any functionality to the normal Random class.
 * Its difference is the way in which the seed is generated.
 * In particular during the initialization of a RRandom object a system call is made to determinate which process are executing at the moment and which part of the memory has been reserved for them.
 * Then this information is hashed and converted into a 64-bit integer which is used as seed.
 * However the setSeed function behaviour has not been changed, so since by default it uses only 48 bits for the seed, the seed space is only 48 bit.
 * Of course the seed can still be set manually after it is first generated. For instance for debugging purposes.
 * The class implements the pattern Singleton.
 * This is done because the system call needed to derive the seed is very expensive,
 * therefore the initialization will be performed at most once during the whole execution of the program.
 */
public class RRandom extends Random {

    private static final Logger LOGGER = LogManager.getLogger(RRandom.class.getName());
    private static final String INITIALIZATION_FAIL = "Continuing with normal Random seed initialization...";
    private static final int LONG_BYTE_LENGTH = 8;  //dimension of a long in bytes
    private static RRandom rRandom = null;

    private RRandom() {
        super();
        LOGGER.info("Initializing RNG...");
        Process hashOut;
        String terminator = ("Linux".equals(System.getProperty("os.name"))) ? "n" : "l";
        try {
            hashOut = Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","top -" + terminator + " 1 | openssl sha1"});
        } catch (IOException e) {
            LOGGER.warn("Unable to calculate random seed.", e);
            LOGGER.info(INITIALIZATION_FAIL);
            return;
        }
        try {
            hashOut.waitFor();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for system random seed.", e);
            LOGGER.info(INITIALIZATION_FAIL);
            return;
        }
        InputStream bashStream= hashOut.getInputStream();
        if (bashStream == null) {
            LOGGER.warn("Unable to connect to system stdout to retrieve the seed.");
            LOGGER.info(INITIALIZATION_FAIL);
            return;
        }
        Scanner hashScanner = new Scanner(bashStream);
        String hash = "";
        while (hashScanner.hasNextLine()) {
            hash = hashScanner.nextLine();
        }
        String[] strings = hash.split(" ");
        hash = strings[strings.length - 1];
        setSeed(numberFromHexString(hash));
        LOGGER.info("RNG successfully initialized.");
    }

    /**
     * It is guaranteed that calling this method will always return the same object during the whole execution of the program.
     * The object will be initialized only when this method is called
     * i.e. if this method is never called during the execution of the program the RRandom object will not be initialized
     * Calling this method is the only way to get a RRandom instance.
     * This method is thread safe.
     * @return an instance of a RRandom object
     */
    public static RRandom getInstance() {
        if (rRandom == null) {
            instantiate();
        }
        return rRandom;
    }

    /**
     * @return a non negative random integer
     */
    public int nextNonNegative() {
        return next(31);
    }

    private static synchronized RRandom instantiate() {
        if (rRandom == null) {
            rRandom = new RRandom();
        }
        return rRandom;
    }

    /**
     * Compress the hex number represented with a string given as parameter to a type{long} number.
     * @param hexString string representation of a hex number
     * @return a compressed number derived from the hexString
     */
    private long numberFromHexString(String hexString) {
        long res = 0;
        int pad = (hexString.length() / 2) % LONG_BYTE_LENGTH;
        byte[] bArray = new byte[hexString.length() / 2 + pad];
        byte[] input = HexBin.decode(hexString);
        for (int i = 0; i < input.length; i++) {
            bArray[i] = input[i];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bArray);
        for (int i = 0; i < bArray.length / LONG_BYTE_LENGTH; i++) {
            res ^= byteBuffer.getLong();
        }
        return res;
    }
}
