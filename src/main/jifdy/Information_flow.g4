grammar Information_flow;

// Labels
PPLABEL     : 'public' | 'private';
SEND        : 'send';
TRY_RCV     : 'try_rcv';
ENCRYPT     : 'e';
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

CARRIAGE_RETURN : '\r' -> skip;
QUOTE   : ' '       -> skip;
NEWLINE : '\n'      -> skip;
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

program
    : globalDeclaration* class
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
    : IDENTIFIER '{' declaration* methodDeclaration* statement* '}'
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
    : ENCRYPT '(' KEY ',' IDENTIFIER ')'
    ;

statement
    : assignmentStatement
    | methodCallOrFormat ';'
    | sendStatement
    | receiveStatement
    | returnStatement
    | ifStatement
    | whileStatement
    | print ';'
    ;

returnStatement
    : 'return' expression ';'
    ;

// EXAMPLE: t = encrypt(z,k); send(t);
sendStatement
    : SEND '(' IDENTIFIER ')' ';'
    ;

receiveStatement
    : TRY_RCV '(' expression ')' cmdBlock
    ;

expression
    : logicalOr
    ;

logicalOr
    : logicalOr OR logicalAnd
    | logicalAnd
    ;

logicalAnd
    : logicalAnd AND equality
    | equality
    ;

equality
    : equality ('==' | '!=') relational
    | relational
    ;

relational
    : relational ('>' | '<' | '>=' | '<=') additive
    | additive
    ;

additive
    : additive ('+' | '-') multiplicative
    | multiplicative
    ;

multiplicative
    : multiplicative ('*' | '/' | '%') unary
    | unary
    ;

unary
    : NOT unary
    | '-' unary
    | primary
    ;

primary
    : INT
    | BOOL
    | STR
    | IDENTIFIER
    | methodCallOrFormat
    | ENCRYPT '(' KEY ',' expression ')'
    | '(' expression ')'
    ;

methodCallOrFormat
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : expression (',' expression)*
    ;

assignmentStatement
    : IDENTIFIER '=' expression ';'
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
