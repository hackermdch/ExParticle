package net.hackermdch.exparticle.api;

import net.hackermdch.exparticle.util.IExecutable;
import net.hackermdch.exparticle.util.ParticleStruct;

import java.util.function.ToIntFunction;

public final class CustomEvaluator implements IExecutable {
    private final ParticleStruct struct = new ParticleStruct();
    private final ToIntFunction<ParticleStruct> function;

    private CustomEvaluator(ToIntFunction<ParticleStruct> function) {
        this.function = function;
    }

    public static CustomEvaluator of(ToIntFunction<ParticleStruct> function) {
        return new CustomEvaluator(function);
    }

    @Override
    public ParticleStruct getData() {
        return struct;
    }

    @Override
    public int invoke() {
        return function.applyAsInt(struct);
    }
}
