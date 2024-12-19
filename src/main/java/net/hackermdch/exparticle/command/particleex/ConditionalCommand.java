package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.ConditionalPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector4f;

public class ConditionalCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("conditional")
                .then(Commands.argument("name", ParticleArgument.particle(ctx)
                ).then(Commands.argument("pos", Vec3Argument.vec3()
                ).then(Commands.argument("color", Color4ArgumentType.color4()
                ).then(Commands.argument("speed", Speed3ArgumentType.speed3()
                ).then(Commands.argument("range", Range3ArgumentType.range3()
                ).then(Commands.argument("expression", SuggestStringArgumentType.argument("null", "\"y>0.25|y<-0.25\"", "\"dis>0.5&dis<1\"", "\"s1>0&s1<0.5&s2>0&dis<1\"")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                0.1, 0, null, 1.0, null)
                ).then(Commands.argument("step", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 0.1)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "step"),
                                0, null, 1.0, null)
                ).then(Commands.argument("age", SuggestIntegerArgumentType.integer(-1, Integer.MAX_VALUE, 0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "step"),
                                IntegerArgumentType.getInteger(context, "age"),
                                null, 1.0, null)
                ).then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\"", "\"(vx,vy,vz)=((random(),random(),random())-0.5)*t/100\"")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "step"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                1.0, null)
                ).then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "step"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                DoubleArgumentType.getDouble(context, "speedStep"),
                                null)
                ).then(Commands.argument("group", SuggestStringArgumentType.argument("null")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                Color4ArgumentType.getColor4(context, "color"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                Range3ArgumentType.getRange3(context, "range"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "step"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                DoubleArgumentType.getDouble(context, "speedStep"),
                                StringArgumentType.getString(context, "group"))
                ))))))))))))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, ParticleOptions effect, Vec3 pos, Vector4f color, Vec3 speed, Vec3 range, String expression, double step, int age, String speedExpression, double speedStep, String group) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new ConditionalPayload(effect, pos, color, speed, range, expression, step, age, speedExpression, speedStep, group));
        return 1;
    }
}
