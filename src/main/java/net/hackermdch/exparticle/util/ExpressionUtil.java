package net.hackermdch.exparticle.util;

import com.google.common.base.Strings;

public class ExpressionUtil {
    public static IExecutable parse(String expression) {
        if (!Strings.isNullOrEmpty(expression) && !expression.equals("null")) {
            try {
                return ClassExpression.parse(expression);
            } catch (RuntimeException e) {
                ClientMessageUtil.addChatMessage(e);
                throw e;
            }
        } else {
            return null;
        }
    }
}
