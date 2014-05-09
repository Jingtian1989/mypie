#	mypie

pie is a demonstration of book TPDSL, it's a simple dynamically-typed language that smacks of Python.
I abandoned the automation tool of antlr, implementing it by the backtrack-LL(k) parsing skill.


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
	x = 1
	def f(y):
		x = 2
		y = 3
		z = 4
	.
	f(5)
