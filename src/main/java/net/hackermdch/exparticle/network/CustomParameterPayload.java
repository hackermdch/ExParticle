package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.CustomParticleBuilder;
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

import java.util.Objects;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class CustomParameterPayload implements CustomPacketPayload {
    private static final Type<CustomParameterPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_parameter"));
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomParameterPayload> CODEC = StreamCodec.ofMember(CustomParameterPayload::write, CustomParameterPayload::new);

    private final boolean polar;
    private final boolean tick;
    private final double x;
    private final double y;
    private final double z;
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

    public CustomParameterPayload(boolean polar, boolean tick, ParticleOptions effect, Vec3 pos, double begin, double end, String expression, double step, int cpt, String speedExpression, double speedStep, String group) {
        this.polar = polar;
        this.tick = tick;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
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

    private CustomParameterPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        polar = buf.readBoolean();
        tick = buf.readBoolean();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        begin = buf.readDouble();
        end = buf.readDouble();
        expression = buf.readUtf();
        step = buf.readDouble();
        cpt = readInt(buf, tick, 0);
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
        buf.writeBoolean(polar);
        buf.writeBoolean(tick);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
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
        boolean flag = validString(group);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(group);
        type.streamCodec().encode(buf, effect);
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (tick) {
                // 使用新方法启动自定义 tick 任务
                ParticleUtil.spawnCustomTickParticle(effect, x, y, z, begin, end, expression, step, cpt, speedExpression, speedStep, group, polar);
            } else {
                var exe = ExpressionUtil.parse(expression);
                if (exe == null) return;
                var data = Objects.requireNonNull(exe).getData();
                for (double t = begin; t <= end; t += step) {
                    data.t = t;
                    exe.invoke();
                    double dx, dy, dz;
                    if (polar) {
                        dx = data.dis * Math.cos(data.s2) * Math.cos(data.s1);
                        dy = data.dis * Math.sin(data.s2);
                        dz = data.dis * Math.cos(data.s2) * Math.sin(data.s1);
                    } else {
                        dx = data.x;
                        dy = data.y;
                        dz = data.z;
                    }
                    // 使用构建器创建粒子（非 tick 模式）
                    CustomParticleBuilder.buildParticle(effect, x + dx, y + dy, z + dz, x, y, z, speedExpression, speedStep, group, data);
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
        registrar.playBidirectional(TYPE, CODEC, CustomParameterPayload::handle);
    }
}