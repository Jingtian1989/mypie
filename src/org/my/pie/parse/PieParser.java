package org.my.pie.parse;

import java.util.LinkedList;
import java.util.List;

import org.my.pie.lex.PieUnkownSymbolException;
import org.my.pie.lex.PieLexer;
import org.my.pie.lex.Token;

public class PieParser {

	public PieLexer lexer;
	public List<Token> lookahead = new LinkedList<Token>();
	public List<Integer> markers = new LinkedList<Integer>();
	public int p = 0;
	
	public PieParser (PieLexer lexer) {
		this.lexer = lexer;
	}

	public void match(int type) throws PieUnkownSymbolException,
			PieMissmatchedException {
		if (lookAhead(1) == type) {
			consumeToken();
		} else {
			throw new PieMissmatchedException("parse error at line "
					+ PieLexer.line + ", expected '" + String.valueOf(type)
					+ "'" + ", but encountered '"
					+ String.valueOf(lookAhead(1)) + "'");
		}
	}

	private void consumeToken() throws PieUnkownSymbolException {
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

	public int lookAhead(int i) throws PieUnkownSymbolException {
		return lookToken(i).getType();
	}

	public Token lookToken(int i) throws PieUnkownSymbolException {
		syncTokens(i);
		return lookahead.get(p + i - 1);
	}

	public void syncTokens(int i) throws PieUnkownSymbolException {
		if (p + i - 1 > (lookahead.size() - 1)) {
			int n = (p + i - 1) - (lookahead.size() - 1);
			fillTokens(n);
		}
	}

	public void fillTokens(int n) throws PieUnkownSymbolException {
		for (int i = 0; i < n; i++) {
			lookahead.add(lexer.nextToken());
		}
	}
	
	
	
}
