grammar UmbrellaSpecification ;

specifications : (formulaAssignment | contextAssignment | actionsAssignment)+ EOF ;
formulaAssignment: ID OPERATOR_ACCESS FORMULA OPERATOR_ASSIGNMENT formula ;
contextAssignment : ID OPERATOR_ACCESS CONTEXT OPERATOR_ASSIGNMENT (CONTEXT_GLOBAL | CONTEXT_IP) ;
action : ACTION_BLOCK | ACTION_LOG ;
actions : action (',' action)* ;
actionsAssignment : ID OPERATOR_ACCESS ACTIONS OPERATOR_ASSIGNMENT actions ;

formula : ID                                                                            # IdentifierFormula
        | literal=(
            LITERAL_BOOLEAN
          | LITERAL_INT
          | LITERAL_FLOAT
          | LITERAL_STRING
        )                                                                               # LiteralFormula
        // unary formulas
        | '(' formula ')'                                                               # ParenthesisFormula
        | ID OPERATOR_ACCESS formula                                                    # AccessFormula
        | ID '[' formula ']' (OPERATOR_ACCESS formula)?                                 # AccessListFormula
        | OPERATOR_NOT formula                                                          # LogicalNotFormula
        | OPERATOR_SUBTRACT formula                                                     # ArithmeticNegativeFormula
        | operator=(
            OPERATOR_YEASTERDAY
          | OPERATOR_ONCE
          | OPERATOR_HISTORICALLY
          ) formula                                                                     # TemporalUnaryFormula
        // binary formulas
        | formula operator=(
            OPERATOR_EQUAL
          | OPERATOR_NOT_EQUAL
          | OPERATOR_GREATER_THAN
          | OPERATOR_LESS_THAN
          | OPERATOR_GREATER_EQUAL_THAN
          | OPERATOR_LESS_EQUAL_THAN
          ) formula                                                                     # CompareBinaryFormula
        | formula operator=(
            OPERATOR_ADD
          | OPERATOR_SUBTRACT
          | OPERATOR_MULTIPLY
          | OPERATOR_DIVIDE
          ) formula                                                                     # ArithmeticBinaryFormula
        | formula operator=(
            OPERATOR_AND
          | OPERATOR_OR
          | OPERATOR_IMPLICATION
          ) formula                                                                     # LogicalBinaryFormula
        | formula OPERATOR_SINCE formula                                                # TemporalBinaryFormula
        ;

FORMULA : 'formula' ;

CONTEXT : 'context' ;
CONTEXT_IP : 'IP' ;
CONTEXT_GLOBAL: 'global' ;

ACTIONS : 'actions' ;
ACTION_BLOCK : 'block' ;
ACTION_LOG : 'log' ;

OPERATOR_ASSIGNMENT : '=' ;
OPERATOR_ACCESS : '.' ;
OPERATOR_EQUAL : '==' ;
OPERATOR_NOT_EQUAL : '!=' ;
OPERATOR_GREATER_THAN : '>' ;
OPERATOR_LESS_THAN : '<' ;
OPERATOR_GREATER_EQUAL_THAN : '>=' ;
OPERATOR_LESS_EQUAL_THAN : '<=' ;
OPERATOR_ADD : '+' ;
OPERATOR_SUBTRACT : '-' ;
OPERATOR_MULTIPLY : '*' ;
OPERATOR_DIVIDE : '/' ;
OPERATOR_NOT : '!' ;
OPERATOR_AND : '&' ;
OPERATOR_OR : '|' ;
OPERATOR_IMPLICATION : '->' ;
OPERATOR_YEASTERDAY: 'Y' ;
OPERATOR_SINCE : 'S' ;
OPERATOR_ONCE : 'O' ;
OPERATOR_HISTORICALLY : 'H' ;

LITERAL_BOOLEAN : 'true' | 'false' ;
LITERAL_INT : [0-9]+ ;
LITERAL_FLOAT : LITERAL_INT '.' LITERAL_INT ;
LITERAL_STRING : '"' ~["]* '"' ;

ID : [0-9a-zA-Z]+ ;
WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT_SINGLE_LINE : '//' ~[\r\n]* NEW_LINE -> skip ;
NEW_LINE : '\r'? '\n' ;