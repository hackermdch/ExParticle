package net.hackermdch.exparticle.util;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariableUtil {
    public enum Type {
        Undefined, Integer, Double, Quaternion
    }

    public static class Var {
        public final Type type;
        public int intValue;
        public double doubleValue;

        public Var(Type type) {
            this.type = type;
        }
    }

    private static final Map<String, Var> vars = new HashMap<>();

    public static void define(String name, Type type, Object value) {
        var v = new Var(type);
        switch (type) {
            case Undefined -> throw new IllegalArgumentException();
            case Integer -> v.intValue = (int) value;
            case Double -> v.doubleValue = (double) value;
        }
        vars.put(name, v);
    }

    public static void undefine(String name) {
        vars.remove(name);
    }

    public static Type find(String name) {
        if (vars.get(name) instanceof Var v) return v.type;
        return Type.Undefined;
    }

    public static int getInt(String name) {
        if (vars.get(name) instanceof Var v) return v.intValue;
        return 0;
    }

    public static double getDouble(String name) {
        if (vars.get(name) instanceof Var v) return v.doubleValue;
        return 0;
    }

    public static void setInt(String name, int value) {
        if (vars.get(name) instanceof Var v) v.intValue = value;
    }

    public static void setDouble(String name, double value) {
        if (vars.get(name) instanceof Var v) v.doubleValue = value;
    }

    public static String peek(String name) {
        if (vars.get(name) instanceof Var v) {
            return switch (v.type) {
                case Integer -> Integer.toString(v.intValue);
                case Double -> Double.toString(v.doubleValue);
                default -> "null";
            };
        }
        return "null";
    }
}
