package com.evgeny;

import DBUtils.ChatDBWorker;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class Main {

    public static String logSetting = null;
    public static void main(String[] args) {
	// write your code here
        FileInputStream fis;
        Properties property = new Properties();
        String host = null;
        String login = null;
        String password = null;
        try {
            fis = new FileInputStream("config.properties");
            property.load(fis);

            host = property.getProperty("db.host");
            login = property.getProperty("db.login");
            password = property.getProperty("db.password");
            logSetting = property.getProperty("log.file");

            System.out.println("HOST: " + host
                    + ", LOGIN: " + login
                    + ", PASSWORD: " + password);

        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсуствует!");
            System.exit(0);
        }
        AppLogger.init();
        ChatDBWorker.init(host,login,password);
        try {
            ChatDBWorker.connect();
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.SEVERE,"Can't connect to DB",e);
            System.exit(0);
        }
        WebServer server = new WebServer();
        server.start();

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                AppLogger.LOGGER.log(Level.SEVERE,"CLOSE APPLICATION!!!!");
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
