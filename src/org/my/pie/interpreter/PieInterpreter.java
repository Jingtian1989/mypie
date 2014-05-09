package org.my.pie.interpreter;

import org.my.pie.scope.GlobalScope;
import org.my.pie.scope.Scope;

public class PieInterpreter {

	public GlobalScope globalScope = new GlobalScope();

	public PieInterpreter() {
	}

	public Scope getGlobalScope() {
		return globalScope;
	}
}
