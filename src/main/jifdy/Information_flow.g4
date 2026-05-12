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
    : class
    ;

class
    : ( PPLABEL )? 'class' classBlock
    ;

classBlock
    : IDENTIFIER '{' declaration* functionDeclaration* '}'
    ;

declaration
    : type SECLABEL IDENTIFIER ('=' expression)? ';'
    | PPLABEL IDENTIFIER '(' decls* ')' '{' (assignmentStatement)* '}'
    | encryptionType SECLABEL IDENTIFIER '=' ENCRYPT '(' KEY ',' ( expression | format ) ')' ';'
    ;

functionDeclaration
    : PPLABEL ( type | 'void' ) SECLABEL IDENTIFIER '(' (decls)* ')' cmdBlock
    ;

cmdBlock
    : '{' (declaration | statement | cmdBlock)* '}'
    ;

decls
    : declItem (',' declItem)*
    ;

declItem
    : type SECLABEL IDENTIFIER
    ;

type
    : basicType
    | encryptionType
    ;

basicType
    : 'int'
    | 'bool'
    | 'String'
    ;

encryptionType
    : ENCRYPT '(' KEY ',' ( expression | format ) ')'
    ;

statement
    : assignmentStatement
    | functionCall ';'
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
    : TRY_RCV '(' format ')' ( IDENTIFIER '=')? cmdBlock
    ;

format
    : (type )? SECLABEL IDENTIFIER                      // Variable OR nested format reference
    | IDENTIFIER '(' formatList ')'
    | ENCRYPT '(' KEY ',' format ')'
    ;

formatList
    : format (',' format)*
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
    | ENCRYPT '(' KEY ',' ( expression | format ) ')'
    | functionCall
    | '(' expression ')'
    ;

functionCall
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
