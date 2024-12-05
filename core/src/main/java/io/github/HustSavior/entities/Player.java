package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;

import java.util.Objects;

public class Player extends Sprite {
    private static final float PPM = GameConfig.PPM;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float MOVEMENT_SPEED = 120f / PPM;
    private static final float COLLISION_RADIUS = 8f;

    // Physics constants
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.4f;
    private static final float RESTITUTION = 0.0f; // Reduced bounce

    public final Animation<TextureRegion> walkLeft;
    public final Animation<TextureRegion> walkRight;
    private Body body;

    // Store textures to manage their lifecycle
    private Texture[] animationTextures;

    private World world;

    public Player(String spritePath, float x, float y, World world) {
        super(Objects.requireNonNull(loadTextureSafely(spritePath)));
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        this.world = world;
        setPosition(x / GameConfig.PPM, y / GameConfig.PPM);

        // Khởi tạo animation walkRight
        walkRight = createAnimation("sprites/WalkRight");

        // Khởi tạo animation walkLeft bằng cách lật ngược walkRight
        walkLeft = createAnimation("sprites/WalkLeft");

        // Khởi tạo physics body
        createBody(world);
    }

    private static Texture loadTextureSafely(String path) {
        try {
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error("Player", "Texture file not found: " + path);
                return null;
            }
            return new Texture(path);
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading texture: " + path, e);
            return null;
        }
    }

    private Animation<TextureRegion> createAnimation(String basePath) {
        TextureRegion[] frames = new TextureRegion[2];
        animationTextures = new Texture[2];

        try {
            animationTextures[0] = loadTexture(basePath + "1.png");
            animationTextures[1] = loadTexture(basePath + "2.png");

            if (animationTextures[0] == null || animationTextures[1] == null) {
                Gdx.app.error("Player", "Failed to load animation textures for: " + basePath);
                return new Animation<>(ANIMATION_SPEED, createFallbackTextureRegions());
            }

            frames[0] = new TextureRegion(animationTextures[0]);
            frames[1] = new TextureRegion(animationTextures[1]);
        } catch (Exception e) {
            Gdx.app.error("Player", "Error creating animation", e);
            return new Animation<>(ANIMATION_SPEED, createFallbackTextureRegions());
        }

        return new Animation<>(ANIMATION_SPEED, frames);
    }


    private Texture loadTexture(String path) {
        try {
            // Check if file exists before loading
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error("Player", "Texture file not found: " + path);
                return null;
            }
            return new Texture(path);
        } catch (Exception e) {
            Gdx.app.error("Player", "Failed to load texture: " + path, e);
            return null;
        }
    }

    private TextureRegion[] createFallbackTextureRegions() {
        // Create a simple fallback texture if animation textures fail to load
        Texture fallbackTexture = new Texture(Gdx.files.internal("sprites/WalkRight1.png"));
        return new TextureRegion[]{
            new TextureRegion(fallbackTexture),
            new TextureRegion(fallbackTexture)
        };
    }

    public void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());
        bodyDef.fixedRotation = true; // Prevent rotation
        bodyDef.linearDamping = 0.5f; // Add some drag

        body = world.createBody(bodyDef);

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

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this); // Gán `this` (Player) cho `userData`
        shape.dispose();
    }

    public void draw(SpriteBatch batch) {
        // Add null checks to prevent NullPointerException
        if (batch == null || getTexture() == null) {
            Gdx.app.error("Player", "Cannot draw: batch or texture is null");
            return;
        }

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

    public void update(float delta) {
        // Update player logic here if needed
    }

    public void dispose() {
        // Safely dispose of all textures
        if (getTexture() != null) {
            getTexture().dispose();
        }

        // Dispose of animation textures
        if (animationTextures != null) {
            for (Texture texture : animationTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
    }
    
    public boolean isTextureLoaded() {
        // Check base texture
        if (getTexture() == null) {
            Gdx.app.error("Player", "Base texture is not loaded.");
            return false;
        }

        // Check animation textures
        if (animationTextures != null) {
            for (Texture texture : animationTextures) {
                if (texture == null) {
                    Gdx.app.error("Player", "An animation texture is not loaded.");
                    return false;
                }
            }
        } else {
            Gdx.app.error("Player", "Animation textures array is null.");
            return false;
        }

        return true;
    }
}
