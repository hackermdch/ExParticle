package net.hackermdch.exparticle.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class GroupChangeTypeArgumentType implements ArgumentType<Integer> {
    public static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.group.change.type.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("parameter", "speedexpression");

    public static GroupChangeTypeArgumentType type() {
        return new GroupChangeTypeArgumentType();
    }

    public static int getType(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, Integer.class);
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            var remaining = builder.getRemaining();
            for (var example : EXAMPLES) if (example.startsWith(remaining)) builder.suggest(example);
            return builder.buildFuture();
        } else {
            return Suggestions.empty();
        }
    }

    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        return switch (reader.readUnquotedString()) {
            case "parameter" -> 0;
            case "speedexpression" -> 1;
            default -> {
                reader.setCursor(start);
                throw INVALID_EXCEPTION.createWithContext(reader);
            }
        };
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
