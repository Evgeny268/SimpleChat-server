package DBUtils;

import com.evgeny.AppLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DBWorker {
    private static String url = null;
    private static String user = null;
    private static String password = null;

    private static final String settings = "?verifyServerCertificate=false"+
            "&useSSL=false"+
            "&requireSSL=false"+
            "&useLegacyDatetimeCode=false"+
            "&amp"+
            "&serverTimezone=UTC"+
            "&allowPublicKeyRetrieval=true";

    protected static boolean alreadyInit = false;
    protected static boolean alreadyConnect = false;

    protected static Connection connection = null;


    public static void init(String url, String user, String password){
        if (alreadyInit) return;
        DBWorker.url = url+settings;
        DBWorker.user = user;
        DBWorker.password = password;
        alreadyInit = true;
    }

    public static void connect() throws SQLException {
        if (alreadyConnect) return;
        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            alreadyConnect = true;
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void disconnectAndReset(){
        url = null;
        user = null;
        password = null;
        alreadyInit = false;
        if (!alreadyConnect) return;
        try {
            connection.close();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't normaly close connection from database",e);
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't normaly close connection from database",e);
        }
        alreadyConnect = false;
    }

}
