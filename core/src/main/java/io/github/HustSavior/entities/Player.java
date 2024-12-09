package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.HustSavior.utils.GameConfig;

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
    private Body mainBody;  // For ground/wall collisions
    private Body hitBody;   // For monster collisions

    private static boolean facingLeft;

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
    // Shield
    private static final float SHIELD_DURATION = 3f; // 10 seconds shield
    private static final float SHIELD_ANIMATION_FRAME_DURATION = 0.1f; // Controls animation speed
    private boolean shieldActive;
    private float shieldTimeRemaining;
    private Animation<TextureRegion> shieldAnimation;
    private float shieldStateTime;
    private TextureRegion[] shieldFrames;
    private static final float SHIELD_ALPHA = 0.5f; // Transparent shield

    private static final float KNOCKBACK_FORCE = 3.0f;
    private static final float KNOCKBACK_DURATION = 0.2f;
    private float knockbackTimer = 0;
    private boolean isKnockedBack = false;

    private World world;

    public Player(Sprite sprite, float x, float y, World world) {
        super(sprite);
        this.world = world;
        setPosition(x, y);
        createBodies(x, y);
        // Initialize animations
        walkRight = createAnimation("sprites/WalkRight");
        walkLeft = createAnimation("sprites/WalkLeft");
        // HP & XP
        this.health = 100;
        this.maxHealth = 100;
        this.xp = 0;
        this.maxXp = 100;
        healthBarTexture = new Texture("HP & XP/health_bar.png");
        xpBarTexture = new Texture("HP & XP/xp_bar.png");
        // Shield
        shieldActive = false;
        shieldTimeRemaining = 0;
        loadShieldAnimation();
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

    protected void createBodies(float x, float y) {
        // Main body for ground physics (smaller, at feet)
        BodyDef mainDef = new BodyDef();
        mainDef.position.set(x / PPM, (y) / PPM );
        mainDef.type = BodyDef.BodyType.DynamicBody;
        mainBody = world.createBody(mainDef);
        mainBody.setUserData(this);

        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(6 / PPM, 3 / PPM);  // Small box for feet
        
        FixtureDef mainFixture = new FixtureDef();
        mainFixture.shape = mainShape;
        mainFixture.filter.categoryBits = GameConfig.BIT_PLAYER;
        mainFixture.filter.maskBits = GameConfig.BIT_GROUND;
        mainBody.createFixture(mainFixture);
        mainShape.dispose();

        // Hit body for monster collisions (larger, covers whole player)
        BodyDef hitDef = new BodyDef();
        hitDef.position.set(x / PPM, (y + 12) / PPM);  // Offset upward from feet
        hitDef.type = BodyDef.BodyType.DynamicBody;
        hitBody = world.createBody(hitDef);
        hitBody.setUserData(this);

        PolygonShape hitShape = new PolygonShape();
        hitShape.setAsBox(8 / PPM, 16 / PPM);  // Larger box for full body
        
        FixtureDef hitFixture = new FixtureDef();
        hitFixture.shape = hitShape;
        hitFixture.isSensor = true;  // No physical response
        hitFixture.filter.categoryBits = GameConfig.BIT_PLAYER_SENSOR;
        hitFixture.filter.maskBits = GameConfig.BIT_MONSTER;
        hitBody.createFixture(hitFixture);
        hitShape.dispose();
    }

    // Update both bodies' positions
    public void updateBodies() {
        Vector2 pos = mainBody.getPosition();
        hitBody.setTransform(pos.x, pos.y + 12/PPM, 0);  // Keep hit body above feet
    }

    public void draw(SpriteBatch batch, OrthographicCamera camera) {
        super.draw(batch);
        float x = mainBody.getPosition().x * GameConfig.PPM - getWidth() / 2;
        float y = mainBody.getPosition().y * GameConfig.PPM - getHeight() / 2;
        setPosition(x, y + 12);  // Offset sprite up from feet position
        
        // Shield
        if (shieldActive) {
            shieldStateTime += Gdx.graphics.getDeltaTime(); // Update state time here instead of update method
            TextureRegion currentFrame = shieldAnimation.getKeyFrame(shieldStateTime, true);
            float shieldScale = 0.45f; // Adjust this value to change shield size
            float shieldX = getX() - (currentFrame.getRegionWidth() * shieldScale - getWidth()) / 2;
            float shieldY = getY() - (currentFrame.getRegionHeight() * shieldScale - getHeight()) / 2;

            // Save current color
            Color oldColor = batch.getColor();
            // Set transparent color
            batch.setColor(1, 1, 1, SHIELD_ALPHA);

            batch.draw(currentFrame,
                shieldX,
                shieldY,
                currentFrame.getRegionWidth() * shieldScale,
                currentFrame.getRegionHeight() * shieldScale);

            // Restore original color
            batch.setColor(oldColor);
        }
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
        return mainBody;
    }

    public void drawDebug(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // Draw player collision boxes
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        
        // Draw main body (feet) in blue
        shapeRenderer.setColor(0, 0, 1, 1);
        Vector2 mainPos = mainBody.getPosition();
        shapeRenderer.rect(
            mainPos.x * PPM - 6,  // 6 is half width from createBodies
            mainPos.y * PPM - 3,  // 3 is half height from createBodies
            12,                   // Full width
            6                     // Full height
        );
        
        // Draw hit body (full body) in red
        shapeRenderer.setColor(1, 0, 0, 1);
        Vector2 hitPos = hitBody.getPosition();
        shapeRenderer.rect(
            hitPos.x * PPM - 8,   // 8 is half width from createBodies
            hitPos.y * PPM - 16,  // 16 is half height from createBodies
            16,                   // Full width
            32                    // Full height
        );
        
        shapeRenderer.end();
    }

    // Add this method to limit maximum velocity
    public void update(float delta) {
        // Add a maximum velocity limit
        Vector2 velocity = mainBody.getLinearVelocity();
        float maxVelocity = 5f; // Adjust this value as needed
        if (velocity.len() > maxVelocity) {
            velocity.nor().scl(maxVelocity);
            mainBody.setLinearVelocity(velocity);
        }
        // Update sprite position to match physics body
        setPosition(
                mainBody.getPosition().x * GameConfig.PPM - getWidth() / 2,
                mainBody.getPosition().y * GameConfig.PPM - getHeight() / 2);
        // Shield
        if (shieldActive) {
            shieldTimeRemaining -= delta;
            if (shieldTimeRemaining <= 0) {
                shieldActive = false;
                shieldStateTime = 0;
            }
        }
    }

    public void stop() {
        // Stop the player's movement
        if (mainBody != null) {
            mainBody.setLinearVelocity(0, 0);
        }
    }


    public void acquireEffect(int id){
        switch(id){
            case 1: break;
            default: ;
        }

    }

    public void setFacingDirection(boolean facingLeft){this.facingLeft=facingLeft;}
    public boolean isFacingLeft(){return facingLeft;}
    public float getPPM(){return PPM;}

    public void heal(float amount) {
        this.health = Math.min(this.health + amount, this.maxHealth);
    }

    public void stopMovement() {
        mainBody.setLinearVelocity(0, 0);
    }

    public void resetMovement() {
        // Reset any movement-related states if needed
        mainBody.setLinearVelocity(0, 0);
    }
    private void loadShieldAnimation() {
        // Load all shield frames into array
        shieldFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture texture = new Texture(Gdx.files.internal("item/shield_effects/shield_effect_" + (i + 1) + ".png"));
            shieldFrames[i] = new TextureRegion(texture);
            Gdx.app.log("Shield", "Loaded shield frame " + (i + 1));
        }
        shieldAnimation = new Animation<>(SHIELD_ANIMATION_FRAME_DURATION, shieldFrames);
        shieldStateTime = 0;
    }

    public void activateShield() {
        shieldActive = true;
        shieldTimeRemaining = SHIELD_DURATION;
        shieldStateTime = 0;
    }

    public void takeDamage(float damage) {
        if (!shieldActive) {  // Only take damage if shield is not active
            health = Math.max(0, health - damage);
        }
    }

    public void applyKnockback(Vector2 sourcePosition) {
        if (!isKnockedBack) {
            Vector2 knockbackDirection = mainBody.getPosition().cpy().sub(sourcePosition).nor();
            mainBody.setLinearVelocity(knockbackDirection.scl(KNOCKBACK_FORCE));
            isKnockedBack = true;
            knockbackTimer = KNOCKBACK_DURATION;
        }
    }
}
