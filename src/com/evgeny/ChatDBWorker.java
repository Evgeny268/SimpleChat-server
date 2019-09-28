package com.evgeny;

import transfers.Message;
import transfers.RequestIn;
import transfers.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
//TODO Данный класс следует полностью переписать. Лютый говнокод

public class ChatDBWorker extends DBWorker {

    public static boolean userIsExist(String login){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            pstmt = connection.prepareStatement("SELECT * FROM user WHERE BINARY user.login = ?");
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
            pstmt = connection.prepareStatement("SELECT * FROM user WHERE BINARY user.login = ? AND BINARY user.password = ?");
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

    public static int getUserId(String login){
        if (!alreadyConnect) return -1;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("SELECT iduser FROM user WHERE user.login = ?");
            pstmt.setString(1,login);
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

    public static ArrayList<Integer> selectFriends(int idUser){
        if (!alreadyConnect) return null;
        ArrayList<Integer> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("select t.friend_id from friend t " +
                    "where t.iduser = ?" +
                    " and exists (" +
                    "  select 0 from friend" +
                    "  where iduser = t.friend_id and friend_id = t.iduser" +
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
            pstmt = connection.prepareStatement("select t.iduser from friend t " +
                    "where friend_id = ? " +
                    "and not exists (" +
                    "  select 0 from friend" +
                    " where iduser = t.friend_id and friend_id = t.iduser" +
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

    public static ArrayList<User> getUserFriend(User user){
        if (!alreadyConnect) return null;
        if (!checkLogAndPass(user.login, user.password)) return null;
        Savepoint savepoint = null;
        ArrayList<Integer> friendsId = null;
        ArrayList<User> friends = null;
        try{
            savepoint = connection.setSavepoint("spRegister");
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"can't create savepoint",e);
            return null;
        }
        int idUser = 0;
        idUser = getUserId(user.login);
        if (idUser>0){
            friendsId = selectFriends(idUser);
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

    public static int addFriend(User user, User friend){
        int result  = 0;
        if (!alreadyConnect) return -2;
        if (!checkLogAndPass(user.login, user.password)) return -1;
        if (!userIsExist(friend.login)) return -2;
        int userId = getUserId(user.login);
        int friendId = getUserId(friend.login);
        if (userId <=0 || friendId <=0) return -2;
        if(insertFriend(userId, friendId)){
            result = 1;
        }else result = 0;
        try {
            connection.commit();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't commit",e);
            return -2;
        }
        return 1;
    }

    public static RequestIn requestIn(User user){
        int result  = 0;
        if (!alreadyConnect) return null;
        if (!checkLogAndPass(user.login, user.password)) return null;
        int userId = getUserId(user.login);
        ArrayList<Integer> reqId = selectStrangerRequest(userId);
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < reqId.size(); i++) {
            User reqUser = getUserById(reqId.get(i));
            users.add(reqUser);
        }
        RequestIn requestIn = new RequestIn(users);
        return requestIn;
    }

    public static boolean insertMessage(int userId, int friendId, String text){
        if (!alreadyConnect) return false;
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO message (id_from, id_to, date, text) VALUES(?,?,?,?)");
            pstmt.setInt(1,userId);
            pstmt.setInt(2,friendId);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = format.format(date);
            pstmt.setString(3,currentDateTime);
            pstmt.setString(4,text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't insert message in db",e);
            return false;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't insert message in db",e);
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

    public static int sendMessage(Message message){
        if (!alreadyConnect) return -2;
        if (!checkLogAndPass(message.login, message.password)) return -1;
        int userId = getUserId(message.login);
        ArrayList<Integer> userFriends = selectFriends(userId);
        if (userFriends.contains(message.id_to)){
            insertMessage(userId,message.id_to,message.text);
            try {
                connection.commit();
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.WARNING,"Can't commit",e);
                return -2;
            }
        }else return -1;
        return 1;
    }
}
