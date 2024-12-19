package net.hackermdch.exparticle.inject;

import net.hackermdch.exparticle.util.IExecutable;
import net.hackermdch.exparticle.util.IParticle;
import org.apache.commons.lang3.NotImplementedException;

public interface ParticleInj extends IParticle {
    default void setExe(IExecutable var1) {
        throw new NotImplementedException();
    }

    default void setStep(double var1) {
        throw new NotImplementedException();
    }

    default double getCenterX() {
        throw new NotImplementedException();
    }

    default void setCenterX(double var1) {
        throw new NotImplementedException();
    }

    default double getCenterY() {
        throw new NotImplementedException();
    }

    default void setCenterY(double var1) {
        throw new NotImplementedException();
    }

    default double getCenterZ() {
        throw new NotImplementedException();
    }

    default void setCenterZ(double var1) {
        throw new NotImplementedException();
    }

    default void setCustomMove(boolean var1) {
        throw new NotImplementedException();
    }

    default void setStop(boolean var1) {
        throw new NotImplementedException();
    }

    default void customTick() {
        throw new NotImplementedException();
    }
}
