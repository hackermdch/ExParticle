package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.CustomConditionalPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CustomConditionalCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("custom-conditional")
                .then(Commands.argument("name", ParticleArgument.particle(ctx))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("attr", SuggestStringArgumentType.argument("null", "\"vx=1; friction=0.8\"", "\"vx=1; friction=-0.5\""))
                .then(Commands.argument("range", Range3ArgumentType.range3())
                .then(Commands.argument("condition", SuggestStringArgumentType.argument("null", "\"y>0.25|y<-0.25\"", "\"dis>0.5&dis<1\"", "\"s1>0&s1<0.5&s2>0&dis<1\""))
                .executes(execute(false, false, false))
                .then(Commands.argument("step", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 0.1))
                .executes(execute(true, false, false))
                .then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"(vx,vy,vz)=(x,y,z,1)*(rotateDeg(0,10,0)-identity(4))\""))
                .executes(execute(true, true, false))
                .then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0))
                .executes(execute(true, true, true))
                .then(Commands.argument("group", SuggestStringArgumentType.argument("null"))
                .executes(execute(true, true, true, true))))))))))));
    }

    private static Command<CommandSourceStack> execute(boolean... flags) {
        return context -> {
            ParticleOptions effect = ParticleArgument.getParticle(context, "name");
            Vec3 pos = Vec3Argument.getVec3(context, "pos");
            String attrExpression = StringArgumentType.getString(context, "attr");
            Vec3 range = Range3ArgumentType.getRange3(context, "range");
            String expression = StringArgumentType.getString(context, "condition");

            double step = flags.length > 0 && flags[0] ? DoubleArgumentType.getDouble(context, "step") : 0.1;
            String speedExpression = flags.length > 1 && flags[1] ? StringArgumentType.getString(context, "speedExpression") : null;
            double speedStep = flags.length > 2 && flags[2] ? DoubleArgumentType.getDouble(context, "speedStep") : 1.0;
            String group = flags.length > 3 && flags[3] ? StringArgumentType.getString(context, "group") : null;

            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(),
                    new CustomConditionalPayload(effect, pos, attrExpression, range, expression, step, speedExpression, speedStep, group));
            return 1;
        };
    }
}