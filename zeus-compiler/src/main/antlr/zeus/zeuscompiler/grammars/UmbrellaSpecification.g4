grammar UmbrellaSpecification ;

specification : EOF ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT_SINGLE_LINE : '//' ~[\r\n]* NEW_LINE -> skip ;
NEW_LINE : '\r'? '\n' ;