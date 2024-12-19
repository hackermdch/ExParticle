package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.ImagePayload;
import net.hackermdch.exparticle.util.ImageUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class ImageCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("image")
                .then(Commands.argument("name", ParticleArgument.particle(ctx)
                ).then(Commands.argument("pos", Vec3Argument.vec3()
                ).then(Commands.argument("path", FileArgumentType.file(ImageUtil.IMAGE_DIR)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                0.1, 0, 0, 0, 0, 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("scaling", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 0.1)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                0, 0, 0, 0, 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("xRotate", RotateArgumentType.rotate()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                0, 0, 0, 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("yRotate", RotateArgumentType.rotate()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                0, 0, 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("zRotate", RotateArgumentType.rotate()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                0, 10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("flip", FlipArgumentType.flip()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
                                10.0, null, 0, null, 1.0, null)
                ).then(Commands.argument("dpb", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 10.0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                null, 0, null, 1.0, null)
                ).then(Commands.argument("speed", Speed3ArgumentType.speed3()).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                0, null, 1.0, null)
                ).then(Commands.argument("age", SuggestIntegerArgumentType.integer(-1, Integer.MAX_VALUE, 0)).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                null, 1.0, null)
                ).then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\"", "\"(vx,vy,vz)=((random(),random(),random())-0.5)*t/100\"")).executes(
                        (context) -> execute(
                                context,
                                ParticleArgument.getParticle(context, "name"),
                                Vec3Argument.getVec3(context, "pos"),
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
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
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
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
                                StringArgumentType.getString(context, "path"),
                                DoubleArgumentType.getDouble(context, "scaling"),
                                RotateArgumentType.getRotate(context, "xRotate"),
                                RotateArgumentType.getRotate(context, "yRotate"),
                                RotateArgumentType.getRotate(context, "zRotate"),
                                FlipArgumentType.getFlip(context, "flip"),
                                DoubleArgumentType.getDouble(context, "dpb"),
                                Speed3ArgumentType.getSpeed3(context, "speed"),
                                IntegerArgumentType.getInteger(context, "age"),
                                StringArgumentType.getString(context, "speedExpression"),
                                DoubleArgumentType.getDouble(context, "speedStep"),
                                StringArgumentType.getString(context, "group")))
                ))))))))))))))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, ParticleOptions effect, Vec3 pos, String path, double scaling, int xRotate, int yRotate, int zRotate, int flip, double dpb, Vec3 speed, int age, String speedExpression, double speedStep, String group) {
        if (flip == 2) zRotate += 2;
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new ImagePayload(effect, pos, path, scaling, xRotate, yRotate, zRotate, flip, dpb, speed, age, speedExpression, speedStep, group));
        return 1;
    }
}
