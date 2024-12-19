package net.hackermdch.exparticle.util;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ImageUtil {
    public static final File IMAGE_DIR = new File("./particleImages");
    private static final Map<File, Double2ObjectOpenHashMap<BufferedImage>> IMAGE_BUF = Maps.newHashMap();

    public static BufferedImage readImage(String path, double scaling, boolean buffered) throws IOException {
        File imageFile = new File(IMAGE_DIR, path);
        BufferedImage resultImage;
        if (buffered) {
            if (IMAGE_BUF.containsKey(imageFile)) {
                var buf = IMAGE_BUF.get(imageFile);
                if (buf.containsKey(scaling)) {
                    resultImage = buf.get(scaling);
                } else {
                    var image = buf.get(1.0);
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int dw = (int) ((double) width * scaling);
                    int dh = (int) ((double) height * scaling);
                    resultImage = new BufferedImage(dw, dh, image.getType());
                    Graphics2D graphics = resultImage.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(image, 0, 0, dw, dh, 0, 0, width, height, null);
                    graphics.dispose();
                    buf.put(scaling, resultImage);
                }
            } else {
                var buf = new Double2ObjectOpenHashMap<BufferedImage>();
                BufferedImage image = ImageIO.read(imageFile);
                if (scaling == 1.0) {
                    resultImage = image;
                    buf.put(1.0, image);
                } else {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int dw = (int) ((double) width * scaling);
                    int dh = (int) ((double) height * scaling);
                    resultImage = new BufferedImage(dw, dh, image.getType());
                    Graphics2D graphics = resultImage.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(image, 0, 0, dw, dh, 0, 0, width, height, null);
                    graphics.dispose();
                    buf.put(1.0F, image);
                    buf.put(scaling, resultImage);
                }
                IMAGE_BUF.put(imageFile, buf);
            }
        } else {
            BufferedImage bi = ImageIO.read(imageFile);
            int width = bi.getWidth();
            int height = bi.getHeight();
            int dw = (int) ((double) width * scaling);
            int dh = (int) ((double) height * scaling);
            resultImage = new BufferedImage(dw, dh, bi.getType());
            Graphics2D g = resultImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(bi, 0, 0, dw, dh, 0, 0, width, height, null);
            g.dispose();
        }

        return resultImage;
    }

    public static String[] getImages() {
        return Arrays.stream(Objects.requireNonNull(IMAGE_DIR.listFiles(File::isFile))).map(File::getName).toArray(String[]::new);
    }

    public static void clear() {
        IMAGE_BUF.clear();
    }

    static {
        if (IMAGE_DIR.exists() && IMAGE_DIR.isFile()) {
            IMAGE_DIR.delete();
        }
        if (!IMAGE_DIR.exists()) {
            IMAGE_DIR.mkdirs();
        }
    }
}
