package net.hackermdch.exparticle.util;

import com.google.common.collect.Lists;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private final char[] source;
    private int pointer = 0;
    private int line = 1;
    private Token nextToken = null;
    private final List<Snapshot> snapshots = Lists.newArrayList();

    public Lexer(String source) {
        this.source = source.toCharArray();
    }

    public Lexer(char[] source) {
        this.source = Arrays.copyOf(source, source.length);
    }

    public Lexer(InputStreamReader isr) throws IOException {
        this.source = readall(isr);
    }

    public Lexer(File file) throws IOException {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));

        try {
            this.source = readall(isr);
        } catch (Throwable var6) {
            try {
                isr.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        isr.close();
    }

    public Lexer(URL url) throws IOException {
        InputStreamReader isr = new InputStreamReader(url.openStream());

        try {
            this.source = readall(isr);
            this.pointer = 0;
            this.line = 1;
        } catch (Throwable var6) {
            try {
                isr.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        isr.close();
    }

    public Token nextToken() {
        if (this.nextToken != null) {
            Token result = this.nextToken;
            this.line = this.nextToken.line;
            this.nextToken = null;
            return result;
        } else {
            this.skipWhiteSpaces();
            if (this.pointer == this.source.length) {
                return new Token(this.line, EnumToken.EOF);
            } else {
                char ch = this.peek();
                switch (ch) {
                    case '!':
                        if (this.test("!=")) {
                            this.skip(2);
                            return new Token(this.line, EnumToken.NEQ);
                        }

                        this.skip(1);
                        return new Token(this.line, EnumToken.NOT);
                    case '%':
                        this.skip(1);
                        return new Token(this.line, EnumToken.MOD);
                    case '&':
                        this.skip(1);
                        return new Token(this.line, EnumToken.AND);
                    case '(':
                        this.skip(1);
                        return new Token(this.line, EnumToken.LPAREN);
                    case ')':
                        this.skip(1);
                        return new Token(this.line, EnumToken.RPAREN);
                    case '*':
                        this.skip(1);
                        return new Token(this.line, EnumToken.MUL);
                    case '+':
                        this.skip(1);
                        return new Token(this.line, EnumToken.ADD);
                    case ',':
                        if (this.test(",,")) {
                            this.skip(2);
                            return new Token(this.line, EnumToken.DCOMMA);
                        }

                        this.skip(1);
                        return new Token(this.line, EnumToken.COMMA);
                    case '-':
                        this.skip(1);
                        return new Token(this.line, EnumToken.MINUS);
                    case '/':
                        this.skip(1);
                        return new Token(this.line, EnumToken.DIV);
                    case ';':
                        this.skip(1);
                        return new Token(this.line, EnumToken.SEMI);
                    case '<':
                        if (this.test("<=")) {
                            this.skip(2);
                            return new Token(this.line, EnumToken.LE);
                        }

                        this.skip(1);
                        return new Token(this.line, EnumToken.LT);
                    case '=':
                        if (this.test("==")) {
                            this.skip(2);
                            return new Token(this.line, EnumToken.EQ);
                        }

                        this.skip(1);
                        return new Token(this.line, EnumToken.ASSIGN);
                    case '>':
                        if (this.test(">=")) {
                            this.skip(2);
                            return new Token(this.line, EnumToken.GE);
                        }

                        this.skip(1);
                        return new Token(this.line, EnumToken.GT);
                    case '^':
                        this.skip(1);
                        return new Token(this.line, EnumToken.POW);
                    case '|':
                        this.skip(1);
                        return new Token(this.line, EnumToken.OR);
                    default:
                        if (ch != '.' && !this.isDigit(ch)) {
                            if (ch != '_' && !this.isLatter(ch)) {
                                throw new RuntimeException("unknow char: " + ch);
                            } else {
                                return new Token(this.line, EnumToken.IDENTIFIER, this.scanIdentifier());
                            }
                        } else {
                            return new Token(this.line, EnumToken.NUMBER, this.scanNumber());
                        }
                }
            }
        }
    }

    public Token nextToken(EnumToken enumToken) {
        Token token = this.nextToken();
        if (token.enumToken != enumToken) {
            throw new RuntimeException("unmatch token: " + token.enumToken.token);
        } else {
            return token;
        }
    }

    public Token nextIdentifier() {
        return this.nextToken(EnumToken.IDENTIFIER);
    }

    public Token peekToken() {
        if (this.nextToken == null) {
            int currentLine = this.line;
            this.nextToken = this.nextToken();
            this.line = currentLine;
        }

        return this.nextToken;
    }

    public int line() {
        return this.line;
    }

    public void snapshot() {
        this.snapshots.add(new Snapshot(this.pointer, this.line, this.nextToken));
    }

    public void recovery() {
        var snapshot = snapshots.removeLast();
        pointer = snapshot.pointer;
        line = snapshot.line;
        nextToken = snapshot.nextToken;
    }

    public void popSnapshot() {
        this.snapshots.removeLast();
    }

    private String scanNumber() {
        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        for (int i = this.pointer; i < this.source.length; ++i) {
            if (this.source[i] == '.') {
                if (hasDot) {
                    break;
                }

                hasDot = true;
                sb.append(this.source[i]);
            } else {
                if (!this.isDigit(this.source[i])) {
                    break;
                }

                sb.append(this.source[i]);
            }
        }
        if (sb.isEmpty()) {
            throw new RuntimeException("not a number");
        } else {
            this.skip(sb.length());
            return sb.toString();
        }
    }

    private String scanIdentifier() {
        StringBuilder sb = new StringBuilder();

        for (int i = this.pointer; i < this.source.length && (this.source[i] == '_' || this.isLatter(this.source[i]) || this.isDigit(this.source[i])); ++i) {
            sb.append(this.source[i]);
        }

        if (sb.isEmpty()) {
            throw new RuntimeException("not a identifier");
        } else {
            this.skip(sb.length());
            return sb.toString();
        }
    }

    private char peek() {
        return this.source[this.pointer];
    }

    private void skipWhiteSpaces() {
        while (true) {
            if (this.pointer < this.source.length) {
                if (this.test("\r\n") || this.test("\n\r")) {
                    this.skip(2);
                    this.nextLine();
                    continue;
                }

                if (this.isNewLine(this.peek())) {
                    this.skip(1);
                    this.nextLine();
                    continue;
                }

                if (this.isWhiteSpace(this.peek())) {
                    this.skip(1);
                    continue;
                }
            }

            return;
        }
    }

    private boolean test(String str) {
        if (this.pointer + str.length() > this.source.length) {
            return false;
        } else {
            for (int i = 0; i < str.length(); ++i) {
                if (this.source[this.pointer + i] != str.charAt(i)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean isNewLine(char ch) {
        return switch (ch) {
            case '\n', '\r' -> true;
            default -> false;
        };
    }

    private boolean isWhiteSpace(char ch) {
        return switch (ch) {
            case '\t', '\n', '\f', '\r', ' ' -> true;
            default -> false;
        };
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isLatter(char ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    private void skip(int n) {
        this.pointer += n;
    }

    private void nextLine() {
        ++this.line;
    }

    private static char[] readall(InputStreamReader isr) throws IOException {
        CharArrayWriter caw = new CharArrayWriter();
        char[] buf = new char[1024];
        int len = 0;
        while ((len = isr.read(buf)) != -1) {
            caw.write(buf, 0, len);
        }
        return caw.toCharArray();
    }

    private record Snapshot(int pointer, int line, Token nextToken) {
    }
}
