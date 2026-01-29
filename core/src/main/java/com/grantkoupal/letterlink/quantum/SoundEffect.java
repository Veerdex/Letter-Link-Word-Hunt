package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundEffect {
    private Sound s;
    private long density = 0;
    private long previousTime = 0;

    public SoundEffect(String path){
        s = Gdx.audio.newSound(Gdx.files.internal(path));
    }

    public void render(float volume, float pitch, float pan){
        if(System.currentTimeMillis() > previousTime + density){
            previousTime = System.currentTimeMillis();
            long id = s.play(volume, pitch, pan);
            s.setVolume(id, Manager.getMasterVolume() * Manager.getSFXVolume());
        }
    }

    public void setMaxDensity(long density){
        this.density = density;
    }

    public void dispose(){
        s.dispose();
    }
}
