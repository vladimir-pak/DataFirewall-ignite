grammar SqlWhere;

parse
    : expression EOF
    ;

// --------------------
// Boolean layer
// --------------------

expression
    : expression AND expression    # AndExpr
    | expression OR expression     # OrExpr
    | NOT expression               # NotExpr
    | predicate                    # PredicateOnly
    | '(' expression ')'           # ParenExpr
    ;

predicate
    : value IS NULL_                              # IsNullExpr
    | value IS NOT NULL_                          # IsNotNullExpr
    | value cmpOp value                           # CompareExpr
    | value LIKE STRING                           # LikeExpr
    | value IN LPAREN value (COMMA value)* RPAREN # InExpr
    ;

// --------------------
// Value layer (now supports arithmetic)
// --------------------

// value = arithmetic expression (so it can appear inside function args too)
value
    : additiveExpr               # ValueExpr
    ;

// precedence: + -
additiveExpr
    : additiveExpr PLUS multiplicativeExpr   # AddExpr
    | additiveExpr MINUS multiplicativeExpr  # SubExpr
    | multiplicativeExpr                     # ToMul
    ;

// precedence: * / %
multiplicativeExpr
    : multiplicativeExpr MUL unaryExpr   # MulExpr
    | multiplicativeExpr DIV unaryExpr   # DivExpr
    | multiplicativeExpr MOD unaryExpr   # ModExpr
    | unaryExpr                          # ToUnary
    ;

// precedence: unary -
unaryExpr
    : MINUS unaryExpr                # UnaryMinusExpr
    | atom                           # ToAtom
    ;

// atoms = what you previously had in `value`
atom
    : DQIDENT                                    # FieldValue
    | NUMBER                                     # NumberValue
    | STRING                                     # StringValue
    | functionCall                               # FuncValue
    | CAST LPAREN value AS IDENT RPAREN          # CastValue
    | LPAREN value RPAREN                        # ValueParen
    ;

// --------------------
// Functions
// --------------------

functionCall
    : IDENT ('.' IDENT)* LPAREN functionArgs RPAREN   # FuncCall
    ;

// args are now `value` (arith expr), not just primitive tokens
functionArgs
    : (value (COMMA value)*)?
    ;

// --------------------
// Operators & tokens
// --------------------

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
COMMA : ',';

// arithmetic tokens
PLUS  : '+';
MINUS : '-';
MUL   : '*';
DIV   : '/';
MOD   : '%';

// literals
DQIDENT : '"' (~["\\] | '\\' . )* '"' ;
STRING  : '\'' (~['\\] | '\\' . )* '\'' ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;

IDENT : [A-Za-z_][A-Za-z0-9_]* ;

WS : [ \t\r\n]+ -> skip ;
