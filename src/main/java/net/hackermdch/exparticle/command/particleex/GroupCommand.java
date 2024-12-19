package net.hackermdch.exparticle.command.particleex;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.hackermdch.exparticle.command.argument.GroupArgumentType;
import net.hackermdch.exparticle.command.argument.GroupChangeTypeArgumentType;
import net.hackermdch.exparticle.command.argument.SuggestStringArgumentType;
import net.hackermdch.exparticle.network.GroupChangePayload;
import net.hackermdch.exparticle.network.GroupRemovePayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class GroupCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.then(Commands.literal("group")
                .then(Commands.literal("remove")
                        .then(Commands.argument("group", GroupArgumentType.group()).executes(
                                (context) -> removeExecute(
                                        context,
                                        StringArgumentType.getString(context, "group"),
                                        null, null)
                        ).then(Commands.argument("expression", SuggestStringArgumentType.argument("\"age>100\"")).executes(
                                (context) -> removeExecute(
                                        context,
                                        StringArgumentType.getString(context, "group"),
                                        StringArgumentType.getString(context, "expression"),
                                        null)
                        ).then(Commands.argument("pos", Vec3Argument.vec3()).executes(
                                (context) -> removeExecute(
                                        context,
                                        StringArgumentType.getString(context, "group"),
                                        StringArgumentType.getString(context, "expression"),
                                        Vec3Argument.getVec3(context, "pos")))
                        ))))
                .then(Commands.literal("change")
                        .then(Commands.argument("type", GroupChangeTypeArgumentType.type()
                        ).then(Commands.argument("group", GroupArgumentType.group()
                        ).then(Commands.argument("expression", SuggestStringArgumentType.argument("\"vy=0.1\"")).executes(
                                (context) -> changeExecute(
                                        context,
                                        GroupChangeTypeArgumentType.getType(context, "type"),
                                        StringArgumentType.getString(context, "group"),
                                        StringArgumentType.getString(context, "expression"),
                                        null, null)
                        ).then(Commands.argument("conditionalExpression", SuggestStringArgumentType.argument("\"age>100\"")).executes(
                                (context) -> changeExecute(
                                        context,
                                        GroupChangeTypeArgumentType.getType(context, "type"),
                                        StringArgumentType.getString(context, "group"),
                                        StringArgumentType.getString(context, "expression"),
                                        StringArgumentType.getString(context, "conditionalExpression"),
                                        null)
                        ).then(Commands.argument("pos", Vec3Argument.vec3()).executes(
                                (context) -> changeExecute(
                                        context,
                                        GroupChangeTypeArgumentType.getType(context, "type"),
                                        StringArgumentType.getString(context, "group"),
                                        StringArgumentType.getString(context, "expression"),
                                        StringArgumentType.getString(context, "conditionalExpression"),
                                        Vec3Argument.getVec3(context, "pos")))
                        )))))
                )
        );
    }

    private static int removeExecute(CommandContext<CommandSourceStack> context, String group, String expression, Vec3 pos) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new GroupRemovePayload(group, expression, pos));
        return 1;
    }

    private static int changeExecute(CommandContext<CommandSourceStack> context, int type, String group, String expression, String conditionalExpression, Vec3 pos) {
        PacketDistributor.sendToPlayersInDimension(context.getSource().getLevel(), new GroupChangePayload(type, group, expression, conditionalExpression, pos));
        return 1;
    }
}
