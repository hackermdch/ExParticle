package net.hackermdch.exparticle.mixin;

import net.hackermdch.exparticle.ExParticleConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Redirect(method = "tickParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At(value = "INVOKE", target = "net.minecraft.client.particle.Particle.tick()V"))
    private void redirectTickParticle(Particle particle) {
        particle.customTick();
    }

    @ModifyArg(method = "lambda$tick$11", at = @At(value = "INVOKE", target = "com.google.common.collect.EvictingQueue.create(I)Lcom/google/common/collect/EvictingQueue;", remap = false))
    private static int modifyArgTick(int maxParticleCount) {
        return ExParticleConfig.config.maxParticleCount;
    }

    @Inject(method = "updateCount", at = @At("HEAD"), cancellable = true)
    private void updateCount(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "tickParticleList", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"), cancellable = true)
    private void tickParticleList(Collection<Particle> particles, CallbackInfo ci) {
        if (ExParticleConfig.config.ParallelParticleUpdate) {
            particles.parallelStream().forEach(this::tickParticle);
            particles.removeIf(pex -> !pex.isAlive());
            ci.cancel();
        }
    }

    @Shadow
    protected abstract void tickParticle(Particle particle);
}
