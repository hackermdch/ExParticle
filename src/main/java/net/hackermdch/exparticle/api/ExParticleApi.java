package net.hackermdch.exparticle.api;

import net.hackermdch.exparticle.util.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.hackermdch.exparticle.util.ParticleUtil.CLIENT;

@OnlyIn(Dist.CLIENT)
public class ExParticleApi {
    public static Particle createParticle(ParticleOptions effect, double x, double y, double z, double cx, double cy, double cz, float red, float green, float blue, float alpha, double vx, double vy, double vz, int age, @Nullable IExecutable expression, double step) {
        var particle = CLIENT.particleEngine.makeParticle(effect, x, y, z, vx, vy, vz);
        if (particle != null) {
            particle.setManaged(true);
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
            particle.setGravity(0.0f);
            particle.setCenterX(cx);
            particle.setCenterY(cy);
            particle.setCenterZ(cz);
            if (age > 0) {
                particle.setLifetime(age);
            } else if (age == -1) {
                particle.setLifetime(Integer.MAX_VALUE);
            }
            if (expression != null) {
                particle.setExe(expression);
                particle.setStep(step);
                particle.setCustomMove(true);
            }
        }
        return particle;
    }

    public static Particle createParticleFromData(ParticleOptions effect, double x, double y, double z, double cx, double cy, double cz, IExecutable speedExpression, double speedStep, ParticleStruct data) {
        var particle = createParticle(effect, x, y, z, cx, cy, cz, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, data.vx, data.vy, data.vz, (int) data.age, speedExpression, speedStep);
        if (particle != null) {
            particle.setCustomSize(data.size);
            particle.setCustomLight(data.light);
            particle.setGravity((float) data.gravity);
            particle.setFriction((float) data.friction);
        }
        return particle;
    }

    public static List<Particle> genNormal(ParticleOptions effect, RandomSource random, Vec3 pos, Vector4f color, Vec3 speed, Vec3 range, int count, int age, @Nullable IExecutable expression, double step) {
        var ps = new ArrayList<Particle>();
        double x = pos.x, y = pos.y, z = pos.z;
        double vx = speed.x, vy = speed.y, vz = speed.z;
        double dx = range.x, dy = range.y, dz = range.z;
        for (int i = 0; i < count; ++i) {
            double rx = random.nextGaussian() * dx;
            double ry = random.nextGaussian() * dy;
            double rz = random.nextGaussian() * dz;
            ps.add(createParticle(effect, x + rx, y + ry, z + rz, x, y, z, color.x, color.y, color.z, color.w, vx, vy, vz, age, expression, step));
        }
        return ps.stream().filter(Objects::nonNull).toList();
    }

    public static List<Particle> genCond(ParticleOptions effect, Vec3 pos, Vector4f color, Vec3 speed, Vec3 range, IExecutable expression, double step, int age, @Nullable IExecutable speedExpression, double speedStep) {
        var ps = new ArrayList<Particle>();
        double x = pos.x, y = pos.y, z = pos.z;
        double vx = speed.x, vy = speed.y, vz = speed.z;
        double dx = range.x, dy = range.y, dz = range.z;
        var data = expression.getData();
        for (var cx = -dx; cx <= dx; cx += step) {
            for (var cy = -dy; cy <= dy; cy += step) {
                for (var cz = -dz; cz <= dz; cz += step) {
                    data.x = cx;
                    data.y = cy;
                    data.z = cz;
                    data.s1 = Math.atan2(cz, cx);
                    data.s2 = Math.atan2(cy, Math.hypot(cx, cz));
                    data.dis = Math.sqrt(cx * cx + cy * cy + cz * cz);
                    if (expression.invoke() != 0)
                        ps.add(createParticle(effect, x + cx, y + cy, z + cz, x, y, z, color.x, color.y, color.z, color.w, vx, vy, vz, age, speedExpression, speedStep));
                }
            }
        }
        return ps.stream().filter(Objects::nonNull).toList();
    }

    public static List<Particle> genParameter(ParticleOptions effect, boolean polar, Vec3 pos, @Nullable Vector4f color, Vec3 speed, double begin, double end, IExecutable expression, double step, int age, @Nullable IExecutable speedExpression, double speedStep) {
        var ps = new ArrayList<Particle>();
        double x = pos.x, y = pos.y, z = pos.z;
        double vx = speed.x, vy = speed.y, vz = speed.z;
        var data = expression.getData();
        for (double t = begin; t <= end; t += step) {
            data.t = t;
            expression.invoke();
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
            if (color == null) {
                ps.add(createParticle(effect, x + dx, y + dy, z + dz, x, y, z, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, vx, vy, vz, age, speedExpression, speedStep));
            } else {
                ps.add(createParticle(effect, x + dx, y + dy, z + dz, x, y, z, color.x, color.y, color.z, color.w, vx, vy, vz, age, speedExpression, speedStep));
            }
        }
        return ps.stream().filter(Objects::nonNull).toList();
    }

    public static List<Particle> genText(ParticleOptions effect, Vec3 startPos, Component text, double scaling, @Nullable IExecutable expression, double dpb, Vec3 speed, int age, @Nullable IExecutable speedExpression, double speedStep) {
        var ps = new ArrayList<Particle>();
        double x = startPos.x, y = startPos.y, z = startPos.z;
        double vx = speed.x, vy = speed.y, vz = speed.z;
        var data = expression != null ? expression.getData() : new ParticleStruct();
        try (var img = TextUtil.toImage(text, (float) scaling)) {
            int rows = img.getHeight();
            int cols = img.getWidth();
            for (int row = 0; row < rows; ++row) {
                for (int col = 0; col < cols; ++col) {
                    int pixel = img.getPixelRGBA(col, row);
                    float alpha = FastColor.ABGR32.alpha(pixel) / 255f;
                    float red = FastColor.ABGR32.red(pixel) / 255f;
                    float green = FastColor.ABGR32.green(pixel) / 255f;
                    float blue = FastColor.ABGR32.blue(pixel) / 255f;
                    double[][] pos = MatrixUtil.matDiv(new int[][]{{col}, {row}, {0}, {1}}, dpb);
                    data.x = pos[0][0];
                    data.y = pos[1][0];
                    data.z = pos[2][0];
                    data.cr = red;
                    data.cg = green;
                    data.cb = blue;
                    data.alpha = alpha;
                    data.vx = vx;
                    data.vy = vy;
                    data.vz = vz;
                    data.age = age;
                    if (expression != null) expression.invoke();
                    if (alpha != 0.0F)
                        ps.add(createParticleFromData(effect, x + data.x, y + data.y, z + data.z, x, y, z, speedExpression, speedStep, data));
                }
            }
        }
        return ps.stream().filter(Objects::nonNull).toList();
    }
}
