package net.hackermdch.exparticle.network;

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

import java.util.Random;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class NormalPayload implements CustomPacketPayload {
    private static final Type<NormalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "normal"));
    private static final StreamCodec<RegistryFriendlyByteBuf, NormalPayload> CODEC = StreamCodec.ofMember(NormalPayload::write, NormalPayload::new);
    private static final Random RANDOM = new Random();
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
    private final double dx;
    private final double dy;
    private final double dz;
    private final int count;
    private final int lifetime;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public NormalPayload(ParticleOptions effect, Vec3 pos, double size, Vector4f color, int light, Vec3 speed, Vec3 range, int count, int age, String expression, double step, String group) {
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
        this.dx = range.x;
        this.dy = range.y;
        this.dz = range.z;
        this.count = count;
        this.lifetime = age;
        this.hasSpeedExpression = validString(expression);
        this.speedExpression = expression;
        this.speedStep = step;
        this.group = group;
        this.effect = effect;
    }

    private NormalPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        size = buf.readDouble();
        red = buf.readFloat();
        green = buf.readFloat();
        blue = buf.readFloat();
        alpha = buf.readFloat();
        light = buf.readInt();
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
        dx = buf.readDouble();
        dy = buf.readDouble();
        dz = buf.readDouble();
        count = buf.readInt();
        lifetime = buf.readInt();
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
        buf.writeDouble(size);
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
        buf.writeFloat(alpha);
        buf.writeInt(light);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
        buf.writeDouble(dx);
        buf.writeDouble(dy);
        buf.writeDouble(dz);
        buf.writeInt(count);
        buf.writeInt(lifetime);
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
            for (int i = 0; i < count; ++i) {
                double rx = RANDOM.nextGaussian() * dx;
                double ry = RANDOM.nextGaussian() * dy;
                double rz = RANDOM.nextGaussian() * dz;
                double lightVal = (light == -1) ? Double.NaN : light / 15.0;
                ParticleUtil.spawnParticle(effect, x + rx, y + ry, z + rz, x, y, z, size, red, green, blue, alpha, lightVal, vx, vy, vz, lifetime, speedExpression, speedStep, group);
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, NormalPayload::handle);
    }
}
