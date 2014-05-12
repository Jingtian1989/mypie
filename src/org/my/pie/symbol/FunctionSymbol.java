package org.my.pie.symbol;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.my.pie.parse.PieAST;
import org.my.pie.scope.Scope;

public class FunctionSymbol extends ScopeSymbol {

	private Map<String, Symbol> formalArgs = new LinkedHashMap<String, Symbol>();
	private PieAST blockAST;

	public FunctionSymbol(String name, Scope enclosingScope) {
		super(name, enclosingScope);
	}

	public Map<String, Symbol> getMembers() {
		return formalArgs;
	}
	
	public Collection<Symbol> getFormalArgs () {
		return formalArgs.values();
	}

	public int getFormalArgsCount() {
		return formalArgs.size();
	}

	public String getName() {
		return name + "{" + formalArgs.keySet().toString() + "}";
	}

	public void setBlockAST(PieAST ast) {
		blockAST = ast;
	}
	
	public PieAST getBlockAST() {
		return blockAST;
	}

}
