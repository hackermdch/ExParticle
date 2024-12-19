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

public class RotateArgumentType implements ArgumentType<Integer> {
    public static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.rotate.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("0", "90", "180", "270");

    public static RotateArgumentType rotate() {
        return new RotateArgumentType();
    }

    public static int getRotate(CommandContext<CommandSourceStack> context, String name) {
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
        int deg = reader.readInt();
        if (deg % 90 != 0) {
            reader.setCursor(start);
            throw INVALID_EXCEPTION.createWithContext(reader);
        } else {
            return deg / 90;
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
