//****************************************************************************
//
// Filename: LoggingManager.java
//
// Description: logging class
//
// @author Rajat Bansal
//***************************************************************************

package core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;

import core.Utils.Utils;

public class LoggingManager {

    public static final String SUCCESS_MESSAGE = "Success" + System.lineSeparator();
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;

    public static String BANNER = System.lineSeparator() +
                                  "Elevator Simulator v" + MAJOR_VERSION + "." +
                                  MINOR_VERSION + ": ";
    
    public static Level getSuccessLevel() {

        return Level.getLevel("GREEN_INFO");
    }

    public static void loggerDeactivate(String mode) {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender(mode);
        ctx.updateLoggers();
    }

    public static Appender getAppender(String name) {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        return config.getAppender(name);
    }

    public static void disableLogging() {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.OFF);
        ctx.updateLoggers();
    }

    public static void enableLogging() {

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.TRACE);
        ctx.updateLoggers();
    }

    public static void createLoggerFile(Logger logger) {

        try {

            FileAppender fp = (FileAppender) LoggingManager.getAppender("IndividualLogger");
            File src = new File(fp.getFileName());

            if (!Utils.isFileExists(src)) {
                logger.error("Unable to Create Log Files");
                return;
            }

            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            CharSequence filename = "Log_" + time;
            File dest = src.toPath().resolveSibling(filename + "." + Utils.getFileExtension(src)).toFile();
            Utils.copyFile(src, dest);
        } catch (IOException e) {
            logger.error("Unable to Create Log Files", e);
        }
    }

    public static void terminate() {

        if (LogManager.getContext() instanceof LoggerContext) {
            Configurator.shutdown((LoggerContext)LogManager.getContext());
        }
    }
}
