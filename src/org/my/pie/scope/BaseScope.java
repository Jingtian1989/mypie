package org.my.pie.scope;

import java.util.LinkedHashMap;
import java.util.Map;
import org.my.pie.symbol.Symbol;

public abstract class BaseScope implements Scope {

	public Scope enclosingScope;
	public Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

	public BaseScope(Scope enclosingScope) {
		this.enclosingScope = enclosingScope;
	}

	public Scope getEnclosingScope() {
		return enclosingScope;
	}

	public void define(Symbol sym) {
		symbols.put(sym.name, sym);
		sym.scope = this;
	}

	public Symbol resolve(String name) {
		Symbol s = symbols.get(name);
		if (s != null)
			return s;
		if (getEnclosingScope() != null)
			return getEnclosingScope().resolve(name);
		return null;
	}

	public String toString() {
		return symbols.keySet().toString();
	}

}
