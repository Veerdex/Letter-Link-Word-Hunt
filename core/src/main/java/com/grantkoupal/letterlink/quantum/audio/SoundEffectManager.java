package com.grantkoupal.letterlink.quantum.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SoundEffectManager {

    private final HashMap<String, Entry> soundMap = new HashMap<>();

    private static class Entry {
        String path;
        Sound sound;
        SoundEffect effect;

        Entry(String path, Sound sound, SoundEffect effect){
            this.path = path;
            this.sound = sound;
            this.effect = effect;
        }
    }

    public SoundEffect get(String name){
        Entry entry = soundMap.get(name);
        return entry == null ? null : entry.effect;
    }

    public void loadNewSoundSet(String dataLocation, String... data){
        if(data.length % 2 != 0){
            throw new IllegalArgumentException("Data must be Name, Path pairs.");
        }

        // requested name -> path
        Map<String, String> requested = new LinkedHashMap<>();
        for(int i = 0; i < data.length; i += 2){
            requested.put(data[i], dataLocation + data[i + 1]);
        }

        // old reusable sounds by path
        Map<String, Sound> reusableSounds = new HashMap<>();
        for(Entry entry : soundMap.values()){
            reusableSounds.putIfAbsent(entry.path, entry.sound);
        }

        // build new map
        HashMap<String, Entry> newSoundMap = new HashMap<>();

        for(Map.Entry<String, String> request : requested.entrySet()){
            String name = request.getKey();
            String path = request.getValue();

            Sound sound = reusableSounds.get(path);
            if(sound == null){
                sound = Gdx.audio.newSound(Gdx.files.internal(path));
            }

            SoundEffect effect = new SoundEffect(sound);
            newSoundMap.put(name, new Entry(path, sound, effect));
        }

        // dispose old sounds no longer needed
        Set<String> neededPaths = new HashSet<>(requested.values());
        Set<Sound> disposed = Collections.newSetFromMap(new IdentityHashMap<>());

        for(Entry entry : soundMap.values()){
            if(!neededPaths.contains(entry.path) && disposed.add(entry.sound)){
                entry.sound.dispose();
            }
        }

        soundMap.clear();
        soundMap.putAll(newSoundMap);
    }

    public void dispose(){
        Set<Sound> disposed = Collections.newSetFromMap(new IdentityHashMap<>());
        for(Entry entry : soundMap.values()){
            if(disposed.add(entry.sound)){
                entry.sound.dispose();
            }
        }
        soundMap.clear();
    }
}
