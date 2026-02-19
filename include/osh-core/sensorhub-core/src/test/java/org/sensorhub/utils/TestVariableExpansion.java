package org.sensorhub.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Various tests to ensure the functionality of the {@link ModuleUtils#expand(String)} and
 * {@link ModuleUtils#expand(String, boolean)} methods.
 * 
 * <p>
 * The following environment variables must be set for all tests to succeed:<br/>
 * - SimpleEnvironment: value2<br/>
 * - DuplicateKey: value4<br/>
 * </p>
 */
public class TestVariableExpansion {
	/**
	 * Helper that allows us to set environment variables for testing (since there is no System.setenv).
	 */
    // environmentVariables.set() not working on JDK>=17 due to illegal reflective to private module
	//@Rule
	//public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
	
	/**
	 * Temporary file that is used to test the functionality of the "${file; ... }" expansion.
	 */
	private File tempFile;
	
	/**
	 * Absolute path to the tempFile above. Used when testing "${file; ... }" expansion.
	 */
	private String tempFilePath;
	
	/**
	 * Non-ASCII string value that will be written to a temporary file for use in testing reading values from files.
	 */
	private static final String FUNKY_UTF8_STRING = "\u221A2 \u03C0";

	@Before
	public void setup() throws IOException {
		System.setProperty("SimpleProperty", "value1");
		System.setProperty("DuplicateKey", "value3");
		
		// The following doesn't work on JDK>=17. This is now set in build.gradle instead
        //environmentVariables.set("SimpleEnvironment", "value2");
		//environmentVariables.set("DuplicateKey", "value4");
		
		tempFile = File.createTempFile("variable-expansion-test-", ".txt");
		tempFilePath = tempFile.getAbsolutePath();
		try (FileWriter fileWriter = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
			fileWriter.write(FUNKY_UTF8_STRING);
		}
	}
	
	@After
	public void tearDown() {
		tempFile.delete();
	}
	
	/**
	 * Testing that substitution with a system property works. Tests evaluating the string alone, with a prefix, with
	 * a suffix, and with both.
	 */
	@Test
	public void testSimpleProperty() {
		String inputString1 = "${SimpleProperty}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value1\"", "value1", outputString1);

		String inputString2 = "PREFIX${SimpleProperty}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue1SUFFIX\"", "PREFIXvalue1SUFFIX", outputString2);

		String inputString3 = "${SimpleProperty}SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value1SUFFIX\"", "value1SUFFIX", outputString3);

		String inputString4 = "PREFIX${SimpleProperty}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIXvalue1\"", "PREFIXvalue1", outputString4);
	}

	/**
	 * Testing that substitution with a system property works even when it starts with "$$". Tests evaluating the string
	 * alone, with a prefix, with a suffix, and with both.
	 */
	@Test
	public void testSimpleLazyProperty() {
		String inputString1 = "$${SimpleProperty}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value1\"", "value1", outputString1);

		String inputString2 = "PREFIX$${SimpleProperty}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue1SUFFIX\"", "PREFIXvalue1SUFFIX", outputString2);

		String inputString3 = "$${SimpleProperty}SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value1SUFFIX\"", "value1SUFFIX", outputString3);

		String inputString4 = "PREFIX$${SimpleProperty}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIXvalue1\"", "PREFIXvalue1", outputString4);
	}

	/**
	 * Testing that substitution with a system property works when using the "prop;" prefix. Tests evaluating the string
	 * alone, with a prefix, with a suffix, and with both.
	 */
	@Test
	public void testPrefixedSimpleProperty() {
		String inputString1 = "${prop;SimpleProperty}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value1\"", "value1", outputString1);

		String inputString2 = "PREFIX${prop;SimpleProperty}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue1SUFFIX\"", "PREFIXvalue1SUFFIX", outputString2);

		String inputString3 = "${prop;SimpleProperty}SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value1SUFFIX\"", "value1SUFFIX", outputString3);

		String inputString4 = "PREFIX${prop;SimpleProperty}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIXvalue1\"", "PREFIXvalue1", outputString4);
	}

	/**
	 * Testing that substitution with an environment variable works. Tests evaluating the string alone, with a prefix,
	 * with a suffix, and with both.
	 */
	@Test
	public void testSimpleEnvironment() {
		String inputString1 = "${SimpleEnvironment}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value2\"", "value2", outputString1);

		String inputString2 = "PREFIX${SimpleEnvironment}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue2SUFFIX\"", "PREFIXvalue2SUFFIX", outputString2);

		String inputString3 = "${SimpleEnvironment}SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value2SUFFIX\"", "value2SUFFIX", outputString3);

		String inputString4 = "PREFIX${SimpleEnvironment}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIXvalue2\"", "PREFIXvalue2", outputString4);
	}

	/**
	 * Testing that substitution with an environment variable works when the expression uses the "env;" prefix. Tests
	 * evaluating the string alone, with a prefix, with a suffix, and with both.
	 */
	@Test
	public void testPrefixedSimpleEnvironment() {
		String inputString1 = "${env;SimpleEnvironment}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value2\"", "value2", outputString1);

		String inputString2 = "PREFIX${env;SimpleEnvironment}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue2SUFFIX\"", "PREFIXvalue2SUFFIX", outputString2);

		String inputString3 = "${env;SimpleEnvironment}SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value2SUFFIX\"", "value2SUFFIX", outputString3);

		String inputString4 = "PREFIX${env;SimpleEnvironment}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIXvalue2\"", "PREFIXvalue2", outputString4);
	}
	
	/**
	 * Tests using the "file;" prefix to get content of a file.
	 */
	@Test
	public void testFile() {
		String inputString1 = "${file;" + tempFilePath + "}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"\u221A2 \u03C0\"", FUNKY_UTF8_STRING, outputString1);

		String inputString2 = "PREFIX" + inputString1 + "SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIX\u221A2 \u03C0SUFFIX\"", "PREFIX" + FUNKY_UTF8_STRING + "SUFFIX", outputString2);

		String inputString3 = inputString1 + "SUFFIX";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"\u221A2 \u03C0SUFFIX\"", FUNKY_UTF8_STRING + "SUFFIX", outputString3);

		String inputString4 = "PREFIX" + inputString1;
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"PREFIX\u221A2 \u03C0\"", "PREFIX" + FUNKY_UTF8_STRING, outputString4);
	}

	/**
	 * Tests that verify that when there are both a system property and environment variable, the system property takes
	 * precedence. Also checks that the precedence isn't affected by text before and after the expression.
	 */
	@Test
	public void testDuplicateKey() {
		String inputString1 = "${DuplicateKey}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Should evaluate to \"value3\"", "value3", outputString1);

		String inputString2 = "PREFIX${DuplicateKey}SUFFIX";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Should evaluate to \"PREFIXvalue3SUFFIX\"", "PREFIXvalue3SUFFIX", outputString2);

		String inputString3 = "${env;DuplicateKey}";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Should evaluate to \"value4\"", "value4", outputString3);

		String inputString4 = "${prop;DuplicateKey}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Should evaluate to \"value3\"", "value3", outputString4);
	}

	/**
	 * Tests that "$$" works as expected, including when it's mixed with plain "$" in a string.
	 */
	@Test
	public void testLazy() {
		String inputString1 = "$${SimpleProperty}";
		String outputString1 = ModuleUtils.expand(inputString1, true);
		assertEquals("$${...} should not be evaluated when honorLazyFlag is true", inputString1, outputString1);

		String outputString2 = ModuleUtils.expand(inputString1, false);
		assertEquals("$${...} should be evaluated when honorLazyFlag is false", "value1", outputString2);
		
		String inputString3 = "$${SimpleProperty} ${SimpleProperty}";
		String outputString3 = ModuleUtils.expand(inputString3, true);
		assertEquals("$${...} mixed with ${...} didn't work right", "$${SimpleProperty} value1", outputString3);

		String inputString4 = "${SimpleProperty} $${SimpleProperty}";
		String outputString4 = ModuleUtils.expand(inputString4, true);
		assertEquals("${...} mixed with $${...} didn't work right", "value1 $${SimpleProperty}", outputString4);
	}

	/**
	 * Various tests to make sure default values work as expected.
	 */
	@Test
	public void testDefaultValues() {
		String inputString1 = "${file;/tmp/doesnotexist.txt:MyDefaultValue}";
		String outputString1 = ModuleUtils.expand(inputString1);
		assertEquals("Default value of MyDefaultValue should be used (file)", "MyDefaultValue", outputString1);

		String inputString2 = "${prop;DoesNotExist:MyDefaultValue}";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Default value of MyDefaultValue should be used (prop)", "MyDefaultValue", outputString2);

		String inputString3 = "${env;DOES_NOT_EXIST:MyDefaultValue}";
		String outputString3 = ModuleUtils.expand(inputString3);
		assertEquals("Default value of MyDefaultValue should be used (env)", "MyDefaultValue", outputString3);

		String inputString4 = "${DoesNotExist:MyDefaultValue}";
		String outputString4 = ModuleUtils.expand(inputString4);
		assertEquals("Default value of MyDefaultValue should be used (no prefix)", "MyDefaultValue", outputString4);
	}
	
	/**
	 * Make sure null and blank are handled as expected, and don't throw exceptions.
	 */
	@Test
	public void testNullAndBlank() {
		String inputString1 = null;
		String outputString1 = ModuleUtils.expand(inputString1);
		assertNull("null output must come from null input", outputString1);
		
		String inputString2 = "";
		String outputString2 = ModuleUtils.expand(inputString2);
		assertEquals("Empty string must come from empty string input", "", outputString2);
	}
}
