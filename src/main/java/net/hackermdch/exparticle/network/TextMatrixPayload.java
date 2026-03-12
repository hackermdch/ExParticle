package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.MatrixUtil;
import net.hackermdch.exparticle.util.ParticleUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class TextMatrixPayload implements CustomPacketPayload {
    private static final Type<TextMatrixPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "text_matrix"));
    private static final StreamCodec<RegistryFriendlyByteBuf, TextMatrixPayload> CODEC = StreamCodec.ofMember(TextMatrixPayload::write, TextMatrixPayload::new);
    private final double x;
    private final double y;
    private final double z;
    private final Component text;
    private final double scaling;
    private final double[][] matrix;
    private final double dpb;
    private final double size;
    private final double vx;
    private final double vy;
    private final double vz;
    private final int lifetime;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public TextMatrixPayload(ParticleOptions effect, Vec3 pos, Component text, double scaling, String matrixStr, double dpb, double size, Vec3 speed, int lifetime, String speedExpression, double speedStep, String group) {
        this.effect = effect;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.text = text;
        this.scaling = scaling;
        this.matrix = MatrixUtil.toMat(matrixStr);
        this.dpb = dpb;
        this.size = size;
        if (speed == null) speed = Vec3.ZERO;
        this.vx = speed.x;
        this.vy = speed.y;
        this.vz = speed.z;
        this.lifetime = lifetime;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
    }

    private TextMatrixPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        text = Component.Serializer.fromJson(buf.readUtf(), buf.registryAccess());
        scaling = buf.readDouble();
        int rows = buf.readInt();
        int cols = buf.readInt();
        matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = buf.readDouble();
            }
        }
        dpb = buf.readDouble();
        size = buf.readDouble();
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
        lifetime = buf.readInt();
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
        buf.writeUtf(Component.Serializer.toJson(text, buf.registryAccess()));
        buf.writeDouble(scaling);
        buf.writeInt(matrix.length);
        buf.writeInt(matrix[0].length);
        for (double[] row : matrix) {
            for (double val : row) {
                buf.writeDouble(val);
            }
        }
        buf.writeDouble(dpb);
        buf.writeDouble(size);
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
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
        context.enqueueWork(() -> ParticleUtil.spawnTextParticle(effect, x, y, z, text, scaling, matrix, dpb, size, vx, vy, vz, lifetime, speedExpression, speedStep, group));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, TextMatrixPayload::handle);
    }
}