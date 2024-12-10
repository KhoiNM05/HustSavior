package io.github.HustSavior.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.HustSavior.HustSavior;
import io.github.HustSavior.utils.GameConfig;

public class DeathScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Texture background;
    private Skin skin;
    private float fadeTimer = 0;
    private final float FADE_IN_DURATION = 1.5f;  // 1 second fade in
    private final float DISPLAY_DURATION = 1.5f;  // 1 second display
    private final float FADE_OUT_DURATION = 2f; // 1 second fade out
    private State currentState = State.FADE_IN;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private enum State {
        FADE_IN,
        DISPLAY,
        FADE_OUT
    }

    public DeathScreen(Game game) {
        this.game = game;
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.zoom = 1.5f;
        camera.update();
        
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("UI/dialogue/dialog.json"));
        this.background = new Texture("screen/deadscreen.png");
        
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fadeTimer += delta;
        updateState();
        
        camera.update();
        ((HustSavior)game).batch.setProjectionMatrix(camera.combined);
        
        ((HustSavior)game).batch.begin();
        ((HustSavior)game).batch.setColor(1, 1, 1, calculateAlpha());
        ((HustSavior)game).batch.draw(background, 
            0, 0,                           
            GameConfig.GAME_WIDTH,          
            GameConfig.GAME_HEIGHT);        
        ((HustSavior)game).batch.setColor(1, 1, 1, 1);
        ((HustSavior)game).batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void updateState() {
        switch (currentState) {
            case FADE_IN:
                if (fadeTimer >= FADE_IN_DURATION) {
                    fadeTimer = 0;
                    currentState = State.DISPLAY;
                }
                break;
            case DISPLAY:
                if (fadeTimer >= DISPLAY_DURATION) {
                    fadeTimer = 0;
                    currentState = State.FADE_OUT;
                }
                break;
            case FADE_OUT:
                if (fadeTimer >= FADE_OUT_DURATION) {
                    game.setScreen(new MainMenuScreen(game));
                }
                break;
        }
    }

    private float calculateAlpha() {
        switch (currentState) {
            case FADE_IN:
                return Math.min(1, fadeTimer / FADE_IN_DURATION);
            case DISPLAY:
                return 1;
            case FADE_OUT:
                return Math.max(0, 1 - (fadeTimer / FADE_OUT_DURATION));
            default:
                return 0;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
} 