package net.hackermdch.exparticle.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static net.hackermdch.exparticle.ExParticle.MOD_ID;

public class Networking {
    private static void register(PayloadRegistrar registrar) {
        ClearParticlePayload.register(registrar);
        ClearCachePayload.register(registrar);
        NormalPayload.register(registrar);
        ConditionalPayload.register(registrar);
        ParameterPayload.register(registrar);
        ImagePayload.register(registrar);
        ImageMatrixPayload.register(registrar);
        VideoPayload.register(registrar);
        VideoMatrixPayload.register(registrar);
        GroupRemovePayload.register(registrar);
        GroupChangePayload.register(registrar);
    }

    public static void register(IEventBus bus) {
        bus.addListener((RegisterPayloadHandlersEvent e) -> register(e.registrar(MOD_ID)));
    }
}
