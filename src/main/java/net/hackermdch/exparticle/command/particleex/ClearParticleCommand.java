package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.network.ClearParticlePayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClearParticleCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("clear-particle").executes(ClearParticleCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new ClearParticlePayload());
        return 1;
    }
}
