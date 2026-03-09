package net.hackermdch.exparticle.util;

import net.hackermdch.exparticle.ExParticle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFunctionUtil {
    private static final Map<String, UserFunction> functions = new HashMap<>();
    private static final Map<String, List<ICacheAble>> handlers = new HashMap<>();

    private static void update(String name) {
        if (handlers.containsKey(name)) handlers.remove(name).forEach(ICacheAble::invalid);
    }

    public static void handle(String name, ICacheAble c) {
        handlers.putIfAbsent(name, new ArrayList<>());
        var list = handlers.get(name);
        list.add(c);
    }

    public static void define(String name, UserFunction function) {
        functions.put(name, function);
        update(name);
    }

    public static void undefine(String name) {
        functions.remove(name);
        update(name);
    }

    public static UserFunction find(String name) {
        var f = functions.get(name);
        try {
            if (f != null) {
                if (f.invalid) f.recompile();
                return f;
            }
        } catch (Throwable e) {
            functions.remove(name);
            ClientMessageUtil.addChatMessage(e);
            ExParticle.LOGGER.error("Recompile function failed: ", e);
        }
        return null;
    }

    public static void list() {
        for (var e : functions.entrySet()) {
            var sb = new StringBuilder();
            for (var a : e.getValue().args) {
                sb.append(",");
                sb.append(a);
            }
            ClientMessageUtil.addChatMessage(Component.literal(String.format("%s(%s) => %s", e.getKey(), sb.substring(1), e.getValue().body)));
        }
    }
}
