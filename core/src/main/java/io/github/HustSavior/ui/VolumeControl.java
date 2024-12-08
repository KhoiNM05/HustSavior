package io.github.HustSavior.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import io.github.HustSavior.sound.SoundManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class VolumeControl {
    private Label masterVolumeLabel;
    private Label masterVolumeValue;
    private TextButton masterIncreaseButton;
    private TextButton masterDecreaseButton;
    private Button masterMuteButton;

    private Label musicVolumeLabel;
    private Label musicVolumeValue;
    private TextButton musicIncreaseButton;
    private TextButton musicDecreaseButton;
    private Button musicMuteButton;

    private Label sfxVolumeLabel;
    private Label sfxVolumeValue;
    private TextButton sfxIncreaseButton;
    private TextButton sfxDecreaseButton;
    private Button sfxMuteButton;

    private static final float VOLUME_STEP = 0.1f;

    private Table parentTable;

    public VolumeControl() {
        // Different skins for each control type
        Skin masterSkin = new Skin(Gdx.files.internal("UI/volume/master/masterVolumeControl.json"));
        Skin musicSkin = new Skin(Gdx.files.internal("UI/volume/music/musicVolumeControl.json"));
        Skin sfxSkin = new Skin(Gdx.files.internal("UI/volume/sfx/sfxVolumeControl.json"));
       // Skin increaseSkin = new Skin(Gdx.files.internal("UI/volume/increase/increaseVolumeControl.json"));
       // Skin decreaseSkin = new Skin(Gdx.files.internal("UI/volume/decrease/decreaseVolumeControl.json"));
       // Skin muteSkin = new Skin(Gdx.files.internal("UI/volume/mute/muteVolumeControl.json"));
        // Master volume controls
        masterVolumeLabel = new Label("Master Volume:", masterSkin);
        masterVolumeValue = new Label("100%", masterSkin);
        masterDecreaseButton = new TextButton("-", masterSkin);
        masterIncreaseButton = new TextButton("+", masterSkin);
        updateMasterVolumeLabel();
        // Music volume controls
        musicVolumeLabel = new Label("Music Volume:", musicSkin);
        musicVolumeValue = new Label("100%", musicSkin);
        musicDecreaseButton = new TextButton("-", musicSkin);
        musicIncreaseButton = new TextButton("+", musicSkin);
        updateMusicVolumeLabel();

        // SFX volume controls
        sfxVolumeLabel = new Label("SFX Volume:", sfxSkin);
        sfxVolumeValue = new Label("100%", sfxSkin);
        sfxDecreaseButton = new TextButton("-", sfxSkin);
        sfxIncreaseButton = new TextButton("+", sfxSkin);
        updateSfxVolumeLabel();

        // Create mute buttons
        masterMuteButton = new Button(masterSkin);
        musicMuteButton = new Button(musicSkin);
        sfxMuteButton = new Button(sfxSkin);

        setupListeners();
    }

    private void setupListeners() {
        // Master volume controls
        masterIncreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMasterVolume();
                if (currentVolume < 1.0f) {
                    SoundManager.getInstance().setMasterVolume(currentVolume + VOLUME_STEP);
                    updateMasterVolumeLabel();
                }
            }
        });

        masterDecreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMasterVolume();
                if (currentVolume > 0.0f) {
                    SoundManager.getInstance().setMasterVolume(currentVolume - VOLUME_STEP);
                    updateMasterVolumeLabel();
                }
            }
        });

        masterVolumeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMasterVolume();
                SoundManager.getInstance().setMasterVolume(currentVolume > 0 ? 0 : 1);
                updateMasterVolumeLabel();
            }
        });

        // Master mute button
        masterMuteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isMuted = SoundManager.getInstance().getMasterVolume() == 0;
                SoundManager.getInstance().setMasterVolume(isMuted ? 1 : 0);
                updateMasterVolumeLabel();
            }
        });

        // Music volume controls
        musicIncreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMusicVolume();
                if (currentVolume < 1.0f) {
                    SoundManager.getInstance().setMusicVolume(currentVolume + VOLUME_STEP);
                    updateMusicVolumeLabel();
                }
            }
        });

        musicDecreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMusicVolume();
                if (currentVolume > 0.0f) {
                    SoundManager.getInstance().setMusicVolume(currentVolume - VOLUME_STEP);
                    updateMusicVolumeLabel();
                }
            }
        });

        musicVolumeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getMusicVolume();
                SoundManager.getInstance().setMusicVolume(currentVolume > 0 ? 0 : 1);
                updateMusicVolumeLabel();
            }
        });

        // Music mute button
        musicMuteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isMuted = SoundManager.getInstance().getMusicVolume() == 0;
                SoundManager.getInstance().setMusicVolume(isMuted ? 1 : 0);
                updateMusicVolumeLabel();
            }
        });

        // SFX volume controls
        sfxIncreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getSfxVolume();
                if (currentVolume < 1.0f) {
                    SoundManager.getInstance().setSfxVolume(currentVolume + VOLUME_STEP);
                    updateSfxVolumeLabel();
                }
            }
        });

        sfxDecreaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getSfxVolume();
                if (currentVolume > 0.0f) {
                    SoundManager.getInstance().setSfxVolume(currentVolume - VOLUME_STEP);
                    updateSfxVolumeLabel();
                }
            }
        });

        sfxVolumeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float currentVolume = SoundManager.getInstance().getSfxVolume();
                SoundManager.getInstance().setSfxVolume(currentVolume > 0 ? 0 : 1);
                updateSfxVolumeLabel();
            }
        });

        // SFX mute button
        sfxMuteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isMuted = SoundManager.getInstance().getSfxVolume() == 0;
                SoundManager.getInstance().setSfxVolume(isMuted ? 1 : 0);
                updateSfxVolumeLabel();
            }
        });
    }

    public void addToTable(Table table) {
        this.parentTable = table;
        updateTableLayout();
    }

    private void updateTableLayout() {
        if (parentTable == null) return;

        parentTable.add(masterVolumeLabel).pad(10);
        parentTable.add(masterMuteButton).pad(10);
        parentTable.add(masterVolumeValue).pad(10);
        parentTable.add(masterDecreaseButton).pad(10);
        parentTable.add(masterIncreaseButton).pad(10).row();

        parentTable.add(musicVolumeLabel).pad(10);
        parentTable.add(musicMuteButton).pad(10);
        parentTable.add(musicVolumeValue).pad(10);
        parentTable.add(musicDecreaseButton).pad(10);
        parentTable.add(musicIncreaseButton).pad(10).row();

        parentTable.add(sfxVolumeLabel).pad(10);
        parentTable.add(sfxMuteButton).pad(10);
        parentTable.add(sfxVolumeValue).pad(10);
        parentTable.add(sfxDecreaseButton).pad(10);
        parentTable.add(sfxIncreaseButton).pad(10).row();
    }

    public void dispose() {
        // Add any cleanup code here if needed
    }

    public void updateMasterVolumeLabel() {
        int percentage = Math.round(SoundManager.getInstance().getMasterVolume() * 100);
        masterVolumeValue.setText(percentage + "%");
    }

    public void updateMusicVolumeLabel() {
        int percentage = Math.round(SoundManager.getInstance().getMusicVolume() * 100);
        musicVolumeValue.setText(percentage + "%");
    }

    public void updateSfxVolumeLabel() {
        int percentage = Math.round(SoundManager.getInstance().getSfxVolume() * 100);
        sfxVolumeValue.setText(percentage + "%");
    }
}
