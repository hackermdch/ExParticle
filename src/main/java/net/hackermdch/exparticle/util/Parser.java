package net.hackermdch.exparticle.util;

import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    public static Expression[] parseBlock(Lexer lexer) {
        var exps = new ArrayList<Expression>();
        while (!isEnd(lexer.peekToken())) {
            Expression exp = parseExp(lexer);
            exps.add(exp);
            if (lexer.peekToken().enumToken == EnumToken.SEMI) {
                lexer.nextToken();
            }
        }
        return exps.toArray(new Expression[0]);
    }

    private static Expression parseAssignOrFunctionCallExp(Lexer lexer) {
        try {
            lexer.snapshot();
            Expression exp = parseAssignExp(lexer);
            lexer.popSnapshot();
            return exp;
        } catch (RuntimeException var3) {
            lexer.recovery();
            if (lexer.peekToken().enumToken == EnumToken.LPAREN) {
                return parseParenExp(lexer);
            } else {
                lexer.snapshot();
                lexer.nextIdentifier();
                Token token = lexer.peekToken();
                lexer.recovery();
                if (Objects.requireNonNull(token.enumToken) == EnumToken.LPAREN) return parseFunctionCallExp(lexer);
                return parseIdentifier(lexer);
            }
        }
    }

    private static Expression parseAssignExp(Lexer lexer) {
        Expression[] vars = parseVars(lexer);
        lexer.nextToken(EnumToken.ASSIGN);
        Expression[] exps = parseExps(lexer);
        if (vars.length != exps.length) {
            throw new RuntimeException("assign expression's vars and exps length not equals");
        } else {
            return new Expression.AssignExp(lexer.line(), vars, exps);
        }
    }

    private static Expression[] parseVars(Lexer lexer) {
        var vars = new ArrayList<Expression>();
        vars.add(parseVar(lexer));
        while (lexer.peekToken().enumToken == EnumToken.COMMA) {
            lexer.nextToken();
            vars.add(parseVar(lexer));
        }
        return vars.toArray(new Expression[0]);
    }

    private static Expression parseVar(Lexer lexer) {
        return switch (lexer.peekToken().enumToken) {
            case LPAREN -> parseVarMatrix(lexer);
            case IDENTIFIER -> parseIdentifier(lexer);
            default -> throw new RuntimeException("not a var");
        };
    }

    private static Expression parseVarMatrix(Lexer lexer) {
        Expression exp = parseParenExp(lexer);
        if (!(exp instanceof Expression.NameMatrixExp)) {
            throw new RuntimeException("not a var matrix");
        } else {
            return exp;
        }
    }

    private static Expression[] parseExps(Lexer lexer) {
        var exps = new ArrayList<Expression>();
        exps.add(parseExp(lexer));
        while (lexer.peekToken().enumToken == EnumToken.COMMA) {
            lexer.nextToken();
            exps.add(parseExp(lexer));
        }
        return exps.toArray(new Expression[0]);
    }

    private static Expression parseExp(Lexer lexer) {
        return parseExp7(lexer);
    }

    private static Expression parseIdentifier(Lexer lexer) {
        Token token = lexer.nextIdentifier();
        return new Expression.NameExp(token.line, token.token);
    }

    private static Expression parseFunctionCallExp(Lexer lexer) {
        var name = lexer.nextIdentifier();
        var line = lexer.nextToken(EnumToken.LPAREN).line;
        Expression[] exps;
        if (lexer.peekToken().enumToken != EnumToken.RPAREN) {
            exps = parseExps(lexer);
        } else {
            exps = new Expression[0];
        }
        var lastLine = lexer.nextToken(EnumToken.RPAREN).line;
        return new Expression.FunctionCallExp(line, lastLine, name.token, exps);
    }

    private static Expression parseParenExp(Lexer lexer) {
        int line = lexer.nextToken(EnumToken.LPAREN).line;
        var exps = new ArrayList<Expression[]>();
        Expression[] expRow = parseExps(lexer);
        exps.add(expRow);
        var row = 1;
        var col = expRow.length;
        var isInt = true;
        var isFloat = true;
        var isVar = true;
        if (checkInt(expRow)) isInt = false;
        if (checkFloat(expRow)) isFloat = false;
        if (checkVar(expRow)) isVar = false;
        while (lexer.peekToken().enumToken == EnumToken.DCOMMA) {
            lexer.nextToken();
            expRow = parseExps(lexer);
            if (expRow.length != col) throw new RuntimeException("bad matrix");
            exps.add(expRow);
            ++row;
            if (isInt && checkInt(expRow)) isInt = false;
            if (isFloat && checkFloat(expRow)) isFloat = false;
            if (isVar && checkVar(expRow)) isVar = false;
        }
        int lastLine = lexer.nextToken(EnumToken.RPAREN).line;
        if (row == 1 && col == 1) {
            return exps.getFirst()[0];
        } else if (isInt) {
            var val = new int[row][col];
            for (int i = 0; i < row; ++i) {
                expRow = exps.get(i);
                for (int j = 0; j < col; ++j) {
                    val[i][j] = ((Expression.IntegerExp) expRow[j]).val;
                }
            }
            return new Expression.IntegerMatrixExp(line, lastLine, val);
        } else if (isFloat) {
            var val = new double[row][col];
            for (int i = 0; i < row; ++i) {
                expRow = exps.get(i);
                for (int j = 0; j < col; ++j) {
                    if (expRow[j] instanceof Expression.FloatExp) {
                        val[i][j] = ((Expression.FloatExp) expRow[j]).val;
                    } else {
                        val[i][j] = ((Expression.IntegerExp) expRow[j]).val;
                    }
                }
            }
            return new Expression.FloatMatrixExp(line, lastLine, val);
        } else if (!isVar) {
            return new Expression.MatrixExp(line, lastLine, exps.toArray(new Expression[exps.size()][]));
        } else {
            var names = new Expression.NameExp[row][col];
            for (int i = 0; i < row; ++i) {
                expRow = exps.get(i);
                for (int j = 0; j < col; ++j) {
                    names[i][j] = (Expression.NameExp) expRow[j];
                }
            }
            return new Expression.NameMatrixExp(line, lastLine, names);
        }
    }

    private static boolean checkInt(Expression[] exps) {
        for (Expression exp : exps) {
            if (!(exp instanceof Expression.IntegerExp)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkFloat(Expression[] exps) {
        for (Expression exp : exps) {
            if (!(exp instanceof Expression.FloatExp) && !(exp instanceof Expression.IntegerExp)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkVar(Expression[] exps) {
        for (Expression exp : exps) {
            if (!(exp instanceof Expression.NameExp)) {
                return true;
            }
        }
        return false;
    }

    private static Expression parseExp7(Lexer lexer) {
        Expression exp;
        Token token;
        for (exp = parseExp6(lexer); lexer.peekToken().enumToken == EnumToken.OR; exp = Optimize.optimizeLogicalOr(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp6(lexer)))) {
            token = lexer.nextToken();
        }
        return exp;
    }

    private static Expression parseExp6(Lexer lexer) {
        Expression exp;
        Token token;
        for (exp = parseExp5(lexer); lexer.peekToken().enumToken == EnumToken.AND; exp = Optimize.optimizeLogicalAnd(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp5(lexer)))) {
            token = lexer.nextToken();
        }
        return exp;
    }

    private static Expression parseExp5(Lexer lexer) {
        Expression exp;
        Token token;
        for (exp = parseExp4(lexer); isExp5(lexer.peekToken()); exp = Optimize.optimizeArithBinaryOp(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp4(lexer)))) {
            token = lexer.nextToken();
        }
        return exp;
    }

    private static Expression parseExp4(Lexer lexer) {
        Expression exp;
        Token token;
        for (exp = parseExp3(lexer); isExp4(lexer.peekToken()); exp = Optimize.optimizeArithBinaryOp(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp3(lexer)))) {
            token = lexer.nextToken();
        }
        return exp;
    }

    private static Expression parseExp3(Lexer lexer) {
        Expression exp;
        Token token;
        for (exp = parseExp2(lexer); isExp3(lexer.peekToken()); exp = Optimize.optimizeArithBinaryOp(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp2(lexer)))) {
            token = lexer.nextToken();
        }
        return exp;
    }

    private static Expression parseExp2(Lexer lexer) {
        if (isExp2(lexer.peekToken())) {
            Token token = lexer.nextToken();
            return Optimize.optimizeUnaryOp(new Expression.UnopExp(token.line, token.enumToken, parseExp2(lexer)));
        } else {
            return parseExp1(lexer);
        }
    }

    private static Expression parseExp1(Lexer lexer) {
        Expression exp = parseExp0(lexer);
        if (lexer.peekToken().enumToken == EnumToken.POW) {
            Token token = lexer.nextToken();
            exp = Optimize.optimizeArithBinaryOp(new Expression.BinopExp(token.line, token.enumToken, exp, parseExp2(lexer)));
        }
        return exp;
    }

    private static Expression parseExp0(Lexer lexer) {
        return switch (lexer.peekToken().enumToken) {
            case LPAREN, IDENTIFIER -> parseAssignOrFunctionCallExp(lexer);
            case NUMBER -> parseNumberExp(lexer);
            default -> throw new RuntimeException("need matrix, function call, assign expression, var, number");
        };
    }

    private static Expression parseNumberExp(Lexer lexer) {
        var token = lexer.nextToken(EnumToken.NUMBER);
        return token.token.contains(".") ? new Expression.FloatExp(token.line, Float.parseFloat(token.token)) : new Expression.IntegerExp(token.line, Integer.parseInt(token.token));
    }

    private static boolean isEnd(Token token) {
        return token.enumToken == EnumToken.EOF || token.enumToken == EnumToken.RPAREN;
    }

    private static boolean isExp5(Token token) {
        return switch (token.enumToken) {
            case LT, LE, GT, GE, EQ, NEQ -> true;
            default -> false;
        };
    }

    private static boolean isExp4(Token token) {
        return switch (token.enumToken) {
            case ADD, MINUS, SUB -> true;
            default -> false;
        };
    }

    private static boolean isExp3(Token token) {
        return switch (token.enumToken) {
            case MUL, DIV, MOD -> true;
            default -> false;
        };
    }

    private static boolean isExp2(Token token) {
        return switch (token.enumToken) {
            case MINUS, NEG, NOT -> true;
            default -> false;
        };
    }
}
