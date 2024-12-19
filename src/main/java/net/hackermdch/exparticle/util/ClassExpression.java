package net.hackermdch.exparticle.util;

import com.google.common.collect.Maps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ClassExpression implements IExecutable {
    private static final Map<String, Method> CACHE = Maps.newHashMap();
    private static int index = 0;
    private final ParticleStruct struct = new ParticleStruct();
    private final Method method;

    private ClassExpression(String expression) {
        var lexer = new Lexer(expression);
        var exps = Parser.parseBlock(lexer);
        var codeGen = new CodeGen(exps);
        var clazz = codeGen.codeGenBlock("EXP_" + index++);
        try {
            this.method = clazz.getMethod("invoke", ParticleStruct.class);
        } catch (SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassExpression(Method method) {
        this.method = method;
    }

    public static ClassExpression parse(String expression) {
        if (CACHE.containsKey(expression)) {
            return new ClassExpression(CACHE.get(expression));
        } else {
            var instance = new ClassExpression(expression);
            CACHE.put(expression, instance.method);
            return instance;
        }
    }

    public ParticleStruct getData() {
        return this.struct;
    }

    public int invoke() {
        try {
            return (int) method.invoke(null, this.struct);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            ClientMessageUtil.addChatMessage(e);
            throw new RuntimeException(e);
        }
    }
}
