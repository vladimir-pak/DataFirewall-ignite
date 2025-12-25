grammar SqlWhere;

parse
    : expression EOF
    ;

expression
    : expression AND expression    # AndExpr
    | expression OR expression     # OrExpr
    | NOT expression               # NotExpr
    | predicate                    # PredicateOnly
    | '(' expression ')'           # ParenExpr
    ;

predicate
    : value IS NULL_               # IsNullExpr
    | value IS NOT NULL_           # IsNotNullExpr
    | value cmpOp value            # CompareExpr
    | functionCall                 # FuncExpr
    | value LIKE STRING            # LikeExpr
    | value IN LPAREN value (COMMA value)* RPAREN # InExpr
    ;

value
    : DQIDENT                      # FieldValue
    | NUMBER                       # NumberValue
    | STRING                       # StringValue
    | functionCall                 # FuncValue
    | CAST LPAREN value AS IDENT RPAREN  # CastValue
    ;

functionCall
    : IDENT ('.' IDENT)* LPAREN functionArgs RPAREN   # FuncCall
    ;

functionArgs
    : (value (',' value)*)?
    ;

cmpOp
    : '=' | '!=' | '<>' | '<' | '>' | '<=' | '>='
    ;

IS    : [Ii][Ss];
NOT   : [Nn][Oo][Tt];
AS    : [Aa][Ss];
CAST  : [Cc][Aa][Ss][Tt];
LIKE  : [Ll][Ii][Kk][Ee];
IN    : [Ii][Nn];
NULL_ : [Nn][Uu][Ll][Ll];

AND : [Aa][Nn][Dd];
OR  : [Oo][Rr];

LPAREN: '(';
RPAREN: ')';
COMMA: ',';

DQIDENT : '"' (~["\\] | '\\' . )* '"' ; // double quoted identifier, allow escapes
STRING  : '\'' (~['\\] | '\\' . )* '\'' ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;

IDENT : [A-Za-z_][A-Za-z0-9_]* ;

WS : [ \t\r\n]+ -> skip ;