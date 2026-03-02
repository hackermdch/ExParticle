package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.SuggestStringArgumentType;
import net.hackermdch.exparticle.network.GlobalVariablePayload;
import net.hackermdch.exparticle.util.GlobalVariableUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaterniond;

public class GlobalVariableCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("global-variable")
                .then(Commands.literal("define")
                        .then(Commands.argument("type", SuggestStringArgumentType.argument("int", "double", "quat")).then(Commands.argument("name", StringArgumentType.word()).then(Commands.argument("value", StringArgumentType.string()).executes(
                                                (context) -> define(
                                                        context,
                                                        StringArgumentType.getString(context, "type"),
                                                        StringArgumentType.getString(context, "name"),
                                                        StringArgumentType.getString(context, "value"))
                                        ))
                                )
                        ))
                .then(Commands.literal("undefine").then(Commands.argument("name", StringArgumentType.word()).executes(
                        (context) -> {
                            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new GlobalVariablePayload(2, 0, StringArgumentType.getString(context, "name"), null));
                            return 1;
                        }
                )))
        );
    }

    private static int define(CommandContext<CommandSourceStack> context, String type, String name, String value) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new GlobalVariablePayload(1, switch (type) {
            case "int" -> 1;
            case "double" -> 2;
            case "quat" -> 3;
            default -> throw new IllegalArgumentException();
        }, name, switch (type) {
            case "int" -> Integer.parseInt(value);
            case "double" -> Double.parseDouble(value);
            case "quat" -> new Quaterniond();
            default -> throw new IllegalArgumentException();
        }));
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
            parent.then(Commands.literal("global-variable")
                    .then(Commands.literal("peek").then(Commands.argument("name", StringArgumentType.word()).executes(
                            (context) -> {
                                context.getSource().sendSystemMessage(Component.literal(GlobalVariableUtil.peek(StringArgumentType.getString(context, "name"))));
                                return 1;
                            }
                    )))
            );
        }
    }
}
