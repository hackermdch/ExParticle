package net.hackermdch.exparticle.command.particleex;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.util.ExFunctions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.*;


public class FunctionListCommand {
    private static final List<Component> functions;

    static {
        var builder = ImmutableList.<Component>builder();
        var allowTypes = List.of(int.class, double.class, int[][].class, double[][].class, long.class);
        var map = Map.of("arg0", "a", "arg1", "b", "arg2", "c", "arg3", "d", "arg4", "e", "arg5", "f");
        Stream.concat(Arrays.stream(Math.class.getMethods()), Arrays.stream(ExFunctions.class.getMethods())).forEach(m -> {
            if ((m.getModifiers() & (PUBLIC | STATIC)) != (PUBLIC | STATIC) || !allowTypes.contains(m.getReturnType()))
                return;
            var length = m.getParameterTypes().length;
            var sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                var p = m.getParameters()[i];
                var pn = map.getOrDefault(p.getName(), p.getName());
                var pt = p.getType();
                if (!allowTypes.contains(pt)) return;
                var ptn = pt.getSimpleName();
                if (i == length - 1) sb.append(ptn).append(' ').append(pn);
                else sb.append(ptn).append(' ').append(pn).append(", ");
            }
            builder.add(Component.literal(m.getReturnType().getSimpleName() + " " + m.getName() + "(" + sb + ")"));
        });
        functions = builder.build();
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("functions").executes(FunctionListCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        for (var fun : functions) context.getSource().sendSuccess(() -> fun, false);
        return 1;
    }
}
