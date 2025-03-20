parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program
    :   (class SEMICOLON)+ EOF
    ;

class
    : CLASS type=TYPE (INHERITS parent=TYPE)? LBRACE (features+=feature SEMICOLON )* RBRACE # class_def
    ;

feature
    :   name=ID LPAREN (formals+=formal (COMMA formals+=formal)*)? RPAREN COLON type=TYPE LBRACE body=expr RBRACE # method_def
    |   name=ID COLON type=TYPE (ASSIGN value=expr)? # attribute_def
    ;

formal
    :   name=ID COLON type=TYPE
    ;

let_vars
    :   name=ID COLON type=TYPE (ASSIGN value=expr)?
    ;
case_branch
    :   name=ID COLON type=TYPE CASEOF body=expr SEMICOLON
    ;
expr
    : class_name=expr (AT type=TYPE)? DOT func=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN # class_method_call
    | name=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN # method_call
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI # if
    | WHILE cond=expr LOOP body=expr POOL # while
    | block # blockExpr
    | LET vars+=let_vars (COMMA vars+=let_vars)* IN body=expr # let
    | CASE var=expr OF (branches+=case_branch)+ ESAC # case
    | NEW type=TYPE # new
    | ISVOID expr # isvoid
    | UNARYMINUS e=expr # unaryMinus
    | left=expr op=(MULT | DIV) right=expr # arithmetic
    | left=expr op=(PLUS | MINUS) right=expr # arithmetic
    | name=ID ASSIGN assign=expr             # assign
    | left=expr op=(LESS | LESS_EQ | EQUAL) right=expr # comparison
    | NOT expr # not
    | LPAREN expr RPAREN # paren_expr
    | ID # id
    | BOOL # bool
    | INT # int
    | STRING # string
    ;




block
    :   LBRACE (expr SEMICOLON)+ RBRACE
    ;
