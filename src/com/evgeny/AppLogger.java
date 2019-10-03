package com.evgeny;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;



public class AppLogger {
    public static Logger LOGGER;
    public static boolean alreadyinit = false;
    public static void init(){
        if (alreadyinit) return;
        try(FileInputStream ins = new FileInputStream(Main.logSetting)){
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(AppLogger.class.getName());
            LOGGER.log(Level.INFO,"################### LOGGER START ######################");
            alreadyinit = true;
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
    }
}
