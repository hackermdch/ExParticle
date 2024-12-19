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

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class VideoPayload implements CustomPacketPayload {
    private static final Type<VideoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "video"));
    private static final StreamCodec<RegistryFriendlyByteBuf, VideoPayload> CODEC = StreamCodec.ofMember(VideoPayload::write, VideoPayload::new);
    private final double x;
    private final double y;
    private final double z;
    private final String path;
    private final double scaling;
    private final int xRotate;
    private final int yRotate;
    private final int zRotate;
    private final boolean flip;
    private final double dpb;
    private final double vx;
    private final double vy;
    private final double vz;
    private final int age;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public VideoPayload(ParticleOptions effect, Vec3 pos, String path, double scaling, int xRotate, int yRotate, int zRotate, int flip, double dpb, Vec3 speed, int age, String speedExpression, double speedStep, String group) {
        this.effect = effect;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.path = path;
        this.scaling = scaling;
        this.xRotate = xRotate;
        this.yRotate = yRotate;
        this.zRotate = zRotate;
        this.flip = flip != 0;
        this.dpb = dpb;
        if (speed == null) speed = Vec3.ZERO;
        vx = speed.x;
        vy = speed.y;
        vz = speed.z;
        this.age = age;
        hasSpeedExpression = speedExpression != null;
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
    }

    private VideoPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        path = buf.readUtf();
        scaling = buf.readDouble();
        xRotate = buf.readInt();
        yRotate = buf.readInt();
        zRotate = buf.readInt();
        flip = buf.readBoolean();
        dpb = buf.readDouble();
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
        age = buf.readInt();
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
        buf.writeUtf(path);
        buf.writeDouble(scaling);
        buf.writeInt(xRotate);
        buf.writeInt(yRotate);
        buf.writeInt(zRotate);
        buf.writeBoolean(flip);
        buf.writeDouble(dpb);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
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
        context.enqueueWork(() -> ParticleUtil.spawnVideoParticle(effect, x, y, z, path, scaling, xRotate, yRotate, zRotate, flip, dpb, vx, vy, vz, age, speedExpression, speedStep, group));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, VideoPayload::handle);
    }
}
