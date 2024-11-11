package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player extends Sprite {
    private Vector2 velocity = new Vector2();
    private Vector2 position = new Vector2();
    private float speed = 60 * 2;

    public Player(Sprite sprite, float x, float y) {
        super(sprite);
        setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    
    public float getSpeed() {
        return speed;
    }
}
