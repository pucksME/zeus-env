grammar BootsSpecification ;

specification : class+ ;
class : KEYWORD_CLASS CLASS '{' GENERATOR '}' ;

KEYWORD_CLASS : 'class' ;
CLASS : [A-Za-z0-9]+ ;
GENERATOR : ';' .+? ';' ;

WHITESPACE : [ \r\t\n]+ -> skip ;