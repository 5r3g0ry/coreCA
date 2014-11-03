package iMovie.coreCA;

import iMovie.coreCA.control.CoreCA;
import iMovie.coreCA.control.PacketHandler;
import iMovie.coreCA.utility.DBInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;


public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class.getName());

    private static final int PORT = 6475;

    public static void main( String[] args ) throws IOException, SQLException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        CoreCA coreCA = new CoreCA(new DBInterface("localhost"));
        LOGGER.info("Waiting for client connection request..");
        while (true) {
            try {
                new PacketHandler(serverSocket.accept(), coreCA).start();
            } catch (IOException | IllegalArgumentException e) {
                //Unable to successfully connect to a client..
                //Continues to listen anyway to some connection request.
            }
        }
    }
}