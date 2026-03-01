package net.hackermdch.exparticle.mixin;

import net.hackermdch.exparticle.util.IParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleAnimatedParticle.class)
public class SimpleAnimatedParticleMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/SimpleAnimatedParticle;setAlpha(F)V"), cancellable = true)
    private void onSetAlpha(CallbackInfo ci) {
        Particle self = (Particle) (Object) this;
        if (((IParticle) self).isManualControl()) {
            ci.cancel(); // 跳过 setAlpha 及后续颜色渐变
        }
    }
}