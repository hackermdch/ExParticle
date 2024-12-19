package net.hackermdch.exparticle.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.hackermdch.exparticle.util.GroupUtil;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class GroupArgumentType implements ArgumentType<String> {
    public static GroupArgumentType group() {
        return new GroupArgumentType();
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            var remaining = builder.getRemaining();
            for (var suggest : GroupUtil.getGroups()) if (suggest.startsWith(remaining)) builder.suggest(suggest);
            return builder.buildFuture();
        } else {
            return Suggestions.empty();
        }
    }
}
