package net.hackermdch.exparticle.command.argument;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FileArgumentType implements ArgumentType<String> {
    private final File path;
    private final List<String> files;
    private File dir;

    public static FileArgumentType file() {
        return file("");
    }

    public static FileArgumentType file(String path) {
        return new FileArgumentType(new File(path));
    }

    public static FileArgumentType file(File path) {
        return new FileArgumentType(path);
    }

    public static String getFile(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    private FileArgumentType(File path) {
        this.files = Lists.newArrayList();
        this.dir = new File(".");
        this.path = path;
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            var remaining = builder.getRemaining();
            remaining = remaining.replaceAll("\"", "");
            var file = new File(this.path, remaining);
            if (file.isFile()) {
                return Suggestions.empty();
            } else {
                int index = remaining.lastIndexOf("/");
                var dirName = remaining.substring(0, index + 1);
                var dir = new File(this.path, dirName);
                if (!dir.isDirectory()) {
                    return Suggestions.empty();
                } else {
                    if (!this.dir.equals(dir)) {
                        this.dir = dir;
                        files.clear();
                        files.addAll(Arrays.asList(Objects.requireNonNull(dir.list())));
                    }
                    var remainingFileName = remaining.substring(index + 1);
                    for (var fileName : files) {
                        if (fileName.startsWith(remainingFileName)) {
                            var suggest = dirName + fileName;
                            for (var ch : suggest.toCharArray()) {
                                if (!StringReader.isAllowedInUnquotedString(ch)) {
                                    suggest = "\"" + suggest + "\"";
                                    break;
                                }
                            }
                            builder.suggest(suggest);
                        }
                    }
                    return builder.buildFuture();
                }
            }
        }
    }

    public static class Info implements ArgumentTypeInfo<FileArgumentType, Info.Template> {
        public final class Template implements ArgumentTypeInfo.Template<FileArgumentType> {
            private final File path;

            public Template(File path) {
                this.path = path;
            }

            @Override
            @NotNull
            public FileArgumentType instantiate(@NotNull CommandBuildContext context) {
                return new FileArgumentType(path);
            }

            @Override
            @NotNull
            public ArgumentTypeInfo<FileArgumentType, ?> type() {
                return Info.this;
            }
        }

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeUtf(template.path.toString());
        }

        @Override
        @NotNull
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            return new Template(new File(buffer.readUtf()));
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("path", template.path.getName());
        }

        @Override
        @NotNull
        public Template unpack(FileArgumentType argument) {
            return new Template(argument.path);
        }
    }
}
