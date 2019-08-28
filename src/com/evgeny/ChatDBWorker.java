package com.evgeny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;

public class ChatDBWorker extends DBWorker {

    public static boolean userIsExist(String login){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            pstmt = connection.prepareStatement("SELECT * FROM user WHERE user.login = ?");
            pstmt.setString(1,login);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()){
                return true;
            }
            return false;
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't search user in database",e);
            return false;
        }finally {
            try {
                resultSet.close();
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close resultSet or pstmt",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close resultSet or pstmt",e);
            }
        }
    }

    public static boolean insertUser(String login, String password){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO user (login,password) VALUES (?,?)");
            pstmt.setString(1,login);
            pstmt.setString(2,password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new user in database",e);
            return false;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new user in database",e);
            return false;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in createUser",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in createUser",e);
            }
        }
        return true;
    }

    public static int registerUser(User user){
        /*
        * 1 - successful registration
        * 0 - user already exist
        * -1 - error while registration
        * */
        if (!alreadyConnect) return -1;
        int result = -1;
        Savepoint savepoint = null;
        try{
            savepoint = connection.setSavepoint("spRegister");
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"can't create savepoint",e);
            return -1;
        }
        if (userIsExist(user.login)){
            result = 0;
        }else {
            if (insertUser(user.login,user.password)){
                try {
                    connection.commit();
                    result = 1;
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't make commit while user registration",e);
                    result =-1;
                }
            }else {
                try {
                    connection.rollback(savepoint);
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't make rollback while user registration",e);
                    return -1;
                }
            }
        }
        if (savepoint!=null){
            try {
                connection.commit();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"can't commit db afer try registration",e);
            }
            try {
                connection.releaseSavepoint(savepoint);
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"can't release Savepoint while user registration",e);
            }
        }
        return result;
    }
}
