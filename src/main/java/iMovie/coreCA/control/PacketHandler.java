package iMovie.coreCA.control;

import iMovie.coreCA.exception.CertificateNotGeneratedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Created by Ivy on 02/11/14.
 */
public class PacketHandler extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(PacketHandler.class.getName());

    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    private final CoreCA coreCA;

    private boolean connectionClosed = false;

    public PacketHandler(Socket clientSocket, CoreCA coreCA) throws IOException, IllegalArgumentException {
        if (clientSocket == null || coreCA == null)
            throw new IllegalArgumentException("Provided null socket or coreCA.");
        this.coreCA = coreCA;
        this.clientSocket = clientSocket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        LOGGER.info("Connection initiated with client: " + clientSocket.getInetAddress());
        while (!connectionClosed) {
            try {
                String message = in.readLine();
                LOGGER.info("Received the following message from " + clientSocket.getInetAddress() + " : " + message);
                if (message == null) {
                    closeConnection();
                    continue;
                }
                new ServeRequest(clientSocket, out, message).start();
            } catch (IOException e) {
                checkConnection();
            }
        }
        LOGGER.info("Connection closed with client: " + clientSocket.getInetAddress());
    }

    private File parseCommand(String input) throws CertificateNotGeneratedException {

        File pkcs = null;

        if (input.isEmpty()) {
            return null;
        }

        String[] parts = input.split(" ");
        if (parts.length != 3) {
            return null;
        }

        if ("gen_cert".equals(parts[0])) {
            pkcs = coreCA.generateCertificate(parts[0], parts[1]);
        }

        if ("rev_cert".equals(parts[0])) {
            LOGGER.info("Requested to revoke a certificate, the operation is not yet supported.");
        }

        return pkcs;

    }

    private void checkConnection() {

        if (!(clientSocket.isClosed() || clientSocket.isInputShutdown() || clientSocket.isOutputShutdown())) {
            return;
        }

        LOGGER.trace("Unable to continue the connection with the client: ", clientSocket.getInetAddress());
        closeConnection();

    }

    private void closeConnection() {
        out.close();
        try {
            in.close();
        } catch (IOException e) {
            //ignore, input connection already closed.
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            //ignore, connection already closed.
        }
        connectionClosed = true;
    }


    private class ServeRequest extends Thread {

        private final Socket clientSocket;
        private final PrintWriter out;
        private final String request;

        protected ServeRequest(Socket clientSocket, PrintWriter out, String request) {
            this.clientSocket = clientSocket;
            this.out = out;
            this.request = request;
        }

        @Override
        public void run() {
            try {
                File certificate = parseCommand(request);
                if (certificate == null) {
                    out.write("Error\n");
                    out.write("\n");
                    out.flush();
                    return;
                }
                LOGGER.trace("Sending the certificate " + certificate.getAbsolutePath() + " to: " + clientSocket.getInetAddress());
                byte[] certificateByteArray = new byte[(int) certificate.length()];
                out.write("File\n");
                out.write("Dim:"+certificateByteArray.length+"\n");
                out.flush();
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificate));
                bis.read(certificateByteArray, 0, certificateByteArray.length);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write(certificateByteArray, 0, certificateByteArray.length);
                outputStream.flush();
                LOGGER.trace("Certificate " + certificate.getAbsolutePath() + " sent to: " + clientSocket.getInetAddress());
            } catch (CertificateNotGeneratedException e) {
                out.write("Error\n");
                out.write(e.getMessage() + "\n");
                out.flush();
            } catch (IOException e) {
                checkConnection();
                //If the connection is not closed the error is due to local reasons => ignore
            }
        }
    }
}