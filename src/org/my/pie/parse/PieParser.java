package org.my.pie.parse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.my.pie.exception.PieMissmatchedException;
import org.my.pie.exception.PiePreviousParseFailedException;
import org.my.pie.exception.PieRecognitionException;
import org.my.pie.interpreter.PieInterpreter;
import org.my.pie.lex.PieLexer;
import org.my.pie.lex.Tag;
import org.my.pie.lex.Token;
import org.my.pie.scope.LocalScope;
import org.my.pie.scope.Scope;
import org.my.pie.symbol.FunctionSymbol;
import org.my.pie.symbol.StructSymbol;

public class PieParser {

	public PieLexer lexer;
	public List<Token> lookahead = new LinkedList<Token>();
	public List<Integer> markers = new LinkedList<Integer>();
	public int p = 0;

	public PieInterpreter interpreter;
	public Scope currentScope;

	Map<Integer, Integer> statementMemo = new HashMap<Integer, Integer>();
	Map<Integer, Integer> functionDefinitionMemo = new HashMap<Integer, Integer>();
	Map<Integer, Integer> structDefinitionMemo = new HashMap<Integer, Integer>();
	Map<Integer, Integer> vardefMemo = new HashMap<Integer, Integer>();
	Map<Integer, Integer> qidMemo = new HashMap<Integer, Integer>();
	Map<Integer, Integer> callMemo = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> exprMemo = new HashMap<Integer, Integer>();

	public PieParser(PieLexer lexer, PieInterpreter interpreter) {
		this.lexer = lexer;
		this.interpreter = interpreter;
		this.currentScope = interpreter.getGlobalScope();
	}

	private int index() {
		return p;
	}

	public void match(int type) throws PieMissmatchedException {
		if (lookAhead(1) == type) {
			consumeToken();
		} else {
			throw new PieMissmatchedException("parse error at line "
					+ PieLexer.line + ", expected '" + String.valueOf(type)
					+ "'" + ", but encountered '"
					+ String.valueOf(lookAhead(1)) + "'");
		}
	}

	private void consumeToken() throws PieMissmatchedException {
		p++;
		if (p == lookahead.size() && !isSpeculating()) {
			p = 0;
			lookahead.clear();
		}
		syncTokens(1);
	}

	public int mark() {
		markers.add(p);
		return p;
	}

	public void release() {
		int marker = markers.get(markers.size() - 1);
		markers.remove(markers.size() - 1);
		seek(marker);
	}

	private void seek(int marker) {
		p = marker;
	}

	private boolean isSpeculating() {
		return markers.size() > 0;
	}

	public int lookAhead(int i) throws PieMissmatchedException {
		return lookToken(i).getType();
	}

	public Token lookToken(int i) throws PieMissmatchedException {
		syncTokens(i);
		return lookahead.get(p + i - 1);
	}

	public void syncTokens(int i) throws PieMissmatchedException {
		if (p + i - 1 > (lookahead.size() - 1)) {
			int n = (p + i - 1) - (lookahead.size() - 1);
			fillTokens(n);
		}
	}

	public void fillTokens(int n) throws PieMissmatchedException {
		for (int i = 0; i < n; i++) {
			lookahead.add(lexer.nextToken());
		}
	}

	private void memorize(Map<Integer, Integer> memo, int startTokenIndex,
			boolean failed) {
		int stopTokenIndex = failed ? -1 : index();
		memo.put(startTokenIndex, stopTokenIndex);
	}

	private boolean alreadyParsedRule(Map<Integer, Integer> memo)
			throws PiePreviousParseFailedException {
		Integer memoIndex = memo.get(index());
		if (memoIndex == null)
			return false;
		int index = memoIndex.intValue();
		if (index == -1)
			throw new PiePreviousParseFailedException();
		seek(index);
		return true;
	}

	public PieAST program() throws PieRecognitionException,
			PieMissmatchedException {
		PieAST root = new PieAST(new Token(Tag.BLOCK, "block"));
		// program : (functionDefinition | statement )+ EOF => ^(BLOCK
		// statement+)
		do {
			if (speculateFunctionDefinition()) {
				_functionDefiniton();
			} else if (speculateStatement()) {
				PieAST statement = _statement();
				root.addChild(statement);
			}
		} while (speculateFunctionDefinition() || speculateStatement());
		return root;
	}

	private boolean speculateFunctionDefinition()
			throws PieMissmatchedException {
		boolean success = true;
		mark();
		try {
			functionDefiniton();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void functionDefiniton() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(functionDefinitionMemo))
			return;
		try {
			_functionDefiniton();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
			throw new PieRecognitionException();
		} finally {
			memorize(functionDefinitionMemo, startTokenIndex, failed);
		}
		return;
	}

	private void _functionDefiniton() throws PieRecognitionException,
			PieMissmatchedException {
		match(Tag.DEF);
		Token id = lookToken(1);
		match(Tag.ID);
		Scope savedScope = currentScope;
		FunctionSymbol fs = null;
		// functionDefinition : 'def' ID '(' (vardef (',' vardef)* )? ')' slist
		// =>pass nothing to interpreter
		if (!isSpeculating()) {
			fs = new FunctionSymbol(id.getValue(), currentScope);
			currentScope.define(fs);
			currentScope = fs;
		}
		match('(');
		if (speculateVardef()) {
			_vardef();
			while (lookAhead(1) == ',') {
				match(',');
				_vardef();
			}
		}
		match(')');

		if (!isSpeculating()) {
			currentScope = new LocalScope(fs);
		}
		PieAST slist = _slist();
		if (!isSpeculating()) {
			fs.setBlockAST(slist);
		}
		currentScope = savedScope;
	}

	private boolean speculateVardef() {
		boolean success = true;
		mark();
		try {
			vardef();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void vardef() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(vardefMemo))
			return;
		try {
			_vardef();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
			throw new PieRecognitionException();
		} finally {
			memorize(vardefMemo, startTokenIndex, failed);
		}
		return;
	}

	private void _vardef() throws PieRecognitionException,
			PieMissmatchedException {

	}

	private PieAST _slist() throws PieMissmatchedException,
			PieRecognitionException {
		PieAST slist = new PieAST(new Token(Tag.BLOCK, "block"));
		// slist : ':' NL statement+ NL => ^(BLOCK statement+)
		if (lookAhead(1) == ':') {
			match(':');
			match(Tag.NL);
			do {
				PieAST statement = _statement();
				slist.addChild(statement);
			} while (speculateStatement());
			match(Tag.DOT);
			match(Tag.NL);
		} else {
			// statement => ^(BLOCK statement)
			PieAST statement = _statement();
			slist.addChild(statement);
		}
		return slist;
	}

	private boolean speculateStatement() throws PieMissmatchedException {
		boolean success = true;
		mark();
		try {
			statement();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void statement() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(statementMemo))
			return;
		try {
			_statement();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
			throw new PieRecognitionException();
		} finally {
			memorize(statementMemo, startTokenIndex, failed);
		}
		return;
	}

	private PieAST _statement() throws PieRecognitionException,
			PieMissmatchedException {
		PieAST statement = null;
		// statement : structDefinition
		if (speculateStructDefinition()) {
			statement = null;
			_structDefinition();
		} else if (speculateQid()) {
			// qid '=' expr NL => ^('=' qid expr)
			statement = new PieAST(lexer.ASSIGN);
			PieAST qid = _qid();
			match(Tag.ASSIGN);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(qid);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.RETURN) {
			// 'return' expr NL => ^('return' expr)
			statement = new PieAST(lexer.NL);
			match(Tag.RETURN);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.PRINT) {
			// 'print' expr NL => ^('print' expr)
			statement = new PieAST(lexer.PRINT);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.IF) {
			// 'if' expr c=slist ('else' el=slist)? => ^('if' expr $c %el?)
			statement = new PieAST(lexer.IF);
			match(Tag.IF);
			PieAST expr = _expr();
			PieAST c = _slist();
			PieAST el = null;
			if (lookToken(1).getValue().equals("else")) {
				match(Tag.ID);
				el = _slist();
			}
			statement.addChild(c);
			if (el != null) {
				statement.addChild(el);
			}
		} else if (lookAhead(1) == Tag.WHILE) {
			// 'while' expr slist => ^('while' expr slist)
			statement = new PieAST(lexer.WHILE);
			PieAST expr = _expr();
			PieAST slist = _slist();
			statement.addChild(expr);
			statement.addChild(slist);
		} else if (speculateCall()) {
			// call NL => call
			statement = _call();
			match(Tag.NL);
		}
		return statement;
	}

	private boolean speculateStructDefinition() {
		boolean success = true;
		mark();
		try {
			structDefinition();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void structDefinition() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(structDefinitionMemo))
			return;
		try {
			_structDefinition();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
			throw new PieRecognitionException();
		} finally {
			memorize(structDefinitionMemo, startTokenIndex, failed);
		}
		return;
	}

	private void _structDefinition() throws PieRecognitionException,
			PieMissmatchedException {
		// structDefinition
		// : 'struct' name=ID '{' vardef (',' vardef)* '}' NL
		// => // pass nothing to interpreter
		//
		Scope savedScope = currentScope;
		match(Tag.STRUCT);
		Token id = lookToken(1);
		match(Tag.ID);
		match('{');

		if (!isSpeculating()) {
			StructSymbol ss = new StructSymbol(id.getValue(), currentScope);
			currentScope.define(ss);
			currentScope = ss;
		}
		_vardef();
		while (lookAhead(1) == ',') {
			match(',');
			_vardef();
		}
		match('}');
		match(Tag.NL);
		currentScope = savedScope;
		return;
	}

	private boolean speculateQid() {
		boolean success = true;
		mark();
		try {
			qid();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void qid() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(qidMemo))
			return;
		try {
			_qid();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
		} finally {
			memorize(qidMemo, startTokenIndex, failed);
		}
		return;
	}

	private PieAST _qid() throws PieMissmatchedException,
			PieRecognitionException {
		// qid : ID ('.'^ ID)*
		Token id = lookToken(1);
		match(Tag.ID);
		PieAST qid = new PieAST(id);
		while (lookAhead(1) == Tag.DOT) {
			match(Tag.DOT);
			Token cid = lookToken(1);
			match(Tag.ID);
			PieAST cst = new PieAST(cid);
			qid.addChild(cst);
		}
		return qid;

	}

	private boolean speculateCall() {
		boolean success = true;
		mark();
		try {
			call();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void call() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(callMemo))
			return;
		try {
			_call();
		} catch (PieRecognitionException e) {
			failed = true;
			throw e;
		} catch (PieMissmatchedException e) {
			failed = true;
			throw new PieRecognitionException();
		} finally {
			memorize(callMemo, startTokenIndex, failed);
		}
		return;
	}

	private PieAST _call() throws PieRecognitionException,
			PieMissmatchedException {
		// call : name=ID '(' (expr (',' expr )*)? ')' => ^(CALL ID expr*) ;
		Token id = lookToken(1);
		match(Tag.ID);
		PieAST call = new PieAST(new Token(Tag.CALL, id.getValue()));
		match('(');
		if (speculateExpr()) {
			PieAST expr = _expr();
			call.addChild(expr);
		}
		while (lookAhead(1) == ',') {
			match(',');
			PieAST expr = _expr();
			call.addChild(expr);
		}
		match(')');
		match(Tag.NL);
		call.setScope(currentScope);
		return call;
	}

	private boolean speculateExpr() {
		boolean success = true;
		mark();
		try {
			expr();
		} catch (PieRecognitionException e) {
			success = false;
		}
		release();
		return success;
	}

	private void expr() throws PieRecognitionException {
		boolean failed = false;
		int startTokenIndex = index();
		if (alreadyParsedRule(exprMemo))
			return;
		try {
			_expr();
		} catch (PieMissmatchedException e) {
			failed = false;
			throw new PieRecognitionException();
		} finally {
			memorize(exprMemo, startTokenIndex, failed);
		}
		return;
	}

	private PieAST _expr() throws PieMissmatchedException {
		// expr: addexpr (('=='|'<')^ addexpr)? ;
		PieAST ret = _addExpr();
		if (lookAhead(1) == Tag.EQ || lookAhead(1) == Tag.LT) {
			PieAST root = new PieAST(lookToken(1));
			consumeToken();
			root.addChild(ret);
			ret = _addExpr();
			root.addChild(ret);
			ret = root;
		}
		return ret;
	}

	private PieAST _addExpr() throws PieMissmatchedException {
		// addexpr : mulexpr (('+'|'-')^ mulexpr)*
		PieAST ret = _mulExpr();
		while (lookAhead(1) == Tag.ADD || lookAhead(1) == Tag.SUB) {
			PieAST root = new PieAST(lookToken(1));
			consumeToken();
			root.addChild(ret);
			ret = _mulExpr();
			root.addChild(ret);
			ret = root;
		}
		return ret;
	}

	private PieAST _mulExpr() throws PieMissmatchedException {
		// mulexpr : atom ('*'^ atom)*
		PieAST ret = _atom();
		while (lookAhead(1) == Tag.MUL) {
			PieAST root = new PieAST(lookToken(1));
			consumeToken();
			root.addChild(ret);
			ret = _atom();
			root.addChild(ret);
			ret = root;
		}
		return ret;
	}

	private PieAST _atom() {
		// TODO Auto-generated method stub
		return null;
	}

}
