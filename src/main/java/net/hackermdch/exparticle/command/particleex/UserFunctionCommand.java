package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.SuggestStringArgumentType;
import net.hackermdch.exparticle.network.UserFunctionPayload;
import net.hackermdch.exparticle.util.UserFunctionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.regex.Pattern;

public class UserFunctionCommand {
    private static final Pattern validator = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

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
                            String name = StringArgumentType.getString(context, "name");
                            PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new UserFunctionPayload(false, name, null, null));
                            return 1;
                        }
                )))
        );
    }

    private static int define(CommandContext<CommandSourceStack> context, String name, String args, String body) {
        if (!validator.matcher(name).matches()) {
            context.getSource().sendFailure(Component.translatable("command.user_function.invalid_id", name));
            return -1;
        }
        for (var a : args.split(",")) {
            if (!validator.matcher(a.trim()).matches()) {
                context.getSource().sendFailure(Component.translatable("command.user_function.invalid_id", a.trim()));
                return -1;
            }
        }
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new UserFunctionPayload(true, name, args, body));
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
            parent.then(Commands.literal("user-function")
                    .then(Commands.literal("list").executes(
                            (context) -> {
                                UserFunctionUtil.list();
                                return 1;
                            }
                    ))
            );
        }
    }
}
