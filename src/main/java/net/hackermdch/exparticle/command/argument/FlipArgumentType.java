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

public class FlipArgumentType implements ArgumentType<Integer> {
    public static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.flip.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("not", "horizontally", "vertical");

    public static FlipArgumentType flip() {
        return new FlipArgumentType();
    }

    public static int getFlip(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
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
            case "not" -> 0;
            case "horizontally" -> 1;
            case "vertical" -> 2;
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
