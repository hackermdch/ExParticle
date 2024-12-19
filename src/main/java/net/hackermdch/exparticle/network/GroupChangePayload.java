package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.ExpressionUtil;
import net.hackermdch.exparticle.util.GroupUtil;
import net.minecraft.client.Minecraft;
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

public class GroupChangePayload implements CustomPacketPayload {
    private static final Type<GroupChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "group_cache"));
    private static final StreamCodec<RegistryFriendlyByteBuf, GroupChangePayload> CODEC = StreamCodec.ofMember(GroupChangePayload::write, GroupChangePayload::new);
    private final int type;
    private final String group;
    private final String expression;
    private final String conditionalExpression;
    private final boolean hasPos;
    private final double x;
    private final double y;
    private final double z;

    public GroupChangePayload(int type, String group, String expression, String conditionalExpression, Vec3 pos) {
        this.type = type;
        this.group = group;
        this.expression = expression;
        this.conditionalExpression = conditionalExpression;
        if (!(hasPos = pos != null)) pos = Vec3.ZERO;
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    private GroupChangePayload(RegistryFriendlyByteBuf buf) {
        var mc = Minecraft.getInstance();
        assert mc.player != null;
        type = buf.readInt();
        group = buf.readUtf();
        expression = buf.readUtf();
        conditionalExpression = readString(buf, buf.readBoolean(), null);
        hasPos = buf.readBoolean();
        x = readDouble(buf, hasPos, mc.player.getX());
        y = readDouble(buf, hasPos, mc.player.getY());
        z = readDouble(buf, hasPos, mc.player.getZ());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(type);
        buf.writeUtf(group);
        buf.writeUtf(expression);
        var flag = validString(conditionalExpression);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(conditionalExpression);
        buf.writeBoolean(hasPos);
        if (hasPos) {
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
        }
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var exe = ExpressionUtil.parse(expression);
            var ce = ExpressionUtil.parse(conditionalExpression);
            for (var particle : GroupUtil.get(group)) {
                if (ce != null) {
                    var data = ce.getData();
                    var dx = particle.x - x;
                    var dy = particle.y - y;
                    var dz = particle.z - z;
                    data.x = dx;
                    data.y = dy;
                    data.z = dz;
                    data.s1 = Math.atan2(dz, dx);
                    data.s2 = Math.atan2(dy, Math.hypot(dx, dz));
                    data.dis = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (ce.invoke() == 0) continue;
                }
                switch (type) {
                    case 0:
                        if (exe == null) break;
                        var data = exe.getData();
                        data.x = particle.x - x;
                        data.y = particle.y - y;
                        data.z = particle.z - z;
                        var pvx = data.vx = particle.xd;
                        var pvy = data.vy = particle.yd;
                        var pvz = data.vz = particle.zd;
                        data.cx = particle.getCenterX();
                        data.cy = particle.getCenterY();
                        data.cz = particle.getCenterZ();
                        data.cr = particle.rCol;
                        data.cg = particle.gCol;
                        data.cb = particle.bCol;
                        data.alpha = particle.alpha;
                        exe.invoke();
                        particle.move(data.x - particle.x + x, data.y - particle.y + y, data.z - particle.z + z);
                        particle.setCenterX(data.cx);
                        particle.setCenterY(data.cy);
                        particle.setCenterZ(data.cz);
                        particle.setColor((float) data.cr, (float) data.cg, (float) data.cb);
                        particle.alpha = (float) data.alpha;
                        if (data.vx != pvx || data.vy != pvy || data.vz != pvz) {
                            particle.setStop(data.vx == (double) 0.0F && data.vy == (double) 0.0F && data.vz == (double) 0.0F);
                        }
                        particle.xd = data.vx;
                        particle.yd = data.vy;
                        particle.zd = data.vz;
                        break;
                    case 1:
                        particle.setCustomMove(exe != null);
                        particle.setExe(ExpressionUtil.parse(expression));
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
        registrar.playBidirectional(TYPE, CODEC, GroupChangePayload::handle);
    }
}
