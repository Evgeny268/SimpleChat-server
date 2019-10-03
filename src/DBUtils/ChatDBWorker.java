package DBUtils;

import com.evgeny.AppLogger;
import transfers.Message;
import transfers.RequestIn;
import transfers.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

public class ChatDBWorker extends DBWorker {

    public static boolean userIsExist(String login) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't search user in database",e);
            throw e;
        }
        finally {
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

    public static boolean checkLogAndPass(String login, String password) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't check user in database",e);
            throw e;
        }
        finally {
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

    private static void insertUser(String login, String password) throws SQLException {
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO user (login,password) VALUES (?,?)");
            pstmt.setString(1,login);
            pstmt.setString(2,password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new user in database",e);
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new user in database",e);
            throw e;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in createUser",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in createUser",e);
            }
        }
    }

    public static void registerUser(User user) throws SQLException, UserAlreadyExistException {
        if (userIsExist(user.login)){
            throw new UserAlreadyExistException("User is already in database!");
        }else {
            insertUser(user.login, user.password);
            connection.commit();
        }
    }

    private static void insertFriend(int userId, int friendId) throws SQLException {
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO friend (iduser, friend_id) VALUES (?,?)");
            pstmt.setInt(1,userId);
            pstmt.setInt(2,friendId);
            pstmt.executeUpdate();
        }catch (SQLException e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new friend in database",e);
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add new friend in database",e);
            throw e;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }
        }
    }

    public static int getUserId(String login) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get user id from database",e);
            throw e;
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

    public static User getUserById(int idUser) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't get User id from database",e);
            throw e;
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

    private static ArrayList<Integer> selectFriends(int idUser) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
            throw e;
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

    public static ArrayList<Integer> selectUserRequest(int idUser) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
            throw e;
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

    public static ArrayList<Integer> selectStrangerRequest(int idUser) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectFriend",e);
            throw e;
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

    public static ArrayList<User> getUserFriend(User user) throws SQLException {
        if (!checkLogAndPass(user.login, user.password)) return null;
        ArrayList<Integer> friendsId;
        ArrayList<User> friends = null;
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
        return friends;
    }

    public static void insertToken(int idUser, String token) throws SQLException {
        PreparedStatement pstmt = null;
        try{
            pstmt = connection.prepareStatement("INSERT INTO user_token (iduser,token) VALUES (?,?)");
            pstmt.setInt(1,idUser);
            pstmt.setString(2,token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't add token in database",e);
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't add token in database",e);
            throw e;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertToken",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertToken",e);
            }
        }
    }

    public static void addFriend(User user, User friend) throws AccessRightsException, SQLException, UserNotFoundException {
        if (!checkLogAndPass(user.login, user.password)) {
            throw new AccessRightsException("User failed authorization!");
        }
        if (!userIsExist(friend.login)){
            throw new UserNotFoundException("user not found!");
        }
        int userId = getUserId(user.login);
        int friendId = getUserId(friend.login);
        if (userId <=0 || friendId <=0) return;
        insertFriend(userId, friendId);
        connection.commit();
    }

    public static RequestIn requestIn(User user) throws AccessRightsException, SQLException {
        if (!checkLogAndPass(user.login, user.password)){
            throw new AccessRightsException("User failed authorization!");
        }
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

    private static void insertMessage(int userId, int friendId, String text) throws SQLException {
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
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't insert message in db",e);
            throw e;
        }finally {
            try{
                pstmt.close();
            }catch (SQLException e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.WARNING,"Can't close PreparedStatement in insertFriend",e);
            }
        }
    }

    public static void sendMessage(Message message) throws AccessRightsException, SQLException, UserNotFriendException {
        if (!checkLogAndPass(message.login, message.password)){
            throw new AccessRightsException("User failed authorization!");
        }
        int userId = getUserId(message.login);
        ArrayList<Integer> userFriends = selectFriends(userId);
        if (userFriends.contains(message.id_to)){
            insertMessage(userId,message.id_to,message.text);
            connection.commit();
        }else{
            throw new UserNotFriendException("send message to not friend user!");
        }
    }

    private static ArrayList<Message> selectMessages(int userId, int friendId, int count) throws SQLException {
        ArrayList<Message> messages = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM message WHERE (id_from = ? AND id_to = ?) OR (id_to = ? AND id_from = ?) ORDER BY message.date DESC LIMIT ?;");
            pstmt.setInt(1,userId);
            pstmt.setInt(2,friendId);
            pstmt.setInt(3,userId);
            pstmt.setInt(4,friendId);
            pstmt.setInt(5,count);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int idMessage = resultSet.getInt(1);
                int id_from = resultSet.getInt(2);
                int id_to = resultSet.getInt(3);
                String sDate = resultSet.getString(4);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date;
                try {
                    date = format.parse(sDate);
                } catch (ParseException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"Can't parce date",e);
                    date = new Date();
                }
                String text = resultSet.getString(5);
                Message message = new Message(idMessage,id_from,id_to,date,text);
                messages.add(message);
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectMessage",e);
            throw e;
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.WARNING,"Can't selectMessage",e);
            throw e;
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
        return messages;
    }

    public static ArrayList<Message> getMessages(User user, int friendId, int count) throws AccessRightsException, SQLException, UserNotFriendException {
        if (!checkLogAndPass(user.login, user.password)){
            throw new AccessRightsException("User failed authorization!");
        }
        int userId = getUserId(user.login);
        ArrayList<Integer> userFriends = selectFriends(userId);
        if (userFriends.contains(friendId)){
            ArrayList<Message> messages = selectMessages(userId,friendId,count);
            return messages;
        }else{
            throw new UserNotFriendException();
        }
    }
}
