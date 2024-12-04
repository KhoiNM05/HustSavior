package io.github.HustSavior.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SettingsButton extends Button {

    private String name = "settings";
    private SettingsWindow settingsWindow;
    private Stage stage;

    public SettingsButton(Stage stage) {
        super(new Skin(Gdx.files.internal("UI/settings/settingsButton.json")));
        this.stage = stage;
        
        // Reduced scale factor
        float baseSize = 80f;
        float scaleFactor = stage.getHeight() * 0.06f / baseSize; // reduced from 0.1f to 0.06f
        setSize(baseSize * scaleFactor, baseSize * scaleFactor);
        
        updatePosition();

        settingsWindow = new SettingsWindow(stage);

        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(!settingsWindow.isVisible());
            }
        });
    }

    public void updatePosition() {
        setPosition(
            Gdx.graphics.getWidth() - getWidth() - 20,
            Gdx.graphics.getHeight() - getHeight() - 20
        );
        if (settingsWindow != null) {
            settingsWindow.updatePosition();
        }
    }

    public String getName() {
        return name;
    }
}
