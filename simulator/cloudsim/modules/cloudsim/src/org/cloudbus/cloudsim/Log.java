/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * The Log class used for performing loggin of the simulation process. It
 * provides the ability to substitute the output stream by any OutputStream
 * subclass.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class Log {

    private static final Level DEFAULT_LEVEL = Level.SEVERE;
    private static final String PATTERN = "yyyy-MM-dd_HH:mm:ss";
    public static Logger logger = null;

    /**
     * Prints the message passed as a non-String object.
     * 
     * @param message
     *        the message
     */
    public static void print(Object message) {
	logger.info("[" + Double.toString(CloudSim.clock()) + "] " + String.valueOf(message));
    }

    /**
     * Prints a string formated as in String.format().
     * 
     * @param format
     *        the format
     * @param args
     *        the args
     */
    public static void format(String format, Object... args) {
	logger.info("[" + Double.toString(CloudSim.clock()) + "] " + String.format(format, args));
    }

    /**
     * Prints a line formated as in String.format().
     * 
     * @param format
     *        the format
     * @param args
     *        the args
     */
    public static void formatLine(String format, Object... args) {
	logger.finer("[" + Double.toString(CloudSim.clock()) + "] " + String.format(format, args));
    }

    public static String formatDate(Date cal) {
	return new SimpleDateFormat(PATTERN).format(cal);
    }

    /**
     * Sets the output.
     * 
     * @param _output
     *        the new output
     */
    public static void setOutput(OutputStream _output) {
	logger.addHandler(new StreamHandler(_output, new SimpleFormatter()));
    }

    public static void printLine() {

    }

    public static String clock() {
	DecimalFormat f = new DecimalFormat("#.##");
	return '[' + f.format(CloudSim.clock()) + "] ";
    }

    public static void init(String fileName, Level level) throws SecurityException, IOException {
	if (logger == null) {
	    ConsoleHandler consoleHandler = new ConsoleHandler();
	    consoleHandler.setLevel(Level.ALL);
	    final SimpleFormatter newFormatter = new SimpleFormatter();
	    consoleHandler.setFormatter(newFormatter);
	    logger = Logger.getLogger("org.cloudbus.cloudsim");
	    logger.setUseParentHandlers(false);
	    logger.addHandler(consoleHandler);
	    logger.setLevel(level);

	    if (fileName != null) {
		FileHandler fileHandler = new FileHandler(fileName, false);
		logger.addHandler(fileHandler);
	    }
	}
    }

    public static void init(String fileName) throws SecurityException, IOException {
	init(fileName, DEFAULT_LEVEL);
    }

    public static void init() throws SecurityException, IOException {
	init(null, DEFAULT_LEVEL);
    }
}
