package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.ExpressionUtil;
import net.hackermdch.exparticle.util.ParticleUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import java.util.Objects;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class ParameterPayload implements CustomPacketPayload {
    private static final Type<ParameterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "parameter"));
    private static final StreamCodec<RegistryFriendlyByteBuf, ParameterPayload> CODEC = StreamCodec.ofMember(ParameterPayload::write, ParameterPayload::new);
    private final boolean polar;
    private final boolean tick;
    private final boolean exp;
    private final double x;
    private final double y;
    private final double z;
    private final double size;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final int light;
    private final double vx;
    private final double vy;
    private final double vz;
    private final int lifetime;
    private final double begin;
    private final double end;
    private final String expression;
    private final double step;
    private final int cpt;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public ParameterPayload(boolean polar, boolean tick, boolean exp, ParticleOptions effect, Vec3 pos, double size, Vector4f color, int light, Vec3 speed, int lifetime, double begin, double end, String expression, double step, int cpt, String speedExpression, double speedStep, String group) {
        if (exp) color = new Vector4f();
        this.polar = polar;
        this.tick = tick;
        this.exp = exp;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.size = size;
        this.red = color.x;
        this.green = color.y;
        this.blue = color.z;
        this.alpha = color.w;
        this.light = light;
        this.vx = speed.x;
        this.vy = speed.y;
        this.vz = speed.z;
        this.lifetime = lifetime;
        this.begin = begin;
        this.end = end;
        this.expression = expression;
        this.step = step;
        this.cpt = cpt;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
        this.effect = effect;
    }

    private ParameterPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        polar = buf.readBoolean();
        tick = buf.readBoolean();
        exp = buf.readBoolean();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        size = readDouble(buf, !exp, 0.0F);
        red = readFloat(buf, !exp, 0.0F);
        green = readFloat(buf, !exp, 0.0F);
        blue = readFloat(buf, !exp, 0.0F);
        alpha = readFloat(buf, !exp, 0.0F);
        light = readInt(buf, !exp, -1);
        vx = readDouble(buf, !exp, 0.0D);
        vy = readDouble(buf, !exp, 0.0D);
        vz = readDouble(buf, !exp, 0.0D);
        lifetime = readInt(buf, !exp, -1);
        begin = buf.readDouble();
        end = buf.readDouble();
        expression = buf.readUtf();
        step = buf.readDouble();
        cpt = readInt(buf, tick, 0);
        hasSpeedExpression = buf.readBoolean();
        speedExpression = readString(buf, hasSpeedExpression, null);
        speedStep = readDouble(buf, hasSpeedExpression, 1.0F);
        group = readString(buf, buf.readBoolean(), null);
        effect = type.streamCodec().decode(buf);
    }

    @SuppressWarnings({"unchecked"})
    private void write(RegistryFriendlyByteBuf buf) {
        var type = (ParticleType<ParticleOptions>) effect.getType();
        buf.writeById(BuiltInRegistries.PARTICLE_TYPE::getId, type);
        buf.writeBoolean(polar);
        buf.writeBoolean(tick);
        buf.writeBoolean(exp);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        if (!exp) {
            buf.writeDouble(size);
            buf.writeFloat(red);
            buf.writeFloat(green);
            buf.writeFloat(blue);
            buf.writeFloat(alpha);
            buf.writeInt(light);
            buf.writeDouble(vx);
            buf.writeDouble(vy);
            buf.writeDouble(vz);
            buf.writeInt(lifetime);
        }
        buf.writeDouble(begin);
        buf.writeDouble(end);
        buf.writeUtf(expression);
        buf.writeDouble(step);
        if (tick) buf.writeInt(cpt);
        buf.writeBoolean(hasSpeedExpression);
        if (hasSpeedExpression) {
            buf.writeUtf(speedExpression);
            buf.writeDouble(speedStep);
        }
        var flag = validString(group);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(group);
        type.streamCodec().encode(buf, effect);
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (tick) {
                if (exp) {
                    ParticleUtil.spawnTickParticle(effect, x, y, z,begin, end, expression, step, cpt, speedExpression, speedStep, group, polar);
                } else {
                    double lightVal = (light == -1) ? Double.NaN : light / 15.0;
                    ParticleUtil.spawnTickParticle(effect, x, y, z, size, red, green, blue, alpha, lightVal, vx, vy, vz, lifetime, begin, end, expression, step, cpt, speedExpression, speedStep, group, polar);
                }
            } else {
                var exe = ExpressionUtil.parse(expression);
                var data = Objects.requireNonNull(exe).getData();
                for (double t = begin; t <= end; t += step) {
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
                    if (exp) {
                        Particle particle = ParticleUtil.spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, data.size, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, data.light, data.vx, data.vy, data.vz, (int)data.lifetime, speedExpression, speedStep, group);
                        if (particle != null) {
                            particle.setFriction(1.0F);
                        }
                    } else {
                        double lightVal = (light == -1) ? Double.NaN : light / 15.0;
                        ParticleUtil.spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, size, red, green, blue, alpha, lightVal, vx, vy, vz, lifetime, speedExpression, speedStep, group);
                    }
                }
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, ParameterPayload::handle);
    }
}
