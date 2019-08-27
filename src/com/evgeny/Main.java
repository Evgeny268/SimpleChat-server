package com.evgeny;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
	// write your code here
        ChatDBWorker.init("jdbc:mysql://localhost:3306/simple_chat","testuser","testuser1234");
        try {
            ChatDBWorker.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(ChatDBWorker.userIsExist("test1"));
        ChatDBWorker.disconnectAndReset();
        System.out.println("test");
    }
}
