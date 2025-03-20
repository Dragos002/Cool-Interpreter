lexer grammar CoolLexer;

@header{
    package cool.lexer;
}

tokens { ERROR }

@members{
    int total_nr_of_chars = 0;
    int char_size_limit = 1024;
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}



//Cuvinte cheie
IF : 'if';
THEN : 'then';
ELSE : 'else';
FI: 'fi';
WHILE : 'while';
LOOP : 'loop';
POOL : 'pool';
LET : 'let';
IN : 'in';
CASE : 'case';
OF : 'of';
ESAC : 'esac';
NOT : 'not';
CLASS : 'class';
INHERITS : 'inherits';
NEW : 'new';
ISVOID : 'isvoid';

//Operatori
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
LESS : '<';
LESS_EQ : '<=';
EQUAL : '=';
UNARYMINUS : '~';
ASSIGN : '<-';
AT : '@';
DOT : '.';
COLON : ':';
SEMICOLON : ';';
COMMA : ',';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACKET : '[';
RBRACKET : ']';
CASEOF : '=>';

// Identificatori
fragment LETTER : [a-zA-Z];
fragment CAPITAL : [A-Z];
fragment DIGIT : [0-9];
fragment NEW_LINE : '\r'? '\n';
TYPE : CAPITAL (LETTER | DIGIT | '_')*;
ID : LETTER (LETTER | DIGIT | '_')*;
INT : DIGIT+;
BOOL : 'true' | 'false';

fragment NULLCHAR: '\u0000';
fragment UNTERMINATEDSTRING: '\r\n';
fragment SPECIALCHARS: '\\"' | '\\b' | '\\t' | '\\n' | '\\f' | '\\\\';
STRING
    : '"' (
        (SPECIALCHARS { total_nr_of_chars++; })
        | '\\\r\n'
        | '\\'
        | (NULLCHAR { raiseError("String contains null character"); })
        | (. { total_nr_of_chars++; })
    )*?
    (
        '"' {
            if (total_nr_of_chars > char_size_limit) {
                raiseError("String constant too long");
            }
            total_nr_of_chars = 0;
        }
        | UNTERMINATEDSTRING {
            raiseError("Unterminated string constant");
        }
        | EOF {
            raiseError("EOF in string constant");
            total_nr_of_chars = 0;
        }
    );



// Comentarii
LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip
    ;

BLOCK_COMMENT
        : '(*'
          (BLOCK_COMMENT | .)*?
          '*)' -> skip;

BLOCK_COMMENT_ERROR
    : '*)' { raiseError("Unmatched *)"); }
    ;

BLOCK_COMMENT_END_OF_LINE
    : '(*'
       (BLOCK_COMMENT | ~([*)]). | NEW_LINE)*?
       (EOF {
            raiseError("EOF in comment");
       });







WS
    :   [ \n\f\r\t]+ -> skip
    ;

INVALID_CHARACTER: . { raiseError("Invalid character: " + getText()); };


