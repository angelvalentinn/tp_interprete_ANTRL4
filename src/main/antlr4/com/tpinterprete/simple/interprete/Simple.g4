grammar Simple;

//------------- Gramatica libre de contexto (VT, VN, S, P) -------------

program: PROGRAM ID BRACKET_OPEN sentence* BRACKET_CLOSE; //Nuestro simbolo inicial
sentence: var_decl | var_assign | print;
var_decl: VAR ID SEMICOLON;
var_assign: ID ASSIGN expression SEMICOLON;
print: PRINT expression SEMICOLON;
number: INTEGER | FLOATING;

expression : expression op=(MULT | DIV) expression   # MulDiv //Esta manera respeta la presedencia matematica, dejando que la mult/div
           | expression op=(PLUS | MINUS) expression # AddSub //quede en los niveles mas bajos del arbol asegurando que se operen primero
           | expression op=(GT | LT | GEQ | LEQ | EQ | NEQ) expression # Comp
           | expression op=AND expression            # And
           | expression op=OR expression             # Or
           | number                                  # numb
           | ID                                      # id
           | PAR_OPEN expression PAR_CLOSE           # parens
           | STRING                                  # str
           | BOOLEAN                                 # bool
           ;

//------------- Gramatica regular (Tokens) -------------

PROGRAM: 'programa';
VAR: 'variable';
PRINT: 'mostrar';
IF: 'si';
ELSE: 'sino';

PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';

AND: '&&';
OR: '||';
NOT: '!';

GT: '>';
LT: '<';
GEQ: '>=';
LEQ: '<=';
EQ: '==';
NEQ: '!=';

ASSIGN: '=';

BRACKET_OPEN: '{';
BRACKET_CLOSE: '}';

PAR_CLOSE: ')';
PAR_OPEN: '(';

SEMICOLON: ';';

COMMENT : '//' ~[\r\n]* -> skip; // "~" quiere decir NOT, entonces en esa cadena se acepta cualquier cosa mientras no sea saltos de linea

FLOATING: [0-9]+ '.' [0-9]+;
INTEGER: [0-9]+; // El signo positivo indica que puede estar 1 o N veces
BOOLEAN: 'verdadero' | 'falso';

WS: [ \t\n\r]+ -> skip; //La instruccion skip funciona para que el Analizador lexico ignore estos tokens

ID: [a-zA-Z][a-zA-Z0-9_]*; //Lo puse debajo de tod0 por si se detectan palabras clave como verdadero o falso y caiga en el token que corresponde

STRING : '"' (~["\r\n])* '"' ;



