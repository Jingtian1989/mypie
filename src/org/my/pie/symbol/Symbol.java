package org.my.pie.symbol;

import org.my.pie.scope.Scope;

public class Symbol {

	public String name;
	public Scope scope;

	public Symbol(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		String s = "";
		if (scope != null)
			s = scope.getScopeName() + ".";
		return s + getName();
	}

}
