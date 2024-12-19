package net.hackermdch.exparticle.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SuggestIntegerArgumentType implements ArgumentType<Integer> {
    private final IntegerArgumentType parser;
    private final int suggest;

    public SuggestIntegerArgumentType(int min, int max, int suggest) {
        this.parser = IntegerArgumentType.integer(min, max);
        this.suggest = suggest;
    }

    public static SuggestIntegerArgumentType integer() {
        return new SuggestIntegerArgumentType(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    public static SuggestIntegerArgumentType integer(int min) {
        return new SuggestIntegerArgumentType(min, Integer.MAX_VALUE, 0);
    }

    public static SuggestIntegerArgumentType integer(int min, int max) {
        return new SuggestIntegerArgumentType(min, max, 0);
    }

    public static SuggestIntegerArgumentType integer(int min, int max, int suggest) {
        return new SuggestIntegerArgumentType(min, max, suggest);
    }

    public int getMinimum() {
        return parser.getMinimum();
    }

    public int getMaximum() {
        return parser.getMaximum();
    }

    public int getSuggest() {
        return suggest;
    }

    public Integer parse(StringReader reader) throws CommandSyntaxException {
        return parser.parse(reader);
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            return String.valueOf(suggest).startsWith(builder.getRemaining()) ? builder.suggest(String.valueOf(suggest)).buildFuture() : Suggestions.empty();
        } else {
            return Suggestions.empty();
        }
    }

    public Collection<String> getExamples() {
        return this.parser.getExamples();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof SuggestIntegerArgumentType type)) {
            return false;
        } else {
            return parser.equals(type.parser) && suggest == type.suggest;
        }
    }

    public int hashCode() {
        return 31 * parser.hashCode() + suggest;
    }

    public String toString() {
        if (parser.getMinimum() == Integer.MIN_VALUE && parser.getMaximum() == Integer.MAX_VALUE && suggest == 0) {
            return "suggestInteger()";
        } else if (parser.getMaximum() == Integer.MAX_VALUE && suggest == 0) {
            return "suggestInteger(" + parser.getMinimum() + ")";
        } else {
            return suggest == 0 ? "suggestInteger(" + parser.getMinimum() + ", " + parser.getMaximum() + ")" : "suggestInteger(" + parser.getMinimum() + ", " + parser.getMaximum() + ", " + suggest + ")";
        }
    }

    public static class Info implements ArgumentTypeInfo<SuggestIntegerArgumentType, Info.Template> {
        public final class Template implements ArgumentTypeInfo.Template<SuggestIntegerArgumentType> {
            private final int minimum;
            private final int maximum;
            private final int suggest;

            public Template(int minimum, int maximum, int suggest) {
                this.minimum = minimum;
                this.maximum = maximum;
                this.suggest = suggest;
            }

            @Override
            @NotNull
            public SuggestIntegerArgumentType instantiate(@NotNull CommandBuildContext context) {
                return new SuggestIntegerArgumentType(minimum, maximum, suggest);
            }

            @Override
            @NotNull
            public ArgumentTypeInfo<SuggestIntegerArgumentType, ?> type() {
                return Info.this;
            }
        }

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeInt(template.minimum);
            buffer.writeInt(template.maximum);
            buffer.writeInt(template.suggest);
        }

        @Override
        @NotNull
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            return new Template(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("min", template.minimum);
            json.addProperty("max", template.maximum);
            json.addProperty("suggest", template.suggest);
        }

        @Override
        @NotNull
        public Template unpack(SuggestIntegerArgumentType argument) {
            return new Template(argument.getMinimum(), argument.getMaximum(), argument.getSuggest());
        }
    }
}
