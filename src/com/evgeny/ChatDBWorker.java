package com.evgeny;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            e.printStackTrace();
            return false;
        }finally {
            try {
                resultSet.close();
                pstmt.close();
            }catch (SQLException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
