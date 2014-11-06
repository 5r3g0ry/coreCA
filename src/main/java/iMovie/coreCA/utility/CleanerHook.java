package iMovie.coreCA.utility;

import iMovie.coreCA.control.CoreCA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This thread is intended to release all the resources allocated by the program before its termination.
 */
public class CleanerHook extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(CleanerHook.class.getName());

    private final CoreCA coreCA;

    public CleanerHook(CoreCA coreCA) {
        this.coreCA = coreCA;
    }

    @Override
    public void run() {
        LOGGER.trace("Terminating the server..");
        coreCA.getDBInterface().closeConnection();
        Configurator.shutdown((LoggerContext) LogManager.getContext());
    }

}
