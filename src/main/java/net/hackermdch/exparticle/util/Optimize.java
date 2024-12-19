package net.hackermdch.exparticle.util;

public class Optimize {
    public static Expression optimizeLogicalOr(Expression.BinopExp exp) {
        if (trueOnly(exp.lexp)) {
            return exp.lexp;
        } else {
            return falseOnly(exp.lexp) ? exp.rexp : exp;
        }
    }

    public static Expression optimizeLogicalAnd(Expression.BinopExp exp) {
        if (falseOnly(exp.lexp)) {
            return exp.lexp;
        } else {
            return trueOnly(exp.lexp) ? exp.rexp : exp;
        }
    }

    public static Expression optimizeArithBinaryOp(Expression.BinopExp exp) {
        if (exp.lexp instanceof Expression.IntegerExp && exp.rexp instanceof Expression.IntegerExp) {
            int l = ((Expression.IntegerExp) exp.lexp).val;
            int r = ((Expression.IntegerExp) exp.rexp).val;
            return switch (exp.op) {
                case ADD -> new Expression.IntegerExp(exp.line, l + r);
                case SUB -> new Expression.IntegerExp(exp.line, l - r);
                case MUL -> new Expression.IntegerExp(exp.line, l * r);
                case DIV -> new Expression.IntegerExp(exp.line, l / r);
                case MOD -> new Expression.IntegerExp(exp.line, l % r);
                case POW -> {
                    if (r >= 0) yield new Expression.IntegerExp(exp.line, pow(l, r));
                    yield new Expression.FloatExp(exp.line, Math.pow(l, r));
                }
                default -> exp;
            };
        } else if ((!(exp.lexp instanceof Expression.FloatExp) || !(exp.rexp instanceof Expression.FloatExp)) && (!(exp.lexp instanceof Expression.IntegerExp) || !(exp.rexp instanceof Expression.FloatExp)) && (!(exp.lexp instanceof Expression.FloatExp) || !(exp.rexp instanceof Expression.IntegerExp))) {
            if (exp.lexp instanceof Expression.IntegerMatrixExp) {
                int[][] lmat = ((Expression.IntegerMatrixExp) exp.lexp).val;
                if (exp.rexp instanceof Expression.IntegerExp) {
                    int r = ((Expression.IntegerExp) exp.rexp).val;
                    switch (exp.op) {
                        case ADD:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matAdd(lmat, r));
                        case SUB:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matSub(lmat, r));
                        case MUL:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMul(lmat, r));
                        case DIV:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matDiv(lmat, r));
                        case MOD:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMod(lmat, r));
                        case POW:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matPow(lmat, r));
                    }
                } else if (exp.rexp instanceof Expression.FloatExp) {
                    double r = ((Expression.FloatExp) exp.rexp).val;
                    switch (exp.op) {
                        case ADD:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matAdd(lmat, r));
                        case SUB:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matSub(lmat, r));
                        case MUL:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMul(lmat, r));
                        case DIV:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matDiv(lmat, r));
                        case MOD:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMod(lmat, r));
                    }
                } else if (exp.rexp instanceof Expression.IntegerMatrixExp) {
                    int[][] rmat = ((Expression.IntegerMatrixExp) exp.rexp).val;
                    switch (exp.op) {
                        case ADD:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matAdd(lmat, rmat));
                        case SUB:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matSub(lmat, rmat));
                        case MUL:
                            return new Expression.IntegerMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matMul(lmat, rmat));
                    }
                } else if (exp.rexp instanceof Expression.FloatMatrixExp) {
                    double[][] rmat = ((Expression.FloatMatrixExp) exp.rexp).val;
                    switch (exp.op) {
                        case ADD:
                            return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matAdd(lmat, rmat));
                        case SUB:
                            return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matSub(lmat, rmat));
                        case MUL:
                            return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matMul(lmat, rmat));
                    }
                }
            } else if (exp.lexp instanceof Expression.FloatMatrixExp) {
                double[][] lmat = ((Expression.FloatMatrixExp) exp.lexp).val;
                if (exp.op == EnumToken.POW && exp.rexp instanceof Expression.IntegerExp) {
                    return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matPow(lmat, ((Expression.IntegerExp) exp.rexp).val));
                }

                if (!(exp.rexp instanceof Expression.IntegerExp) && !(exp.rexp instanceof Expression.FloatExp)) {
                    if (exp.rexp instanceof Expression.IntegerMatrixExp) {
                        int[][] rmat = ((Expression.IntegerMatrixExp) exp.rexp).val;
                        switch (exp.op) {
                            case ADD:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matAdd(lmat, rmat));
                            case SUB:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matSub(lmat, rmat));
                            case MUL:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.IntegerMatrixExp) exp.rexp).lastLine, MatrixUtil.matMul(lmat, rmat));
                        }
                    } else if (exp.rexp instanceof Expression.FloatMatrixExp) {
                        double[][] rmat = ((Expression.FloatMatrixExp) exp.rexp).val;
                        switch (exp.op) {
                            case ADD:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matAdd(lmat, rmat));
                            case SUB:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matSub(lmat, rmat));
                            case MUL:
                                return new Expression.FloatMatrixExp(exp.lexp.line, ((Expression.FloatMatrixExp) exp.rexp).lastLine, MatrixUtil.matMul(lmat, rmat));
                        }
                    }
                } else {
                    double r = exp.rexp instanceof Expression.IntegerExp ? (double) ((Expression.IntegerExp) exp.rexp).val : ((Expression.FloatExp) exp.rexp).val;
                    switch (exp.op) {
                        case ADD:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matAdd(lmat, r));
                        case SUB:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matSub(lmat, r));
                        case MUL:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMul(lmat, r));
                        case DIV:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matDiv(lmat, r));
                        case MOD:
                            return new Expression.FloatMatrixExp(exp.lexp.line, exp.rexp.line, MatrixUtil.matMod(lmat, r));
                    }
                }
            }

            return exp;
        } else {
            double l = exp.lexp instanceof Expression.IntegerExp ? (double) ((Expression.IntegerExp) exp.lexp).val : ((Expression.FloatExp) exp.lexp).val;
            double r = exp.rexp instanceof Expression.IntegerExp ? (double) ((Expression.IntegerExp) exp.rexp).val : ((Expression.FloatExp) exp.rexp).val;
            return switch (exp.op) {
                case ADD -> new Expression.FloatExp(exp.line, l + r);
                case SUB -> new Expression.FloatExp(exp.line, l - r);
                case MUL -> new Expression.FloatExp(exp.line, l * r);
                case DIV -> new Expression.FloatExp(exp.line, l / r);
                case MOD -> new Expression.FloatExp(exp.line, l % r);
                case POW -> new Expression.FloatExp(exp.line, Math.pow(l, r));
                default -> exp;
            };
        }
    }

    public static Expression optimizeUnaryOp(Expression.UnopExp exp) {
        switch (exp.op) {
            case NEG:
                if (exp.exp instanceof Expression.IntegerExp) {
                    return new Expression.IntegerExp(exp.line, -((Expression.IntegerExp) exp.exp).val);
                } else if (exp.exp instanceof Expression.FloatExp) {
                    return new Expression.FloatExp(exp.line, -((Expression.FloatExp) exp.exp).val);
                } else if (exp.exp instanceof Expression.IntegerMatrixExp) {
                    return new Expression.IntegerMatrixExp(exp.exp.line, ((Expression.IntegerMatrixExp) exp.exp).lastLine, MatrixUtil.matNeg(((Expression.IntegerMatrixExp) exp.exp).val));
                } else if (exp.exp instanceof Expression.FloatMatrixExp) {
                    return new Expression.FloatMatrixExp(exp.exp.line, ((Expression.FloatMatrixExp) exp.exp).lastLine, MatrixUtil.matNeg(((Expression.FloatMatrixExp) exp.exp).val));
                }
            case NOT:
                if (exp.exp instanceof Expression.IntegerExp) {
                    return new Expression.IntegerExp(exp.line, ((Expression.IntegerExp) exp.exp).val == 0 ? 1 : 0);
                } else if (exp.exp instanceof Expression.FloatExp) {
                    return new Expression.FloatExp(exp.line, ((Expression.FloatExp) exp.exp).val == (double) 0.0F ? (double) 1.0F : (double) 0.0F);
                }
            default:
                return exp;
        }
    }

    private static boolean trueOnly(Expression exp) {
        if (exp instanceof Expression.IntegerExp) {
            return ((Expression.IntegerExp) exp).val != 0;
        } else if (exp instanceof Expression.FloatExp) {
            return (int) ((Expression.FloatExp) exp).val != 0;
        } else {
            return false;
        }
    }

    private static boolean falseOnly(Expression exp) {
        if (exp instanceof Expression.IntegerExp) {
            return ((Expression.IntegerExp) exp).val == 0;
        } else if (exp instanceof Expression.FloatExp) {
            return (int) ((Expression.FloatExp) exp).val == 0;
        } else {
            return false;
        }
    }

    private static int pow(int l, int r) {
        int result = 1;
        for (int i = 0; i < r; ++i) {
            result *= l;
        }
        return result;
    }
}
