package net.hackermdch.exparticle.command.argument;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class Range3ArgumentType implements ArgumentType<Vec3> {
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.range3d.incomplete"));
    private static final DoubleArgumentType PARSER = DoubleArgumentType.doubleArg(0.0F);
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "1 1 1", "1 0 1");

    public static Range3ArgumentType range3() {
        return new Range3ArgumentType();
    }

    public static Vec3 getRange3(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Vec3.class);
    }

    public Vec3 parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        double vx = PARSER.parse(reader);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            double vy = PARSER.parse(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                double vz = PARSER.parse(reader);
                return new Vec3(vx, vy, vz);
            } else {
                reader.setCursor(start);
                throw INCOMPLETE_EXCEPTION.createWithContext(reader);
            }
        } else {
            reader.setCursor(start);
            throw INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            String remaining = builder.getRemaining();
            if (Strings.isNullOrEmpty(remaining)) {
                builder.suggest("1");
                builder.suggest("1 1");
                builder.suggest("1 1 1");
            } else {
                var predicate = Commands.createValidator(this::parse);
                var args = remaining.split(" ");
                if (args.length == 1) {
                    if (predicate.test(args[0] + " 1 1")) {
                        builder.suggest(args[0] + " 1");
                        builder.suggest(args[0] + " 1 1");
                    }
                } else if (args.length == 2 && predicate.test(String.join(" ", args) + " 1")) {
                    builder.suggest(String.join(" ", args) + " 1");
                }
            }
            return builder.buildFuture();
        } else {
            return Suggestions.empty();
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
