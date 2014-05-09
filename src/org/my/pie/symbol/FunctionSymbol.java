package org.my.pie.symbol;

import java.util.LinkedHashMap;
import java.util.Map;

import org.my.pie.parse.PieAST;
import org.my.pie.scope.Scope;

public class FunctionSymbol extends ScopeSymbol {

	Map<String, Symbol> formalArgs = new LinkedHashMap<String, Symbol>();
	PieAST blockAST;

	public FunctionSymbol(String name, Scope enclosingScope) {
		super(name, enclosingScope);
	}

	public Map<String, Symbol> getMembers() {
		return formalArgs;
	}

	public String getName() {
		return name + "{" + formalArgs.keySet().toString() + "}";
	}

	public void setBlockAST(PieAST ast) {
		blockAST = ast;
	}

}
