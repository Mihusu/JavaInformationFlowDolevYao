grammar Information_flow;

// Labels
PPLABEL     : 'public' | 'private';
SEND        : 'send';
TRY_RCV     : 'try_rcv';
ENCRYPT     : 'encrypt';
PUBLICKEY   : 'publicKey';
PRIVATEKEY  : 'privateKey';

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
    : typeExpr
    ;

typeExpr
    : 'int'
    | 'bool'
    | 'String'
    | 'ciphertext'
    ;

statement
    : assignmentStatement
    | sendStatement
    | receiveStatement
    | encryptStatement
    | expression ';'
    | returnStatement
    | ifStatement
    | whileStatement
    | print ';'
    ;

returnStatement
    : 'return' expression ';'
    ;

encryptStatement
    : IDENTIFIER '=' ENCRYPT '(' expression ',' expression ')' ';'
    ;

// EXAMPLE: t = encrypt(z,k); send(t);
sendStatement
    : SEND '(' IDENTIFIER ')' ';'
    ;

receiveStatement
    : TRY_RCV '(' receivePattern ')' cmdBlock
    ;

receivePattern
    : IDENTIFIER
    | IDENTIFIER '(' receivePatternList ')'
    | ENCRYPT '(' expression ',' receivePattern ')'
    ;

// Formats
receivePatternList
    : receivePattern (',' receivePattern)*
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
