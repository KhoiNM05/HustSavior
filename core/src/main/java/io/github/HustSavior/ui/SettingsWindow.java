package io.github.HustSavior.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.HustSavior.utils.ResolutionManager;

public class SettingsWindow extends Window {
    private VolumeControl volumeControl;
    private ResolutionControl resolutionControl;

    public SettingsWindow(Stage stage) {
        super("Settings", new Skin(Gdx.files.internal("UI/settings/settingsWindow.json")));
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }
        setupWindow();
        createControls();
        stage.addActor(this);
    }

    private void setupWindow() {
        updateWindowSize();
        setModal(true);
        setMovable(false);
        setVisible(false);
        toFront();
    }

    private void updateWindowSize() {
        // Get current resolution from ResolutionManager
        String currentRes = ResolutionManager.getInstance().getCurrentResolution();
        String[] dimensions = currentRes.split("x");
        float screenWidth = Float.parseFloat(dimensions[0]);
        float screenHeight = Float.parseFloat(dimensions[1]);
        
        // Calculate 60% of current resolution
        float windowWidth = screenWidth * 0.6f;
        float windowHeight = screenHeight * 0.6f;
        setSize(windowWidth, windowHeight);
        
        // Scale font based on current resolution
        float fontScale = Math.min(
            screenWidth / 1920f,
            screenHeight / 1080f
        );
        getTitleLabel().setFontScale(fontScale * 2.0f);
        
        // Center the window
        setPosition(
            Gdx.graphics.getWidth() / 2f - windowWidth / 2,
            Gdx.graphics.getHeight() / 2f - windowHeight / 2
        );
        
        // Force layout update
        invalidate();
        pack();
    }

    private void createControls() {
        // Create main table for layout
        Table table = new Table();
        table.pad(20);
        
        // Volume controls section
        Table volumeTable = new Table();
        volumeControl = new VolumeControl();
        volumeControl.addToTable(volumeTable);
        table.add(volumeTable).row();
        
        // Resolution controls section
        Table resolutionTable = new Table();
        resolutionControl = new ResolutionControl();
        resolutionControl.addToTable(resolutionTable);
        table.add(resolutionTable).row();
        
        // Add close button
        addCloseButton(table);

        add(table).expand().fill();
    }

    private void addCloseButton(Table table) {
        Skin closeButtonSkin = new Skin(Gdx.files.internal("UI/settings/closeButton.json"));
        Button closeButton = new Button(closeButtonSkin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });

        // Position the close button in the top-right corner
        closeButton.setPosition(getWidth() - closeButton.getWidth() - 10, getHeight() - closeButton.getHeight() - 10);
        addActor(closeButton);  // Add directly to window instead of table
    }

    public VolumeControl getVolumeControl() {
        return volumeControl;
    }

    public ResolutionControl getResolutionControl() {
        return resolutionControl;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            toFront();
        }
    }

    public void updatePosition() {
        // Update window size and position when resolution changes
        float windowWidth = Gdx.graphics.getWidth() * 0.9f;
        float windowHeight = Gdx.graphics.getHeight() * 0.9f;
        setSize(windowWidth, windowHeight);
        
        float fontScale = Math.min(
            Gdx.graphics.getWidth() / 1920f,
            Gdx.graphics.getHeight() / 1080f
        );
        getTitleLabel().setFontScale(fontScale * 3.0f);
        
        setPosition(
            Gdx.graphics.getWidth() / 2f - windowWidth / 2,
            Gdx.graphics.getHeight() / 2f - windowHeight / 2
        );
        
        // Force layout update
        invalidate();
        pack();
    }
} 