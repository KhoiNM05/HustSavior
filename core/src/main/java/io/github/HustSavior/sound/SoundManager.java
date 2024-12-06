package io.github.HustSavior.sound;

public class SoundManager {
    private static SoundManager instance;
    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float sfxVolume = 1.0f;
    
    private SoundManager() {}
    
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
        this.masterVolume = Math.clamp(volume, 0f, 1f);
        updateVolumes();
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.clamp(volume, 0f, 1f);
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
    }
} 