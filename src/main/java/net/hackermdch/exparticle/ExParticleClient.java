package net.hackermdch.exparticle;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.JarContents;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static net.hackermdch.exparticle.ExParticle.LOGGER;
import static net.hackermdch.exparticle.ExParticle.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ExParticleClient {
    private static boolean hasJavaCV = true;

    public static boolean hasJavaCV() {
        return hasJavaCV;
    }

    @SubscribeEvent
    private static void init(FMLClientSetupEvent event) {
        try {
            var path = Paths.get("./javacv");
            if (Files.exists(path) && !Files.isDirectory(path)) Files.delete(path);
            if (!Files.exists(path)) Files.createDirectory(path);
            var finder = ModuleFinder.of(path);
            var all = finder.findAll().stream().map(ModuleReference::location).map(u -> Path.of(u.orElseThrow())).toList();
            var jars = all.stream().map(JarContents::of).map(SecureJar::from).toList();
            var bootstrap = Launcher.class.getModule().getLayer();
            var bootstrapMl = (ModuleClassLoader) Launcher.class.getClassLoader();
            var config = bootstrap.configuration().resolveAndBind(JarModuleFinder.of(jars.toArray(SecureJar[]::new)), ModuleFinder.ofSystem(), jars.stream().map(SecureJar::name).toList());
            bootstrapMl.setFallbackClassLoader(new ModuleClassLoader("LIBRARIES", config, List.of(bootstrap)));
            MethodHandles.lookup().ensureInitialized(FFmpegFrameGrabber.class);
        } catch (Throwable e) {
            hasJavaCV = false;
            LOGGER.warn("Failed to load JavaCV, video feature has been disabled", e);
        }
    }
}
