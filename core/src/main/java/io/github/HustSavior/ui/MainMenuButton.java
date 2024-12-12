package io.github.HustSavior.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.HustSavior.screen.MainMenuScreen;

public class MainMenuButton extends Button {
    protected Game game;

    public MainMenuButton(Game game) {
        super(new Skin(Gdx.files.internal("UI/mainMenu/mainMenuButton.json")));
        this.game = game;
        
        // Reduced scale factor
        float baseSize = 60f;
        float scaleFactor = Gdx.graphics.getHeight() * 0.05f / baseSize; // reduced from 0.08f to 0.05f
        setSize(baseSize * scaleFactor, baseSize * scaleFactor);
        
        updatePosition();

        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Get current screen and dispose if it's Play screen
                Screen currentScreen = game.getScreen();
                if (currentScreen != null) {
                    currentScreen.dispose();
                }
                game.setScreen(new MainMenuScreen(game));
            }
        });
    }

    public MainMenuButton(Skin skin) {
        super(skin);
    }

    public void updatePosition() {
        setPosition(20, Gdx.graphics.getHeight() - getHeight() - 20);
    }
} 