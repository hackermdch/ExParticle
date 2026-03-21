package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.UserFunction;
import net.hackermdch.exparticle.util.UserFunctionUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class UserFunctionPayload implements CustomPacketPayload {
    private static final Type<UserFunctionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "user_function"));
    private static final StreamCodec<RegistryFriendlyByteBuf, UserFunctionPayload> CODEC = StreamCodec.ofMember(UserFunctionPayload::write, UserFunctionPayload::new);
    private final boolean define;
    private final String name;
    private final String args;
    private final String body;

    public UserFunctionPayload(boolean define, String name, String args, String body) {
        this.define = define;
        this.name = name;
        this.args = args;
        this.body = body;
    }

    private UserFunctionPayload(RegistryFriendlyByteBuf buf) {
        define = buf.readBoolean();
        name = buf.readUtf();
        args = define ? buf.readUtf() : "";
        body = define ? buf.readUtf() : "";
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(define);
        buf.writeUtf(name);
        if (define) {
            buf.writeUtf(args);
            buf.writeUtf(body);
        }
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (define) UserFunctionUtil.define(name, UserFunction.create(name, args, body));
            else UserFunctionUtil.undefine(name);
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, UserFunctionPayload::handle);
    }
}
