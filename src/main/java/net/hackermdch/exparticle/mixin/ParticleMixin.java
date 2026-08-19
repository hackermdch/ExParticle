package net.hackermdch.exparticle.mixin;

import net.hackermdch.exparticle.util.ClientMessageUtil;
import net.hackermdch.exparticle.util.IExecutable;
import net.hackermdch.exparticle.util.IParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(Particle.class)
public abstract class ParticleMixin implements IParticle {
    @Unique
    private IExecutable exe;
    @Unique
    private double step;
    @Unique
    private double centerX;
    @Unique
    private double centerY;
    @Unique
    private double centerZ;
    @Unique
    private boolean customMove;
    @Unique
    private boolean stop;
    @Unique
    private double moveT;
    @Unique
    private double preX;
    @Unique
    private double preY;
    @Unique
    private double preZ;
    @Unique
    private boolean managed;
    @Unique
    private double customSize = Double.NaN;
    @Unique
    private double customLight = Double.NaN;
    @Unique
    private Supplier<Vec3> bindCenter;

    @Override
    public void setExe(IExecutable exe) {
        this.exe = exe;
    }

    @Override
    public void setStep(double step) {
        this.step = step;
    }

    @Override
    public double getCenterX() {
        return this.centerX;
    }

    @Override
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    @Override
    public double getCenterY() {
        return this.centerY;
    }

    @Override
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    @Override
    public double getCenterZ() {
        return this.centerZ;
    }

    @Override
    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }

    @Override
    public void setCustomMove(boolean customMove) {
        this.customMove = customMove;
    }

    @Override
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void setCustomSize(double size) {
        this.customSize = size * 0.125;
    }

    @Override
    public double getCustomSize() {
        return this.customSize;
    }

    @Override
    public void setCustomLight(double light) {
        customLight = Double.isNaN(light) ? Double.NaN : ((int) (light * 255) & 0xFF) / 255.0;
    }

    @Override
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    @Override
    public void setFriction(float friction) {
        this.friction = friction;
    }

    @Override
    public void customTick() {
        preX = x;
        preY = y;
        preZ = z;
        tick();
        if (stop) setPos(preX, preY, preZ);
        customMove();
    }

    @Unique
    protected void customMove() {
        if (customMove && exe != null) {
            var data = exe.getData();
            if (moveT == 0.0) {
                data.cx = centerX;
                data.cy = centerY;
                data.cz = centerZ;
                data.dx = x - centerX;
                data.dy = y - centerY;
                data.dz = z - centerZ;
                data.ddis = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) + (z - centerZ) * (z - centerZ));
                data.ds1 = Math.atan2(z - centerZ, x - centerX);
                data.ds2 = Math.atan2(y - centerY, Math.hypot(x - centerX, z - centerZ));
            }
            data.vx = xd;
            data.vy = yd;
            data.vz = zd;
            data.x = x - centerX;
            data.y = y - centerY;
            data.z = z - centerZ;
            data.size = customSize;
            data.cr = rCol;
            data.cg = gCol;
            data.cb = bCol;
            data.alpha = alpha;
            data.light = customLight;
            data.dis = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) + (z - centerZ) * (z - centerZ));
            data.s1 = Math.atan2(z - centerZ, x - centerX);
            data.s2 = Math.atan2(y - centerY, Math.hypot(x - centerX, z - centerZ));
            data.t = moveT;
            moveT += step;
            try {
                exe.invoke();
            } catch (RuntimeException e) {
                ClientMessageUtil.addChatMessage(e);
                remove();
                return;
            }
            if (data.destroy != 0.0) {
                remove();
                return;
            }
            if (!Double.isNaN(data.size) && data.size != customSize) setCustomSize(data.size);
            if (!Double.isNaN(data.light) && data.light != customLight) setCustomLight(data.light);
            if (bindCenter != null) {
                var nc = bindCenter.get();
                var dis = nc.subtract(centerX, centerY, centerZ);
                data.vx = nanToZero(data.vx) + dis.x;
                data.vy = nanToZero(data.vy) + dis.y;
                data.vz = nanToZero(data.vz) + dis.z;
                centerX = nc.x;
                centerY = nc.y;
                centerZ = nc.z;
                data.cx = centerX;
                data.cy = centerY;
                data.cz = centerZ;
            }
            if (!Double.isNaN(data.vx) || !Double.isNaN(data.vy) || !Double.isNaN(data.vz)) {
                setPos(preX, preY, preZ);
                data.vx = nanToZero(data.vx);
                data.vy = nanToZero(data.vy);
                data.vz = nanToZero(data.vz);
                move(data.vx, data.vy, data.vz);
            }
            rCol = (float) data.cr;
            gCol = (float) data.cg;
            bCol = (float) data.cb;
            alpha = (float) data.alpha;
        }
    }

    @Override
    public void setManaged(boolean val) {
        managed = val;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    @Override
    public void setBind(Supplier<Vec3> provider) {
        bindCenter = provider;
    }

    @Unique
    private double nanToZero(double num) {
        return !Double.isNaN(num) ? num : (double) 0.0F;
    }

    @Inject(method = "getLightColor", at = @At("HEAD"), cancellable = true)
    protected void onGetLightColor(float partialTick, CallbackInfoReturnable<Integer> cir) {
        double custom = customLight;
        if (!Double.isNaN(custom)) {
            int block = (int) (custom * 15);
            int sky = (int) (custom * 15);
            cir.setReturnValue((sky << 20) | (block << 4));
        }
    }

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract void move(double var1, double var3, double var5);

    @Shadow
    public abstract void remove();

    @Shadow
    public abstract void setPos(double var1, double var3, double var5);

    @Shadow
    public double x;
    @Shadow
    public double y;
    @Shadow
    public double z;
    @Shadow
    public double xd;
    @Shadow
    public double yd;
    @Shadow
    public double zd;
    @Shadow
    public float rCol = 1.0F;
    @Shadow
    public float gCol = 1.0F;
    @Shadow
    public float bCol = 1.0F;
    @Shadow
    public float alpha = 1.0F;
    @Shadow
    protected float gravity;
    @Shadow
    protected float friction;
}
