package update;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Establishes connection to IBM i through JDBC.
 *
 * @author Vladimír Župka 2016
 *
 */
public class U_ConnectDB {
    // Error message

    static String msg;

    static ResourceBundle locMessages;
    static String driver, connErr;
    static ResourceBundle titles;
    static String decSeparator;
    static String sortLanguage;

    static String host;
    static String libraryList;
    static String userName;
    static String password;
    static String language;
    static Connection connection;
    final static String PROP_COMMENT = "SqlUpdate for IBM i, © Vladimír Župka 2015";

    static Path parPath = Paths
            .get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    static String encoding = System.getProperty("file.encoding");

    /**
     * Connects to IBM i using IBM i JDBC driver
     *
     * @return object of class java.sql.Connection
     */
    public static Connection connect() {
        // Application properties
      Properties prop = new Properties();
      try {
         BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
         prop.load(infile);
         infile.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
        host = prop.getProperty("HOST");
        userName = prop.getProperty("USER_NAME");
        libraryList = prop.getProperty("LIBRARY_LIST"); // library list
        language = prop.getProperty("LANGUAGE");

        Locale currentLocale = Locale.forLanguageTag(language);

        locMessages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);
        // Localized messages
        driver = locMessages.getString("Driver");
        connErr = locMessages.getString("ConnErr");
        // Localized decimal separator character
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        decSeparator = titles.getString("DecSeparator");
        sortLanguage = titles.getString("SortLanguage");
        try {
            // Obtain JDBC driver for DB2
            Class.forName("com.ibm.as400.access.AS400JDBCDriver");

            // Set connection properties
            Properties conprop = new Properties();
            conprop.put("user", userName);

            // ???? password
            ///conprop.put("password", "????");

            // decimal separator LOCALIZED
            conprop.put("decimal separator", decSeparator);
            // intermediate BigDecimal conversion for packed and zoned "true"
            // (or "false" - convert directly to double)
            conprop.put("big decimal", "true");
            // sort language LOCALIZED
            conprop.put("sort language", sortLanguage);
            // lower case = upper case (or "unique")
            conprop.put("sort weight", "shared");
            // no exception (or "true" = exception)
            conprop.put("data truncation", "false");
            // system naming (or "sql")
            conprop.put("naming", "system");
            // sort by language (or "hex" or "table")
            conprop.put("sort", "language");
            // library list
            ///conprop.put("libraries", libraryList);
            // date format ISO (or "mdy" "dmy" "ymd" "usa" "eur" "jis"
            // "julian")
            conprop.put("date format", "iso");
            // time format ISO (or "hms" "usa" "eur" "jis")
            conprop.put("time format", "iso");
            // block unless FOR UPDATE is specified "2"
            // (or "0" (no record blocking)
            // or "1" (block if FOR FETCH ONLY is specified))
            conprop.put("block criteria", "2");
            // block size 512 (or "0" "8" "16" "32" "64" "128" "256")
            conprop.put("block size", "512");
            // result data compression true (or "false")
            conprop.put("data compression", "true");
            // error message detail basic (or "full")
            conprop.put("errors", "basic");
            // access all (or "read call" (SELECT and CALL statements allowed)
            // or "read only" (SELECT statements only))
            conprop.put("access", "all");
            // "1" = Optimize query for first block of data (*FIRSTIO)
            // or "2" = Optimize query for entire result set (*ALLIO)
            // or "0" = Optimize query for first block of data (*FIRSTIO) when
            // extended dynamic packages are used; Optimize query for entire result
            // set (*ALLIO) when packages are not used
            conprop.put("query optimize goal", "1");
            // full open a file "false" optimizes performance (or "true")
            conprop.put("full open", "false");
            // "false" - writing truncated data to the database 
            // - no exception, no attention
            // (or "true" - exception, attention)
            conprop.put("data truncation", "false");

            // DriverManager gets connection object for JDBC
            connection = DriverManager.getConnection("jdbc:as400://" + host, conprop);

            // All changes to tables will be automatically committed
            connection.setAutoCommit(true);

        } catch (SQLException exc2) {
            // Connection error
            msg = connErr + exc2.toString();
            System.out.println(msg);
            return null;
        } catch (ClassNotFoundException exc) {
            // Class "com.ibm.as400.access.AS400JDBCDriver" not found
            msg = driver + host + ": " + exc.toString();
            System.out.println(msg);
            return null;
        }
        return connection;
    }

    /**
     * Disconnects from IBM i
     *
     * @param connection
     * object of class java.sql.Connection
     */
    public static void disconnect(Connection connection) {
        try {
            connection.close();
        } catch (SQLException sqle) {
        }
    }
}
