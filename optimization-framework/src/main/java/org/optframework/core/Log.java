package org.optframework.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    public static Logger logger = Logger.getLogger("MyLog");
    private static FileHandler fileHandler;

    public static void init(){
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
            fileHandler  = new FileHandler("resources/log/"+ date+ ".log");

            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);

            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
