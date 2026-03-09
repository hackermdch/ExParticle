package net.hackermdch.exparticle.util;

import org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;

public class UserFunction implements ICacheAble {
    public enum Type {
        Int, Double, IntMatrix, DoubleMatrix
    }

    private static int index = 0;
    private final String name;
    final String[] args;
    final Type retType;
    final String signature;
    final String body;
    private Method method;
    boolean invalid;

    private UserFunction(String name, String[] args, Type retType, String body) {
        this.name = name;
        this.args = args;
        this.retType = retType;
        this.signature = String.format("(%s)D", "D".repeat(args.length));
        this.body = body;
    }

    @Override
    public void invalid() {
        invalid = true;
        UserFunctionUtil.define(name, this);
    }

    void recompile() {
        var lexer = new Lexer(body);
        var es = Parser.parseBlock(lexer);
        var codeGen = new CodeGen(es);
        try {
            var clazz = codeGen.codeGenFunction("FUNC_" + index++, signature, switch (retType) {
                case Int -> Opcodes.T_INT;
                case Double -> Opcodes.T_DOUBLE;
                case IntMatrix -> CodeGen.T_INTMAT;
                case DoubleMatrix -> CodeGen.T_DOUBLEMAT;
            }, args);
            method = clazz.getMethod("invoke", MethodType.fromMethodDescriptorString(signature, Thread.currentThread().getContextClassLoader()).parameterArray());
            for (var r : codeGen.references()) GlobalVariableUtil.handle(r, this);
            for (var r : codeGen.funcReferences()) UserFunctionUtil.handle(r, this);
        } catch (Throwable e) {
            method = null;
            throw new RuntimeException(e);
        }
    }

    public static UserFunction create(String name, String args, String body) {
        var uf = new UserFunction(name, Arrays.stream(args.split(",")).map(String::trim).toArray(String[]::new), Type.Double, body);
        uf.recompile();
        return uf;
    }

    public Method getMethod() {
        return method;
    }
}
