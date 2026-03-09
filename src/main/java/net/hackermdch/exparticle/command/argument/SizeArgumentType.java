package net.hackermdch.exparticle.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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

public class SizeArgumentType implements ArgumentType<Double> {
    private static final SimpleCommandExceptionType INVALID_SIZE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.size.invalid"));
    private static final DoubleArgumentType PARSER = DoubleArgumentType.doubleArg(-1.0, Double.MAX_VALUE);
    private static final Collection<String> EXAMPLES = Arrays.asList("-1", "0.5", "1", "2.5");

    public static SizeArgumentType size() {
        return new SizeArgumentType();
    }

    public static double getSize(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Double.class);
    }

    @Override
    public Double parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        double value = PARSER.parse(reader);
        if (value != -1.0 && value < 0.0) {
            reader.setCursor(start);
            throw INVALID_SIZE_EXCEPTION.createWithContext(reader);
        }
        return value == -1.0 ? Double.NaN : value;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            String remaining = builder.getRemaining();
            if (remaining.isEmpty()) {
                builder.suggest("-1");
                builder.suggest("0.5");
                builder.suggest("1");
                builder.suggest("2.5");
            } else {
                if ("-1".startsWith(remaining)) builder.suggest("-1");
                if ("0.5".startsWith(remaining)) builder.suggest("0.5");
                if ("1".startsWith(remaining)) builder.suggest("1");
                if ("2.5".startsWith(remaining)) builder.suggest("2.5");
            }
            return builder.buildFuture();
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}