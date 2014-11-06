package iMovie.coreCA.utility;

import iMovie.coreCA.control.CoreCA;

/**
 * This thread is intended to release all the resources allocated by the program before its termination.
 */
public class CleanerHook extends Thread {

    private final CoreCA coreCA;

    public CleanerHook(CoreCA coreCA) {
        this.coreCA = coreCA;
    }

    @Override
    public void run() {
        coreCA.getDBInterface().closeConnection();
    }

}
