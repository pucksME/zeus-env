grammar Thunder;

codeModules : (clientCodeModule | instanceCodeModule)+ EOF ;

// client code module
clientCodeModule : KEYWORD_CODE_MODULE ID DESCRIPTION BLOCK_START head body BLOCK_END ;

head : input* output* config* ;

type : (PRIMITIVE_TYPE | typeList | typeMap | typeObject | ID) ;
typeList : LIST_START type LIST_END (OPERATOR_SIZE LITERAL_INT)? ;
typeMap : LIST_START type ',' type LIST_END ;
typeObjectItem : (ID ':' type) ;
typeObject : BLOCK_START typeObjectItem (',' typeObjectItem)* BLOCK_END ;

declaration: ID ':' type (OPERATOR_ASSIGNMENT expression)? ';' ;
input : KEYWORD_INPUT ID ':' type ';' ;
output : KEYWORD_OUTPUT declaration ;
config: KEYWORD_CONFIG declaration ;

body : (statement | expression)* ;
controlStatementBody : (statement | expression)* ;

statement : declarationTypeStatement
          | declarationVariableStatement
          | assignmentStatement
          | accessWriteStatement
          | accessWriteObjectStatement
          | ifStatement
          | whileStatement
          | assertStatement
          ;

declarationTypeStatement : KEYWORD_PUBLIC? KEYWORD_TYPE ID OPERATOR_ASSIGNMENT type ';' ;
declarationVariableStatement : KEYWORD_DECLARATION declaration ;
assignmentStatement: ID OPERATOR_ASSIGNMENT expression ';' ;
accessWriteStatement: expression LIST_START expression LIST_END OPERATOR_ASSIGNMENT expression ';' ;
accessWriteObjectStatement : expression OPERATOR_ACCESS ID OPERATOR_ASSIGNMENT expression ';' ;
ifStatement : KEYWORD_IF expression BLOCK_START controlStatementBody BLOCK_END (KEYWORD_ELSE BLOCK_START controlStatementBody BLOCK_END)?;
whileStatement : KEYWORD_WHILE expression BLOCK_START controlStatementBody BLOCK_END ;
assertStatement : KEYWORD_ASSERT '(' expression ')' ';' ;

expression : ID                                                        # IdentifierExpression
           | literal                                                   # LiteralExpression
           | expressionList                                            # ListExpression
           | expressionMap                                             # MapExpression
           | expressionObject                                          # ObjectExpression
           // unary expressions
           | expression OPERATOR_ACCESS ID                             # ObjectReadAccessExpression
           | '(' expression ')'                                        # GroupExpression
           | '(' type ')' expression                                   # CastExpression
           | OPERATOR_NEGATE expression                                # NegateExpression
           | OPERATOR_SUBTRACT expression                              # NegativeExpression
           | OPERATOR_SIZE expression                                  # SizeExpression
           // binary expressions
           | expression LIST_START expression LIST_END                 # ReadAccessExpression
           | expression OPERATOR_GREATER_THAN expression               # GreaterThanExpression
           | expression OPERATOR_GREATER_EQUAL_THAN expression         # GreaterEqualThanExpression
           | expression OPERATOR_LESS_THAN expression                  # LessThanExpression
           | expression OPERATOR_LESS_EQUAL_THAN expression            # LessEqualThanExpression
           | <assoc=right> expression OPERATOR_POWER expression        # PowerExpression
           | expression OPERATOR_MULTIPLY expression                   # MultiplyExpression
           | expression OPERATOR_DIVIDE expression                     # DivideExpression
           | expression OPERATOR_MODULO expression                     # ModuloExpression
           | expression OPERATOR_ADD expression                        # AddExpression
           | expression OPERATOR_SUBTRACT expression                   # SubtractExpression
           | expression OPERATOR_AND expression                        # AndExpression
           | expression OPERATOR_OR expression                         # OrExpression
           | expression OPERATOR_COMPARE expression                    # CompareExpression
           | expression OPERATOR_COMPARE_NEGATED expression            # CompareNegatedExpression
           // ternary expressions
           | <assoc=right> expression '?' expression ':' expression    # IfElseExpression
           ;

literal: LITERAL_INT
       | LITERAL_FLOAT
       | LITERAL_STRING
       | LITERAL_BOOLEAN
       | LITERAL_NULL
       ;

expressionList : LIST_START (expression (',' expression)*)? LIST_END ;
expressionMapItem : '(' expression ',' expression ')' ;
expressionMap : LIST_START (expressionMapItem (',' expressionMapItem)*)? LIST_END ;
expressionObjectItem : ID ':' expression ;
expressionObject : BLOCK_START expressionObjectItem (',' expressionObjectItem)* BLOCK_END ;

// instance code module
instanceCodeModule : KEYWORD_CODE_MODULE KEYWORD_INSTANCE ID DESCRIPTION BLOCK_START instanceBody BLOCK_END;

instanceBody : instanceStatement*;
instanceStatement : connectionStatement ;

connectionStatement : expressionCodeModulePort OPERATOR_CONNECTION expressionCodeModulePort ';' ;
expressionCodeModulePort : ID OPERATOR_ACCESS ID ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT_SINGLE_LINE : '//' ~[\r\n]* NEW_LINE -> skip ;

KEYWORD_CODE_MODULE : 'module' ;
KEYWORD_INSTANCE : 'instance' ;


KEYWORD_INPUT : 'input' ;
KEYWORD_OUTPUT : 'output' ;
KEYWORD_CONFIG : 'config' ;
KEYWORD_DECLARATION : 'var' ;
KEYWORD_IF : 'if' ;
KEYWORD_ELSE : 'else' ;
KEYWORD_WHILE : 'while' ;
KEYWORD_ASSERT : 'assert' ;
KEYWORD_TYPE : 'type' ;
KEYWORD_PUBLIC : 'public' ;

PRIMITIVE_TYPE : 'int'
               | 'float'
               | 'string'
               | 'boolean'
               // | 'any'
               ;

LITERAL_FLOAT : LITERAL_INT '.' LITERAL_INT ;
LITERAL_INT : [0-9]+ ;
LITERAL_STRING : '"' ~["]* '"' ;
LITERAL_BOOLEAN : 'true' | 'false' ;
LITERAL_NULL : 'null' ;

LIST_START : '[' ;
LIST_END : ']' ;

ID : [A-Za-z0-9]+ ;
DESCRIPTION : '::' ~[{]+ ;

// unary operators
OPERATOR_NEGATE : '!' ;
OPERATOR_SIZE : '#' ;
OPERATOR_ACCESS : '.' ;

// binary operators
OPERATOR_ASSIGNMENT : '=' ;
OPERATOR_ADD : '+' ;
OPERATOR_SUBTRACT : '-' ;
OPERATOR_MULTIPLY : '*' ;
OPERATOR_DIVIDE : '/' ;
OPERATOR_MODULO : '%' ;
OPERATOR_POWER : '^' ;
OPERATOR_GREATER_THAN : '>' ;
OPERATOR_GREATER_EQUAL_THAN : '>=' ;
OPERATOR_LESS_THAN : '<' ;
OPERATOR_LESS_EQUAL_THAN : '<=' ;
OPERATOR_AND : '&&' ;
OPERATOR_OR : '||' ;
OPERATOR_COMPARE : '==' ;
OPERATOR_COMPARE_NEGATED : '!=' ;
OPERATOR_CONNECTION : '->' ;

BLOCK_START : '{' ;
BLOCK_END : '}' ;
NEW_LINE : '\r'? '\n' ;
