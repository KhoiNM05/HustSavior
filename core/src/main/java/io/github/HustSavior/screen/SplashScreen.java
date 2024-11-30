package io.github.HustSavior.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
// import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.HustSavior.HustSavior;
import io.github.HustSavior.Play;
import io.github.HustSavior.utils.GameConfig;

public class SplashScreen implements Screen {
    private static final float FADE_DURATION = 1.0f;
    private static final float DISPLAY_DURATION = 2.0f;
    private static final String SPLASH_PATH = "sprites/splash.jpg";


    private final HustSavior game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private Texture splashTexture;
    private boolean useBlackScreen = false;


    private float elapsed = 0f;
    private State currentState = State.FADE_IN;


    private enum State {
        FADE_IN,
        DISPLAY,
        FADE_OUT
    }
    public SplashScreen(HustSavior game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);        batch = new SpriteBatch();
        camera.update();
        loadSplashTexture();

        Gdx.app.log("SplashScreen", "Initialized");
    }
    private void loadSplashTexture(){
        try {
            if (Gdx.files.internal(SPLASH_PATH).exists()) {
                splashTexture = new Texture(SPLASH_PATH);
                Gdx.app.log("SplashScreen", "Splash texture loaded successfully");
            } else {
                createBlackTexture();
            }
        } catch (Exception e) {
            Gdx.app.error("SplashScreen", "Error loading splash texture", e);
            createBlackTexture();
        }
    }

    private void createBlackTexture() {
        Gdx.app.log("SplashScreen", "Creating black texture fallback");
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        splashTexture = new Texture(pixmap);
        pixmap.dispose();
        useBlackScreen = true;
    }

    @Override
    public void render(float delta){
        elapsed += delta;
        updateState();

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        float alpha = calculateAlpha();

        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        batch.draw(splashTexture,
            viewport.getWorldWidth() / 2 - splashTexture.getWidth() / 2,
            viewport.getWorldHeight() / 2 - splashTexture.getHeight() / 2);
        batch.end();

        Gdx.app.log("SplashScreen", "State: " + currentState + ", Alpha: " + alpha);
    }

    private void updateState() {
        switch (currentState) {
            case FADE_IN:
                if (elapsed >= FADE_DURATION) {
                    elapsed = 0;
                    currentState = State.DISPLAY;
                    Gdx.app.log("SplashScreen", "Transitioning to DISPLAY");
                }
                break;
            case DISPLAY:
                if (elapsed >= DISPLAY_DURATION) {
                    elapsed = 0;
                    currentState = State.FADE_OUT;
                    Gdx.app.log("SplashScreen", "Transitioning to FADE_OUT");
                }
                break;
            case FADE_OUT:
                if (elapsed >= FADE_DURATION) {
                    Gdx.app.log("SplashScreen", "Transitioning to Play screen");
                    game.setScreen(new Play());
                }
                break;
        }
    }

    private float calculateAlpha() {
        switch (currentState) {
            case FADE_IN:
                return Math.min(1, elapsed / FADE_DURATION);
            case DISPLAY:
                return 1;
            case FADE_OUT:
                return Math.max(0, 1 - (elapsed / FADE_DURATION));
            default:
                return 0;
        }
    }
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        splashTexture.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
