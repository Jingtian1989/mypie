package org.my.pie.symbol;

import java.util.LinkedHashMap;
import java.util.Map;

import org.my.pie.scope.Scope;

public class StructSymbol extends ScopeSymbol implements Scope {

	private Map<String, Symbol> fields = new LinkedHashMap<String, Symbol>();

	public StructSymbol(String name, Scope enclosingScope) {
		super(name, enclosingScope);
	}

	public Symbol resolveMember(String name) {
		return fields.get(name);
	}

	public Map<String, Symbol> getMembers() {
		return fields;
	}

	public String toString() {
		return "struct" + name + ":{" + fields.keySet().toString() + "}";
	}

}
