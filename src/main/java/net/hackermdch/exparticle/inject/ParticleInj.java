package net.hackermdch.exparticle.inject;

import net.hackermdch.exparticle.util.IExecutable;
import net.hackermdch.exparticle.util.IParticle;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Supplier;

public interface ParticleInj extends IParticle {
    @Override
    default void setExe(IExecutable var1) {
        throw new NotImplementedException();
    }

    @Override
    default void setStep(double var1) {
        throw new NotImplementedException();
    }

    @Override
    default double getCenterX() {
        throw new NotImplementedException();
    }

    @Override
    default void setCenterX(double var1) {
        throw new NotImplementedException();
    }

    @Override
    default double getCenterY() {
        throw new NotImplementedException();
    }

    @Override
    default void setCenterY(double var1) {
        throw new NotImplementedException();
    }

    @Override
    default double getCenterZ() {
        throw new NotImplementedException();
    }

    @Override
    default void setCenterZ(double var1) {
        throw new NotImplementedException();
    }

    @Override
    default void setCustomMove(boolean var1) {
        throw new NotImplementedException();
    }

    @Override
    default void setStop(boolean var1) {
        throw new NotImplementedException();
    }

    @Override
    default void customTick() {
        throw new NotImplementedException();
    }

    @Override
    default void setManaged(boolean val) {
        throw new NotImplementedException();
    }

    @Override
    default boolean isManaged() {
        throw new NotImplementedException();
    }

    @Override
    default void setCustomSize(double size) {
        throw new NotImplementedException();
    }

    @Override
    default double getCustomSize() {
        throw new NotImplementedException();
    }

    @Override
    default void setCustomLight(double light) {
        throw new NotImplementedException();
    }

    @Override
    default void setGravity(float gravity) {
        throw new NotImplementedException();
    }

    @Override
    default void setFriction(float friction) {
        throw new NotImplementedException();
    }

    @Override
    default void setBind(Supplier<Vec3> provider) {
        throw new NotImplementedException();
    }
}
