package net.hackermdch.exparticle.util;

public class Expression {
    public final int line;
    public int returnType;

    protected Expression(int line, int returnType) {
        this.line = line;
        this.returnType = returnType;
    }

    public static class IntegerExp extends Expression {
        public final int val;

        public IntegerExp(int line, int val) {
            super(line, 10);
            this.val = val;
        }

        public String toString() {
            return String.valueOf(this.val);
        }
    }

    public static class FloatExp extends Expression {
        public final double val;

        public FloatExp(int line, double val) {
            super(line, 7);
            this.val = val;
        }

        public String toString() {
            return String.valueOf(this.val);
        }
    }

    public static class IntegerMatrixExp extends Expression {
        public final int lastLine;
        public final int[][] val;

        public IntegerMatrixExp(int line, int lastLine, int[][] val) {
            super(line, 12);
            this.lastLine = lastLine;
            this.val = val;
        }

        public String toString() {
            var sb = new StringBuilder();
            sb.append("(");
            for (var ints : val) {
                for (var anInt : ints) {
                    sb.append(anInt).append(",");
                }
                sb.deleteCharAt(sb.length() - 1).append(",,");
            }
            sb.delete(sb.length() - 2, sb.length()).append(")");
            return sb.toString();
        }
    }

    public static class FloatMatrixExp extends Expression {
        public final int lastLine;
        public final double[][] val;

        public FloatMatrixExp(int line, int lastLine, double[][] val) {
            super(line, 13);
            this.lastLine = lastLine;
            this.val = val;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (var doubles : val) {
                for (var v : doubles) {
                    sb.append(v).append(",");
                }
                sb.deleteCharAt(sb.length() - 1).append(",,");
            }
            sb.delete(sb.length() - 2, sb.length()).append(")");
            return sb.toString();
        }
    }

    public static class MatrixExp extends Expression {
        public final int lastLine;
        public final Expression[][] exps;

        public MatrixExp(int line, int lastLine, Expression[][] exps) {
            super(line, 13);
            this.lastLine = lastLine;
            this.exps = exps;
        }

        public String toString() {
            var sb = new StringBuilder();
            sb.append("(");
            for (var exp : exps) {
                for (var expression : exp) {
                    sb.append(expression.toString()).append(",");
                }
                sb.deleteCharAt(sb.length() - 1).append(",,");
            }
            sb.delete(sb.length() - 2, sb.length()).append(")");
            return sb.toString();
        }
    }

    public static class NameMatrixExp extends Expression {
        public final int lastLine;
        public final NameExp[][] names;

        public NameMatrixExp(int line, int lastLine, NameExp[][] names) {
            super(line, 0);
            this.lastLine = lastLine;
            this.names = names;
        }

        public String toString() {
            var sb = new StringBuilder();
            sb.append("(");
            for (var name : names) {
                for (var exp : name) {
                    sb.append(exp.name).append(",");
                }
                sb.deleteCharAt(sb.length() - 1).append(",,");
            }
            sb.delete(sb.length() - 2, sb.length()).append(")");
            return sb.toString();
        }
    }

    public static class NameExp extends Expression {
        public final String name;

        public NameExp(int line, String name) {
            super(line, -1);
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static class UnopExp extends Expression {
        public final EnumToken op;
        public final Expression exp;

        public UnopExp(int line, EnumToken op, Expression exp) {
            super(line, exp.returnType);
            if (op == EnumToken.MINUS || op == EnumToken.SUB) {
                op = EnumToken.NEG;
            }

            this.op = op;
            this.exp = exp;
        }

        public String toString() {
            return this.op.token + this.exp.toString();
        }
    }

    public static class BinopExp extends Expression {
        public final EnumToken op;
        public final Expression lexp;
        public final Expression rexp;

        public BinopExp(int line, EnumToken op, Expression lexp, Expression rexp) {
            super(line, -1);
            if (op == EnumToken.MINUS || op == EnumToken.NEG) {
                op = EnumToken.SUB;
            }
            this.op = op;
            this.lexp = lexp;
            this.rexp = rexp;
            if (op == EnumToken.AND || op == EnumToken.OR) {
                this.returnType = 10;
            }
            if (lexp.returnType != -1 && rexp.returnType != -1) {
                if (lexp.returnType == 10 && rexp.returnType == 10) {
                    this.returnType = op == EnumToken.POW ? 7 : 10;
                } else if ((lexp.returnType != 10 || rexp.returnType != 7) && (lexp.returnType != 7 || rexp.returnType != 10) && (lexp.returnType != 7 || rexp.returnType != 7)) {
                    if (lexp.returnType != 12 || rexp.returnType != 10 && rexp.returnType != 12) {
                        this.returnType = 13;
                    } else {
                        this.returnType = 12;
                    }
                } else {
                    this.returnType = 7;
                }
            }
        }

        public String toString() {
            return this.lexp.toString() + this.op.token + this.rexp.toString();
        }
    }

    public static class FunctionCallExp extends Expression {
        public final int lastLine;
        public final String name;
        public final Expression[] args;

        public FunctionCallExp(int line, int lastLine, String name, Expression[] args) {
            super(line, -1);
            this.lastLine = lastLine;
            this.name = name;
            this.args = args;
        }

        public String toString() {
            var sb = new StringBuilder();
            sb.append(name).append("(");
            for (var arg : args) {
                sb.append(arg.toString()).append(",");
            }
            return sb.deleteCharAt(sb.length() - 1).append(")").toString();
        }
    }

    public static class AssignExp extends Expression {
        public final Expression[] varList;
        public final Expression[] expList;

        public AssignExp(int line, Expression[] varList, Expression[] expList) {
            super(line, expList[expList.length - 1].returnType);
            this.varList = varList;
            this.expList = expList;
        }

        public String toString() {
            var sb = new StringBuilder();
            for (var exp : varList) sb.append(exp.toString()).append(",");
            sb.deleteCharAt(sb.length() - 1).append("=");
            for (var exp : expList) sb.append(exp.toString()).append(",");
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }
}
