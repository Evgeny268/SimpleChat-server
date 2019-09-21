package com.evgeny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
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

    public static boolean checkLogAndPass(String login, String password){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            pstmt = connection.prepareStatement("SELECT * FROM user WHERE user.login = ? AND user.password = ?");
            pstmt.setString(1,login);
            pstmt.setString(2,password);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()){
                return true;
            }
            return false;
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't check user in database",e);
            return false;
        }finally {
            try{
                resultSet.close();
            }catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING, "Can't close resultSet", e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close pstmt",e);
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

    public static boolean insertFriend(int userId, int friendId){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO friend (iduser, friend_id) VALUES (?,?)");
            pstmt.setInt(1,userId);
            pstmt.setInt(2,friendId);
            pstmt.executeUpdate();
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new friend in database",e);
            return false;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new friend in database",e);
            return false;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }
        }
        return true;
    }

    public static int getUserId(String login, String password){
        if (!alreadyConnect) return -1;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("SELECT iduser FROM user WHERE user.login = ? AND user.password = ?");
            pstmt.setString(1,login);
            pstmt.setString(2,password);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get user id from database",e);
            return -1;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get user id from database",e);
            return -1;
        }finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close result set",e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close prepared statement set",e);
            }
        }
    }

    public static User getUserById(int idUser){
        if (!alreadyConnect) return null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM user WHERE user.iduser = ?");
            pstmt.setInt(1,idUser);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()){
                return new User(resultSet.getInt(1),resultSet.getString(2),resultSet.getString(3));
            }
            return null;
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get User id from database",e);
            return null;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get User id from database",e);
            return null;
        }finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close result set",e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close prepared statement set",e);
            }
        }
    }

    public static ArrayList<Integer> selectFriend(int idUser){
        if (!alreadyConnect) return null;
        ArrayList<Integer> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("select t.friend_id from friend t\n" +
                    "where t.iduser = ? \n" +
                    "and exists (\n" +
                    "  select 0 from friend\n" +
                    "  where iduser = t.friend_id and friend_id = t.iduser \n" +
                    ")");
            pstmt.setInt(1, idUser);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close result set",e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close prepared statement set",e);
            }
        }
        return list;
    }

    public static ArrayList<Integer> selectUserRequest(int idUser){
        if (!alreadyConnect) return null;
        ArrayList<Integer> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("select t.friend_id from friend t\n" +
                    "where t.iduser = ? \n" +
                    "and not exists (\n" +
                    "  select 0 from friend\n" +
                    "  where iduser = t.friend_id and friend_id = t.iduser \n" +
                    ")");
            pstmt.setInt(1, idUser);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close result set",e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close prepared statement set",e);
            }
        }
        return list;
    }

    public static ArrayList<Integer> selectStrangerRequest(int idUser){
        if (!alreadyConnect) return null;
        ArrayList<Integer> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("select t.iduser from friend t\n" +
                    "where friend_id = ? \n" +
                    "and not exists (\n" +
                    "  select 0 from friend\n" +
                    "  where iduser = t.friend_id and friend_id = t.iduser \n" +
                    ")\n");
            pstmt.setInt(1, idUser);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
        }finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close result set",e);
            }
            try {
                pstmt.close();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't close prepared statement set",e);
            }
        }
        return list;
    }

    public static ArrayList<User> getUserFriend(User user){
        if (!alreadyConnect) return null;
        Savepoint savepoint = null;
        ArrayList<Integer> friendsId = null;
        ArrayList<User> friends = null;
        try{
            savepoint = connection.setSavepoint("spRegister");
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"can't create savepoint",e);
            return null;
        }
        int idUser = getUserId(user.login, user.password);
        if (idUser>0){
            friendsId = selectFriend(idUser);
            if (friendsId != null){
                friends = new ArrayList<>();
                for (int i = 0; i < friendsId.size(); i++) {
                    friends.add(getUserById(friendsId.get(i)));
                }
            }
        }
        if (savepoint!=null){
            try {
                connection.commit();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"can't commit",e);
            }
            try {
                connection.releaseSavepoint(savepoint);
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"can't release Savepoint",e);
            }
        }
        return friends;
    }

    public static boolean insertToken(int idUser, String token){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO user_token (iduser,token) VALUES (?,?)");
            pstmt.setInt(1,idUser);
            pstmt.setString(2,token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't add token in database",e);
            return false;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add token in database",e);
            return false;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertToken",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertToken",e);
            }
        }
        return true;
    }
}
