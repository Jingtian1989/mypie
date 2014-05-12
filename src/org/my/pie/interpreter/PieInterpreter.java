package org.my.pie.interpreter;

import java.util.List;
import java.util.Stack;

import org.my.pie.exception.PieUnsupportedOperationException;
import org.my.pie.lex.Tag;
import org.my.pie.parse.PieAST;
import org.my.pie.scope.GlobalScope;
import org.my.pie.scope.Scope;
import org.my.pie.symbol.FunctionSymbol;
import org.my.pie.symbol.StructSymbol;
import org.my.pie.symbol.Symbol;
import org.my.pie.symbol.VariableSymbol;

public class PieInterpreter {

	private GlobalScope globalScope = new GlobalScope();
	private MemorySpace globalSpace = new MemorySpace("global");
	private Stack<FunctionSpace> functionStack = new Stack<FunctionSpace>();
	private PieAST codeAST;

	public Scope getGlobalScope() {
		return globalScope;
	}

	public void execute() {
		block(codeAST);
	}

	private Object executeAST(PieAST ast) {

		try {
			switch (ast.getASTType()) {
			case Tag.BLOCK:
				block(ast);
				break;
			case Tag.ASSIGN:
				assign(ast);
				break;
			case Tag.RETURN:
				returnStat(ast);
				break;
			case Tag.PRINT:
				print(ast);
				break;
			case Tag.IF:
				ifStat(ast);
				break;
			case Tag.WHILE:
				whileStat(ast);
				break;
			case Tag.CALL:
				return call(ast);
			case Tag.NEW:
				return instance(ast);
			case Tag.ADD:
				return add(ast);
			case Tag.SUB:
				return op(ast);
			case Tag.MUL:
				return op(ast);
			case Tag.EQ:
				return eq(ast);
			case Tag.LT:
				return lt(ast);
			case Tag.INT:
				return Integer.parseInt(ast.getASTValue());
			case Tag.CHAR:
				return new Character(ast.getASTValue().charAt(0));
			case Tag.FLOAT:
				return Float.parseFloat(ast.getASTValue());
			case Tag.STRING:
				return ast.getASTValue();
			case Tag.DOT:
			case Tag.ID:
				return load(ast);
			default:
				throw new PieUnsupportedOperationException(ast.getASTValue()
						+ "<" + String.valueOf(ast.getASTType()) + ">");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void error(String err) {
		System.err.println(err);
	}

	private void block(PieAST ast) {
		List<PieAST> slist = ast.getChildren();
		for (PieAST x : slist) {
			executeAST(ast);
		}
	}

	private void assign(PieAST ast) {
		PieAST left = ast.getChild(0);
		PieAST right = ast.getChild(1);

		Object value = executeAST(right);
		if (left.getASTType() == Tag.DOT) {
			fieldAssign(left, value);
			return;
		}
		MemorySpace space = getSpaceWithSymbol(left.getASTValue());
		if (space == null) {
			space = globalSpace;
		}
		space.put(left.getASTValue(), value);
	}

	private void fieldAssign(PieAST ast, Object value) {
		PieAST obj = ast.getChild(0);
		PieAST field = ast.getChild(1);

		String fieldName = field.getASTValue();
		Object objSpace = load(obj);

		if (!(objSpace instanceof StructSpace)) {
			error(obj.getASTValue() + " is not a symbol");
			return;
		}

		StructSpace objSpace2 = (StructSpace) objSpace;
		if (objSpace2.getStructSymbol().resolveMember(fieldName) == null) {
			error(obj.getASTValue() + "has no field " + fieldName);
			return;
		}
		objSpace2.put(fieldName, value);
	}

	private MemorySpace getSpaceWithSymbol(String astValue) {
		if (functionStack.size() > 0
				&& functionStack.peek().get(astValue) != null) {
			return functionStack.peek();
		}
		if (globalSpace.get(astValue) != null)
			return globalSpace;
		return null;
	}

	private Object load(PieAST ast) {

		if (ast.getASTType() == Tag.DOT) {
			return fieldLoad(ast);
		}
		MemorySpace space = getSpaceWithSymbol(ast.getASTValue());
		if (space != null)
			return space.get(ast.getASTValue());
		error("no such variable " + ast.getASTValue());
		return null;
	}

	private Object fieldLoad(PieAST ast) {
		PieAST obj = ast.getChild(0);
		PieAST field = ast.getChild(1);
		String fieldName = field.getASTValue();
		StructSpace structSpace = (StructSpace) load(obj);
		if (structSpace.getStructSymbol().resolveMember(fieldName) == null) {
			error(obj.getASTValue() + " has no field " + fieldName);
		}
		return structSpace.get(fieldName);
	}

	private void returnStat(PieAST ast) {
		Object obj = executeAST(ast.getChild(0));
		ReturnValue value = new ReturnValue();
		value.setValue(obj);
		throw value;
	}

	private void print(PieAST ast) {
		PieAST expr = ast.getChild(0);
		System.out.println(executeAST(ast));
	}

	private void ifStat(PieAST ast) {
		PieAST condStart = ast.getChild(0);
		PieAST codeStart = ast.getChild(1);
		PieAST elseStart = null;

		if (ast.getChildCount() == 3) {
			elseStart = ast.getChild(2);
		}
		Boolean cond = (Boolean) executeAST(condStart);
		if (cond.booleanValue()) {
			executeAST(codeStart);
		} else if (elseStart != null) {
			executeAST(elseStart);
		}
	}

	private void whileStat(PieAST ast) {
		PieAST condStart = ast.getChild(0);
		PieAST codeStart = ast.getChild(1);
		Boolean cond = (Boolean) executeAST(ast);
		while (cond.booleanValue()) {
			executeAST(condStart);
			cond = (Boolean) executeAST(condStart);
		}
	}

	private Object call(PieAST ast) {
		String functionName = ast.getChild(0).getASTValue();
		FunctionSymbol functionSymbol = (FunctionSymbol) ast.getScope()
				.resolve(functionName);
		if (functionSymbol == null) {
			error("no such function " + functionName);
		}
		FunctionSpace functionSpace = new FunctionSpace(functionSymbol);

		int argCount = ast.getChildCount() - 1;
		if (functionSymbol.getFormalArgsCount() != argCount) {
			error("function " + functionName + " argument list mismatch");
			return null;
		}
		int i = 0;
		for (Symbol argSymbol : functionSymbol.getFormalArgs()) {
			VariableSymbol arg = (VariableSymbol) argSymbol;
			PieAST argExpr = ast.getChild(i);
			Object argValue = executeAST(argExpr);
			functionSpace.put(arg.getName(), argValue);
			i++;
		}
		Object result = null;
		functionStack.push(functionSpace);
		try {
			executeAST(functionSymbol.getBlockAST());
		} catch (ReturnValue value) {
			result = null;
		}
		functionStack.pop();
		return result;
	}

	private Object instance(PieAST ast) {
		String instanceType = ast.getASTValue();
		StructSymbol symbol = (StructSymbol) ast.getScope().resolve(
				instanceType);
		return new StructSpace(symbol);
	}

	private Object add(PieAST ast) {
		Object arg1 = executeAST(ast.getChild(0));
		Object arg2 = executeAST(ast.getChild(1));
		if (arg1 instanceof String || arg2 instanceof String) {
			return arg1.toString() + arg2.toString();
		}
		return op(ast);
	}

	private Object op(PieAST ast) {
		Object arg1 = executeAST(ast.getChild(0));
		Object arg2 = executeAST(ast.getChild(1));
		if (arg1 instanceof Float || arg2 instanceof Float) {
			float farg1 = ((Number) arg1).floatValue();
			float farg2 = ((Number) arg2).floatValue();
			switch (ast.getASTType()) {
			case Tag.ADD:
				return farg1 + farg2;
			case Tag.SUB:
				return farg1 - farg2;
			case Tag.MUL:
				return farg1 * farg2;
			}
		}
		if (arg1 instanceof Integer || arg2 instanceof Integer) {
			int iarg1 = ((Number) arg1).intValue();
			int iarg2 = ((Number) arg2).intValue();
			switch (ast.getASTType()) {
			case Tag.ADD:
				return iarg1 + iarg2;
			case Tag.SUB:
				return iarg1 - iarg2;
			case Tag.MUL:
				return iarg1 * iarg2;
			}
		}
		return 0;
	}

	private Object eq(PieAST ast) {
		Object arg1 = executeAST(ast.getChild(0));
        Object arg2 = executeAST(ast.getChild(1));
        return arg1.equals(arg2);
	}

	private Object lt(PieAST ast) {
		Object arg1 = executeAST(ast.getChild(0));
		Object arg2 = executeAST(ast.getChild(1));
		if (arg1 instanceof Number && arg2 instanceof Number) {
			Number narg1 = (Number) arg1;
			Number narg2 = (Number) arg2;
			return narg1.floatValue() < narg2.floatValue();
		}
		return false;
	}

	public void setCodeAST(PieAST root) {
		this.codeAST = root;
	}
}
