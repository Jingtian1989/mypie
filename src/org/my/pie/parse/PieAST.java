package org.my.pie.parse;

import java.util.LinkedList;
import java.util.List;

import org.my.pie.lex.Token;

public class PieAST {

	public Token token;
	public List<PieAST> children = new LinkedList<PieAST>();

	public PieAST(Token token) {
		this.token = token;
	}
	
	public int getNodeType() {
		return token.getType();
	}
	public void addChild(PieAST t) {
		children.add(t);
	}

}
