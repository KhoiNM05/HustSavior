package io.github.HustSavior.sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Music currentMusic;
    private boolean isLooping = true;
    
    private MusicPlayer() {}
    
    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }
    
    public void playMusic(String musicPath) {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
        }
        
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
        currentMusic.setLooping(isLooping);
        updateVolume();
        currentMusic.play();
    }
    
    public void updateVolume() {
        if (currentMusic != null) {
            float finalVolume = SoundManager.getInstance().getMasterVolume() 
                              * SoundManager.getInstance().getMusicVolume();
            currentMusic.setVolume(finalVolume);
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
} 