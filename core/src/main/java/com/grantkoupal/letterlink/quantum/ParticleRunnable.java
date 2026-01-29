package com.grantkoupal.letterlink.quantum;

public interface ParticleRunnable {
    /**
     * Return true to remove particle
     */
    public boolean run(Particle.ParticleInstance pi, float delta, float speed, float time);
}
