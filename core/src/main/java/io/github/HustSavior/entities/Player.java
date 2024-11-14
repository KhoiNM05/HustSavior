package io.github.HustSavior.entities;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.HustSavior.Play;
public class Player extends Sprite {
    private float speed = 0.5f;
    private Body body;
    private Vector2 prevPos ;
    private Vector2 currPos;
    private float accumulator;
    private static final float FRAME_TIME = 1/60f;
    public Player(Sprite sprite,World world, float x, float y) {
        super(sprite);
        setPosition(x, y);

        // Create player body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x/Play.PPM, y/ Play.PPM);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(getWidth() / 2 / Play.PPM, getHeight()/2/Play.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = Play.CATEGORY_PLAYER;
        fixtureDef.filter.maskBits = Play.MASK_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();

        prevPos = new Vector2(body.getPosition());
        currPos = new Vector2(body.getPosition());
        accumulator = 0f;
    }
    public void update(float delta) {
        accumulator += delta;

        if(accumulator >= FRAME_TIME){
            prevPos.set(currPos);
            currPos.set(body.getPosition());
            accumulator -= FRAME_TIME;
        }
        float alpha = accumulator / FRAME_TIME;
        float interpolatedX = prevPos.x + alpha * (currPos.x - prevPos.x);
        float interpolatedY = prevPos.y + alpha * (currPos.y - prevPos.y);

        setPosition(interpolatedX * Play.PPM - getWidth()/2, interpolatedY * Play.PPM - getHeight()/2);
        System.out.println("Player position: " + getX() + ", " + getY());
    }
    public void setVelocity(Vector2 velocity) {
        body.setLinearVelocity(velocity);
    }

    @Override
    public void draw(Batch batch) {
        update(Gdx.graphics.getDeltaTime());
        super.draw(batch);
    }
    public float getSpeed() {
        return speed;
    }
}
