package org.my.pie.test;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Util {

	private static Scanner scanner;
	private static final String CHARSET_NAME = "UTF-8";
	private static final Locale LOCALE = Locale.US;
	private static final Pattern WHITESPACE_PATTERN = Pattern
			.compile("\\p{javaWhitespace}+");
	private static final Pattern EMPTY_PATTERN = Pattern.compile("");
	private static final Pattern EVERYTHING_PATTERN = Pattern.compile("\\A");

	public static String readFile(String fileName) {
		try {
			String path = System.getProperty("user.dir") + fileName;
			File file = new File(path);
			scanner = new Scanner(file, CHARSET_NAME);
			scanner.useLocale(LOCALE);
		} catch (IOException ioe) {
			System.err.println("Could not open " + fileName);
		}
		if (!scanner.hasNextLine())
			return "";
		String result = scanner.useDelimiter(EVERYTHING_PATTERN).next();
		scanner.useDelimiter(WHITESPACE_PATTERN);
		return result;
	}

}
