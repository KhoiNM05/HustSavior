package io.github.HustSavior.ui;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.HustSavior.Play;
import io.github.HustSavior.sound.MusicPlayer;

public class PlayButton extends Button {
    private String name = "play";
    private Game game;

    public PlayButton(Game game) {
        super(new Skin(Gdx.files.internal("UI/play/play.json")));
        this.game = game;
        
        // Set size maintaining aspect ratio
        float baseWidth = 200f;
        float baseHeight = 80f;
        float aspectRatio = baseWidth / baseHeight;
        
        float buttonWidth = Gdx.graphics.getWidth() * 0.12f; // reduced from 0.2f to 0.12f
        float buttonHeight = buttonWidth / aspectRatio;
        
        setSize(buttonWidth, buttonHeight);
        updatePosition();

        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Stop main menu music before transitioning
                MusicPlayer.getInstance().stop();
                game.setScreen(new Play(game));
            }
        });
    }

    // Add this method to update position
    public void updatePosition() {
        // Center the button on screen
        setPosition(
            Gdx.graphics.getWidth() / 2f - getWidth() / 2,
            Gdx.graphics.getHeight() / 2f - getHeight() / 2
        );
    }

    public String getName() {
        return name;
    }
}
