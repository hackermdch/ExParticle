package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.ParticleUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class TextPayload implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<TextPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "text"));
    private static final StreamCodec<RegistryFriendlyByteBuf, TextPayload> CODEC = StreamCodec.ofMember(TextPayload::write, TextPayload::new);
    private final double x;
    private final double y;
    private final double z;
    private final Component text;
    private final double scaling;
    private final String expression;
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

    public TextPayload(ParticleOptions effect, Vec3 pos, Component text, double scaling, String expression, double dpb, Vec3 speed, int age, String speedExpression, double speedStep, String group) {
        if (speed == null) speed = Vec3.ZERO;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.text = text;
        this.scaling = scaling;
        this.expression = expression;
        this.dpb = dpb;
        this.vx = speed.x;
        this.vy = speed.y;
        this.vz = speed.z;
        this.age = age;
        this.hasSpeedExpression = NetworkUtils.validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
        this.effect = effect;
    }

    private TextPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        text = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        scaling = buf.readDouble();
        expression = buf.readUtf();
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
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, text);
        buf.writeDouble(scaling);
        buf.writeUtf(expression);
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
        context.enqueueWork(() -> ParticleUtil.spawnTextParticle(effect, x, y, z, text, scaling, expression, dpb, vx, vy, vz, age, speedExpression, speedStep, group));
    }

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, TextPayload::handle);
    }
}
