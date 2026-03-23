package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.Speed3ArgumentType;
import net.hackermdch.exparticle.command.argument.SuggestDoubleArgumentType;
import net.hackermdch.exparticle.command.argument.SuggestIntegerArgumentType;
import net.hackermdch.exparticle.command.argument.SuggestStringArgumentType;
import net.hackermdch.exparticle.network.TextPayload;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class TextCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("text")
                .then(Commands.argument("name", ParticleArgument.particle(ctx)
                ).then(Commands.argument("pos", Vec3Argument.vec3()
                ).then(Commands.argument("text", ComponentArgument.textComponent(ctx)
                ).then(Commands.argument("scaling", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 1.0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                "null", 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("expression", SuggestStringArgumentType.argument("null", "\"(x,y,z)=(x,y,z,1)*rotate(PI/4,0,0)\"", "\"(x,y,z)=(x,y,z,1)*scale(2,3,1)*rotate(PI/4,0,0)\"")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("dpb", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 10.0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                null, 0, null, 1.0, null)
                ).then(Commands.argument("speed", Speed3ArgumentType.speed3()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                0, null, 1.0, null)
                ).then(Commands.argument("age", SuggestIntegerArgumentType.integer(-1, Integer.MAX_VALUE, 0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                null, 1.0, null)
                ).then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\"", "\"(vx,vy,vz)=((random(),random(),random())-0.5)*t/100\"")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                1.0, null)
                ).then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                DoubleArgumentType.getDouble(context, "speedStep"),
                                null)
                ).then(Commands.argument("group", SuggestStringArgumentType.argument("null")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                ComponentArgument.getComponent(context, "text"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                StringArgumentType.getString(context, "expression"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                DoubleArgumentType.getDouble(context, "speedStep"),
                                StringArgumentType.getString(context, "group"))
                ))))))))))))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, ParticleOptions effect, Vec3 pos, Component text, double scaling, String expression, double dpb, Vec3 speed, int age, String speedExpression, double speedStep, String group) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new TextPayload(effect, pos, text, scaling, expression, dpb, speed, age, speedExpression, speedStep, group));
        return 1;
    }
}
