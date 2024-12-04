package io.github.HustSavior.sound;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import java.util.HashMap;

public class SfxPlayer {
    private static SfxPlayer instance;
    private HashMap<String, Sound> soundCache;
    
    private SfxPlayer() {
        soundCache = new HashMap<>();
    }
    
    public static SfxPlayer getInstance() {
        if (instance == null) {
            instance = new SfxPlayer();
        }
        return instance;
    }
    
    public void playSound(String soundPath) {
        Sound sound = getSound(soundPath);
        if (sound != null) {
            float finalVolume = SoundManager.getInstance().getMasterVolume() 
                              * SoundManager.getInstance().getSfxVolume();
            sound.play(finalVolume);
        }
    }
    
    private Sound getSound(String soundPath) {
        if (!soundCache.containsKey(soundPath)) {
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
                soundCache.put(soundPath, sound);
            } catch (Exception e) {
                Gdx.app.error("SfxPlayer", "Error loading sound: " + soundPath, e);
                return null;
            }
        }
        return soundCache.get(soundPath);
    }
    
    public void updateVolume() {
        // For future implementation if needed
        // Could be used to update currently playing sound effects
    }
    
    public void dispose() {
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }
} 