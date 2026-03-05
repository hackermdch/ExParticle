package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.TextMatrixPayload;
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

public class TextMatrixCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("text-matrix")
                .then(Commands.argument("name", ParticleArgument.particle(ctx))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("text", ComponentArgument.textComponent(ctx))
                                        .executes(ctx1 -> execute(ctx1,
                                                ParticleArgument.getParticle(ctx1, "name"),
                                                Vec3Argument.getVec3(ctx1, "pos"),
                                                ComponentArgument.getComponent(ctx1, "text"),
                                                1.0, "E3", 10.0, null, 0, null, 1.0, null))
                                        .then(Commands.argument("scaling", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 1.0))
                                                .executes(ctx1 -> execute(ctx1,
                                                        ParticleArgument.getParticle(ctx1, "name"),
                                                        Vec3Argument.getVec3(ctx1, "pos"),
                                                        ComponentArgument.getComponent(ctx1, "text"),
                                                        DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                        "E3", 10.0, null, 0, null, 1.0, null))
                                                .then(Commands.argument("matrix", SuggestStringArgumentType.argument("E3", "E4",
                                                                "\"(1,0,0,0,,0,1,0,0,,0,0,1,-100,,0,0,0,1)\"",
                                                                "\"(0.5,-0.5,0.7071,-0.7071,,0.1464466,0.8535534,0.5,-5,,-0.8535534,-0.1464466,0.5,-5,,0,0,0,1)\""))
                                                        .executes(ctx1 -> execute(ctx1,
                                                                ParticleArgument.getParticle(ctx1, "name"),
                                                                Vec3Argument.getVec3(ctx1, "pos"),
                                                                ComponentArgument.getComponent(ctx1, "text"),
                                                                DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                StringArgumentType.getString(ctx1, "matrix"),
                                                                10.0, null, 0, null, 1.0, null))
                                                        .then(Commands.argument("dpb", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 10.0))
                                                                .executes(ctx1 -> execute(ctx1,
                                                                        ParticleArgument.getParticle(ctx1, "name"),
                                                                        Vec3Argument.getVec3(ctx1, "pos"),
                                                                        ComponentArgument.getComponent(ctx1, "text"),
                                                                        DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                        StringArgumentType.getString(ctx1, "matrix"),
                                                                        DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                        null, 0, null, 1.0, null))
                                                                .then(Commands.argument("speed", Speed3ArgumentType.speed3())
                                                                        .executes(ctx1 -> execute(ctx1,
                                                                                ParticleArgument.getParticle(ctx1, "name"),
                                                                                Vec3Argument.getVec3(ctx1, "pos"),
                                                                                ComponentArgument.getComponent(ctx1, "text"),
                                                                                DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                                StringArgumentType.getString(ctx1, "matrix"),
                                                                                DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                                Speed3ArgumentType.getSpeed3(ctx1, "speed"),
                                                                                0, null, 1.0, null))
                                                                        .then(Commands.argument("age", SuggestIntegerArgumentType.integer(-1, Integer.MAX_VALUE, 0))
                                                                                .executes(ctx1 -> execute(ctx1,
                                                                                        ParticleArgument.getParticle(ctx1, "name"),
                                                                                        Vec3Argument.getVec3(ctx1, "pos"),
                                                                                        ComponentArgument.getComponent(ctx1, "text"),
                                                                                        DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                                        StringArgumentType.getString(ctx1, "matrix"),
                                                                                        DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                                        Speed3ArgumentType.getSpeed3(ctx1, "speed"),
                                                                                        IntegerArgumentType.getInteger(ctx1, "age"),
                                                                                        null, 1.0, null))
                                                                                .then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\"", "\"(vx,vy,vz)=((random(),random(),random())-0.5)*t/100\""))
                                                                                        .executes(ctx1 -> execute(ctx1,
                                                                                                ParticleArgument.getParticle(ctx1, "name"),
                                                                                                Vec3Argument.getVec3(ctx1, "pos"),
                                                                                                ComponentArgument.getComponent(ctx1, "text"),
                                                                                                DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                                                StringArgumentType.getString(ctx1, "matrix"),
                                                                                                DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                                                Speed3ArgumentType.getSpeed3(ctx1, "speed"),
                                                                                                IntegerArgumentType.getInteger(ctx1, "age"),
                                                                                                StringArgumentType.getString(ctx1, "speedExpression"),
                                                                                                1.0, null))
                                                                                        .then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0))
                                                                                                .executes(ctx1 -> execute(ctx1,
                                                                                                        ParticleArgument.getParticle(ctx1, "name"),
                                                                                                        Vec3Argument.getVec3(ctx1, "pos"),
                                                                                                        ComponentArgument.getComponent(ctx1, "text"),
                                                                                                        DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                                                        StringArgumentType.getString(ctx1, "matrix"),
                                                                                                        DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                                                        Speed3ArgumentType.getSpeed3(ctx1, "speed"),
                                                                                                        IntegerArgumentType.getInteger(ctx1, "age"),
                                                                                                        StringArgumentType.getString(ctx1, "speedExpression"),
                                                                                                        DoubleArgumentType.getDouble(ctx1, "speedStep"),
                                                                                                        null))
                                                                                                .then(Commands.argument("group", SuggestStringArgumentType.argument("null"))
                                                                                                        .executes(ctx1 -> execute(ctx1,
                                                                                                                ParticleArgument.getParticle(ctx1, "name"),
                                                                                                                Vec3Argument.getVec3(ctx1, "pos"),
                                                                                                                ComponentArgument.getComponent(ctx1, "text"),
                                                                                                                DoubleArgumentType.getDouble(ctx1, "scaling"),
                                                                                                                StringArgumentType.getString(ctx1, "matrix"),
                                                                                                                DoubleArgumentType.getDouble(ctx1, "dpb"),
                                                                                                                Speed3ArgumentType.getSpeed3(ctx1, "speed"),
                                                                                                                IntegerArgumentType.getInteger(ctx1, "age"),
                                                                                                                StringArgumentType.getString(ctx1, "speedExpression"),
                                                                                                                DoubleArgumentType.getDouble(ctx1, "speedStep"),
                                                                                                                StringArgumentType.getString(ctx1, "group")))
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, ParticleOptions effect, Vec3 pos, Component text,
                               double scaling, String matrixStr, double dpb, Vec3 speed,
                               int age, String speedExpression, double speedStep, String group) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(),
                new TextMatrixPayload(effect, pos, text, scaling, matrixStr, dpb, speed, age, speedExpression, speedStep, group));
        return 1;
    }
}