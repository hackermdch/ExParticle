package net.hackermdch.exparticle.command.argument;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class Color4ArgumentType implements ArgumentType<Vector4f> {
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.color4f.incomplete"));
    private static final FloatArgumentType PARSER = FloatArgumentType.floatArg(0.0F, 1.0F);
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0 1", "1 1 1 1", "0.5 0.5 0.5 1", "1 0 0 1", "0 1 0 1", "0 0 1 1", "1 1 1 0.5");

    public static Color4ArgumentType color4() {
        return new Color4ArgumentType();
    }

    public static Vector4f getColor4(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Vector4f.class);
    }

    public Vector4f parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        float red = PARSER.parse(reader);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            float green = PARSER.parse(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                float blue = PARSER.parse(reader);
                if (reader.canRead() && reader.peek() == ' ') {
                    reader.skip();
                    float alpha = PARSER.parse(reader);
                    return new Vector4f(red, green, blue, alpha);
                } else {
                    reader.setCursor(start);
                    throw INCOMPLETE_EXCEPTION.createWithContext(reader);
                }
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
            var remaining = builder.getRemaining();
            if (Strings.isNullOrEmpty(remaining)) {
                builder.suggest("1");
                builder.suggest("1 1");
                builder.suggest("1 1 1");
                builder.suggest("1 1 1 1");
            } else {
                var predicate = Commands.createValidator(this::parse);
                var args = remaining.split(" ");
                if (args.length == 1) {
                    if (predicate.test(args[0] + " 1 1 1")) {
                        builder.suggest(args[0] + " 1");
                        builder.suggest(args[0] + " 1 1");
                        builder.suggest(args[0] + " 1 1 1");
                    }
                } else if (args.length == 2) {
                    if (predicate.test(String.join(" ", args) + " 1 1")) {
                        builder.suggest(String.join(" ", args) + " 1");
                        builder.suggest(String.join(" ", args) + " 1 1");
                    }
                } else if (args.length == 3 && predicate.test(String.join(" ", args) + " 1")) {
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
