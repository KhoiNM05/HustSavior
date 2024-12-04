package io.github.HustSavior.screen;
import com.badlogic.gdx.Game;
import io.github.HustSavior.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HustSavior.HustSavior;
import io.github.HustSavior.sound.MusicPlayer;

import java.util.ArrayList;


public class MainMenuScreen implements Screen {
    private final Game game;
    private Texture background;
    private Stage stage;
    private ArrayList<Button> buttons ;
    private String SPLASH_PATH = "sprites/Hust1.png";
    private boolean  useBlackScreen = false;
    public MainMenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        loadBackGround();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        initializeButtons();
        loadButtons();
        
        // Start playing main menu music
        MusicPlayer.getInstance().playMainMenuMusic();
    }

    private void loadBackGround(){
        try {
            if (Gdx.files.internal(SPLASH_PATH).exists()) {
                background = new Texture(SPLASH_PATH);
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
        background = new Texture(pixmap);
        pixmap.dispose();
        useBlackScreen = true;
    }
    // initialize buttons
    private void initializeButtons() {
        buttons = new ArrayList<>();
        buttons.add(new PlayButton(game));
        buttons.add(new SettingsButton(stage));
    }
    // load buttons
    private void loadButtons(){
        for(Button x : buttons){
            // if(x instanceof SettingsButton){
            //     ((SettingsButton) x).createSettingsWindow(this.stage);
            // }
            stage.addActor(x);
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float backgroundAspect = (float)background.getWidth() / background.getHeight();
        float screenAspect = screenWidth / screenHeight;
        
        float renderWidth = screenWidth;
        float renderHeight = screenHeight;
        float x = 0, y = 0;
        
        if (screenAspect > backgroundAspect) {
            // Screen is wider than background - fill width and overflow height
            renderWidth = screenWidth;
            renderHeight = screenWidth / backgroundAspect;
            y = -(renderHeight - screenHeight) / 2;
        } else {
            // Screen is taller than background - fill height and overflow width
            renderHeight = screenHeight;
            renderWidth = screenHeight * backgroundAspect;
            x = -(renderWidth - screenWidth) / 2;
        }

        ((HustSavior)game).batch.begin();
        ((HustSavior)game).batch.draw(background, x, y, renderWidth, renderHeight);
        ((HustSavior)game).batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // Update button positions
        for(Button button : buttons) {
            if(button instanceof PlayButton) {
                ((PlayButton)button).updatePosition();
            }
            // Add similar checks for other buttons that need repositioning
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // Stop the music when the screen is hidden
        MusicPlayer.getInstance().stop();
    }

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        for(Button x : buttons){
            x.getSkin().dispose();
        }
    }
}
