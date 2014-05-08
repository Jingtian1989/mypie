package org.my.pie.lex;

public class Token {

	public int tag;
	public String text;

	public Token(int tag, String text) {
		this.tag = tag;
		this.text = text;
	}

	public String toString() {
		return text;
	}

	public int getType() {
		return tag;
	}
}
