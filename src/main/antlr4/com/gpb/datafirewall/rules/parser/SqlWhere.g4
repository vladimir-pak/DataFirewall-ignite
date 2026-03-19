grammar SqlWhere;

parse
    : expression EOF
    ;

// --------------------
// Boolean layer with precedence:
// OR < AND < NOT < predicate
// --------------------

expression
    : orExpression
    ;

orExpression
    : andExpression (OR andExpression)*          # OrChainExpr
    ;

andExpression
    : notExpression (AND notExpression)*         # AndChainExpr
    ;

notExpression
    : NOT notExpression                          # NotExpr
    | predicate                                  # PredicateOnly
    | LPAREN expression RPAREN                   # ParenExpr
    ;

predicate
    : value IS NULL_                                    # IsNullExpr
    | value IS NOT NULL_                                # IsNotNullExpr
    | value cmpOp value                                 # CompareExpr
    | value LIKE STRING                                 # LikeExpr
    | value IN LPAREN value (COMMA value)* RPAREN       # InExpr
    | value NOT LIKE STRING                             # NotLikeExpr
    | value NOT IN LPAREN value (COMMA value)* RPAREN   # NotInExpr
    | value NOT BETWEEN value AND value                 # NotBetweenExpr
    | value BETWEEN value AND value                     # BetweenExpr
    | value REGEXP (STRING | DQIDENT)                   # RegexpExpr
    | functionCall                                      # FuncPredicateExpr
    ;

// --------------------
// Value layer (supports arithmetic)
// --------------------

value
    : additiveExpr               # ValueExpr
    ;

additiveExpr
    : additiveExpr PLUS multiplicativeExpr   # AddExpr
    | additiveExpr MINUS multiplicativeExpr  # SubExpr
    | multiplicativeExpr                     # ToMul
    ;

multiplicativeExpr
    : multiplicativeExpr MUL unaryExpr   # MulExpr
    | multiplicativeExpr DIV unaryExpr   # DivExpr
    | multiplicativeExpr MOD unaryExpr   # ModExpr
    | unaryExpr                          # ToUnary
    ;

unaryExpr
    : MINUS unaryExpr                # UnaryMinusExpr
    | atom                           # ToAtom
    ;

atom
    : DQIDENT                                    # FieldValue
    | NUMBER                                     # NumberValue
    | STRING                                     # StringValue
    | functionCall                               # FuncValue
    | CAST LPAREN value AS IDENT RPAREN          # CastValue
    | EXTRACT LPAREN IDENT FROM value RPAREN     # ExtractValue
    | INTERVAL STRING                            # IntervalValue
    | IDENT                                      # IdentValue
    | LPAREN value RPAREN                        # ValueParen
    ;

// --------------------
// Functions
// --------------------

functionCall
    : IDENT ('.' IDENT)* LPAREN functionArgs RPAREN   # FuncCall
    ;

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
REGEXP: [Rr][Ee][Gg][Ee][Xx][Pp];
NULL_ : [Nn][Uu][Ll][Ll];

FROM     : [Ff][Rr][Oo][Mm];
EXTRACT  : [Ee][Xx][Tt][Rr][Aa][Cc][Tt];
INTERVAL : [Ii][Nn][Tt][Ee][Rr][Vv][Aa][Ll];

BETWEEN : [Bb][Ee][Tt][Ww][Ee][Ee][Nn];

AND : [Aa][Nn][Dd];
OR  : [Oo][Rr];

LPAREN: '(';
RPAREN: ')';
COMMA : ',';

PLUS  : '+';
MINUS : '-';
MUL   : '*';
DIV   : '/';
MOD   : '%';

DQIDENT : '"' (~["\\] | '\\' . )* '"' ;
STRING  : '\'' ( '\'\'' | '\\' . | ~['\\] )* '\'' ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;

IDENT : [A-Za-z_][A-Za-z0-9_]* ;

WS : [ \t\r\n]+ -> skip ;