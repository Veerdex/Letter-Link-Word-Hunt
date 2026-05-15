package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.grantkoupal.letterlink.quantum.audio.SoundEffect;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {
    public static List<SoundEffect> tileSelectSounds = new ArrayList<>();
    public static List<SoundEffect> correctSounds = new ArrayList<>();
    public static SoundEffect tickingClock = new SoundEffect(Gdx.audio.newSound(Gdx.files.internal("Audio/SoundEffects/Ticking Clock (Loop).wav")));

    public static void update(){
        while(!tileSelectSounds.isEmpty()){
            tileSelectSounds.get(0).dispose();
            tileSelectSounds.remove(0);
        }
        tileSelectSounds = loadSoundEffects("Tile Select Sounds");
        while(!correctSounds.isEmpty()){
            correctSounds.get(0).dispose();
            correctSounds.remove(0);
        }
        correctSounds = loadSoundEffects("Tile Correct Sounds");
    }

    public static List<SoundEffect> loadSoundEffects(String path) {
        List<SoundEffect> soundEffects = new ArrayList<>();

        String folderPath = "Audio/SoundEffects/" + ThemeManager.currentTheme + "/" + path + "/";
        FileHandle dataFile = Gdx.files.internal(folderPath + "Data.txt");

        if (!dataFile.exists()) {
            throw new RuntimeException("Missing Data.txt at: " + folderPath + "Data.txt");
        }

        String[] lines = dataFile.readString("UTF-8").split("\\r?\\n");

        for (String line : lines) {
            String fileName = line.trim();

            if (fileName.isEmpty()) {
                continue;
            }

            FileHandle soundFile = Gdx.files.internal(folderPath + fileName);

            if (!soundFile.exists()) {
                throw new RuntimeException("Missing sound file: " + folderPath + fileName);
            }

            Sound sound = Gdx.audio.newSound(soundFile);
            soundEffects.add(new SoundEffect(sound));
        }

        return soundEffects;
    }

    public static void disposeSounds(){
        while(!tileSelectSounds.isEmpty()){
            tileSelectSounds.get(0).dispose();
            tileSelectSounds.remove(0);
        }
    }
}
