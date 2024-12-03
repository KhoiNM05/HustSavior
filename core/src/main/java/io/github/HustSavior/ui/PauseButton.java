package io.github.HustSavior.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.HustSavior.screen.MainMenuScreen;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.HustSavior.Play;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;

public class PauseButton extends Button {
    private Table pauseMenu;
    private boolean isPaused = false;
    private final Stage stage;
    private final Game game;
    private Actor darkOverlay;
    private SettingsWindow settingsWindow;
    private MainMenuButton mainMenuButton;
    private final Play playScreen;

    public PauseButton(Stage stage, Game game, Play playScreen) {
        super(new Skin(Gdx.files.internal("UI/pause/pauseButton.json")));
        this.stage = stage;
        this.game = game;
        this.playScreen = playScreen;
        
        createDarkOverlay();
        createMainMenuButton();
        createPauseMenu();
        createSettingsWindow();
        
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePauseMenu();
            }
        });
        
        setPosition(stage.getWidth() - getWidth() - 10, stage.getHeight() - getHeight() - 10);
    }

    private void createDarkOverlay() {
        darkOverlay = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.end();
                Gdx.gl.glEnable(GL20.GL_BLEND);
                ShapeRenderer shapeRenderer = new ShapeRenderer();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 0, 0, isPaused ? 0.5f : 0f); // 50% black when paused
                shapeRenderer.rect(0, 0, stage.getWidth(), stage.getHeight());
                shapeRenderer.end();
                batch.begin();
            }
        };
        darkOverlay.setZIndex(0);
        stage.addActor(darkOverlay);
        setZIndex(stage.getActors().size - 1);
    }

    private void createMainMenuButton() {
        mainMenuButton = new MainMenuButton(new Skin(Gdx.files.internal("UI/mainMenu/mainMenuButton.json")));
        mainMenuButton.setVisible(false);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        stage.addActor(mainMenuButton);
    }

    private void createPauseMenu() {
        pauseMenu = new Table();
        pauseMenu.setVisible(false);
        
        Button resumeButton = new Button(new Skin(Gdx.files.internal("UI/resume/resumeButton.json")));
        Button settingsButton = new Button(new Skin(Gdx.files.internal("UI/settings/settingsButton.json")));
        Button mainMenuButton = new Button(new Skin(Gdx.files.internal("UI/mainMenu/mainMenuButton.json")));
        
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePauseMenu();
            }
        });
        
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsWindow.setVisible(true);
            }
        });
        
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        
        float baseWidth = 200f; // Original design width
        float baseHeight = 200f; // Original design height
        float aspectRatio = baseWidth / baseHeight;
        
        float buttonWidth = stage.getWidth() * 0.08f;  // reduced from 0.15f to 0.08f
        float buttonHeight = buttonWidth / aspectRatio;
        
        float padding = stage.getWidth() * 0.01f;      // 1% of screen width
        
        pauseMenu.add(resumeButton).pad(padding).size(buttonWidth, buttonHeight);
        pauseMenu.add(settingsButton).pad(padding).size(buttonWidth, buttonHeight);
        pauseMenu.add(mainMenuButton).pad(padding).size(buttonWidth, buttonHeight);
        
        pauseMenu.setPosition(
            stage.getWidth() / 2 - pauseMenu.getWidth() / 2,
            stage.getHeight() / 2 - pauseMenu.getHeight() / 2
        );
        
        stage.addActor(pauseMenu);
    }

    private void createSettingsWindow() {
        settingsWindow = new SettingsWindow(stage);
        settingsWindow.setVisible(false);
    }

    private void togglePauseMenu() {
        isPaused = !isPaused;
        pauseMenu.setVisible(isPaused);
        mainMenuButton.setVisible(isPaused);
        // Update the game pause state
        playScreen.setPaused(isPaused);
    }

    public void updatePosition() {
        setPosition(stage.getWidth() - getWidth() - 10, stage.getHeight() - getHeight() - 10);
        darkOverlay.setSize(stage.getWidth(), stage.getHeight());
        
        if (mainMenuButton != null) {
            mainMenuButton.setPosition(
                stage.getWidth() / 2 - mainMenuButton.getWidth() / 2,
                stage.getHeight() / 2 - mainMenuButton.getHeight() / 2
            );
        }
        
        if (pauseMenu != null) {
            float baseWidth = 200f;
            float baseHeight = 200f;
            float aspectRatio = baseWidth / baseHeight;
            
            float buttonWidth = stage.getWidth() * 0.08f;
            float buttonHeight = buttonWidth / aspectRatio;
            float padding = stage.getWidth() * 0.01f;
            
            pauseMenu.getChildren().forEach(child -> {
                if (child instanceof Button) {
                    child.setSize(buttonWidth, buttonHeight);
                }
            });
            
            pauseMenu.setPosition(
                stage.getWidth() / 2 - pauseMenu.getWidth() / 2,
                stage.getHeight() / 2 - pauseMenu.getHeight() / 2
            );
        }
    }
} 