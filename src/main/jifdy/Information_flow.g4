grammar Information_flow;

// Labels
PPLABEL     : 'public' | 'private';
SEND        : 'send';
INPUT       : 'input';
TRY_RCV     : 'try_rcv';
KEY         : 'k'[a-zA-Z0-9_]*;

// Security labels
SECLABEL : 'high' | 'low';

//Primitive types
INT     : [0-9]+;
BOOL    : 'true' | 'false';
STR     : '"' ('\\' ["\\] | ~["\\\r\n])* '"';

//Control structures
IF      : 'if';
ELSEIF  : 'elseif';
ELSE    : 'else';
FOR     : 'for';
WHILE   : 'while';

//Operators
OP      : '%' | '+' | '-' | '*' | '/' | '^' | '>' | '<' | '>=' | '<=' | '==' | '!=';
NOT     : 'not'|'!';
AND     : 'and'|'&&';
OR      : 'or'|'||';

//Delimiters
IDENTIFIER  : [a-zA-Z][a-zA-Z0-9_]*;

WHITESPACE : [ \n\t]+  -> skip ;
CARRIAGE_RETURN : '\r' -> skip;
QUOTE   : ' '          -> skip;
NEWLINE : '\n'         -> skip;
LINE_COMMENT
    : '//' ~[\r\n]*    -> skip
    ;

program
    : globalDeclaration* class+
    ;

globalDeclaration
    : keyDeclaration
    | formatDeclaration
    ;

keyDeclaration
    : 'key' KEY ';'
    ;

formatDeclaration
    : 'format' IDENTIFIER '(' decls? ')' ';'
    ;

class
    : ( PPLABEL )? 'class' classBlock
    ;

classBlock
    : IDENTIFIER ( 'extends' IDENTIFIER )? '{' declaration* methodDeclaration* statement* '}'
    ;

declaration
    : type SECLABEL IDENTIFIER ('=' expression )? ';'
    | PPLABEL IDENTIFIER '(' decls* ')' '{' assignmentStatement* '}' // Constructor decleration
    ;

methodDeclaration
    : PPLABEL ( type | 'void' ) SECLABEL IDENTIFIER '(' (decls)* ')' cmdBlock
    ;

cmdBlock
    : '{' ( declaration | statement | cmdBlock )* '}'
    ;

decls
    : declItem ( ',' declItem )*
    ;

declItem
    : type SECLABEL IDENTIFIER
    ;

type
    : basicType
    | encryptionType
    | IDENTIFIER
    ;

basicType
    : 'int'
    | 'bool'
    | 'String'
    ;

encryptionType
    : 'e' '(' KEY ',' IDENTIFIER ')'
    ;

statement
    : assignmentStatement
    | methodCallOrFormat ';'
    | sendStatement
    | inputStatement
    | receiveStatement
    | returnStatement
    | ifStatement
    | whileStatement
    | print ';'
    ;

returnStatement
    : 'return' expression ';'
    ;

// EXAMPLE: t = e(k,z); send(t);
sendStatement
    : SEND '(' IDENTIFIER ')' ';'
    ;

inputStatement
    : 'input' '(' lvalue ')' ';'
    ;

receiveStatement
    : TRY_RCV '(' expression ')' cmdBlock
    ;

expression
    : INT
    | BOOL
    | STR
    | 'new' IDENTIFIER '(' ')'
    | IDENTIFIER
    | methodCallOrFormat
    | 'e' '(' KEY ',' expression ')'
    | '(' expression ')'
    | expression '.' IDENTIFIER
    | expression '.' methodCallOrFormat
    | NOT expression
    | '-' expression
    | expression ('*' | '/' | '%') expression
    | expression ('+' | '-') expression
    | expression ('>' | '<' | '>=' | '<=') expression
    | expression ('==' | '!=') expression
    | expression AND expression
    | expression OR expression
    ;

methodCallOrFormat
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : expression (',' expression)*
    ;

assignmentStatement
    : lvalue '=' expression ';'
    ;

lvalue
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

ifStatement
    : 'if' '(' expression ')' cmdBlock (elseifStatement)* (elseStatement)?
    ;

elseStatement
    : 'else' cmdBlock
    ;

elseifStatement
    : 'elseif' '(' expression ')' cmdBlock
    ;

whileStatement
    : 'while' '(' expression ')' cmdBlock
    ;

print
    : 'print' '(' (expression) (',' (expression))* ')'
    ;
