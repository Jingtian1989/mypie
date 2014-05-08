package org.my.pie.scope;

public class LocalScope extends BaseScope{

	public LocalScope(Scope enclosingScope) {
		super(enclosingScope);
	}

	public String getScopeName() {
		return "local";
	}
}
