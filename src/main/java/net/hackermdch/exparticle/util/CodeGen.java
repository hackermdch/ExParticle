package net.hackermdch.exparticle.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

public class CodeGen {
    public static final int T_UNKNOW = -1;
    public static final int T_VOID = 0;
    public static final int T_INTMAT = 12;
    public static final int T_DOUBLEMAT = 13;
    private static final Object2IntMap<EnumToken> IOP2OOP = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<EnumToken> DOP2OOP = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<EnumToken> DIOP2OOP = new Object2IntOpenHashMap<>();
    private static final List<String> FIELDS;
    private static final Method[] METHODS = Stream.concat(Arrays.stream(Math.class.getMethods()), Arrays.stream(ExFunctions.class.getMethods())).toArray(Method[]::new);
    private final Expression[] block;
    private final ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
    private MethodVisitor mv;
    private MethodVisitor simulationMv;
    private int simulationCount;
    private int maxLocal;
    private final Map<String, LocalVarInfo> localVars = new HashMap<>();

    public CodeGen(Expression[] block) {
        this.block = Arrays.copyOf(block, block.length);
    }

    public Class<?> codeGenBlock(String name) {
        cw.visit(V21, ACC_PUBLIC | ACC_SUPER, "net/hackermdch/exparticle/util/CodeGen$" + name, null, "java/lang/Object", null);
        cw.visitInnerClass("net/hackermdch/exparticle/util/CodeGen$" + name, "net/hackermdch/exparticle/util/CodeGen", name, ACC_PUBLIC | ACC_STATIC);
        mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "invoke", "(Lnet/hackermdch/exparticle/util/ParticleStruct;)I", null, null);
        addLocalVar("this", T_UNKNOW);
        mv.visitCode();
        for (int i = 0; i < block.length - 1; ++i) {
            codeGenExp(block[i], T_VOID);
        }
        codeGenReturn(block[block.length - 1]);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        var cl = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = cl.getClass();
        Method method = null;
        while (clazz != null) {
            try {
                method = clazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                method.setAccessible(true);
                break;
            } catch (NoSuchMethodException ignore) {
            }
            clazz = clazz.getSuperclass();
        }
        var bytes = cw.toByteArray();
        try {
            assert method != null;
            return (Class<?>) method.invoke(cl, "net.hackermdch.exparticle.util.CodeGen$" + name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int codeGenExp(Expression exp, int targetType) {
        return switch (exp) {
            case Expression.IntegerExp integerExp ->
                    codeGenTypeTransform(codeGenLoadInteger(integerExp.val), targetType);
            case Expression.FloatExp floatExp -> codeGenTypeTransform(codeGenLoadFloat(floatExp.val), targetType);
            case Expression.IntegerMatrixExp integerMatrixExp ->
                    codeGenTypeTransform(codeGenLoadIntegerMatrix(integerMatrixExp.val), targetType);
            case Expression.FloatMatrixExp floatMatrixExp ->
                    codeGenTypeTransform(codeGenLoadFloatMatrix(floatMatrixExp.val), targetType);
            case Expression.MatrixExp matrixExp -> codeGenTypeTransform(codeGenLoadMatrix(matrixExp.exps), targetType);
            case Expression.NameMatrixExp nameMatrixExp ->
                    codeGenTypeTransform(codeGenLoadMatrix(nameMatrixExp.names), targetType);
            case Expression.UnopExp unopExp ->
                    codeGenTypeTransform(codeGenUnopExp(unopExp.op, unopExp.exp), targetType);
            case Expression.BinopExp binopExp -> codeGenTypeTransform(codeGenBinopExp(binopExp), targetType);
            case Expression.NameExp nameExp -> codeGenTypeTransform(codeGenNameExp(nameExp.name), targetType);
            case Expression.FunctionCallExp functionCallExp ->
                    codeGenTypeTransform(codeGenFunctionCallExp(functionCallExp.name, functionCallExp.args), targetType);
            case null, default ->
                    exp instanceof Expression.AssignExp ? codeGenTypeTransform(codeGenAssignExp(((Expression.AssignExp) exp).varList, ((Expression.AssignExp) exp).expList, targetType != T_VOID), targetType) : T_VOID;
        };
    }

    private void codeGenReturn(Expression exp) {
        codeGenExp(exp, T_INT);
        mv.visitInsn(IRETURN);
    }

    private int codeGenLoadInteger(int n) {
        if (n >= T_UNKNOW && n < 6) {
            mv.visitInsn(ICONST_0 + n);
        } else if (n >= Byte.MIN_VALUE && n <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, n);
        } else if (n >= Short.MIN_VALUE && n <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, n);
        } else {
            mv.visitLdcInsn(n);
        }
        return T_INT;
    }

    private int codeGenLoadFloat(double n) {
        if (n == 0.0) {
            mv.visitInsn(DCONST_0);
        } else if (n == 1.0) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(n);
        }
        return T_DOUBLE;
    }

    private int codeGenLoadIntegerMatrix(int[][] val) {
        codeGenLoadInteger(val.length);
        mv.visitTypeInsn(ANEWARRAY, "[I");
        for (int i = 0; i < val.length; ++i) {
            mv.visitInsn(DUP);
            codeGenLoadInteger(i);
            codeGenLoadInteger(val[i].length);
            mv.visitIntInsn(NEWARRAY, T_INT);
            for (int j = 0; j < val[i].length; ++j) {
                mv.visitInsn(DUP);
                codeGenLoadInteger(j);
                codeGenLoadInteger(val[i][j]);
                mv.visitInsn(IASTORE);
            }
            mv.visitInsn(AASTORE);
        }
        return T_INTMAT;
    }

    private int codeGenLoadFloatMatrix(double[][] val) {
        codeGenLoadInteger(val.length);
        mv.visitTypeInsn(ANEWARRAY, "[D");
        for (int i = 0; i < val.length; ++i) {
            mv.visitInsn(DUP);
            codeGenLoadInteger(i);
            codeGenLoadInteger(val[i].length);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            for (int j = 0; j < val[i].length; ++j) {
                mv.visitInsn(DUP);
                codeGenLoadInteger(j);
                codeGenLoadFloat(val[i][j]);
                mv.visitInsn(DASTORE);
            }
            mv.visitInsn(AASTORE);
        }
        return T_DOUBLEMAT;
    }

    private int codeGenLoadMatrix(Expression[][] exps) {
        codeGenLoadInteger(exps.length);
        mv.visitTypeInsn(ANEWARRAY, "[D");
        for (int i = 0; i < exps.length; ++i) {
            mv.visitInsn(DUP);
            codeGenLoadInteger(i);
            codeGenLoadInteger(exps[i].length);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            for (int j = 0; j < exps[i].length; ++j) {
                mv.visitInsn(DUP);
                codeGenLoadInteger(j);
                codeGenExp(exps[i][j], T_DOUBLE);
                mv.visitInsn(DASTORE);
            }
            mv.visitInsn(AASTORE);
        }
        return T_DOUBLEMAT;
    }

    private int codeGenUnopExp(EnumToken op, Expression exp) {
        switch (op) {
            case NEG:
                return switch (codeGenExp(exp, T_UNKNOW)) {
                    case T_DOUBLE -> {
                        mv.visitInsn(DNEG);
                        yield T_DOUBLE;
                    }
                    case T_INT -> {
                        mv.visitInsn(INEG);
                        yield T_INT;
                    }
                    case T_INTMAT -> {
                        mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "matNeg", "([[I)[[I", false);
                        yield T_INTMAT;
                    }
                    case T_DOUBLEMAT -> {
                        mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "matNeg", "([[D)[[D", false);
                        yield T_DOUBLEMAT;
                    }
                    default -> throw new RuntimeException("bad type");
                };
            case NOT:
                switch (codeGenExp(exp, T_UNKNOW)) {
                    case T_DOUBLE:
                        mv.visitInsn(DCONST_0);
                        mv.visitInsn(DCMPL);
                        var dJumpLabel = new Label();
                        mv.visitJumpInsn(IFEQ, dJumpLabel);
                        mv.visitInsn(DCONST_0);
                        var dEndLabel = new Label();
                        mv.visitJumpInsn(GOTO, dEndLabel);
                        mv.visitLabel(dJumpLabel);
                        mv.visitInsn(DCONST_1);
                        mv.visitLabel(dEndLabel);
                        return T_DOUBLE;
                    case T_INT:
                        var iJumpLabel = new Label();
                        mv.visitJumpInsn(IFEQ, iJumpLabel);
                        mv.visitInsn(ICONST_0);
                        var iEndLabel = new Label();
                        mv.visitJumpInsn(GOTO, iEndLabel);
                        mv.visitLabel(iJumpLabel);
                        mv.visitInsn(ICONST_1);
                        mv.visitLabel(iEndLabel);
                        return T_INT;
                    default:
                        throw new RuntimeException("bad type");
                }
            default:
                throw new RuntimeException("bad operator: " + op);
        }
    }

    private int codeGenBinopExp(Expression.BinopExp exp) {
        switch (exp.op) {
            case AND:
                codeGenExp(exp.lexp, T_INT);
                var aJumpLabel = new Label();
                mv.visitJumpInsn(IFEQ, aJumpLabel);
                codeGenExp(exp.rexp, T_INT);
                mv.visitJumpInsn(IFEQ, aJumpLabel);
                mv.visitInsn(ICONST_1);
                var aEndLabel = new Label();
                mv.visitJumpInsn(GOTO, aEndLabel);
                mv.visitLabel(aJumpLabel);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(aEndLabel);
                return T_INT;
            case OR:
                codeGenExp(exp.lexp, T_INT);
                var oJumpLabel = new Label();
                mv.visitJumpInsn(IFNE, oJumpLabel);
                codeGenExp(exp.rexp, T_INT);
                mv.visitJumpInsn(IFNE, oJumpLabel);
                mv.visitInsn(ICONST_0);
                var oEndLabel = new Label();
                mv.visitJumpInsn(GOTO, oEndLabel);
                mv.visitLabel(oJumpLabel);
                mv.visitInsn(ICONST_1);
                mv.visitLabel(oEndLabel);
                return T_INT;
            default:
                if (exp.returnType == T_UNKNOW) {
                    startSimulation();
                    exp.returnType = upwardType(codeGenExp(exp.lexp, T_UNKNOW), codeGenExp(exp.rexp, T_UNKNOW));
                    stopSimulation();
                }
                switch (exp.returnType) {
                    case T_DOUBLE:
                        codeGenExp(exp.lexp, T_DOUBLE);
                        codeGenExp(exp.rexp, T_DOUBLE);
                        switch (exp.op) {
                            case ADD:
                                mv.visitInsn(DADD);
                                return T_DOUBLE;
                            case SUB:
                                mv.visitInsn(DSUB);
                                return T_DOUBLE;
                            case MUL:
                                mv.visitInsn(DMUL);
                                return T_DOUBLE;
                            case DIV:
                                mv.visitInsn(DDIV);
                                return T_DOUBLE;
                            case MOD:
                                mv.visitInsn(DREM);
                                return T_DOUBLE;
                            case POW:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
                                return T_DOUBLE;
                            case LT:
                            case LE:
                            case GT:
                            case GE:
                            case EQ:
                            case NEQ:
                                mv.visitInsn(DOP2OOP.getInt(exp.op));
                                var logicJumpLabel = new Label();
                                mv.visitJumpInsn(DIOP2OOP.getInt(exp.op), logicJumpLabel);
                                mv.visitInsn(ICONST_1);
                                var logicEndLabel = new Label();
                                mv.visitJumpInsn(GOTO, logicEndLabel);
                                mv.visitLabel(logicJumpLabel);
                                mv.visitInsn(ICONST_0);
                                mv.visitLabel(logicEndLabel);
                                return T_INT;
                            default:
                                throw new RuntimeException("bad operator: " + exp.op.token);
                        }
                    case T_INT:
                        codeGenExp(exp.lexp, T_INT);
                        codeGenExp(exp.rexp, T_INT);
                        switch (exp.op) {
                            case ADD:
                                mv.visitInsn(IADD);
                                return T_INT;
                            case SUB:
                                mv.visitInsn(ISUB);
                                return T_INT;
                            case MUL:
                                mv.visitInsn(IMUL);
                                return T_INT;
                            case DIV:
                                mv.visitInsn(IDIV);
                                return T_INT;
                            case MOD:
                                mv.visitInsn(IREM);
                                return T_INT;
                            case POW:
                                mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "pow", "(II)D", false);
                                return T_DOUBLE;
                            case LT:
                            case LE:
                            case GT:
                            case GE:
                            case EQ:
                            case NEQ:
                                var logicJumpLabel = new Label();
                                mv.visitJumpInsn(IOP2OOP.getInt(exp.op), logicJumpLabel);
                                mv.visitInsn(ICONST_1);
                                var logicEndLabel = new Label();
                                mv.visitJumpInsn(GOTO, logicEndLabel);
                                mv.visitLabel(logicJumpLabel);
                                mv.visitInsn(ICONST_0);
                                mv.visitLabel(logicEndLabel);
                                return T_INT;
                            default:
                                throw new RuntimeException("bad operator: " + exp.op.token);
                        }
                    default:
                        var ltype = codeGenExp(exp.lexp, T_UNKNOW);
                        var rtype = codeGenExp(exp.rexp, T_UNKNOW);
                        var returntype = T_VOID;
                        String returnName = null;
                        String arg1Name;
                        switch (ltype) {
                            case T_INTMAT:
                                arg1Name = "[[I";
                                break;
                            case T_DOUBLEMAT:
                                arg1Name = "[[D";
                                returnName = "[[D";
                                returntype = T_DOUBLEMAT;
                                break;
                            default:
                                throw new RuntimeException("the number must appear on the right side of the matrix");
                        }
                        String arg2Name;
                        switch (rtype) {
                            case T_DOUBLE:
                                arg2Name = "D";
                                returnName = "[[D";
                                returntype = T_DOUBLEMAT;
                                break;
                            case T_INT:
                                arg2Name = "I";
                                break;
                            case T_INTMAT:
                                arg2Name = "[[I";
                                break;
                            case T_DOUBLEMAT:
                                arg2Name = "[[D";
                                returnName = "[[D";
                                returntype = T_DOUBLEMAT;
                                break;
                            case T_BYTE:
                            case T_SHORT:
                            case T_LONG:
                            default:
                                throw new RuntimeException("bad type: " + rtype);
                        }
                        if (returnName == null) {
                            returnName = "[[I";
                            returntype = T_INTMAT;
                        }
                        var functionName = switch (exp.op) {
                            case ADD -> "matAdd";
                            case SUB -> "matSub";
                            case MUL -> "matMul";
                            case DIV -> "matDiv";
                            case MOD -> "matMod";
                            case POW -> "matPow";
                            default -> throw new RuntimeException("bad operator: " + exp.op.token);
                        };
                        if ((!functionName.equals("matDiv") && !functionName.equals("matMod") || !arg2Name.startsWith("[[")) && (!functionName.equals("matPow") || arg2Name.equals("I"))) {
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", functionName, "(" + arg1Name + arg2Name + ")" + returnName, false);
                            return returntype;
                        } else {
                            throw new RuntimeException("bad operator: " + exp.op.token);
                        }
                }
        }
    }

    private int codeGenNameExp(String name) {
        if (FIELDS.contains(name)) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "net/hackermdch/exparticle/util/ParticleStruct", name, "D");
            return T_DOUBLE;
        } else if (localVars.containsKey(name)) {
            var info = localVars.get(name);
            return switch (info.type) {
                case T_DOUBLE -> {
                    mv.visitVarInsn(DLOAD, info.index);
                    yield T_DOUBLE;
                }
                case T_INT -> {
                    mv.visitVarInsn(ILOAD, info.index);
                    yield T_INT;
                }
                case T_INTMAT -> {
                    mv.visitVarInsn(ALOAD, info.index);
                    yield T_INTMAT;
                }
                case T_DOUBLEMAT -> {
                    mv.visitVarInsn(ALOAD, info.index);
                    yield T_DOUBLEMAT;
                }
                default -> throw new RuntimeException("bad type: " + info.type);
            };
        } else {
            throw new RuntimeException("undefine var: " + name);
        }
    }

    private int codeGenFunctionCallExp(String name, Expression[] args) {
        var methods = new ArrayList<Method>();
        for (var method : METHODS) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                methods.add(method);
            }
        }
        if (methods.isEmpty()) {
            throw new RuntimeException("function not found: " + name);
        } else {
            for (var arg : args) {
                if (arg.returnType == T_UNKNOW) {
                    startSimulation();
                    arg.returnType = codeGenExp(arg, T_UNKNOW);
                    stopSimulation();
                }
            }
            int maxSimilarity = 0;
            int maxSimilarityIndex = T_UNKNOW;
            if (args.length == 0) {
                maxSimilarityIndex = 0;
            } else {
                int i = 0;
                while (i < methods.size()) {
                    var method = methods.get(i);
                    var parameterTypes = method.getParameterTypes();
                    int similarity = 0;
                    int j = 0;
                    while (true) {
                        label:
                        {
                            if (j < args.length) {
                                switch (args[j].returnType) {
                                    case T_DOUBLE:
                                        if (parameterTypes[j] == int.class) {
                                            similarity += 4;
                                            break label;
                                        }
                                        if (parameterTypes[j] == double.class) {
                                            similarity += 6;
                                            break label;
                                        }
                                        break;
                                    case T_INT:
                                        if (parameterTypes[j] == int.class) {
                                            similarity += 6;
                                            break label;
                                        }
                                        if (parameterTypes[j] == double.class) {
                                            similarity += 5;
                                            break label;
                                        }
                                        break;
                                    case T_INTMAT:
                                        if (parameterTypes[j] == int[][].class) {
                                            similarity += 6;
                                            break label;
                                        }
                                        if (parameterTypes[j] == int.class) {
                                            similarity += 3;
                                            break label;
                                        }
                                        if (parameterTypes[j] == double.class) {
                                            similarity += 2;
                                            break label;
                                        }
                                        break;
                                    case T_DOUBLEMAT:
                                        if (parameterTypes[j] == double[][].class) {
                                            similarity += 6;
                                            break label;
                                        }
                                        if (parameterTypes[j] == int.class) {
                                            ++similarity;
                                            break label;
                                        }
                                        if (parameterTypes[j] == double.class) {
                                            similarity += 3;
                                            break label;
                                        }
                                        break;
                                    case T_BYTE:
                                    case T_SHORT:
                                    case T_LONG:
                                    default:
                                        throw new RuntimeException("bad type: " + args[j].returnType);
                                }
                            } else if (similarity > maxSimilarity) {
                                maxSimilarity = similarity;
                                maxSimilarityIndex = i;
                            }
                            ++i;
                            break;
                        }
                        ++j;
                    }
                }
            }
            if (maxSimilarityIndex == T_UNKNOW) {
                throw new RuntimeException("function not found: " + name);
            } else {
                var method = methods.get(maxSimilarityIndex);
                var parameterTypes = method.getParameterTypes();
                var returnType = method.getReturnType();
                var sb = new StringBuilder();
                sb.append("(");
                for (int i = 0; i < args.length; ++i) {
                    if (parameterTypes[i].isArray()) {
                        codeGenExp(args[i], parameterTypes[i] == int[][].class ? T_INTMAT : T_DOUBLEMAT);
                        sb.append(parameterTypes[i] == int[][].class ? "[[I" : "[[D");
                    } else {
                        codeGenExp(args[i], parameterTypes[i] == int.class ? T_INT : T_DOUBLE);
                        sb.append(parameterTypes[i] == int.class ? "I" : "D");
                    }
                }
                sb.append(")");
                if (returnType.isArray()) {
                    if (returnType == int[][].class) {
                        sb.append("[[I");
                        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), name, sb.toString(), false);
                        return T_INTMAT;
                    } else {
                        sb.append("[[D");
                        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), name, sb.toString(), false);
                        return T_DOUBLEMAT;
                    }
                } else {
                    if (returnType == long.class) {
                        sb.append("J");
                        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), name, sb.toString(), false);
                        mv.visitInsn(L2I);
                        return T_INT;
                    } else {
                        sb.append(returnType == int.class ? "I" : "D");
                        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), name, sb.toString(), false);
                        return returnType == int.class ? T_INT : T_DOUBLE;
                    }
                }
            }
        }
    }

    private int codeGenAssignExp(Expression[] varList, Expression[] expList, boolean needReturn) {
        var types = new int[expList.length];
        for (int i = 0; i < expList.length; ++i) {
            if (varList[i] instanceof Expression.NameExp && FIELDS.contains(((Expression.NameExp) varList[i]).name)) {
                mv.visitVarInsn(ALOAD, 0);
            }
            types[i] = codeGenExp(expList[i], T_UNKNOW);
        }
        if (needReturn) {
            mv.visitInsn(types[expList.length - 1] == T_DOUBLE ? DUP2 : DUP);
            codeGenStore("__RETURN", types[expList.length - 1]);
        }
        for (int i = varList.length - 1; i >= 0; --i) {
            if (varList[i] instanceof Expression.NameExp) {
                codeGenStore(((Expression.NameExp) varList[i]).name, types[i]);
            } else if (varList[i] instanceof Expression.NameMatrixExp) {
                if (types[i] != T_INTMAT && types[i] != T_DOUBLEMAT) {
                    throw new RuntimeException("can't deconstruction number: " + types[i]);
                }
                var names = ((Expression.NameMatrixExp) varList[i]).names;
                for (int j = 0; j < names.length; ++j) {
                    if (j < names.length - 1) {
                        mv.visitInsn(DUP);
                    }
                    codeGenLoadInteger(j);
                    mv.visitInsn(AALOAD);
                    for (int k = 0; k < names[j].length; ++k) {
                        if (k < names[j].length - 1) mv.visitInsn(DUP);
                        codeGenLoadInteger(k);
                        mv.visitInsn(types[i] == T_INTMAT ? IALOAD : DALOAD);
                        codeGenStore("__TEMP", types[i] == T_INTMAT ? T_INT : T_DOUBLE);
                        mv.visitVarInsn(ALOAD, 0);
                        codeGenNameExp("__TEMP");
                        codeGenStore(names[j][k].name, types[i] == T_INTMAT ? T_INT : T_DOUBLE);
                    }
                }
            }
        }
        if (needReturn) codeGenNameExp("__RETURN");
        return needReturn ? types[expList.length - 1] : T_VOID;
    }

    private int codeGenTypeTransform(int sourceType, int targetType) {
        if (targetType == T_UNKNOW) {
            return sourceType;
        } else if (sourceType == targetType) {
            return targetType;
        } else if (targetType == T_VOID) {
            if (sourceType == T_DOUBLE) mv.visitInsn(POP2);
            else mv.visitInsn(POP);
            return targetType;
        } else {
            switch (sourceType) {
                case T_DOUBLE:
                    switch (targetType) {
                        case T_INT:
                            mv.visitInsn(D2I);
                            return targetType;
                        case T_INTMAT:
                            mv.visitInsn(D2I);
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toMat", "(I)[[I", false);
                            return targetType;
                        case T_DOUBLEMAT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toMat", "(D)[[D", false);
                            return targetType;
                        case T_LONG:
                        default:
                            throw new RuntimeException("bad type: " + targetType);
                    }
                case T_INT:
                    switch (targetType) {
                        case T_DOUBLE:
                            mv.visitInsn(I2D);
                            return targetType;
                        case T_INTMAT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toMat", "(I)[[I", false);
                            return targetType;
                        case T_DOUBLEMAT:
                            mv.visitInsn(I2D);
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toMat", "(D)[[D", false);
                            return targetType;
                        default:
                            throw new RuntimeException("bad type: " + targetType);
                    }
                case T_INTMAT:
                    switch (targetType) {
                        case T_DOUBLE:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toNumber", "([[I)I", false);
                            mv.visitInsn(I2D);
                            return targetType;
                        case T_INT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toNumber", "([[I)I", false);
                            return targetType;
                        case T_DOUBLEMAT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "matToMat", "([[I)D[[", false);
                            return targetType;
                        default:
                            throw new RuntimeException("bad type: " + targetType);
                    }
                case T_DOUBLEMAT:
                    switch (targetType) {
                        case T_DOUBLE:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toNumber", "([[D)D", false);
                            return targetType;
                        case T_INT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "toNumber", "([[D)D", false);
                            mv.visitInsn(D2I);
                            return targetType;
                        case T_INTMAT:
                            mv.visitMethodInsn(INVOKESTATIC, "net/hackermdch/exparticle/util/MatrixUtil", "matToMat", "([[D)I[[", false);
                            return targetType;
                        default:
                            throw new RuntimeException("bad type: " + targetType);
                    }
                case T_BYTE:
                case T_SHORT:
                case T_LONG:
                default:
                    throw new RuntimeException("bad type: " + targetType);
            }
        }
    }

    private void codeGenStore(String name, int targetType) {
        if (FIELDS.contains(name)) {
            codeGenTypeTransform(targetType, T_DOUBLE);
            mv.visitFieldInsn(PUTFIELD, "net/hackermdch/exparticle/util/ParticleStruct", name, "D");
        } else {
            if (!localVars.containsKey(name)) addLocalVar(name, targetType);
            var info = localVars.get(name);
            switch (info.type) {
                case T_DOUBLE:
                    codeGenTypeTransform(targetType, T_DOUBLE);
                    mv.visitVarInsn(DSTORE, info.index);
                    return;
                case T_INT:
                    codeGenTypeTransform(targetType, T_INT);
                    mv.visitVarInsn(ISTORE, info.index);
                    return;
                case T_INTMAT:
                    codeGenTypeTransform(targetType, T_INTMAT);
                    mv.visitVarInsn(ASTORE, info.index);
                    return;
                case T_DOUBLEMAT:
                    codeGenTypeTransform(targetType, T_DOUBLEMAT);
                    mv.visitVarInsn(ASTORE, info.index);
                    return;
                case T_BYTE:
                case T_SHORT:
                case T_LONG:
                default:
                    throw new RuntimeException("bad type: " + info.type);
            }
        }
    }

    private int upwardType(int ltype, int rtype) {
        if (Math.max(ltype, rtype) <= 0) {
            throw new RuntimeException("bad type");
        } else if (ltype == T_INT && rtype == T_INT) {
            return T_INT;
        } else {
            return (ltype != T_INT || rtype != T_DOUBLE) && (ltype != T_DOUBLE || rtype != T_INT) && (ltype != T_DOUBLE || rtype != T_DOUBLE) ? T_UNKNOW : T_DOUBLE;
        }
    }

    private void addLocalVar(String name, int type) {
        var index = maxLocal;
        if (type == T_DOUBLE) maxLocal += 2;
        else ++maxLocal;
        localVars.put(name, new LocalVarInfo(type, index));
    }

    private void startSimulation() {
        if (simulationCount++ == 0) {
            simulationMv = mv;
            mv = new MethodVisitor(ASM9) {
            };
        }
    }

    private void stopSimulation() {
        if (--simulationCount == 0) mv = simulationMv;
    }

    static {
        IOP2OOP.put(EnumToken.LT, IF_ICMPGE);
        IOP2OOP.put(EnumToken.LE, IF_ICMPGT);
        IOP2OOP.put(EnumToken.GT, IF_ICMPLE);
        IOP2OOP.put(EnumToken.GE, IF_ICMPLT);
        IOP2OOP.put(EnumToken.EQ, IF_ICMPNE);
        IOP2OOP.put(EnumToken.NEQ, IF_ICMPEQ);
        DOP2OOP.put(EnumToken.LT, DCMPG);
        DOP2OOP.put(EnumToken.LE, DCMPG);
        DOP2OOP.put(EnumToken.GT, DCMPL);
        DOP2OOP.put(EnumToken.GE, DCMPL);
        DOP2OOP.put(EnumToken.EQ, DCMPL);
        DOP2OOP.put(EnumToken.NEQ, DCMPL);
        DIOP2OOP.put(EnumToken.LT, IFGE);
        DIOP2OOP.put(EnumToken.LE, IFGT);
        DIOP2OOP.put(EnumToken.GT, IFLE);
        DIOP2OOP.put(EnumToken.GE, IFLT);
        DIOP2OOP.put(EnumToken.EQ, IFNE);
        DIOP2OOP.put(EnumToken.NEQ, IFEQ);
        var builder = ImmutableList.<String>builder();
        for (var field : ParticleStruct.class.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) builder.add(field.getName());
        }
        FIELDS = builder.build();
    }

    private record LocalVarInfo(int type, int index) {
    }
}
