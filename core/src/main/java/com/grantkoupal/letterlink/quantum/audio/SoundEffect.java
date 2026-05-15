package com.grantkoupal.letterlink.quantum.audio;

import com.badlogic.gdx.audio.Sound;

public class SoundEffect {
    private final Sound s;
    private long density = 0;
    private long previousTime = 0;

    public SoundEffect(Sound s){
        this.s = s;
    }

    public void render(float volume, float pitch, float pan){
        long currentTime = System.currentTimeMillis();
        if(currentTime > previousTime + density){
            previousTime = currentTime;
            long id = s.play(volume, pitch, pan);
            s.setVolume(id, volume);
        }
    }

    public void setMaxDensity(long density){
        this.density = density;
    }

    public void dispose(){
        s.dispose();
    }
}
