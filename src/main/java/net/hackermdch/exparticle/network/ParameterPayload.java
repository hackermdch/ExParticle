package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.ExpressionUtil;
import net.hackermdch.exparticle.util.ParticleUtil;
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
    private final boolean rgba;
    private final double x;
    private final double y;
    private final double z;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final double vx;
    private final double vy;
    private final double vz;
    private final double begin;
    private final double end;
    private final String expression;
    private final double step;
    private final int cpt;
    private final int age;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public ParameterPayload(boolean polar, boolean tick, boolean rgba, ParticleOptions effect, Vec3 pos, Vector4f color, Vec3 speed, double begin, double end, String expression, double step, int cpt, int age, String speedExpression, double speedStep, String group) {
        if (rgba) color = new Vector4f();
        this.polar = polar;
        this.tick = tick;
        this.rgba = rgba;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.red = color.x;
        this.green = color.y;
        this.blue = color.z;
        this.alpha = color.w;
        this.vx = speed.x;
        this.vy = speed.y;
        this.vz = speed.z;
        this.begin = begin;
        this.end = end;
        this.expression = expression;
        this.step = step;
        this.cpt = cpt;
        this.age = age;
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
        rgba = buf.readBoolean();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        red = readFloat(buf, !rgba, 0.0F);
        green = readFloat(buf, !rgba, 0.0F);
        blue = readFloat(buf, !rgba, 0.0F);
        alpha = readFloat(buf, !rgba, 0.0F);
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
        begin = buf.readDouble();
        end = buf.readDouble();
        expression = buf.readUtf();
        step = buf.readDouble();
        cpt = readInt(buf, tick, 0);
        age = buf.readInt();
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
        buf.writeBoolean(rgba);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        if (!rgba) {
            buf.writeFloat(red);
            buf.writeFloat(green);
            buf.writeFloat(blue);
            buf.writeFloat(alpha);
        }
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeDouble(begin);
        buf.writeDouble(end);
        buf.writeUtf(expression);
        buf.writeDouble(step);
        if (tick) buf.writeInt(cpt);
        buf.writeInt(age);
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
                if (rgba) {
                    ParticleUtil.spawnTickParticle(effect, x, y, z, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar);
                } else {
                    ParticleUtil.spawnTickParticle(effect, x, y, z, red, green, blue, alpha, vx, vy, vz, begin, end, expression, step, cpt, age, speedExpression, speedStep, group, polar);
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
                    if (rgba) {
                        ParticleUtil.spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, (float) data.cr, (float) data.cg, (float) data.cb, (float) data.alpha, vx, vy, vz, age, speedExpression, speedStep, group);
                    } else {
                        ParticleUtil.spawnParticle(effect, x + dx, y + dy, z + dz, x, y, z, red, green, blue, alpha, vx, vy, vz, age, speedExpression, speedStep, group);
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
