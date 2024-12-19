package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.ImageUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class ClearCachePayload implements CustomPacketPayload {
    private static final Type<ClearCachePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "clear_cache"));
    private static final StreamCodec<RegistryFriendlyByteBuf, ClearCachePayload> CODEC = StreamCodec.ofMember(ClearCachePayload::write, ClearCachePayload::new);

    public ClearCachePayload() {
    }

    private ClearCachePayload(RegistryFriendlyByteBuf buf) {
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(ImageUtil::clear);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, ClearCachePayload::handle);
    }
}
