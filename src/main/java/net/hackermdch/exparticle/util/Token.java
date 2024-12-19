package net.hackermdch.exparticle.util;

public class Token {
    public int line;
    public EnumToken enumToken;
    public String token;

    public Token(int line, EnumToken enumToken) {
        this(line, enumToken, enumToken.token);
    }

    public Token(int line, EnumToken enumToken, String token) {
        this.line = line;
        this.enumToken = enumToken;
        this.token = token;
    }

    public String toString() {
        return String.format("line: %d, enumToken: %s, token: %s", this.line, this.enumToken.name(), this.token);
    }
}
