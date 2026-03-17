package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.ExParticle;
import net.hackermdch.exparticle.util.*;
import net.minecraft.client.particle.Particle;
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Predicate;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;
import static net.hackermdch.exparticle.network.NetworkUtils.*;

public class CustomVideoMatrixPayload implements CustomPacketPayload {
    private static final Type<CustomVideoMatrixPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_video_matrix"));
    private static final StreamCodec<RegistryFriendlyByteBuf, CustomVideoMatrixPayload> CODEC = StreamCodec.ofMember(CustomVideoMatrixPayload::write, CustomVideoMatrixPayload::new);

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

    public CustomVideoMatrixPayload(ParticleOptions effect, Vec3 pos, String path, double scaling, String matrixStr, double dpb, String attrExpression, String speedExpression, double speedStep, String group) {
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

    private CustomVideoMatrixPayload(RegistryFriendlyByteBuf buf) {
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

            Predicate<BufferedImage> consumer = new Predicate<>() {
                private int init = 0;
                private Particle[][] particles;

                @Override
                public boolean test(BufferedImage image) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    int dw = (int) ((double) width * scaling);
                    int dh = (int) ((double) height * scaling);
                    BufferedImage scaled = new BufferedImage(dw, dh, image.getType());
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(image, 0, 0, dw, dh, 0, 0, width, height, null);
                    g.dispose();
                    if (init == 0) {
                        init = 1;
                        particles = new Particle[dh][dw];
                        ParticleUtil.CLIENT.execute(() -> {
                            for (int row = 0; row < dh; ++row) {
                                for (int col = 0; col < dw; ++col) {
                                    int pixel = scaled.getRGB(col, row);
                                    float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                                    float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                                    float blue = (float) (pixel & 0xff) / 255.0F;
                                    double[][] pos = MatrixUtil.matDiv(MatrixUtil.matMul(matrix, new int[][]{{col}, {row}, {0}, {1}}), dpb);
                                    double dx = pos[0][0];
                                    double dy = pos[1][0];
                                    double dz = pos[2][0];
                                    if (hasAttrExpression) {
                                        attrExe.invoke();
                                    }
                                    data.cr = red;
                                    data.cg = green;
                                    data.cb = blue;
                                    particles[row][col] = CustomParticleBuilder.buildParticle(effect, x + dx, y + dy, z + dz, x, y, z, speedExpression, speedStep, group, data);

                                }
                            }
                            init = 2;
                        });
                        return true;
                    } else {
                        while (init != 2) {
                            try {
                                Thread.sleep(10L);
                            } catch (InterruptedException e) {
                                ExParticle.LOGGER.error(e.getMessage(), e);
                            }
                        }
                        boolean alive = false;
                        for (int row = 0; row < dh; ++row) {
                            for (int col = 0; col < dw; ++col) {
                                if (particles[row][col] != null && particles[row][col].isAlive()) {
                                    int pixel = scaled.getRGB(col, row);
                                    float red = (float) ((pixel & 0xff0000) >>> 16) / 255.0F;
                                    float green = (float) ((pixel & 0xff00) >>> 8) / 255.0F;
                                    float blue = (float) (pixel & 0xff) / 255.0F;
                                    particles[row][col].setColor(red, green, blue);
                                    alive = true;
                                }
                            }
                        }
                        return alive;
                    }
                }
            };

            VideoUtil.decoder(path, consumer);
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, CustomVideoMatrixPayload::handle);
    }
}