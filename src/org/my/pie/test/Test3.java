package org.my.pie.test;

import org.my.pie.exception.PieMissmatchedException;
import org.my.pie.exception.PieRecognitionException;
import org.my.pie.interpreter.PieInterpreter;
import org.my.pie.lex.PieLexer;
import org.my.pie.parse.PieAST;
import org.my.pie.parse.PieParser;

public class Test3 {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String text = Util.readFile("\\src\\org\\my\\pie\\test\\test3.pie");
		PieLexer lexer = new PieLexer(text);
		PieInterpreter interpreter = new PieInterpreter();
		PieParser parser = new PieParser(lexer, interpreter);
		try {
			PieAST root = parser.program();
		} catch (PieRecognitionException | PieMissmatchedException e) {
			e.printStackTrace();
		}
	}
}
