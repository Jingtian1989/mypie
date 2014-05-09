package org.my.pie.parse;

import java.util.LinkedList;
import java.util.List;

import org.my.pie.lex.Token;
import org.my.pie.scope.Scope;

public class PieAST {

	private Scope scope;
	private Token token;
	private List<PieAST> children = new LinkedList<PieAST>();

	public PieAST(Token token) {
		this.token = token;
	}

	public int getASTType() {
		return token.getType();
	}

	public String getASTValue() {
		return token.getValue();
	}

	public PieAST getChild(int index) {
		return children.get(index);
	}

	public List getChildren() {
		return children;
	}

	public void addChild(PieAST t) {
		children.add(t);
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public String toString() {
		return token.toString();
	}

}
