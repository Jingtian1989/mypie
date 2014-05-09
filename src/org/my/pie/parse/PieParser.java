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

	/*
	 * program : (functionDefinition | statement )+ EOF => ^(BLOCK statement+)
	 */
	public PieAST program() throws PieRecognitionException,
			PieMissmatchedException {
		PieAST root = new PieAST(new Token(Tag.BLOCK, "block"));
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

	/*
	 * functionDefinition : 'def' ID '(' (vardef (',' vardef)* )? ')' slist =>
	 * // pass nothing to interpreter
	 */
	private void _functionDefiniton() throws PieRecognitionException,
			PieMissmatchedException {
		match(Tag.DEF);
		Token id = lookToken(1);
		match(Tag.ID);
		Scope savedScope = currentScope;
		FunctionSymbol fs = null;
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

	/*
	 * slist : ':' NL statement+ NL => ^(BLOCK statement+) | statement =>
	 * ^(BLOCK statement)
	 */
	private PieAST _slist() throws PieMissmatchedException,
			PieRecognitionException {
		PieAST slist = new PieAST(new Token(Tag.BLOCK, "block"));
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

	/*
	 * statement : structDefinition | qid '=' expr NL => ^('=' qid expr) |
	 * 'return' expr NL => ^('return' expr) | 'print' expr NL => ^('print' expr)
	 * | 'if' expr c=slist ('else' el=slist)? => ^('if' expr $c %el?) | 'while'
	 * expr slist => ^('while' expr slist) | call NL => call | NL ->
	 */
	private PieAST _statement() throws PieRecognitionException,
			PieMissmatchedException {
		PieAST statement = null;
		if (speculateStructDefinition()) {
			statement = null;
			_structDefinition();
		} else if (speculateQid()) {
			statement = new PieAST(lexer.ASSIGN);
			PieAST qid = _qid();
			match(Tag.ASSIGN);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(qid);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.RETURN) {
			statement = new PieAST(lexer.NL);
			match(Tag.RETURN);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.PRINT) {
			statement = new PieAST(lexer.PRINT);
			PieAST expr = _expr();
			match(Tag.NL);
			statement.addChild(expr);
		} else if (lookAhead(1) == Tag.IF) {
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
			statement = new PieAST(lexer.WHILE);
			PieAST expr = _expr();
			PieAST slist = _slist();
			statement.addChild(expr);
			statement.addChild(slist);
		} else if (speculateCall()) {
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
		match(Tag.STRUCT);
		Token id = lookToken(1);
		match(Tag.ID);
		match('{');
		Scope savedScope = currentScope;
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

	private PieAST _call() {
		return null;
	}

	private boolean speculateCall() {
		return false;
	}

	private PieAST _expr() {
		return null;
	}

}
