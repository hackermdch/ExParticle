package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.network.ClearCachePayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClearCacheCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("clear-cache").executes(ClearCacheCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new ClearCachePayload());
        return 1;
    }
}
