package net.hackermdch.exparticle.util;

public enum EnumToken {
    EOF,
    SEMI(";"),
    COMMA(","),
    DCOMMA(",,"),
    DOT("."),
    LPAREN("("),
    RPAREN(")"),
    ASSIGN("="),
    MINUS("-"),
    NEG("-"),
    SUB("-"),
    ADD("+"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    POW("^"),
    NOT("!"),
    AND("&"),
    OR("|"),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    EQ("=="),
    NEQ("!="),
    QUESTION("?"),
    COLON(":"),
    NUMBER,
    IDENTIFIER;

    public final String token;

    EnumToken() {
        this(null);
    }

    EnumToken(String token) {
        this.token = token;
    }
}
