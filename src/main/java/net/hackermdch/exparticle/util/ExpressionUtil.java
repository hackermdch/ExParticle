package net.hackermdch.exparticle.util;

import com.google.common.base.Strings;
import org.joml.Quaterniond;

import java.util.regex.Pattern;

public class ExpressionUtil {
    private static final Pattern validator = Pattern.compile("^\\(\\s*([^,]+?)\\s*,\\s*([^,]+?)\\s*,\\s*([^,]+?)\\s*,\\s*([^,]+?)\\s*\\)$");

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

    public static Quaterniond toQuaternion(String expression) {
        if (expression != null && !expression.isEmpty()) {
            var matcher = validator.matcher(expression);
            if (matcher.matches())
                return new Quaterniond(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)));
            throw new IllegalArgumentException(expression);
        }
        return new Quaterniond();
    }
}