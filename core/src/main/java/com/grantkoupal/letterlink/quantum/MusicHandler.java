package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.audio.Music;

public class MusicHandler{

    private static float transitionTime = 2f;

    private static boolean transitioning = false;

    private static float volume = 1;

    private static Music nextMusic;
    private static Music currentMusic;

    public static Music getCurrentMusic(){
        return currentMusic;
    }

    public static void updateVolume(){
        if(currentMusic != null){
            currentMusic.setVolume(Manager.getMasterVolume() * Manager.getMusicVolume());
        }
        volume = Manager.getMasterVolume() * Manager.getMusicVolume();
    }

    public static void play(Music m){
        if(!transitioning) return;
        if(currentMusic == null){
            currentMusic = m;
            m.play();
            m.setVolume(volume);
        } else{
            transitioning = true;
            nextMusic = m;
            m.play();
            transition();
        }
    }

    public static void loop(Music m){
        if(transitioning) return;
        if(currentMusic == null){
            currentMusic = m;
            m.play();
            m.setLooping(true);
            m.setVolume(volume);
        } else{
            transitioning = true;
            nextMusic = m;
            m.play();
            m.setLooping(true);
            transition();
        }
    }

    private static void transition(){
        final float transitionAmount = 1 / transitionTime * 100 * volume;
        nextMusic.setVolume(0);
        Timer t = new Timer(.01f, (int)transitionTime * 100, new TimeFrame(){
            @Override
            public void run(long iteration) {
                float nextValue = nextMusic.getVolume() + transitionAmount;
                if(nextValue > volume){
                    nextValue = volume;
                }
                nextMusic.setVolume(nextValue);
                nextValue = currentMusic.getVolume() - transitionAmount;
                if(nextValue < 0){
                    nextValue = 0;
                }
                currentMusic.setVolume(nextValue);
            }

        });
        t.onEnd(new Runnable(){
            @Override
            public void run(){
                nextMusic.setVolume(1);
                currentMusic.dispose();
                currentMusic = nextMusic;
                nextMusic = null;
                transitioning = false;
            }
        });
        Manager.addTimer(t);
    }

    public static void setTransitionTime(float f){
        transitionTime = f;
    }

    public static float getTransitionTime(){
        return transitionTime;
    }

    public static boolean isPlaying(){
        return currentMusic.isPlaying();
    }

    public static void dispose(){
        if(nextMusic != null){
            nextMusic.dispose();
        }
        if(currentMusic != null){
            currentMusic.dispose();
        }
    }
}
