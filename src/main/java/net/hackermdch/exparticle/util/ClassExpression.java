package net.hackermdch.exparticle.util;

import com.google.common.collect.Maps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

public class ClassExpression implements IExecutable {
    private static final Map<String, ClassExpression> CACHE = Maps.newHashMap();
    private static int index = 0;
    private final ParticleStruct struct = new ParticleStruct();
    private final MethodHandle method;
    private boolean invalid;

    private ClassExpression(String expression) {
        var lexer = new Lexer(expression);
        var es = Parser.parseBlock(lexer);
        var codeGen = new CodeGen(es);
        try {
            var clazz = codeGen.codeGenBlock("EXP_" + index++);
            method = MethodHandles.lookup().findStatic(clazz, "invoke", MethodType.methodType(int.class, ParticleStruct.class));
            for (var r : codeGen.references()) GlobalVariableUtil.handle(r, this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private ClassExpression(ClassExpression other) {
        method = other.method;
        invalid = other.invalid;
    }

    public void invalid() {
        invalid = true;
    }

    private static ClassExpression create(String expression) {
        var instance = new ClassExpression(expression);
        CACHE.put(expression, instance);
        return instance;
    }

    public static ClassExpression parse(String expression) {
        if (CACHE.containsKey(expression)) {
            var c = CACHE.get(expression);
            if (c.invalid) return create(expression);
            return new ClassExpression(c);
        } else return create(expression);
    }

    public ParticleStruct getData() {
        return struct;
    }

    public int invoke() {
        try {
            return (int) method.invokeExact(struct);
        } catch (Throwable e) {
            ClientMessageUtil.addChatMessage(e);
            throw new RuntimeException(e);
        }
    }
}
