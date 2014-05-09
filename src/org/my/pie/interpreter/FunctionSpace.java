package org.my.pie.interpreter;

import org.my.pie.symbol.FunctionSymbol;

public class FunctionSpace extends MemorySpace {
	private FunctionSymbol functionSymbol;

	public FunctionSpace(FunctionSymbol function) {
		super(function.getName());
		this.functionSymbol = function;
	}

}
