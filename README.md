#	mypie

Pie is a demonstration in book TPDSL, it's a simple dynamically-typed language that smacks of Python.
I abandoned using the automation tool antlr, implementing it using the backtrack-LL(k) by hand.


##	Implementing Details

###	The Syntax

	program
		:	( functionDefinition | statement )+ EOF 
			-> ^(BLOCK statement+)
		;
		
	structDefinition
	    :   'struct' name=ID '{'  vardef (',' vardef)*  '}' NL
			-> // pass nothing to interpreter
	    ;

	functionDefinition
		:	'def' ID  '(' (vardef (',' vardef)* )? ')'	slist
			-> // pass nothing to interpreter
		;

	slist
		:	':' NL statement+ '.' NL	-> ^(BLOCK statement+)
		|	statement					-> ^(BLOCK statement)
		;

	statement
		:	structDefinition
		|	qid '=' expr NL				-> ^('=' qid expr)
		|	'return' expr NL  			-> ^('return' expr)
		|	'print' expr NL	 			-> ^('print' expr)
		|	'if' expr c=slist ('else' el=slist)? -> ^('if' expr $c $el?)
		|	'while' expr slist			-> ^('while' expr slist)
		|	call NL						-> call
		|	NL							->
		;

	call
		:	name=ID '(' (expr (',' expr )*)? ')' -> ^(CALL ID expr*) ;

	expr:	addexpr (('=='|'<')^ addexpr)? ;

	addexpr
		:	mulexpr (('+'|'-')^ mulexpr)*
		;

	mulexpr 
		:	atom ('*'^ atom)*
		;

	atom 
		:	INT		    
		|	CHAR	    
		|	FLOAT	    
		|	STRING	    
		|	qid		    
		|	call
		|	instance
		|	'(' expr ')' -> expr
		;

	instance
		:	'new' sname=ID
			-> ^('new' ID)
		;

	qid :	ID ('.'^ ID)* ;  // CAN'T RESOLVE TIL RUNTIME!

	vardef
		:	ID
		;

	// Lexical  Rules

	NL	:	'\r'? '\n' ;

	ID  :   LETTER (LETTER | '0'..'9')*  ;

	fragment
	LETTER
		:   ('a'..'z' | 'A'..'Z')
	    ;

	CHAR:	'\'' . '\'' ;

	STRING:	'\"' .* '\"' ;

	INT :   '0'..'9'+ ;
	    
	FLOAT
		:	INT '.' INT*
		|	'.' INT+
		;





###	A Program Demo

####	source code
	def printn(n):
		i = 0
		while i < n:
			print i
			i = i + 1
		.
	.
	n = 5
	printn(n)


####	output
	0
	1
	2
	3
	4