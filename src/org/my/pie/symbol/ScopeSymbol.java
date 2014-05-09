package org.my.pie.symbol;

import java.util.Map;

import org.my.pie.scope.Scope;

public abstract class ScopeSymbol extends Symbol implements Scope {

	private Scope enclosingScope;

	public ScopeSymbol(String name, Scope enclosingScope) {
		super(name);
		this.enclosingScope = enclosingScope;
	}

	public String getScopeName() {
		return name;
	}

	public Scope getEnclosingScope() {
		return enclosingScope;
	}

	public void define(Symbol sym) {
		this.getMembers().put(sym.name, sym);
	}

	public Symbol resolve(String name) {
		Symbol s = this.getMembers().get(name);
		if (s != null)
			return s;
		if (this.getEnclosingScope() != null) {
			return this.getEnclosingScope().resolve(name);
		}
		return null;
	}

	public abstract Map<String, Symbol> getMembers();

}
