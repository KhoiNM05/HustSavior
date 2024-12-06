package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class Player extends Sprite {
    private static final float PPM = GameConfig.PPM;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float MOVEMENT_SPEED = 120f / PPM;
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

    // HP and XP
    private Texture healthBarTexture;
    private static final float HEALTH_BAR_WIDTH = 30f;
    private static final float HEALTH_BAR_HEIGHT = 3f;
    private static final float HEALTH_BAR_OFFSET_X = 6.5f;
    private static final float HEALTH_BAR_OFFSET_Y = 37f;
    private Texture xpBarTexture;
    private static final float XP_BAR_WIDTH = 750f;
    private static final float XP_BAR_HEIGHT = 10f;
    private static final float XP_BAR_OFFSET_Y = 15f;

    public Player(Sprite sprite, float x, float y, World world) {
        super(sprite);
        setPosition(x / GameConfig.PPM, y / GameConfig.PPM);
        // Initialize animations
        walkRight = createAnimation("sprites/WalkRight");
        walkLeft = createAnimation("sprites/WalkLeft");
        // Initialize physics body
        body = createBody(world, x / GameConfig.PPM, y / GameConfig.PPM);
        // HP & XP
        this.health = 100;
        this.maxHealth = 100;
        this.xp = 0;
        this.maxXp = 100;
        healthBarTexture = new Texture("HP & XP/health_bar.png");
        xpBarTexture = new Texture("HP & XP/xp_bar.png");
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
        bodyDef.position.set(
                x, y);
        bodyDef.fixedRotation = true; // Prevent rotation
        bodyDef.linearDamping = 0.5f; // Add some drag
        Body playerBody = world.createBody(bodyDef);
        playerBody.setUserData(this);
        // Replace circle shape with a small rectangle for feet
        PolygonShape shape = new PolygonShape();
        float footWidth = COLLISION_RADIUS / GameConfig.PPM;
        float footHeight = (COLLISION_RADIUS / 3) / GameConfig.PPM; // Make height smaller for feet
        // Offset the shape to the bottom of the sprite
        shape.setAsBox(
                footWidth, // half-width
                footHeight, // half-height
                new Vector2(0, -getHeight() / (4 * PPM)), // offset to bottom
                0 // rotation
        );
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = DENSITY;
        fixtureDef.friction = FRICTION;
        fixtureDef.restitution = RESTITUTION;
        // Set collision filtering
        fixtureDef.filter.categoryBits = 0x0002;
        fixtureDef.filter.maskBits = 0x0001;
        playerBody.createFixture(fixtureDef);
        shape.dispose();
        return playerBody;
    }

    public void draw(SpriteBatch batch, OrthographicCamera camera) {
        super.draw(batch);
        float x = body.getPosition().x * GameConfig.PPM - getWidth() / 2;
        float y = body.getPosition().y * GameConfig.PPM - getHeight() / 2;
        setPosition(x, y);
        // Player's HP
        float healthPercentage = getHealth() / getMaxHealth();
        float healthBarX = getX() - HEALTH_BAR_OFFSET_X;
        float healthBarY = getY() - getHeight() + HEALTH_BAR_OFFSET_Y;
        batch.setColor(0, 0, 0, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY - 1, HEALTH_BAR_WIDTH + 2, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY + HEALTH_BAR_HEIGHT, HEALTH_BAR_WIDTH + 2, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY - 1, 1, HEALTH_BAR_HEIGHT + 2);
        batch.draw(healthBarTexture, healthBarX + HEALTH_BAR_WIDTH, healthBarY - 1, 1, HEALTH_BAR_HEIGHT + 2);
        batch.setColor(1, 1, 1, 1);
        batch.draw(healthBarTexture, healthBarX, healthBarY, HEALTH_BAR_WIDTH * healthPercentage, HEALTH_BAR_HEIGHT);
        // Player's XP
        float xpPercentage = getXp() / getMaxXp();
        float xpBarX = camera.position.x - XP_BAR_WIDTH / 2;
        float xpBarY = camera.position.y + camera.viewportHeight / 2 - XP_BAR_OFFSET_Y;
        batch.setColor(0, 0, 0, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY - 1, XP_BAR_WIDTH + 2, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY + XP_BAR_HEIGHT, XP_BAR_WIDTH + 2, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY - 1, 1, XP_BAR_HEIGHT + 2);
        batch.draw(xpBarTexture, xpBarX + XP_BAR_WIDTH, xpBarY - 1, 1, XP_BAR_HEIGHT + 2);
        batch.setColor(1, 1, 1, 1);
        batch.draw(xpBarTexture, xpBarX, xpBarY, XP_BAR_WIDTH * xpPercentage, XP_BAR_HEIGHT);
    }

    public float getSpeed() {
        return MOVEMENT_SPEED;
    }

    public Body getBody() {
        return body;
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        // Get the body's position and shape
        Body body = getBody();
        for (Fixture fixture : body.getFixtureList()) {
            if (fixture.getShape() instanceof PolygonShape) {
                PolygonShape shape = (PolygonShape) fixture.getShape();
                shapeRenderer.setColor(0, 0, 0, 1); // Black color
                shapeRenderer.begin(ShapeType.Line);
                // Get vertices of the box
                float[] vertices = new float[8];
                for (int i = 0; i < 4; i++) {
                    Vector2 vertex = new Vector2();
                    shape.getVertex(i, vertex);
                    vertex.rotateRad(body.getAngle());
                    vertex.add(body.getPosition());
                    vertices[i * 2] = vertex.x * PPM;
                    vertices[i * 2 + 1] = vertex.y * PPM;
                }
                // Draw the box
                shapeRenderer.polygon(vertices);
                shapeRenderer.end();
            }
        }
    }

    // Add this method to limit maximum velocity
    public void update(float delta) {
        // Add a maximum velocity limit
        Vector2 velocity = body.getLinearVelocity();
        float maxVelocity = 5f; // Adjust this value as needed
        if (velocity.len() > maxVelocity) {
            velocity.nor().scl(maxVelocity);
            body.setLinearVelocity(velocity);
        }
        // Update sprite position to match physics body
        setPosition(
                body.getPosition().x * GameConfig.PPM - getWidth() / 2,
                body.getPosition().y * GameConfig.PPM - getHeight() / 2);
    }

    public void stop() {
        // Stop the player's movement
        if (body != null) {
            body.setLinearVelocity(0, 0);
        }
    }

    public void resetMovement() {
        // Reset any movement restrictions
        // This method is called when the dialog closes
        Gdx.app.log("Player", "Movement reset"); // Debug log
        // The player will now be able to move again because the InputHandler
        // will process new movement inputs
    }
}
