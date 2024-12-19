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

public class ConditionalPayload implements CustomPacketPayload {
    private static final Type<ConditionalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "conditional"));
    private static final StreamCodec<RegistryFriendlyByteBuf, ConditionalPayload> CODEC = StreamCodec.ofMember(ConditionalPayload::write, ConditionalPayload::new);
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
    private final double dx;
    private final double dy;
    private final double dz;
    private final int age;
    private final boolean hasExpression;
    private final String expression;
    private final double step;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public ConditionalPayload(ParticleOptions effect, Vec3 pos, Vector4f color, Vec3 speed, Vec3 range, String expression, double step, int age, String speedExpression, double speedStep, String group) {
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
        this.dx = range.x;
        this.dy = range.y;
        this.dz = range.z;
        this.age = age;
        this.hasExpression = validString(expression);
        this.expression = expression;
        this.step = step;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
        this.effect = effect;
    }

    private ConditionalPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        red = buf.readFloat();
        green = buf.readFloat();
        blue = buf.readFloat();
        alpha = buf.readFloat();
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
        dx = buf.readDouble();
        dy = buf.readDouble();
        dz = buf.readDouble();
        age = buf.readInt();
        hasExpression = buf.readBoolean();
        expression = readString(buf, hasExpression, null);
        step = readDouble(buf, hasExpression, 0.1);
        hasSpeedExpression = buf.readBoolean();
        speedExpression = readString(buf, hasSpeedExpression, null);
        speedStep = readDouble(buf, hasSpeedExpression, 1.0);
        group = readString(buf, buf.readBoolean(), null);
        effect = type.streamCodec().decode(buf);
    }

    @SuppressWarnings({"unchecked"})
    private void write(RegistryFriendlyByteBuf buf) {
        var type = (ParticleType<ParticleOptions>) effect.getType();
        buf.writeById(BuiltInRegistries.PARTICLE_TYPE::getId, type);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
        buf.writeFloat(alpha);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeDouble(dx);
        buf.writeDouble(dy);
        buf.writeDouble(dz);
        buf.writeInt(age);
        buf.writeBoolean(hasExpression);
        if (hasExpression) {
            buf.writeUtf(expression);
            buf.writeDouble(step);
        }
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
            var exe = ExpressionUtil.parse(expression);
            var data = Objects.requireNonNull(exe).getData();
            for (var cx = -dx; cx <= dx; cx += step) {
                for (var cy = -dy; cy <= dy; cy += step) {
                    for (var cz = -dz; cz <= dz; cz += step) {
                        data.x = cx;
                        data.y = cy;
                        data.z = cz;
                        data.s1 = Math.atan2(cz, cx);
                        data.s2 = Math.atan2(cy, Math.hypot(cx, cz));
                        data.dis = Math.sqrt(cx * cx + cy * cy + cz * cz);
                        if (exe.invoke() != 0)
                            ParticleUtil.spawnParticle(effect, x + cx, y + cy, z + cz, x, y, z, red, green, blue, alpha, vx, vy, vz, age, expression, step, group);
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
        registrar.playBidirectional(TYPE, CODEC, ConditionalPayload::handle);
    }
}
