package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class FunctionListCommand {
    private static final String FUNCTIONS = "sin(a), cos(a), tan(a), asin(a), acos(a), atan(a), toRadians(angdeg), toDegrees(angrad), exp(a), log(a), log10(a), sqrt(a), cbrt(a), IEEEremainder(f1, f2), ceil(a), floor(a), rint(a), atan2(y, x), pow(a, b), round(a), random(), addExact(x, y), subtractExact(x, y), multiplyExact(x, y), incrementExact(a), decrementExact(a), negateExact(a), floorDiv(x, y), floorMod(x, y), abs(a), max(a, b), min(a, b), ulp(d), signum(d), sinh(x), cosh(x), tanh(x), hypot(x, y), expm1(x), log1p(x), copySign(magnitude, sign), getExponent(d), nextAfter(start, direction), nextUp(d), nextDown(d), scalb(d, scaleFactor), powerOfTwoD(n)";

    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("functions").executes(FunctionListCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(FUNCTIONS), false);
        return 1;
    }
}
