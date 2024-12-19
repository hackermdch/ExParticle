package net.hackermdch.exparticle;

import net.hackermdch.exparticle.command.ParticleExCommand;
import net.hackermdch.exparticle.command.argument.CustomArgumentTypes;
import net.hackermdch.exparticle.network.Networking;
import net.hackermdch.exparticle.util.ParticleUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mod(MOD_ID)
public class ExParticle {
    public static final String MOD_ID = "exparticle";
    public static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("deprecation")
    public ExParticle(IEventBus modBus) {
        try {
            var unsafeF = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeF.setAccessible(true);
            var unsafe = (Unsafe) unsafeF.get(null);
            MethodHandles.lookup().ensureInitialized(MethodHandles.Lookup.class);
            var field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            var lookup = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
            var addOpens = lookup.findVirtual(Module.class, "implAddOpens", MethodType.methodType(void.class, String.class, Module.class));
            var lang = ModuleLayer.boot().findModule("java.base").orElseThrow();
            addOpens.invoke(lang, "java.lang", ExParticle.class.getModule());
            ExParticleConfig.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        Networking.register(modBus);
        CustomArgumentTypes.register(modBus);
        EVENT_BUS.addListener(ExParticle::registerCommands);
        EVENT_BUS.addListener(ParticleUtil::onStartClientTick);
        EVENT_BUS.addListener(ParticleUtil::onEndClientTick);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        ParticleExCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}
