package io.github.HustSavior.entities;
<<<<<<< HEAD
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
=======

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;
>>>>>>> dev

import io.github.HustSavior.Play;
public class Player extends Sprite {
<<<<<<< HEAD
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
=======
    private static final float PPM = GameConfig.PPM;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float MOVEMENT_SPEED = 120f/PPM;
    private static final float COLLISION_RADIUS = 8f;

    // Physics constants
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.4f;
    private static final float RESTITUTION = 0.0f; // Reduced bounce

    public final Animation<TextureRegion> walkLeft;
    public final Animation<TextureRegion> walkRight;
    private final Body body;

    public Player(Sprite sprite, float x, float y, World world) {
        super(sprite);
        setPosition(x / GameConfig.PPM, y / GameConfig.PPM);

        // Initialize animations
        walkLeft = createAnimation("sprites/WalkRight");
        walkRight = createAnimation("sprites/WalkRight");

        // Initialize physics body
        body = createBody(world, x / GameConfig.PPM, y / GameConfig.PPM);
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
        bodyDef.fixedRotation = true; // Prevent rotation
        bodyDef.linearDamping = 0.5f; // Add some drag

        Body playerBody = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(COLLISION_RADIUS / GameConfig.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = DENSITY;
        fixtureDef.friction = FRICTION;
        fixtureDef.restitution = RESTITUTION;

        // Set collision filtering
        fixtureDef.filter.categoryBits = 0x0002; // Player category
        fixtureDef.filter.maskBits = 0x0001;     // Collide with world objects

        playerBody.createFixture(fixtureDef);
        shape.dispose();

        return playerBody;
    }


    public void draw(SpriteBatch batch) {
>>>>>>> dev
        super.draw(batch);
        float x = body.getPosition().x * GameConfig.PPM - getWidth() / 2;
        float y = body.getPosition().y * GameConfig.PPM - getHeight() / 2;
        setPosition(x, y);
    }
<<<<<<< HEAD
=======

>>>>>>> dev
    public float getSpeed() {
        return MOVEMENT_SPEED;
    }

    public Body getBody() {
        return body;
    }
}
