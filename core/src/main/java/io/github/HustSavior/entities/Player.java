package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;

import io.github.HustSavior.Play;
public class Player extends Sprite {
    private static final float PPM = GameConfig.PPM;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float MOVEMENT_SPEED = 120f/PPM;
    private static final float COLLISION_RADIUS = 8f;

    // Physics constants
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.4f;
    private static final float RESTITUTION = 0.0f; // Reduced bounce

    private float health;
    private float maxHealth;
    private float xp;
    private float maxXp;

    public final Animation<TextureRegion> walkLeft;
    public final Animation<TextureRegion> walkRight;
    private final Body body;

    public Player(Sprite sprite, float x, float y, World world) {
        super(sprite);
        setPosition(x / GameConfig.PPM, y / GameConfig.PPM);

        // Initialize animations
        walkRight = createAnimation("sprites/WalkRight");
        walkLeft = createAnimation("sprites/WalkLeft");

        // Initialize physics body
        body = createBody(world, x / GameConfig.PPM, y / GameConfig.PPM);
        this.health = 100;
        this.maxHealth = 100;
        this.xp = 0;
        this.maxXp = 100;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getXp() {
        return xp;
    }

    public float getMaxXp() {
        return maxXp;
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
        fixtureDef.filter.maskBits = 0x0001 | 0x0004;     // Collide with world objects

        playerBody.createFixture(fixtureDef);
        shape.dispose();

        return playerBody;
    }


    public void draw(SpriteBatch batch) {
        super.draw(batch);
        float x = body.getPosition().x * GameConfig.PPM - getWidth() / 2;
        float y = body.getPosition().y * GameConfig.PPM - getHeight() / 2;
        setPosition(x, y);
    }
    public float getSpeed() {
        return MOVEMENT_SPEED;
    }

    public Body getBody() {
        return body;
    }
}
