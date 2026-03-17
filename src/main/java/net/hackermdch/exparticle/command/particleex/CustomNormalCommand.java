package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.CustomNormalPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CustomNormalCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("custom-normal")
                .then(Commands.argument("name", ParticleArgument.particle(ctx))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("attr", SuggestStringArgumentType.argument("\"size=1; cr=1; cg=1; cb=1; alpha=1; light=15; vx=0; vy=0; vz=0; age=100\""))
                .then(Commands.argument("range", Range3ArgumentType.range3())
                .then(Commands.argument("count", SuggestIntegerArgumentType.integer(0, Integer.MAX_VALUE, 1))
                .executes(execute(false, false, false))
                .then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\""))
                .executes(execute(true, false, false))
                .then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0))
                .executes(execute(true, true, false))
                .then(Commands.argument("group", SuggestStringArgumentType.argument("null"))
                .executes(execute(true, true, true)))))))))));
    }

    private static Command<CommandSourceStack> execute(boolean... flags) {
        return context -> {
            ParticleOptions effect = ParticleArgument.getParticle(context, "name");
            Vec3 pos = Vec3Argument.getVec3(context, "pos");
            String attrExpression = StringArgumentType.getString(context, "attr");
            Vec3 range = Range3ArgumentType.getRange3(context, "range");
            int count = IntegerArgumentType.getInteger(context, "count");

            String speedExpression = flags.length > 0 && flags[0] ? StringArgumentType.getString(context, "speedExpression") : null;
            double speedStep = flags.length > 1 && flags[1] ? DoubleArgumentType.getDouble(context, "speedStep") : 1.0;
            String group = flags.length > 2 && flags[2] ? StringArgumentType.getString(context, "group") : null;

            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(),
                    new CustomNormalPayload(effect, pos, attrExpression, range, count, speedExpression, speedStep, group));
            return 1;
        };
    }
}