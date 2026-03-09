package net.hackermdch.exparticle.util;

import com.google.common.collect.Queues;
import net.hackermdch.exparticle.ExParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.function.Predicate;

public class ParticleUtil {
    private static final Queue<TimerTask> TICK_START_TASKS = Queues.newArrayDeque();
    private static final Queue<TimerTask> TICK_END_TASKS = Queues.newArrayDeque();
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static Particle spawnParticle(ParticleOptions effect, double x, double y, double z, double cx, double cy, double cz, double size, float red, float green, float blue, float alpha, double light, double vx, double vy, double vz, int age, String expression, double step, String group) {
        try {
            var particle = CLIENT.particleEngine.createParticle(effect, x, y, z, vx, vy, vz);
            if (particle != null) {
                particle.setManaged(true);
                particle.setCustomSize(size);
                particle.setColor(red, green, blue);
                particle.alpha = alpha;
                particle.setCustomLight(light);
                if (vx == (double) 0.0F && vy == (double) 0.0F && vz == (double) 0.0F) {
                    particle.setStop(true);
                } else {
                    particle.setStop(false);
                    particle.xd = vx;
                    particle.yd = vy;
                    particle.zd = vz;
                }
                particle.setCenterX(cx);
                particle.setCenterY(cy);
                particle.setCenterZ(cz);
                if (age > 0) {
                    particle.setLifetime(age);
                } else if (age == -1) {
                    particle.setLifetime(Integer.MAX_VALUE);
                }
                if (expression != null && !expression.equals("null")) {
                    particle.setExe(ExpressionUtil.parse(expression));
                    particle.setStep(step);
                    particle.setCustomMove(true);
                }
                GroupUtil.add(group, particle);
            }
            return particle;
        } catch (RuntimeException e) {
            ClientMessageUtil.addChatMessage(e);
            return null;
        }
    }

    public static void spawnTickParticle(ParticleOptions effect, double x, double y, double z, double size, float red, float green, float blue, float alpha, double light, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar) {
        new TickParticleTask(effect, x, y, z, size, red, green, blue, alpha, light, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar, false).run();
    }

    public static void spawnTickParticle(ParticleOptions effect, double x, double y, double z, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar) {
        new TickParticleTask(effect, x, y, z, -1.0, 1.0F, 1.0F, 1.0F, 1.0F, -1.0, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar, true).run();
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnImageParticle(effect, x, y, z, path, scaling, size, xRotate, yRotate, zRotate, flip, null, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnImageParticle(effect, x, y, z, path, scaling, size, 0, 0, 0, false, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        try {
            var image = ImageUtil.readImage(path, scaling, true);
            int rows = image.getHeight();
            int cols = image.getWidth();
            var rotateFlipMat = getRotateFlipMat(xRotate, yRotate, zRotate, flip, rows, cols);
            for (int row = 0; row < rows; ++row) {
                for (int col = 0; col < cols; ++col) {
                    int pixel = image.getRGB(col, row);
                    float alpha = (float) ((pixel & 0xff000000) >>> 24) / 255.0F;
                    float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                    float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                    float blue = (float) (pixel & 0xff) / 255.0F;
                    double[][] pos = MatrixUtil.matDiv(MatrixUtil.matMul(rotateFlipMat, new int[][]{{col}, {row}, {0}, {1}}), dpb);
                    if (matrix != null) {
                        pos = MatrixUtil.matMul(matrix, pos);
                    }
                    double dx = pos[0][0];
                    double dy = pos[1][0];
                    double dz = pos[2][0];
                    if (alpha != 0.0F) {
                        spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, size, red, green, blue, alpha, Double.NaN, vx, vy, vz, age, speedExpression, speedStep, group);
                    }
                }
            }
        } catch (IOException e) {
            ClientMessageUtil.addChatMessage(e);
        }
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnVideoParticle(effect, x, y, z, path, scaling, size, xRotate, yRotate, zRotate, flip, null, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnVideoParticle(effect, x, y, z, path, scaling, size, 0, 0, 0, false, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        VideoUtil.decoder(path, new VideoConsumer(effect, x, y, z, scaling, size, xRotate, yRotate, zRotate, flip, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group));
    }

    private static int[][] getRotateFlipMat(int xRotate, int yRotate, int zRotate, boolean flip, int rows, int cols) {
        var flipMat = new int[][]{{flip ? -1 : 1, 0, 0, flip ? cols - 1 : 0}, {0, -1, 0, rows - 1}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        var zMat = new int[][]{{dCos(zRotate), -dSin(zRotate), 0, xMove(zRotate, rows, cols)}, {dSin(zRotate), dCos(zRotate), 0, yMove(zRotate, rows, cols)}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        var yMat = new int[][]{{dCos(yRotate), 0, dSin(yRotate), 0}, {0, 1, 0, 0}, {-dSin(yRotate), 0, dCos(yRotate), 0}, {0, 0, 0, 1}};
        var xMat = new int[][]{{1, 0, 0, 0}, {0, dCos(xRotate), dSin(xRotate), 0}, {0, -dSin(xRotate), dCos(xRotate), 0}, {0, 0, 0, 1}};
        return MatrixUtil.matMul(xMat, MatrixUtil.matMul(yMat, MatrixUtil.matMul(zMat, flipMat)));
    }

    private static int dSin(int n) {
        return switch (n % 4) {
            case 0, 2 -> 0;
            case 1 -> 1;
            case 3 -> -1;
            default -> n;
        };
    }

    private static int dCos(int n) {
        return switch (n % 4) {
            case 0 -> 1;
            case 1, 3 -> 0;
            case 2 -> -1;
            default -> n;
        };
    }

    private static int xMove(int rotate, int rows, int cols) {
        return switch (rotate % 4) {
            case 0, 3 -> 0;
            case 1 -> rows - 1;
            case 2 -> cols - 1;
            default -> rotate;
        };
    }

    private static int yMove(int rotate, int rows, int cols) {
        return switch (rotate % 4) {
            case 0, 1 -> 0;
            case 2 -> rows - 1;
            case 3 -> cols - 1;
            default -> rotate;
        };
    }

    private static void addTask(TimerTask task, boolean start) {
        if (start) {
            synchronized (TICK_START_TASKS) {
                TICK_START_TASKS.add(task);
            }
        } else {
            synchronized (TICK_END_TASKS) {
                TICK_END_TASKS.add(task);
            }
        }
    }

    public static void onStartClientTick(ClientTickEvent.Pre ignore) {
        synchronized (TICK_START_TASKS) {
            while (!TICK_START_TASKS.isEmpty()) {
                TICK_START_TASKS.poll().run();
            }
        }
    }

    public static void onEndClientTick(ClientTickEvent.Post ignore) {
        synchronized (TICK_END_TASKS) {
            while (!TICK_END_TASKS.isEmpty()) {
                TICK_END_TASKS.poll().run();
            }
        }
    }

    // 文本粒子生成方法
    public static void spawnTextParticle(ParticleOptions effect, double x, double y, double z, Component text, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnTextParticle(effect, x, y, z, text, scaling, size, xRotate, yRotate, zRotate, flip, null, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnTextParticle(ParticleOptions effect, double x, double y, double z, Component text, double scaling, double size, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnTextParticle(effect, x, y, z, text, scaling, size, 0, 0, 0, false, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    private static void spawnTextParticle(ParticleOptions effect, double x, double y, double z, Component text, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        try {
            BufferedImage image = renderComponentToImage(text, scaling);
            int rows = image.getHeight();
            int cols = image.getWidth();
            var rotateFlipMat = ParticleUtil.getRotateFlipMat(xRotate, yRotate, zRotate, flip, rows, cols);
            for (int row = 0; row < rows; ++row) {
                for (int col = 0; col < cols; ++col) {
                    int pixel = image.getRGB(col, row);
                    int a = (pixel >> 24) & 0xFF;
                    if (a == 0) continue;
                    float red = ((pixel >> 16) & 0xFF) / 255.0F;
                    float green = ((pixel >> 8) & 0xFF) / 255.0F;
                    float blue = (pixel & 0xFF) / 255.0F;
                    float alpha = a / 255.0F;
                    double[][] pos = MatrixUtil.matDiv(MatrixUtil.matMul(rotateFlipMat, new int[][]{{col}, {row}, {0}, {1}}), dpb);
                    if (matrix != null) {
                        pos = MatrixUtil.matMul(matrix, pos);
                    }
                    double dx = pos[0][0];
                    double dy = pos[1][0];
                    double dz = pos[2][0];
                    spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, size, red, green, blue, alpha,Double.NaN, vx, vy, vz, age, speedExpression, speedStep, group);
                }
            }
        } catch (Exception e) {
            ClientMessageUtil.addChatMessage(e);
        }
    }

    // 将文本组件渲染为 BufferedImage（使用 AWT，样式映射）
    private static BufferedImage renderComponentToImage(Component component, double scaling) {
        // 实际使用的缩放因子
        double textScale = scaling * 2.5; // 用于字体大小的缩放
        double lineScale = scaling;       // 用于线条宽度的缩放

        List<StyledString> fragments = new ArrayList<>();
        collectFragments(component, Style.EMPTY, fragments);

        int totalWidth = 0;
        int lineHeight = 0;
        for (StyledString frag : fragments) {
            Font awtFont = createAwtFont(frag.style, textScale);
            java.awt.font.FontRenderContext awtFrc = new java.awt.font.FontRenderContext(null, true, true);
            Rectangle2D bounds = awtFont.getStringBounds(frag.text, awtFrc);
            frag.width = (int) Math.ceil(bounds.getWidth());
            frag.height = (int) Math.ceil(bounds.getHeight());
            totalWidth += frag.width;
            lineHeight = Math.max(lineHeight, frag.height);
        }

        if (totalWidth == 0) totalWidth = 1;
        if (lineHeight == 0) lineHeight = 1;

        BufferedImage image = new BufferedImage(totalWidth, lineHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setBackground(new Color(0, true));
        g2d.clearRect(0, 0, totalWidth, lineHeight);

        // 设置线条宽度（基于 lineScale）
        float lineWidth = (float) Math.max(1.0, lineScale);
        g2d.setStroke(new BasicStroke(lineWidth));

        int x = 0;
        for (StyledString frag : fragments) {
            Font awtFont = createAwtFont(frag.style, textScale);
            g2d.setFont(awtFont);
            Color awtColor = getAwtColor(frag.style.getColor());
            g2d.setColor(awtColor);

            int y = g2d.getFontMetrics().getAscent();
            g2d.drawString(frag.text, x, y);

            // 下划线
            if (frag.style.isUnderlined()) {
                int underlineY = y + 2 + (int)(lineWidth / 2);
                g2d.drawLine(x, underlineY, x + frag.width - 1, underlineY);
            }

            // 删除线
            if (frag.style.isStrikethrough()) {
                int strikeY = y - (int)(g2d.getFontMetrics().getAscent() * 0.3);
                g2d.drawLine(x, strikeY, x + frag.width - 1, strikeY);
            }

            x += frag.width;
        }
        g2d.dispose();
        return image;
    }

    // 辅助类：带样式的文本片段
    private static class StyledString {
        String text;
        Style style;
        int width, height;
        StyledString(String text, Style style) { this.text = text; this.style = style; }
    }

    // 递归收集片段
    private static void collectFragments(Component comp, Style parentStyle, List<StyledString> out) {
        Style current = parentStyle.applyTo(comp.getStyle());
        if (comp.getContents() instanceof PlainTextContents.LiteralContents(String text)) {
            out.add(new StyledString(text, current));
        }
        for (Component sibling : comp.getSiblings()) {
            collectFragments(sibling, current, out);
        }
    }

    // 根据 Style 创建 AWT Font
    private static Font createAwtFont(Style style, double textScale) {
        int awtStyle = Font.PLAIN;
        if (style.isBold()) awtStyle |= Font.BOLD;
        if (style.isItalic()) awtStyle |= Font.ITALIC;
        int fontSize = (int) Math.max(1, 12 * textScale);
        return new Font(Font.SANS_SERIF, awtStyle, fontSize);
    }

    // 获取 AWT Color
    private static Color getAwtColor(TextColor color) {
        if (color == null) return Color.WHITE;
        return new Color(color.getValue());
    }

    private static class TickParticleTask extends TimerTask {
        private final ParticleOptions particleType;
        private final double x;
        private final double y;
        private final double z;
        private final double size;
        private float red;
        private float green;
        private float blue;
        private float alpha;
        private final double light;
        private final double vx;
        private final double vy;
        private final double vz;
        private final IExecutable exe;
        private final double step;
        private final int cpt;
        private double t;
        private final double end;
        private final int age;
        private final String speedExpression;
        private final double speedStep;
        private final String group;
        private final boolean polar;
        private final boolean rgba;

        public TickParticleTask(ParticleOptions particleType, double x, double y, double z, double size, float red, float green, float blue, float alpha, double light, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar, boolean rgba) {
            this.polar = polar;
            this.rgba = rgba;
            this.particleType = particleType;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.light = light;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.t = begin;
            this.end = end;
            this.exe = ExpressionUtil.parse(expression);
            this.step = step;
            this.cpt = cpt;
            this.age = age;
            this.speedExpression = speedExpression;
            this.speedStep = speedStep;
            this.group = group;
            if (!rgba) {
                this.red = red;
                this.green = green;
                this.blue = blue;
                this.alpha = alpha;
            }
        }

        public void run() {
            var data = exe.getData();
            for (int i = 0; i < cpt && t <= end; t += step) {
                data.t = t;
                exe.invoke();
                double dx;
                double dy;
                double dz;
                if (polar) {
                    dx = data.dis * Math.cos(data.s2) * Math.cos(data.s1);
                    dy = data.dis * Math.sin(data.s2);
                    dz = data.dis * Math.cos(data.s2) * Math.sin(data.s1);
                } else {
                    dx = data.x;
                    dy = data.y;
                    dz = data.z;
                }
                if (rgba) {
                    ParticleUtil.spawnParticle(particleType, x + dx, y + dy, z + dz, x, y, z, data.size, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, data.light, vx, vy, vz, age, speedExpression, speedStep, group);
                } else {
                    ParticleUtil.spawnParticle(particleType, x + dx, y + dy, z + dz, x, y, z, size, red, green, blue, alpha, light, vx, vy, vz, age, speedExpression, speedStep, group);
                }
                ++i;
            }
            if (t <= end) ParticleUtil.addTask(new TickEndTask(this), false);
        }
    }

    private static class TickEndTask extends TimerTask {
        private final TimerTask nextTask;

        public TickEndTask(TimerTask nextTask) {
            this.nextTask = nextTask;
        }

        public void run() {
            ParticleUtil.addTask(this.nextTask, true);
        }
    }

    private static class VideoConsumer implements Predicate<BufferedImage> {
        private final ParticleOptions effect;
        private final double x;
        private final double y;
        private final double z;
        private final double scaling;
        private final double size;
        private final int xRotate;
        private final int yRotate;
        private final int zRotate;
        private final boolean flip;
        private final double[][] matrix;
        private final double dpb;
        private final double vx;
        private final double vy;
        private final double vz;
        private final int age;
        private final String speedExpression;
        private final double speedStep;
        private final String group;
        private int init;
        private Particle[][] particles;

        private VideoConsumer(ParticleOptions effect, double x, double y, double z, double scaling, double size, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
            this.effect = effect;
            this.x = x;
            this.y = y;
            this.z = z;
            this.scaling = scaling;
            this.size = size;
            this.xRotate = xRotate;
            this.yRotate = yRotate;
            this.zRotate = zRotate;
            this.flip = flip;
            this.matrix = matrix;
            this.dpb = dpb;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.age = age;
            this.speedExpression = speedExpression;
            this.speedStep = speedStep;
            this.group = group;
        }

        public boolean test(BufferedImage image) {
            int width = image.getWidth();
            int height = image.getHeight();
            int dw = (int) ((double) width * this.scaling);
            int dh = (int) ((double) height * this.scaling);
            var result = new BufferedImage(dw, dh, image.getType());
            var graphics = result.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, dw, dh, 0, 0, width, height, null);
            graphics.dispose();
            var rotateFlipMat = ParticleUtil.getRotateFlipMat(this.xRotate, this.yRotate, this.zRotate, this.flip, dh, dw);
            if (init == 0) {
                init = 1;
                particles = new Particle[dh][dw];
                ParticleUtil.CLIENT.execute(() -> {
                    for (int row = 0; row < dh; ++row) {
                        for (int col = 0; col < dw; ++col) {
                            int pixel = result.getRGB(col, row);
                            float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                            float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                            float blue = (float) (pixel & 0xff) / 255.0F;
                            var pos = MatrixUtil.matDiv(MatrixUtil.matMul(rotateFlipMat, new int[][]{{col}, {row}, {0}, {1}}), dpb);
                            if (matrix != null) pos = MatrixUtil.matMul(matrix, pos);
                            double dx = pos[0][0];
                            double dy = pos[1][0];
                            double dz = pos[2][0];
                            particles[row][col] = ParticleUtil.spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, size, red, green, blue, 1.0F, Double.NaN, vx, vy, vz, age, speedExpression, speedStep, group);
                        }
                    }
                    init = 2;
                });
                return true;
            } else {
                while (init != 2) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        ExParticle.LOGGER.error(e.getMessage(), e);
                    }
                }
                var alive = false;
                for (int row = 0; row < dh; ++row) {
                    for (int col = 0; col < dw; ++col) {
                        if (particles[row][col].isAlive()) {
                            int pixel = result.getRGB(col, row);
                            float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                            float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                            float blue = (float) (pixel & 0xff) / 255.0F;
                            particles[row][col].setColor(red, green, blue);
                            alive = true;
                        }
                    }
                }
                return alive;
            }
        }
    }
}