grammar UmbrellaSpecification ;

specification : (formulaAssignment | contextAssignment | actionAssignment)+ EOF ;
formulaAssignment: ID OPERATOR_ACCESS FORMULA OPERATOR_ASSIGNMENT formula ;
contextAssignment : ID OPERATOR_ACCESS CONTEXT OPERATOR_ASSIGNMENT (CONTEXT_GLOBAL | CONTEXT_IP) ;
actionAssignment : ID OPERATOR_ACCESS ACTION OPERATOR_ASSIGNMENT (ACTION_ALLOW | ACTION_BLOCK | ACTION_LOG) ;

literal : LITERAL_BOOLEAN
        | LITERAL_INT
        | LITERAL_FLOAT
        | LITERAL_STRING
        ;

formula : ID                                                                            # IdentifierFormula
        | literal                                                                       # LiteralFormula
        | '(' formula ')'                                                               # ParenthesisFormula
        | formula OPERATOR_ACCESS ID                                                    # AccessFormula
        // unary formulas
        | OPERATOR_NOT formula                                                          # LogicalNotFormula
        | (OPERATOR_YEASTERDAY | OPERATOR_ONCE | OPERATOR_HISTORICALLY) formula         # TemporalUnaryFormula
        // binary formulas
        | formula (
            OPERATOR_EQUAL
          | OPERATOR_NOT_EQUAL
          | OPERATOR_GREATER_THAN
          | OPERATOR_LESS_THAN
          | OPERATOR_GREATER_EQUAL_THAN
          | OPERATOR_LESS_EQUAL_THAN
          ) formula                                                                     # CompareFormula
        | formula (
            OPERATOR_ADD
          | OPERATOR_SUBTRACT
          | OPERATOR_MULTIPLY
          | OPERATOR_DIVIDE
          ) formula                                                                     # ArithmeticFormula
        | formula (OPERATOR_AND | OPERATOR_OR | OPERATOR_IMPLICATION) formula           # LogicalBinaryFormula
        | formula OPERATOR_SINCE formula                                                # TemporalSinceFormula
        ;

FORMULA : 'formula' ;

CONTEXT : 'context' ;
CONTEXT_IP : 'IP' ;
CONTEXT_GLOBAL: 'global' ;

ACTION : 'action' ;
ACTION_BLOCK : 'block' ;
ACTION_ALLOW : 'allow' ;
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