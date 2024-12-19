package net.hackermdch.exparticle.util;

import com.google.common.collect.Queues;
import net.hackermdch.exparticle.ExParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Queue;
import java.util.TimerTask;
import java.util.function.Predicate;

public class ParticleUtil {
    private static final Queue<TimerTask> TICK_START_TASKS = Queues.newArrayDeque();
    private static final Queue<TimerTask> TICK_END_TASKS = Queues.newArrayDeque();
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static Particle spawnParticle(ParticleOptions effect, double x, double y, double z, double cx, double cy, double cz, float red, float green, float blue, float alpha, double vx, double vy, double vz, int age, String expression, double step, String group) {
        try {
            var particle = CLIENT.particleEngine.createParticle(effect, x, y, z, vx, vy, vz);
            if (particle != null) {
                particle.setColor(red, green, blue);
                particle.alpha = alpha;
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

    public static void spawnTickParticle(ParticleOptions effect, double x, double y, double z, float red, float green, float blue, float alpha, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar) {
        new TickParticleTask(effect, x, y, z, red, green, blue, alpha, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar, false).run();
    }

    public static void spawnTickParticle(ParticleOptions effect, double x, double y, double z, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar) {
        new TickParticleTask(effect, x, y, z, 1.0F, 1.0F, 1.0F, 1.0F, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar, true).run();
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, int xRotate, int yRotate, int zRotate, boolean flip, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnImageParticle(effect, x, y, z, path, scaling, xRotate, yRotate, zRotate, flip, null, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnImageParticle(effect, x, y, z, path, scaling, 0, 0, 0, false, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnImageParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
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
                        spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, red, green, blue, alpha, vx, vy, vz, age, speedExpression, speedStep, group);
                    }
                }
            }
        } catch (IOException e) {
            ClientMessageUtil.addChatMessage(e);
        }
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, int xRotate, int yRotate, int zRotate, boolean flip, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnVideoParticle(effect, x, y, z, path, scaling, xRotate, yRotate, zRotate, flip, null, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        spawnVideoParticle(effect, x, y, z, path, scaling, 0, 0, 0, false, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group);
    }

    public static void spawnVideoParticle(ParticleOptions effect, double x, double y, double z, String path, double scaling, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
        VideoUtil.decoder(path, new VideoConsumer(effect, x, y, z, scaling, xRotate, yRotate, zRotate, flip, matrix, dpb, vx, vy, vz, age, speedExpression, speedStep, group));
    }

    private static int[][] getRotateFlipMat(int xRotate, int yRotate, int zRotate, boolean flip, int rows, int cols) {
        int[][] flipmat = new int[][]{{flip ? -1 : 1, 0, 0, flip ? cols - 1 : 0}, {0, -1, 0, rows - 1}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        int[][] zmat = new int[][]{{dcos(zRotate), -dsin(zRotate), 0, xmove(zRotate, rows, cols)}, {dsin(zRotate), dcos(zRotate), 0, ymove(zRotate, rows, cols)}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        int[][] ymat = new int[][]{{dcos(yRotate), 0, dsin(yRotate), 0}, {0, 1, 0, 0}, {-dsin(yRotate), 0, dcos(yRotate), 0}, {0, 0, 0, 1}};
        int[][] xmat = new int[][]{{1, 0, 0, 0}, {0, dcos(xRotate), dsin(xRotate), 0}, {0, -dsin(xRotate), dcos(xRotate), 0}, {0, 0, 0, 1}};
        return MatrixUtil.matMul(xmat, MatrixUtil.matMul(ymat, MatrixUtil.matMul(zmat, flipmat)));
    }

    private static int dsin(int n) {
        return switch (n % 4) {
            case 0, 2 -> 0;
            case 1 -> 1;
            case 3 -> -1;
            default -> n;
        };
    }

    private static int dcos(int n) {
        return switch (n % 4) {
            case 0 -> 1;
            case 1, 3 -> 0;
            case 2 -> -1;
            default -> n;
        };
    }

    private static int xmove(int rotate, int rows, int cols) {
        return switch (rotate % 4) {
            case 0, 3 -> 0;
            case 1 -> rows - 1;
            case 2 -> cols - 1;
            default -> rotate;
        };
    }

    private static int ymove(int rotate, int rows, int cols) {
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

    public static void onStartClientTick(ClientTickEvent.Pre event) {
        synchronized (TICK_START_TASKS) {
            while (!TICK_START_TASKS.isEmpty()) {
                TICK_START_TASKS.poll().run();
            }
        }
    }

    public static void onEndClientTick(ClientTickEvent.Post post) {
        synchronized (TICK_END_TASKS) {
            while (!TICK_END_TASKS.isEmpty()) {
                TICK_END_TASKS.poll().run();
            }
        }
    }

    private static class TickParticleTask extends TimerTask {
        private final ParticleOptions particleType;
        private final double x;
        private final double y;
        private final double z;
        private float red;
        private float green;
        private float blue;
        private float alpha;
        private double vx;
        private double vy;
        private double vz;
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

        public TickParticleTask(ParticleOptions particleType, double x, double y, double z, float red, float green, float blue, float alpha, double vx, double vy, double vz, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group, boolean polar, boolean rgba) {
            this.polar = polar;
            this.rgba = rgba;
            this.particleType = particleType;
            this.x = x;
            this.y = y;
            this.z = z;
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
                this.vx = vx;
                this.vy = vy;
                this.vz = vz;
            }
        }

        public void run() {
            var data = exe.getData();
            for (int i = 0; i < this.cpt && this.t <= this.end; this.t += this.step) {
                data.t = this.t;
                exe.invoke();
                double dx;
                double dy;
                double dz;
                if (this.polar) {
                    dx = data.dis * Math.cos(data.s2) * Math.cos(data.s1);
                    dy = data.dis * Math.sin(data.s2);
                    dz = data.dis * Math.cos(data.s2) * Math.sin(data.s1);
                } else {
                    dx = data.x;
                    dy = data.y;
                    dz = data.z;
                }

                if (this.rgba) {
                    double vx = data.vx;
                    double vy = data.vy;
                    double vz = data.vz;
                    ParticleUtil.spawnParticle(this.particleType, this.x + dx, this.y + dy, this.z + dz, this.x, this.y, this.z, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, vx, vy, vz, this.age, this.speedExpression, this.speedStep, this.group);
                } else {
                    ParticleUtil.spawnParticle(this.particleType, this.x + dx, this.y + dy, this.z + dz, this.x, this.y, this.z, this.red, this.green, this.blue, this.alpha, this.vx, this.vy, this.vz, this.age, this.speedExpression, this.speedStep, this.group);
                }

                ++i;
            }

            if (this.t <= this.end) {
                ParticleUtil.addTask(new TickEndTask(this), false);
            }
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

        private VideoConsumer(ParticleOptions effect, double x, double y, double z, double scaling, int xRotate, int yRotate, int zRotate, boolean flip, double[][] matrix, double dpb, double vx, double vy, double vz, int age, String speedExpression, double speedStep, String group) {
            this.effect = effect;
            this.x = x;
            this.y = y;
            this.z = z;
            this.scaling = scaling;
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
            BufferedImage resultImage = new BufferedImage(dw, dh, image.getType());
            Graphics2D graphics = resultImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, dw, dh, 0, 0, width, height, null);
            graphics.dispose();
            int[][] rotateFlipMat = ParticleUtil.getRotateFlipMat(this.xRotate, this.yRotate, this.zRotate, this.flip, dh, dw);
            if (this.init == 0) {
                this.init = 1;
                this.particles = new Particle[dh][dw];
                ParticleUtil.CLIENT.execute(() -> {
                    for (int row = 0; row < dh; ++row) {
                        for (int col = 0; col < dw; ++col) {
                            int pixel = resultImage.getRGB(col, row);
                            float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                            float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                            float blue = (float) (pixel & 0xff) / 255.0F;
                            double[][] pos = MatrixUtil.matDiv(MatrixUtil.matMul(rotateFlipMat, new int[][]{{col}, {row}, {0}, {1}}), this.dpb);
                            if (this.matrix != null) {
                                pos = MatrixUtil.matMul(this.matrix, pos);
                            }

                            double dx = pos[0][0];
                            double dy = pos[1][0];
                            double dz = pos[2][0];
                            this.particles[row][col] = ParticleUtil.spawnParticle(this.effect, this.x + dx, this.y + dy, this.z + dz, this.x, this.y, this.z, red, green, blue, 1.0F, this.vx, this.vy, this.vz, this.age, this.speedExpression, this.speedStep, this.group);
                        }
                    }
                    this.init = 2;
                });
                return true;
            } else {
                while (this.init != 2) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        ExParticle.LOGGER.error(e.getMessage(), e);
                    }
                }
                boolean alive = false;
                for (int row = 0; row < dh; ++row) {
                    for (int col = 0; col < dw; ++col) {
                        if (this.particles[row][col].isAlive()) {
                            int pixel = resultImage.getRGB(col, row);
                            float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                            float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                            float blue = (float) (pixel & 0xff) / 255.0F;
                            this.particles[row][col].setColor(red, green, blue);
                            alive = true;
                        }
                    }
                }
                return alive;
            }
        }
    }
}
