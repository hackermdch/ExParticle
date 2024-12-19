package net.hackermdch.exparticle.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.hackermdch.exparticle.ExParticleClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

public class VideoUtil {
    public static final File VIDEO_DIR = new File("./particleVideos");
    private static final ThreadPoolExecutor VIDEO_DECODER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Video Decoder #%d").setDaemon(true).setUncaughtExceptionHandler((thread, throwable) -> ClientMessageUtil.addChatMessage(throwable)).build());

    public static void decoder(String path, Predicate<BufferedImage> consumer) {
        if (!ExParticleClient.hasJavaCV()) {
            var click = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/bytedeco/javacv");
            var link = Component.literal("JavaCV").withStyle(Style.EMPTY.withClickEvent(click).withUnderlined(true).applyFormat(ChatFormatting.BLUE));
            ClientMessageUtil.addChatMessage(Component.translatable("command.video.unavailable", link).withStyle(ChatFormatting.RED));
            return;
        }
        VIDEO_DECODER_THREAD_POOL.execute(() -> {
            try (var grabber = new FFmpegFrameGrabber(new File(VIDEO_DIR, path))) {
                try (var converter = new Java2DFrameConverter()) {
                    grabber.start();
                    var startTime = System.currentTimeMillis();
                    var count = 0;
                    var rate = grabber.getFrameRate();
                    var length = grabber.getLengthInVideoFrames() - 1;
                    while (count < length) {
                        long curTime = System.currentTimeMillis();
                        if (curTime - startTime > (count * 1000) / rate) {
                            var frame = grabber.grab();
                            if (frame != null && frame.image != null) {
                                if (!consumer.test(converter.convert(frame))) {
                                    break;
                                }
                                ++count;
                            }
                        } else Thread.sleep(10L);
                    }
                    grabber.stop();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static {
        if (VIDEO_DIR.exists() && VIDEO_DIR.isFile()) {
            VIDEO_DIR.delete();
        }
        if (!VIDEO_DIR.exists()) {
            VIDEO_DIR.mkdirs();
        }
    }
}
