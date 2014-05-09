package org.my.pie.interpreter;

import java.util.List;
import java.util.Stack;

import org.my.pie.exception.PieUnsupportedOperationException;
import org.my.pie.lex.Tag;
import org.my.pie.parse.PieAST;
import org.my.pie.scope.GlobalScope;
import org.my.pie.scope.Scope;

public class PieInterpreter {

	private GlobalScope globalScope = new GlobalScope();
	private MemorySpace currentSpace = new MemorySpace("global");
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
			space = currentSpace;
		}
		space.put(left.getASTValue(), value);
	}

	private MemorySpace getSpaceWithSymbol(String astValue) {
		return null;
	}

	private void fieldAssign(PieAST left, Object value) {

	}

	private void returnStat(PieAST ast) {
		// TODO Auto-generated method stub

	}

	private void print(PieAST ast) {
		// TODO Auto-generated method stub

	}

	private void ifStat(PieAST ast) {
		// TODO Auto-generated method stub

	}

	private void whileStat(PieAST ast) {
		// TODO Auto-generated method stub

	}

	private Object call(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object instance(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object add(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object op(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object eq(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object lt(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object load(PieAST ast) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCodeAST(PieAST root) {
		this.codeAST = root;
	}
}
