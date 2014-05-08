package org.my.pie.scope;

import org.my.pie.symbol.Symbol;

public interface Scope {

	public String getScopeName();

	public Scope getEnclosingScope();
	
	public void define(Symbol sym);
	
	public Symbol resolve(String name);
	
}
