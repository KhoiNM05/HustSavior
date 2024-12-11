package io.github.HustSavior.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Music currentMusic;
    private boolean isLooping = true;
    
    private static final String MAIN_MENU_MUSIC = "sound/main_menu_sound.mp3";
    private static final String GAMEPLAY_MUSIC = "sound/gameplay_music.mp3";
    private static final String DEATH_MUSIC = "sound/death_sound.mp3";
    
    private MusicPlayer() {}
    
    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }
    
    public void playMusic(String musicPath) {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
            }
            
            Gdx.app.log("MusicPlayer", "Attempting to play music: " + musicPath);
            
            if (!Gdx.files.internal(musicPath).exists()) {
                Gdx.app.error("MusicPlayer", "Music file not found: " + musicPath);
                return;
            }
            
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
            if (currentMusic == null) {
                Gdx.app.error("MusicPlayer", "Failed to create Music instance");
                return;
            }
            
            currentMusic.setVolume(5.0f);  // Maximum volume
            currentMusic.setLooping(isLooping);
            currentMusic.play();
            
            Gdx.app.log("MusicPlayer", "Music started playing successfully");
            Gdx.app.log("MusicPlayer", "Current volume: " + currentMusic.getVolume());
            Gdx.app.log("MusicPlayer", "Is playing: " + currentMusic.isPlaying());
            
        } catch (Exception e) {
            Gdx.app.error("MusicPlayer", "Error playing music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void updateVolume() {
        if (currentMusic != null) {
            float baseVolume = 10f;  // Base volume for gameplay music
            float finalVolume = baseVolume * SoundManager.getInstance().getMasterVolume() 
                              * SoundManager.getInstance().getMusicVolume();
            currentMusic.setVolume(finalVolume);
            Gdx.app.log("MusicPlayer", "Volume set to: " + finalVolume);
        }
    }
    
    public void pause() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }
    
    public void resume() {
        if (currentMusic != null) {
            currentMusic.play();
        }
    }
    
    public void stop() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }
    
    public void dispose() {
        if (currentMusic != null) {
            currentMusic.dispose();
            currentMusic = null;
        }
    }
    
    public void setLooping(boolean looping) {
        this.isLooping = looping;
        if (currentMusic != null) {
            currentMusic.setLooping(looping);
        }
    }
    
    public void playMainMenuMusic() {
        playMusic(MAIN_MENU_MUSIC);
    }
    
    public void playGameplayMusic() {
        Gdx.app.log("MusicPlayer", "Starting gameplay music");
        try {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
            }
            
            if (!Gdx.files.internal(GAMEPLAY_MUSIC).exists()) {
                Gdx.app.error("MusicPlayer", "Gameplay music file not found at: " + GAMEPLAY_MUSIC);
                return;
            }
            
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(GAMEPLAY_MUSIC));
            if (currentMusic != null) {
                currentMusic.setVolume(1.0f);  // Set to normal volume (1.0 instead of 5.0)
                currentMusic.setLooping(true);
                currentMusic.play();
                Gdx.app.log("MusicPlayer", "Gameplay music started successfully");
                Gdx.app.log("MusicPlayer", "Current volume: " + currentMusic.getVolume());
                Gdx.app.log("MusicPlayer", "Is playing: " + currentMusic.isPlaying());
            } else {
                Gdx.app.error("MusicPlayer", "Failed to create music instance");
            }
        } catch (Exception e) {
            Gdx.app.error("MusicPlayer", "Error playing gameplay music", e);
            e.printStackTrace();
        }
    }
    
    public void playDeathMusic() {
        setLooping(false);
        playMusic(DEATH_MUSIC);
    }
    
    public float getCurrentVolume() {
        return currentMusic != null ? currentMusic.getVolume() : 0f;
    }
    
    public boolean isCurrentlyPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }
} 