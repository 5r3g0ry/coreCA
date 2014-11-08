public class CertInserter extends Thread {

  private DbInterface dbInterface;
  private int serial;
  private String password;
  private File cert;

  public CertInserter(DbInterface db, int s, String p, File c) {
    dbInterface = db;
    serial = s;
    password = p;
    cert = c;
  }

  public void run() {
    try {
      while(!dbInterface.insertCert(serial, password, cert));
    } catch () {
      //
    }

  }

}
