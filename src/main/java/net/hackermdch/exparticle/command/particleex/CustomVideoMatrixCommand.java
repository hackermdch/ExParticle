package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.hackermdch.exparticle.command.argument.*;
import net.hackermdch.exparticle.network.CustomVideoMatrixPayload;
import net.hackermdch.exparticle.util.VideoUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CustomVideoMatrixCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext ctx) {
        parent.then(Commands.literal("custom-video-matrix")
                .then(Commands.argument("name", ParticleArgument.particle(ctx))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("path", FileArgumentType.file(VideoUtil.VIDEO_DIR))
                .then(Commands.argument("scaling", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 0.1))
                .then(Commands.argument("matrix", SuggestStringArgumentType.argument("E3", "E4", "\"(1,0,0,0,,0,1,0,0,,0,0,1,-100,,0,0,0,1)\""))
                .then(Commands.argument("dpb", SuggestDoubleArgumentType.doubleArg(0.0, Double.MAX_VALUE, 10.0))
                .executes(execute(false, false, false))
                .then(Commands.argument("attr", SuggestStringArgumentType.argument("\"size=0.75; age=-1\""))
                .executes(execute(true, false, false))
                .then(Commands.argument("speedExpression", SuggestStringArgumentType.argument("null", "\"vy=0.1\""))
                .executes(execute(true, true, false))
                .then(Commands.argument("speedStep", SuggestDoubleArgumentType.doubleArg(Math.ulp(0.0), Double.MAX_VALUE, 1.0))
                .executes(execute(true, true, true))
                .then(Commands.argument("group", SuggestStringArgumentType.argument("null"))
                .executes(execute(true, true, true, true)))))))))))));
    }

    private static Command<CommandSourceStack> execute(boolean... flags) {
        return context -> {
            ParticleOptions effect = ParticleArgument.getParticle(context, "name");
            Vec3 pos = Vec3Argument.getVec3(context, "pos");
            String path = StringArgumentType.getString(context, "path");
            double scaling = DoubleArgumentType.getDouble(context, "scaling");
            String matrixStr = StringArgumentType.getString(context, "matrix");
            double dpb = DoubleArgumentType.getDouble(context, "dpb");

            String attrExpression = flags.length > 0 && flags[0] ? StringArgumentType.getString(context, "attr") : null;
            String speedExpression = flags.length > 1 && flags[1] ? StringArgumentType.getString(context, "speedExpression") : null;
            double speedStep = flags.length > 2 && flags[2] ? DoubleArgumentType.getDouble(context, "speedStep") : 1.0;
            String group = flags.length > 3 && flags[3] ? StringArgumentType.getString(context, "group") : null;

            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(),
                    new CustomVideoMatrixPayload(effect, pos, path, scaling, matrixStr, dpb, attrExpression, speedExpression, speedStep, group));
            return 1;
        };
    }
}