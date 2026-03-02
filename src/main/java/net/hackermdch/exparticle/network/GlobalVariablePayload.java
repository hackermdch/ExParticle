package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.GlobalVariableUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class GlobalVariablePayload implements CustomPacketPayload {
    private static final Type<GlobalVariablePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "global_variable"));
    private static final StreamCodec<RegistryFriendlyByteBuf, GlobalVariablePayload> CODEC = StreamCodec.ofMember(GlobalVariablePayload::write, GlobalVariablePayload::new);
    private final int op;
    private final int type;
    private final String name;
    private final Object value;

    public GlobalVariablePayload(int op, int type, String name, Object value) {
        this.op = op;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    private GlobalVariablePayload(RegistryFriendlyByteBuf buf) {
        op = buf.readInt();
        type = buf.readInt();
        name = buf.readUtf();
        Object v = null;
        if (op == 1) {
            v = switch (type) {
                case 1 -> buf.readInt();
                case 2 -> buf.readDouble();
                default -> throw new IllegalArgumentException();
            };
        }
        value = v;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(op);
        buf.writeInt(type);
        buf.writeUtf(name);
        if (op == 1) {
            switch (type) {
                case 1 -> buf.writeInt((int) value);
                case 2 -> buf.writeDouble((double) value);
            }
        }
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            switch (op) {
                case 1 -> GlobalVariableUtil.define(name, switch (type) {
                    case 1 -> GlobalVariableUtil.Type.Integer;
                    case 2 -> GlobalVariableUtil.Type.Double;
                    case 3 -> GlobalVariableUtil.Type.Quaternion;
                    default -> throw new IllegalArgumentException();
                }, value);
                case 2 -> GlobalVariableUtil.undefine(name);
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, GlobalVariablePayload::handle);
    }
}
