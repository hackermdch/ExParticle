package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.CustomParticleBuilder;
import net.hackermdch.exparticle.util.ExpressionUtil;
import net.hackermdch.exparticle.util.ParticleStruct;
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

import java.util.Random;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class CustomNormalPayload implements CustomPacketPayload {
    private static final Type<CustomNormalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_normal"));
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomNormalPayload> CODEC = StreamCodec.ofMember(CustomNormalPayload::write, CustomNormalPayload::new);
    private static final Random RANDOM = new Random();
    private final double x, y, z;
    private final String attrExpression;
    private final double dx, dy, dz;
    private final int count;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public CustomNormalPayload(ParticleOptions effect, Vec3 pos, String attrExpression, Vec3 range, int count, String speedExpression, double speedStep, String group) {
        this.effect = effect;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.attrExpression = attrExpression;
        this.dx = range.x;
        this.dy = range.y;
        this.dz = range.z;
        this.count = count;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
    }

    private CustomNormalPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        attrExpression = buf.readUtf();
        dx = buf.readDouble();
        dy = buf.readDouble();
        dz = buf.readDouble();
        count = buf.readInt();
        hasSpeedExpression = buf.readBoolean();
        speedExpression = readString(buf, hasSpeedExpression, null);
        speedStep = readDouble(buf, hasSpeedExpression, 1.0);
        group = readString(buf, buf.readBoolean(), null);
        effect = type.streamCodec().decode(buf);
    }

    @SuppressWarnings("unchecked")
    private void write(RegistryFriendlyByteBuf buf) {
        var type = (ParticleType<ParticleOptions>) effect.getType();
        buf.writeById(BuiltInRegistries.PARTICLE_TYPE::getId, type);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeUtf(attrExpression);
        buf.writeDouble(dx);
        buf.writeDouble(dy);
        buf.writeDouble(dz);
        buf.writeInt(count);
        buf.writeBoolean(hasSpeedExpression);
        if (hasSpeedExpression) {
            buf.writeUtf(speedExpression);
            buf.writeDouble(speedStep);
        }
        boolean flag = validString(group);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(group);
        type.streamCodec().encode(buf, effect);
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var attrExe = ExpressionUtil.parse(attrExpression);
            boolean hasAttrExpression = attrExe != null;
            ParticleStruct data = hasAttrExpression ? attrExe.getData() : new ParticleStruct();

            for (int i = 0; i < count; i++) {
                if (hasAttrExpression) {
                    attrExe.invoke();
                }
                double rx = RANDOM.nextGaussian() * dx;
                double ry = RANDOM.nextGaussian() * dy;
                double rz = RANDOM.nextGaussian() * dz;
                CustomParticleBuilder.buildParticle(effect, x + rx, y + ry, z + rz, x, y, z, speedExpression, speedStep, group, data);
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, CustomNormalPayload::handle);
    }
}