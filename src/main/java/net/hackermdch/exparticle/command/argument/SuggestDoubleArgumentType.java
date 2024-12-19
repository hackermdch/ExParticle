package net.hackermdch.exparticle.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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

public class SuggestDoubleArgumentType implements ArgumentType<Double> {
    private final DoubleArgumentType parser;
    private final double suggest;

    public static SuggestDoubleArgumentType doubleArg() {
        return new SuggestDoubleArgumentType(Double.MIN_VALUE, Double.MAX_VALUE, (double) 0.0F);
    }

    public static SuggestDoubleArgumentType doubleArg(double min) {
        return new SuggestDoubleArgumentType(min, Double.MAX_VALUE, (double) 0.0F);
    }

    public static SuggestDoubleArgumentType doubleArg(double min, double max) {
        return new SuggestDoubleArgumentType(min, max, (double) 0.0F);
    }

    public static SuggestDoubleArgumentType doubleArg(double min, double max, double suggest) {
        return new SuggestDoubleArgumentType(min, max, suggest);
    }

    public SuggestDoubleArgumentType(double min, double max, double suggest) {
        this.parser = DoubleArgumentType.doubleArg(min, max);
        this.suggest = suggest;
    }

    public double getMinimum() {
        return parser.getMinimum();
    }

    public double getMaximum() {
        return parser.getMaximum();
    }

    public double getSuggest() {
        return suggest;
    }

    public Double parse(StringReader reader) throws CommandSyntaxException {
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
        } else if (!(other instanceof SuggestDoubleArgumentType type)) {
            return false;
        } else {
            return parser.equals(type.parser) && suggest == type.suggest;
        }
    }

    public int hashCode() {
        return (int) ((double) (31 * parser.hashCode()) + suggest);
    }

    public String toString() {
        if (parser.getMinimum() == Double.MIN_VALUE && parser.getMaximum() == Double.MAX_VALUE && suggest == 0.0) {
            return "suggestDouble()";
        } else if (parser.getMaximum() == Double.MAX_VALUE && suggest == 0.0) {
            return "suggestDouble(" + parser.getMinimum() + ")";
        } else {
            return suggest == 0.0 ? "suggestDouble(" + parser.getMinimum() + ", " + parser.getMaximum() + ")" : "suggestDouble(" + parser.getMinimum() + ", " + parser.getMaximum() + ", " + suggest + ")";
        }
    }

    public static class Info implements ArgumentTypeInfo<SuggestDoubleArgumentType, Info.Template> {
        public final class Template implements ArgumentTypeInfo.Template<SuggestDoubleArgumentType> {
            private final double minimum;
            private final double maximum;
            private final double suggest;

            public Template(double minimum, double maximum, double suggest) {
                this.minimum = minimum;
                this.maximum = maximum;
                this.suggest = suggest;
            }

            @Override
            @NotNull
            public SuggestDoubleArgumentType instantiate(@NotNull CommandBuildContext context) {
                return new SuggestDoubleArgumentType(minimum, maximum, suggest);
            }

            @Override
            @NotNull
            public ArgumentTypeInfo<SuggestDoubleArgumentType, ?> type() {
                return Info.this;
            }
        }

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeDouble(template.minimum);
            buffer.writeDouble(template.maximum);
            buffer.writeDouble(template.suggest);
        }

        @Override
        @NotNull
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            return new Template(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("min", template.minimum);
            json.addProperty("max", template.maximum);
            json.addProperty("suggest", template.suggest);
        }

        @Override
        @NotNull
        public Template unpack(SuggestDoubleArgumentType argument) {
            return new Template(argument.getMinimum(), argument.getMaximum(), argument.getSuggest());
        }
    }
}
