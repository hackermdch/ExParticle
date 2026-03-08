package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.SuggestStringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class UserFunctionCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("user-function")
                .then(Commands.literal("define")
                        .then(Commands.argument("name", StringArgumentType.word()).then(Commands.argument("args", SuggestStringArgumentType.argument("\"a,b,c\"")).then(Commands.argument("body", SuggestStringArgumentType.argument("\"(a+b)*c\"")).executes(
                                                (context) -> define(
                                                        context,
                                                        StringArgumentType.getString(context, "name"),
                                                        StringArgumentType.getString(context, "args"),
                                                        StringArgumentType.getString(context, "body"))
                                        ))
                                )
                        ))
                .then(Commands.literal("undefine").then(Commands.argument("name", StringArgumentType.word()).executes(
                        (context) -> {
                            return 1;
                        }
                )))
        );
    }

    private static int define(CommandContext<CommandSourceStack> context, String name, String args, String body) {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
            parent.then(Commands.literal("user-function")
                    .then(Commands.literal("list").executes(
                            (context) -> {
                                return 1;
                            }
                    ))
            );
        }
    }
}
