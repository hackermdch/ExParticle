package net.hackermdch.exparticle.network;

import net.hackermdch.exparticle.util.GroupUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class ClearParticlePayload implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<ClearParticlePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "clear_particle"));
    private static final StreamCodec<RegistryFriendlyByteBuf, ClearParticlePayload> CODEC = StreamCodec.ofMember(ClearParticlePayload::write, ClearParticlePayload::new);

    public ClearParticlePayload() {
    }

    private ClearParticlePayload(RegistryFriendlyByteBuf buf) {
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    private void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            GroupUtil.clear();
            var mc = Minecraft.getInstance();
            mc.particleEngine.setLevel(mc.level);
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(TYPE, CODEC, ClearParticlePayload::handle);
    }
}
