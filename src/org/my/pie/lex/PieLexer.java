package org.my.pie.lex;

import java.util.HashMap;

import org.my.pie.exception.PieEOFException;
import org.my.pie.exception.PieMissmatchedException;

public class PieLexer {

	public static final Token IF = new Token(Tag.IF, "if");
	public static final Token ASSIGN = new Token(Tag.ASSIGN, "=");
	public static final Token PRINT = new Token(Tag.PRINT, "print");
	public static final Token WHILE = new Token(Tag.WHILE, "while");
	public static final Token RETURN = new Token(Tag.RETURN, "return");
	public static final Token DEF = new Token(Tag.DEF, "def");
	public static final Token ADD = new Token(Tag.ADD, "+");
	public static final Token SUB = new Token(Tag.SUB, "-");
	public static final Token MUL = new Token(Tag.MUL, "*");
	public static final Token EQ = new Token(Tag.EQ, "==");
	public static final Token LT = new Token(Tag.LT, "<");
	public static final Token STRUCT = new Token(Tag.STRUCT, "struct");
	public static final Token DOT = new Token(Tag.DOT, ".");
	public static final Token NEW = new Token(Tag.NEW, "new");

	public static final Token EOF = new Token(Tag.EOF, "EOF");
	public static final Token NL = new Token(Tag.NL, "NL");

	public static int line = 0;

	public HashMap<String, Token> words = new HashMap<String, Token>();
	public int cursor = -1;
	public int size = 0;
	public char peek = ' ';
	public String text;

	public PieLexer(String input) {
		this.text = input;
		this.size = input.length();
		init();
	}

	private void init() {
		words.put("if", PieLexer.IF);
		words.put("print", PieLexer.PRINT);
		words.put("while", PieLexer.WHILE);
		words.put("return", PieLexer.RETURN);
		words.put("def", PieLexer.DEF);
		words.put("struct", PieLexer.STRUCT);
		words.put("new", PieLexer.NEW);
	}

	private void consumeChar() throws PieEOFException {
		cursor++;
		if (cursor >= size) {
			peek = ' ';
			throw new PieEOFException();
		}
		peek = text.charAt(cursor);
	}

	private void match(char c) throws PieMissmatchedException, PieEOFException {
		if (peek != c)
			throw new PieMissmatchedException("lex error at line "
					+ PieLexer.line + ", expected '" + String.valueOf(c) + "'"
					+ ", but encountered '" + String.valueOf(peek) + "'");
		consumeChar();
	}

	private boolean isDigit(char c) {
		if (c >= '0' && c <= '9') {
			return true;
		}
		return false;
	}

	private boolean isLetter(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return true;
		}
		return false;
	}

	private int toInt(char c) {
		return c - '0';
	}

	public Token nextToken() throws PieMissmatchedException {
		Token ret = null;
		try {
			while (peek == ' ' || peek == '\t') {
				consumeChar();
			}
			if (peek == '\n') {
				ret = PieLexer.NL;
				consumeChar();
				PieLexer.line++;
				return ret;
			} else if (peek == '+') {
				consumeChar();
				ret = PieLexer.ADD;
				return ret;
			} else if (peek == '-') {
				consumeChar();
				ret = PieLexer.SUB;
				return ret;
			} else if (peek == '*') {
				consumeChar();
				ret = PieLexer.MUL;
				return ret;
			} else if (peek == '=') {
				consumeChar();
				ret = PieLexer.ASSIGN;
				if (peek == '=') {
					consumeChar();
					ret = PieLexer.EQ;
				}
				return ret;
			} else if (peek == '<') {
				consumeChar();
				ret = PieLexer.LT;
				return ret;
			} else if (peek == '.') {
				consumeChar();
				ret = PieLexer.DOT;
				return ret;
			} else if (peek == '\'') {
				consumeChar();
				ret = new Token(Tag.CHAR, String.valueOf(peek));
				consumeChar();
				match('\'');
				return ret;
			} else if (peek == '"') {
				StringBuffer sb = new StringBuffer();
				consumeChar();
				while (peek != '"') {
					sb.append(peek);
					consumeChar();
				}
				ret = new Token(Tag.STRING, sb.toString());
				match('"');
				return ret;

			} else if (isDigit(peek)) {
				int h = 0, l = 0;
				float base = 1.0f;
				while (isDigit(peek)) {
					h = 10 * h + toInt(peek);
					consumeChar();
				}
				ret = new Token(Tag.INT, String.valueOf(h));
				if (peek == '.') {
					consumeChar();
					while (isDigit(peek)) {
						l = 10 * l + toInt(peek);
						base = 10 * base;
						consumeChar();
					}
					ret = new Token(Tag.FLOAT, String.valueOf((h + l) / base));
				}
				return ret;
			} else if (isLetter(peek)) {
				StringBuffer sb = new StringBuffer();
				while (isLetter(peek) || isDigit(peek)) {
					sb.append(peek);
					consumeChar();
				}
				String s = sb.toString();
				if ((ret = words.get(s)) == null) {
					ret = new Token(Tag.ID, s);
					words.put(s, ret);
				}
				return ret;
			}

			ret = new Token(peek, String.valueOf(peek));
			consumeChar();
			return ret;

		} catch (PieEOFException e) {
			if (ret == null)
				return PieLexer.EOF;
			return ret;
		}

	}
}
