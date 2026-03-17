package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.*;
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

import java.io.IOException;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class CustomImageMatrixPayload implements CustomPacketPayload {
    private static final Type<CustomImageMatrixPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_image_matrix"));
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomImageMatrixPayload> CODEC = StreamCodec.ofMember(CustomImageMatrixPayload::write, CustomImageMatrixPayload::new);

    private final double x, y, z;
    private final String path;
    private final double scaling;
    private final double[][] matrix;
    private final double dpb;
    private final String attrExpression;
    private final boolean hasSpeedExpression;
    private final String speedExpression;
    private final double speedStep;
    private final String group;
    private final ParticleOptions effect;

    public CustomImageMatrixPayload(ParticleOptions effect, Vec3 pos, String path, double scaling, String matrixStr, double dpb, String attrExpression, String speedExpression, double speedStep, String group) {
        this.effect = effect;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.path = path;
        this.scaling = scaling;
        this.matrix = MatrixUtil.toMat(matrixStr);
        this.dpb = dpb;
        this.attrExpression = attrExpression;
        this.hasSpeedExpression = validString(speedExpression);
        this.speedExpression = speedExpression;
        this.speedStep = speedStep;
        this.group = group;
    }

    private CustomImageMatrixPayload(RegistryFriendlyByteBuf buf) {
        var type = buf.readById(BuiltInRegistries.PARTICLE_TYPE::byId);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        path = buf.readUtf();
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
        attrExpression = readString(buf, buf.readBoolean(), null);
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
        buf.writeUtf(path);
        buf.writeDouble(scaling);
        buf.writeInt(matrix.length);
        buf.writeInt(matrix[0].length);
        for (double[] row : matrix) {
            for (double val : row) {
                buf.writeDouble(val);
            }
        }
        buf.writeDouble(dpb);
        boolean hasAttrExpression = validString(attrExpression);
        buf.writeBoolean(hasAttrExpression);
        if (hasAttrExpression) buf.writeUtf(attrExpression);
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

            try {
                var image = ImageUtil.readImage(path, scaling, true);
                int rows = image.getHeight();
                int cols = image.getWidth();
                for (int row = 0; row < rows; ++row) {
                    for (int col = 0; col < cols; ++col) {
                        if (hasAttrExpression) {
                            attrExe.invoke();
                        }
                        int pixel = image.getRGB(col, row);
                        float alpha = (float) ((pixel & 0xff000000) >>> 24) / 255.0F;
                        float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                        float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                        float blue = (float) (pixel & 0xff) / 255.0F;
                        double[][] pos = MatrixUtil.matDiv(MatrixUtil.matMul(matrix, new int[][]{{col}, {row}, {0}, {1}}), dpb);
                        double dx = pos[0][0];
                        double dy = pos[1][0];
                        double dz = pos[2][0];
                        if (alpha != 0.0F) {
                            data.cr = red;
                            data.cg = green;
                            data.cb = blue;
                            data.alpha = alpha;
                            CustomParticleBuilder.buildParticle(effect, x + dx, y + dy, z + dz, x, y, z, speedExpression, speedStep, group, data);
                        }
                    }
                }
            } catch (IOException e) {
                ClientMessageUtil.addChatMessage(e);
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, CustomImageMatrixPayload::handle);
    }
}