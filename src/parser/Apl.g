grammar Apl;

options {
    output = AST;
    ASTLabelType = AplTree;
}

// Imaginary tokens to create some AST nodes

tokens {
    LIST_FUNCTIONS; // List of functions (the root of the tree)
    ASSIGN;     // Assignment instruction
    PARAMS;     // List of parameters in the declaration of a function
    FUNCALL;    // Function call
    ARGLIST;    // List of arguments passed in a function call
    LIST_INSTR; // Block of instructions
    BOOLEAN;    // Boolean atom (for Boolean constants "true" or "false")
    PVALUE;     // Parameter by value in the list of parameters
    PREF;       // Parameter by reference in the list of parameters
    IDARR;      // An ID with array getter ([x])
}

@header {
package parser;
import interp.AplTree;
}

@lexer::header {
package parser;
}


// A program is a list of functions
prog	: func+ EOF -> ^(LIST_FUNCTIONS func+)
        ;
            
// A function has a name, a list of parameters and a block of instructions	
func	: FUNC^ ID params block_instructions END!
        ;

// The list of parameters grouped in a subtree (it can be empty)
params	: '(' paramlist? ')' -> ^(PARAMS paramlist?)
        ;

// Parameters are separated by commas
paramlist: param (','! param)*
        ;

// Parameters with & as prefix are passed by reference
// Only one node with the name of the parameter is created
param   :   '&' id_atom -> ^(PREF id_atom)
        |   id_atom -> ^(PVALUE id_atom)
        ;

// A list of instructions, all of them gouped in a subtree
block_instructions
        :	 (instruction ';'?)* return_stmt?
            -> ^(LIST_INSTR instruction* return_stmt?)
        ;

// The different types of instructions
instruction
        :	assign          // Assignment
        |	ite_stmt        // if-then-else
        |	for_stmt        // for statement
        |	pfor_stmt       // pfor statement
        |	while_stmt      // while statement
        |   funcall         // Call to a procedure (no result produced)
        |	read            // Read a variable
        | 	write           // Write a string or an expression
        |   free            // Free a malloc'd array
        ;

// Assignment
assign  :    id_atom eq=ASSEQ expr -> ^(ASSIGN[$eq,":="] id_atom expr)
        ;

// if-then-else (else is optional)
ite_stmt	:	IF^ expr THEN! block_instructions (ELIF! expr THEN! block_instructions)* (ELSE! block_instructions)? END!
            ;

// for statement
for_stmt	:	FOR^ id_atom IN! expr ':'! expr block_instructions END!
            ;

// pfor statement
pfor_stmt	:	PFOR^ id_atom IN! expr ':'! expr block_instructions END!
            ;

// while statement
while_stmt	:	WHILE^ expr DO! block_instructions END!
            ;

// Return statement with an expression
return_stmt	:	RETURN^ expr?
        ;

// Read a variable
read	:	READ^ id_atom (FROM! expr)?
        ;

// Write an expression or a string
write	:   WRITE^ expr (TO! id_atom)?
        ;

// Write an expression or a string
free   :   FREE^ id_atom
        ;

// Grammar for expressions with boolean, relational and aritmetic operators
expr    :   boolterm (OR^ boolterm)*
        ;

boolterm:   boolfact (AND^ boolfact)*
        ;

boolfact:   num_expr ((EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?
        ;

num_expr:   term ( (PLUS^ | MINUS^) term)*
        ;

term    :   factor ( (MUL^ | DIV^ | MOD^) factor)*
        ;

factor  :   (NOT^ | PLUS^ | MINUS^)? atom
        ;

// Atom of the expressions (variables, integer and boolean literals).
// An atom can also be a function call or another expression
// in parenthesis
atom    :   id_atom
        |   INT
        |   FLOAT
        |   CHAR
        |   STRING
        |   (b=TRUE | b=FALSE)  -> ^(BOOLEAN[$b,$b.text])
        |   funcall
        |   write
        |   read
        |   '('! expr ')'!
        ;


id_atom :   ID
        |   id=ID('[' num_expr ']') -> ^(IDARR $id num_expr)
        ;

// A function call has a lits of arguments in parenthesis (possibly empty)
funcall :   ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?))
        ;

// A list of expressions separated by commas
expr_list:  expr (','! expr)*
        ;

// Basic tokens
ASSEQ   : '=' ;
EQUAL	: '==' ;
NOT_EQUAL: '!=' ;
LT	    : '<' ;
LE	    : '<=';
GT	    : '>';
GE	    : '>=';
PLUS	: '+' ;
MINUS	: '-' ;
MUL	    : '*';
DIV	    : '/';
MOD	    : '%' ;
NOT	    : 'not';
AND	    : 'and' ;
OR	    : 'or' ;	
IF  	: 'if' ;
ELIF    : 'elif' ;
THEN	: 'then' ;
ELSE	: 'else' ;
FOR	    : 'for' ;
PFOR	: 'pfor' ;
IN      : 'in';
WHILE	: 'while' ;
DO	    : 'do' ;
FUNC	: 'func' ;
RETURN	: 'return' ;
END     : 'end';
READ	: 'read' ;
FROM    : 'from' ;
WRITE	: 'write' ;
TO	    : 'to' ;
FREE    : 'free' ;
TRUE    : 'true' ;
FALSE   : 'false';
ID  	:	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
INT 	:	'0'..'9'+ ;
FLOAT 	:	('0'..'9'+'.'('0'..'9'+)? | '.''0'..'9'+) ;

// C-style comments
COMMENT	: '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    	| '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    	;

// Strings (in quotes) with escape sequences        
STRING  :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
        ;

// Char (in semiquotes) with escape sequences        
CHAR  :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
        ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

// White spaces
WS  	: ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    	;


