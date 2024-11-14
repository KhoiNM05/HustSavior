package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Player extends Sprite {
    private Vector2 velocity = new Vector2();
    private Vector2 position = new Vector2();
    private TextureRegion[] left, right;
    public Animation<TextureRegion> walkLeft, walkRight;

    private float speed = 60 * 2;


    public Player(Sprite sprite, float x, float y) {
        super(sprite);
        setPosition(x, y);
        left= new TextureRegion[2];
        right=new TextureRegion[2];

        left[0]=new TextureRegion(new Texture("sprites/WalkRight1.png"));
        left[1]=new TextureRegion(new Texture("sprites/WalkRight2.png"));

        right[0]=new TextureRegion(new Texture("sprites/WalkRight1.png"));
        right[1]=new TextureRegion(new Texture("sprites/WalkRight2.png"));
        walkLeft= new Animation<TextureRegion>(0.2f, left);
        walkRight= new Animation<TextureRegion>(0.2f, left);
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }


    public float getSpeed() {
        return speed;
    }
}
