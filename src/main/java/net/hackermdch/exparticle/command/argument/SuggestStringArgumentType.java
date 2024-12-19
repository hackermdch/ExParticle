package net.hackermdch.exparticle.command.argument;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SuggestStringArgumentType implements ArgumentType<String> {
    private final String[] suggests;

    public static SuggestStringArgumentType argument(String... suggests) {
        return new SuggestStringArgumentType(suggests);
    }

    private SuggestStringArgumentType(String[] suggests) {
        this.suggests = suggests;
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            var remaining = builder.getRemaining();
            for (var suggest : suggests) if (suggest.startsWith(remaining)) builder.suggest(suggest);
            return builder.buildFuture();
        } else {
            return Suggestions.empty();
        }
    }

    public static class Info implements ArgumentTypeInfo<SuggestStringArgumentType, Info.Template> {
        public final class Template implements ArgumentTypeInfo.Template<SuggestStringArgumentType> {
            private final String[] suggests;

            public Template(String[] suggests) {
                this.suggests = suggests;
            }

            @Override
            @NotNull
            public SuggestStringArgumentType instantiate(@NotNull CommandBuildContext context) {
                return new SuggestStringArgumentType(suggests);
            }

            @Override
            @NotNull
            public ArgumentTypeInfo<SuggestStringArgumentType, ?> type() {
                return Info.this;
            }
        }

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeInt(template.suggests.length);
            for (var suggest : template.suggests) buffer.writeUtf(suggest);
        }

        @Override
        @NotNull
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            var suggests = new String[buffer.readInt()];
            for (int i = 0; i < suggests.length; ++i) {
                suggests[i] = buffer.readUtf();
            }
            return new Template(suggests);
        }

        @Override
        public void serializeToJson(Template template, @NotNull JsonObject json) {
            var suggests = new JsonArray();
            for (var suggest : template.suggests) suggests.add(suggest);
            json.add("suggests", suggests);
        }

        @Override
        @NotNull
        public Template unpack(SuggestStringArgumentType argument) {
            return new Template(argument.suggests);
        }
    }
}
