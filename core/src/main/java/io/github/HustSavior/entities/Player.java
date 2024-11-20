package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public class Player extends Sprite {
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float MOVEMENT_SPEED = 120f;
    private static final float COLLISION_RADIUS = 8f;
    
    public final Animation<TextureRegion> walkLeft;
    public final Animation<TextureRegion> walkRight;
    private final Body body;

    public Player(Sprite sprite, float x, float y, World world) {
        super(sprite);
        setPosition(x, y);
        
        // Initialize animations
        walkLeft = createAnimation("sprites/WalkRight");
        walkRight = createAnimation("sprites/WalkRight");
        
        // Initialize physics body
        body = createBody(world, x, y);
    }

    private Animation<TextureRegion> createAnimation(String basePath) {
        TextureRegion[] frames = new TextureRegion[2];
        frames[0] = new TextureRegion(new Texture(basePath + "1.png"));
        frames[1] = new TextureRegion(new Texture(basePath + "2.png"));
        return new Animation<>(ANIMATION_SPEED, frames);
    }

    private Body createBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        
        Body playerBody = world.createBody(bodyDef);
        
        CircleShape shape = new CircleShape();
        shape.setRadius(COLLISION_RADIUS);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        playerBody.createFixture(fixtureDef);
        shape.dispose();
        
        return playerBody;
    }

   
    public void draw(SpriteBatch batch) {
        setPosition(body.getPosition().x - getWidth() / 2, 
                   body.getPosition().y - getHeight() / 2);
        super.draw(batch);
    }

    public float getSpeed() {
        return MOVEMENT_SPEED;
    }

    public Body getBody() {
        return body;
    }
}
