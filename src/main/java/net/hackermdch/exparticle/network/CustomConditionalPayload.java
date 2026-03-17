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

import java.util.Objects;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class CustomConditionalPayload implements CustomPacketPayload {
    private static final Type<CustomConditionalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_conditional"));
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomConditionalPayload> CODEC = StreamCodec.ofMember(CustomConditionalPayload::write, CustomConditionalPayload::new);

    private final double x, y, z;
    private final String attrExpression;
    private final double dx, dy, dz;
    private final boolean hasExpression;
    private final String expression;
    private final double step;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public CustomConditionalPayload(ParticleOptions effect, Vec3 pos, String attrExpression, Vec3 range, String expression, double step, String speedExpression, double speedStep, String group) {
        this.effect = effect;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.attrExpression = attrExpression;
        this.dx = range.x;
        this.dy = range.y;
        this.dz = range.z;
        this.hasExpression = validString(expression);
        this.expression = expression;
        this.step = step;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
    }

    private CustomConditionalPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        attrExpression = buf.readUtf();
        dx = buf.readDouble();
        dy = buf.readDouble();
        dz = buf.readDouble();
        hasExpression = buf.readBoolean();
        expression = readString(buf, hasExpression, null);
        step = readDouble(buf, hasExpression, 0.1);
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
        boolean flag = validString(group);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(group);
        type.streamCodec().encode(buf, effect);
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {

            var attrExe = ExpressionUtil.parse(attrExpression);
            boolean hasAttrExpression = attrExe != null;
            ParticleStruct attrData = hasAttrExpression ? attrExe.getData() : new ParticleStruct();

            var exe = ExpressionUtil.parse(expression);
            var data = Objects.requireNonNull(exe).getData();
            for (double cx = -dx; cx <= dx; cx += step) {
                for (double cy = -dy; cy <= dy; cy += step) {
                    for (double cz = -dz; cz <= dz; cz += step) {
                        data.x = cx;
                        data.y = cy;
                        data.z = cz;
                        data.s1 = Math.atan2(cz, cx);
                        data.s2 = Math.atan2(cy, Math.hypot(cx, cz));
                        data.dis = Math.sqrt(cx * cx + cy * cy + cz * cz);
                        if (exe.invoke() == 0) continue;
                        if (hasAttrExpression) {
                            attrExe.invoke();
                        }
                        CustomParticleBuilder.buildParticle(effect, x + cx, y + cy, z + cz, x, y, z, speedExpression, speedStep, group, attrData);
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
        registrar.playBidirectional(TYPE, CODEC, CustomConditionalPayload::handle);
    }
}