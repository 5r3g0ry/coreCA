package iMovie.coreCA;

import java.util.*;
import java.io.*;
import java.net.Socket;

/**
 * Simple client to communicate with the CoreCA server.
 * It sends the text commands as received from the command line and forward them to the server.
 * The server then elaborate the request.
 * There are two types of reply (both are text in utf-8 format): 'Error' or 'File'
 * If an 'Error' reply is received the  next packet contains text and explains the nature of the error.
 * If a 'File' reply is received the next packet contains again text. This text contains the dimension in bytes of the file in the following format: "Dim:x"
 * Then a stream of bytes is sent, there will be sent exactly x bytes, where x is the number received in the previous text message.
 */
public class Client {

    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    private static int current = 0;
    private static String baseDirectory;

    public Client(Socket sock) throws IOException{
        this.sock = sock;
        out = new PrintWriter(sock.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

    }


    public static void main(String[] argv) throws Exception {

        if (argv.length != 2) {
            System.out.println("Please launch this program with two arguments:");
            System.out.println("1) The IP address of the server");
            System.out.println("2) The path to the directory where the PKCS#12 certificates will be saved");
            return;
        }

        Client m = new Client(new Socket(argv[0], 6475));
        baseDirectory = argv[1];
        Scanner iStr = new Scanner(System.in);
        String userInput;
        while (true) {
            System.out.print("Enter the message to be sent at the server: (<!!> to abort) ");
            userInput = iStr.nextLine();
            if ("!!".equals(userInput)) {
                break;
            }
            m.send(userInput);
            String resp = m.received();
            if ("Error".equals(resp)) {
                System.out.println("Error: " + m.received());
                continue;
            }
            if ("File".equals(resp)) {
                System.out.println("Receiving a file...");
                m.readFile();
            }
        }
        m.closeConnection();
    }

    public void send(String mess) throws IOException {
        out.write(mess + "\n");
        out.flush();
    }

    public String received() throws IOException {
        return in.readLine();
    }

    public File readFile() throws IOException {
        InputStream is = sock.getInputStream();
        FileOutputStream fos = new FileOutputStream(baseDirectory + File.separator + current +".p12");
        current++;
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int length = Integer.parseInt(received().split(":")[1]);
        System.out.println("The file is " + length + " bytes long");
        byte[] mybytearray = new byte[length];
        int bytesRead = is.read(mybytearray, 0, mybytearray.length);
        bos.write(mybytearray, 0, bytesRead);
        bos.close();
        fos.close();
        return new File("/Users/Ivy/Desktop/client/cert.cnf");
    }

    public void closeConnection() throws IOException {
        in.close();
        out.close();
        sock.close();
    }
}