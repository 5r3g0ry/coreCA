package iMovie.coreCA.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class represents the information contained in a row of the user table
 */
public class UserData {

    private static final String UID_COL = "uid";
    private static final String LAST_NAME_COL = "lastname";
    private static final String FIRST_NAME_COL = "firstname";
    private static final String EMAIL_COL = "email";
    private static final String PWD_COL = "pwd";


    private final String uid;
    private final String lastName;
    private final String firstName;
    private final String email;
    private final String pwd;

    public UserData(ResultSet rs) throws SQLException{
        uid = rs.getString(UID_COL);
        lastName = rs.getString(LAST_NAME_COL);
        firstName = rs.getString(FIRST_NAME_COL);
        email = rs.getString(EMAIL_COL);
        pwd = rs.getString(PWD_COL);
    }


    public UserData(String uid, String lastName, String firstName, String email, String pwd) {
        this.uid = uid;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.pwd = pwd;
    }

    public String getUid() {
        return uid;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getPwd() {
        return pwd;
    }
}
