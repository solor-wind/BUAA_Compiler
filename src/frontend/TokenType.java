package frontend;

public enum TokenType {
    IDENFR,//Ident
    INTCON,//IntConst
    STRCON,//StringConst
    CHRCON,//CharConst

    MAINTK,//main
    CONSTTK,//const
    INTTK,//int
    CHARTK,//char
    VOIDTK,//void
    FORTK,//for
    BREAKTK,//break
    CONTINUETK,//continue
    IFTK,//if
    ELSETK,//else

    GETINTTK,//getint
    GETCHARTK,//getchar
    PRINTFTK,//printf
    RETURNTK,//return

    NOT,//!
    AND,//&&
    OR,//||
    PLUS,//+
    MINU,//-
    MULT,//*
    DIV,// /
    MOD,//%
    LSS,//<
    LEQ,//<=
    GRE,//>
    GEQ,//>=
    EQL,//==
    NEQ,//!=
    ASSIGN,//=
    SEMICN,//;
    COMMA,//,
    LPARENT,//(
    RPARENT,//)
    LBRACK,//[
    RBRACK,//]
    LBRACE,//{
    RBRACE,//}

    ERROR,// |&
}
