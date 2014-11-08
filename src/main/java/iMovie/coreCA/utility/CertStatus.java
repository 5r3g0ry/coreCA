package iMovie.coreCA.utility;

/**
 * Created by Ivy on 08/11/14.
 */
public enum CertStatus {
    VALID, REVOKED, NOT_ISSUED;


    public static CertStatus stringToCertStatus(String str) {
        if (str == null) {
            return NOT_ISSUED;
        }
        switch (str) {
            case "V":
                return VALID;
            case "R":
                return REVOKED;
            default:
                return NOT_ISSUED;
        }
    }
}
