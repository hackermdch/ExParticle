package net.hackermdch.exparticle.network;

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

public class GroupRemovePayload implements CustomPacketPayload {
    private static final Type<GroupRemovePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "group_remove"));
    private static final StreamCodec<RegistryFriendlyByteBuf, GroupRemovePayload> CODEC = StreamCodec.ofMember(GroupRemovePayload::write, GroupRemovePayload::new);
    private final String group;
    private final String expression;
    private final boolean hasPos;
    private final double x;
    private final double y;
    private final double z;

    public GroupRemovePayload(String group, String expression, Vec3 pos) {
        this.group = group;
        this.expression = expression;
        if (!(hasPos = pos != null)) pos = Vec3.ZERO;
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    private GroupRemovePayload(RegistryFriendlyByteBuf buf) {
        var mc = Minecraft.getInstance();
        assert mc.player != null;
        group = buf.readUtf();
        expression = readString(buf, buf.readBoolean(), null);
        hasPos = buf.readBoolean();
        x = readDouble(buf, hasPos, mc.player.getX());
        y = readDouble(buf, hasPos, mc.player.getY());
        z = readDouble(buf, hasPos, mc.player.getZ());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(group);
        var flag = validString(expression);
        buf.writeBoolean(flag);
        if (flag) buf.writeUtf(expression);
        buf.writeBoolean(hasPos);
        if (hasPos) {
            buf.writeDouble(x);
            buf.writeDouble(y);
            buf.writeDouble(z);
        }
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> GroupUtil.remove(group, expression, x, y, z));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, GroupRemovePayload::handle);
    }
}
