/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cloudbus.cloudsim.core.CloudSim;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
@Ignore
public class LogTest {

    private static final ByteArrayOutputStream OUTPUT = new ByteArrayOutputStream();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Before
    public void setUp() throws Exception {
	Log.setOutput(OUTPUT);
    }

    @Test
    public void testPrint() throws IOException {
	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "test test");
	assertEquals("test test", OUTPUT.toString());
	OUTPUT.reset();

	Log.print(123);
	assertEquals("123", OUTPUT.toString());
	OUTPUT.reset();

	Log.print(123L);
	assertEquals("123", OUTPUT.toString());
	OUTPUT.reset();

	Log.print(123.0);
	assertEquals("123.0", OUTPUT.toString());
	OUTPUT.reset();
    }

    @Test
    public void testPrintLine() throws IOException {
	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + "test test");
	assertEquals("test test" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + String.valueOf(123));
	assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + String.valueOf(123L));
	assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.logger.info("[" + Double.toString(CloudSim.clock()) + "] " + String.valueOf(123.0));
	assertEquals("123.0" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();
    }

    @Test
    public void testFormat() throws IOException {
	Log.format("test %s test", "test");
	assertEquals("test test test", OUTPUT.toString());
	OUTPUT.reset();

	Log.format("%d", 123);
	assertEquals("123", OUTPUT.toString());
	OUTPUT.reset();

	Log.format("%d", 123L);
	assertEquals("123", OUTPUT.toString());
	OUTPUT.reset();

	Log.format("%.2f", 123.01);
	assertEquals("123.01", OUTPUT.toString());
	OUTPUT.reset();
    }

    @Test
    public void testFormatLine() throws IOException {
	Log.formatLine("test %s test", "test");
	assertEquals("test test test" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.formatLine("%d", 123);
	assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.formatLine("%d", 123L);
	assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();

	Log.formatLine("%.2f", 123.01);
	assertEquals("123.01" + LINE_SEPARATOR, OUTPUT.toString());
	OUTPUT.reset();
    }
}
