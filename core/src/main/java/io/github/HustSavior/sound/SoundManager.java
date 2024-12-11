package io.github.HustSavior.sound;

import com.badlogic.gdx.Gdx;

public class SoundManager {
    private static SoundManager instance;
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    
    private SoundManager() {
        masterVolume = 1.0f;
        musicVolume = 5.0f;
        sfxVolume = 1.0f;
        updateVolumes();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(5f, volume));
        updateVolumes();
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        updateVolumes();
    }
    
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.clamp(volume, 0f, 1f);
        updateVolumes();
    }
    
    private void updateVolumes() {
        MusicPlayer.getInstance().updateVolume();
        SfxPlayer.getInstance().updateVolume();
        Gdx.app.log("SoundManager", "Master Volume: " + masterVolume);
        Gdx.app.log("SoundManager", "Music Volume: " + musicVolume);
    }
} 