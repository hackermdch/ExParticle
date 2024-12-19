package net.hackermdch.exparticle.command.argument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class CustomArgumentTypes {
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARG_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MOD_ID);
    private static final Supplier<ArgumentTypeInfo<Color4ArgumentType, ?>> COLOR_RGB_A = ARG_TYPES.register("color_rgb_a", () -> SingletonArgumentInfo.contextFree(Color4ArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<FileArgumentType, ?>> FILE = ARG_TYPES.register("file", FileArgumentType.Info::new);
    private static final Supplier<ArgumentTypeInfo<FlipArgumentType, ?>> FLIP = ARG_TYPES.register("flip", () -> SingletonArgumentInfo.contextFree(FlipArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<GroupArgumentType, ?>> GROUP = ARG_TYPES.register("group", () -> SingletonArgumentInfo.contextFree(GroupArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<GroupChangeTypeArgumentType, ?>> GROUP_CHANGE = ARG_TYPES.register("group_change", () -> SingletonArgumentInfo.contextFree(GroupChangeTypeArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<Range3ArgumentType, ?>> RANGE3F = ARG_TYPES.register("range3f", () -> SingletonArgumentInfo.contextFree(Range3ArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<RotateArgumentType, ?>> ROTATE = ARG_TYPES.register("rotate", () -> SingletonArgumentInfo.contextFree(RotateArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<Speed3ArgumentType, ?>> SPEED3F = ARG_TYPES.register("speed3f", () -> SingletonArgumentInfo.contextFree(Speed3ArgumentType::new));
    private static final Supplier<ArgumentTypeInfo<SuggestDoubleArgumentType, ?>> SUGGEST_DOUBLE = ARG_TYPES.register("suggest_double", SuggestDoubleArgumentType.Info::new);
    private static final Supplier<ArgumentTypeInfo<SuggestIntegerArgumentType, ?>> SUGGEST_INT = ARG_TYPES.register("suggest_int", SuggestIntegerArgumentType.Info::new);
    private static final Supplier<ArgumentTypeInfo<SuggestStringArgumentType, ?>> SUGGEST_STRING = ARG_TYPES.register("suggest_string", SuggestStringArgumentType.Info::new);

    private static void register() {
        ArgumentTypeInfos.registerByClass(Color4ArgumentType.class, COLOR_RGB_A.get());
        ArgumentTypeInfos.registerByClass(FileArgumentType.class, FILE.get());
        ArgumentTypeInfos.registerByClass(FlipArgumentType.class, FLIP.get());
        ArgumentTypeInfos.registerByClass(GroupArgumentType.class, GROUP.get());
        ArgumentTypeInfos.registerByClass(GroupChangeTypeArgumentType.class, GROUP_CHANGE.get());
        ArgumentTypeInfos.registerByClass(Range3ArgumentType.class, RANGE3F.get());
        ArgumentTypeInfos.registerByClass(RotateArgumentType.class, ROTATE.get());
        ArgumentTypeInfos.registerByClass(Speed3ArgumentType.class, SPEED3F.get());
        ArgumentTypeInfos.registerByClass(SuggestDoubleArgumentType.class, SUGGEST_DOUBLE.get());
        ArgumentTypeInfos.registerByClass(SuggestIntegerArgumentType.class, SUGGEST_INT.get());
        ArgumentTypeInfos.registerByClass(SuggestStringArgumentType.class, SUGGEST_STRING.get());
    }

    public static void register(IEventBus bus) {
        ARG_TYPES.register(bus);
        bus.addListener((FMLCommonSetupEvent event) -> register());
    }
}
