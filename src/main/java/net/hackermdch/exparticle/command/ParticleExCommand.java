package net.hackermdch.exparticle.command;

import com.mojang.brigadier.CommandDispatcher;
import net.hackermdch.exparticle.command.particleex.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ParticleExCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        var root = Commands.literal("particlex").requires((source) -> source.hasPermission(2));
        FunctionListCommand.register(root);
        ClearParticleCommand.register(root);
        ClearCacheCommand.register(root);
        NormalCommand.register(root, context);
        ConditionalCommand.register(root, context);
        ParameterCommand.register(root, context);
        ImageCommand.register(root, context);
        ImageMatrixCommand.register(root, context);
        VideoCommand.register(root, context);
        VideoMatrixCommand.register(root, context);
        GroupCommand.register(root);
        dispatcher.register(root);
    }
}
