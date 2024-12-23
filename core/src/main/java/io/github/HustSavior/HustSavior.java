package io.github.HustSavior;
//package
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.HustSavior.screen.SplashScreen;
import io.github.HustSavior.utils.ResolutionManager;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class HustSavior extends Game {
    public SpriteBatch batch;
    private Texture image;
    private Play map;

    @Override
    public void create() {

        batch = new SpriteBatch();
        ResolutionManager.getInstance();
        setScreen(new SplashScreen(this));

    }
    @Override
    public void resize(int width, int height){
        super.resize(width, height);
    }


    @Override       
    public void pause() {
        super.pause();
    }

    @Override
    public void render() {
        super.render();
    }
    // dispose to save resources
    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void resume() {
        super.resume();
    }
}
