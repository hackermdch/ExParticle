package net.hackermdch.exparticle.mixin;

import net.hackermdch.exparticle.util.IParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin implements IParticle {
    @Shadow protected float quadSize;

    @Inject(method = "getQuadSize", at = @At("HEAD"), cancellable = true)
    private void onGetQuadSize(float scaleFactor, CallbackInfoReturnable<Float> cir) {
        double custom = this.getCustomSize();
        if (!Double.isNaN(custom)) {
            cir.setReturnValue((float) custom);
        }
    }
}