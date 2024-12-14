package io.github.HustSavior.entities;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import io.github.HustSavior.Play;
import io.github.HustSavior.collision.TileCollision;
import io.github.HustSavior.screen.DeathScreen;
import io.github.HustSavior.skills.SkillManager;
import io.github.HustSavior.sound.MusicPlayer;
import io.github.HustSavior.utils.GameConfig;

public class Player extends Sprite {
    private static final float PPM = GameConfig.PPM;
    private static final float ANIMATION_SPEED = 50f;
    private static final float MOVEMENT_SPEED = 200f;
    private static final float COLLISION_RADIUS = 8f;

    private static final float SHIELD_DURATION = 10f; // 10 seconds shield duration
    private float shieldTimer = 0f;
    private boolean shieldActive = false;



    private float health;
    private float maxHealth;
    private static float xp;
    private static float maxXp;
    private float SPEED = 200f;
    private static int level = 1;

    public final Animation<TextureRegion> walkLeft = new Animation<>(ANIMATION_SPEED,
        new TextureRegion(new Texture("sprites/WalkLeft1.png")),
        new TextureRegion(new Texture("sprites/WalkLeft2.png"))
    );
    public final Animation<TextureRegion> walkRight = new Animation<>(ANIMATION_SPEED,
        new TextureRegion(new Texture("sprites/WalkRight1.png")),
        new TextureRegion(new Texture("sprites/WalkRight2.png"))
    );


    private static boolean facingLeft;

    // HP and XP
    private Texture healthBarTexture;
    private static final float HEALTH_BAR_WIDTH = 30f;
    private static final float HEALTH_BAR_HEIGHT = 3f;
    private static final float HEALTH_BAR_OFFSET_X = 6.5f;
    private static final float HEALTH_BAR_OFFSET_Y = 37f;
    private Texture xpBarTexture;
    private static final float XP_BAR_WIDTH = 330f;
    private static final float XP_BAR_HEIGHT = 10f;
    private static final float XP_BAR_OFFSET_Y = 220f;
    // Shield
    private static final float SHIELD_ANIMATION_FRAME_DURATION = 0.1f; // Controls animation speed
    private Animation<TextureRegion> shieldAnimation;
    private float shieldStateTime;
    private TextureRegion[] shieldFrames;
    private static final float SHIELD_ALPHA = 0.5f; // Transparent shield
    // SkillManager
    private SkillManager skillManager;

    private static final float KNOCKBACK_FORCE = 10f;
    private static final float KNOCKBACK_DURATION = 0.2f;
    private float knockbackTimer = 0;
    private boolean isKnockedBack = false;

    private World world;

    private float alpha = 1.0f;

    private float mapWidth;
    private float mapHeight;
    private static final float CAMERA_PADDING = 100f; // Adjust this value as needed

    private Animation<TextureRegion> deathAnimation;
    private boolean isDead = false;
    private static final float DEATH_ANIMATION_DURATION = 1.0f;
    private float deathTimer = 0;

    private final Game game;

    private static final float FADE_DURATION = 1.0f;
    private float fadeTimer = 0;
    private boolean startFading = false;

    // Replace Box2D bodies with simple position and hitbox
    private Vector2 position;
    private Vector2 velocity = new Vector2();
    private float width;
    private float height;

    private float speed = MOVEMENT_SPEED; // Add at class level

    private Rectangle bounds;
    private TileCollision tileCollision;

    private TiledMap tiledMap;

    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private float stateTime = 0;
    private boolean facingRight = true;

    private static final float ANIMATION_FRAME_DURATION = 0.15f;  // Slower animation (was 0.1f)
    private static final int FRAME_COUNT = 2;  // We have 2 frames per direction

    private OrthographicCamera camera;  // Add at class level

    private boolean slashActivated = false;  // Add this field

    private float defense = 10f;
    private float attack = 10f;


    public Player(Sprite sprite, float x, float y, World world, Game game, TiledMap tiledMap) {
        super(sprite);
        this.game = game;
        this.world = world;
        this.tiledMap = tiledMap;

        // Initialize fields first
        float width = sprite.getWidth();
        float height = sprite.getHeight();

        // Initialize position in world coordinates
        this.position = new Vector2(x / PPM, y / PPM);  // Convert to world units
        this.velocity = new Vector2(0, 0);  // Ensure velocity is zero initially

        // Set up collision bounds
        this.bounds = new Rectangle(
            (x / PPM) - (width / (2 * PPM)),  // Center the bounds
            (y / PPM) - (height / (2 * PPM)),
            width / PPM,
            height / PPM
        );

        this.tileCollision = new TileCollision(tiledMap);

        // Initialize animations
        initializeAnimations();

        // Set initial sprite position in screen coordinates
        setPosition(x - width/2, y - height/2);

        // Initialize other components
        this.health = 10000;
        this.maxHealth = 10000;
        this.xp = 0;
        this.maxXp = 100;
        healthBarTexture = new Texture("HP & XP/health_bar.png");
        xpBarTexture = new Texture("HP & XP/xp_bar.png");

        // Shield
//        shieldActive = false;
//        shieldTimeRemaining = 0;
//        loadShieldAnimation();
        skillManager = new SkillManager(this, world);


        // Get map dimensions
        this.mapWidth = GameConfig.MAP_WIDTH;
        this.mapHeight = GameConfig.MAP_HEIGHT;

        this.camera = new OrthographicCamera();

        // Initialize death animation
        deathAnimation = createDeathAnimation();
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

    public int getLevel() {
        return level;
    }

    public static void addXP(float amount) {
        xp += amount;
        while (xp >= maxXp) {
            xp -= maxXp;
            maxXp *= 1.5f; // Increase XP required for next level
            levelUp();
        }
    }

    private Animation<TextureRegion> createAnimation(String basePath) {
        TextureRegion[] frames = new TextureRegion[2];
        frames[0] = new TextureRegion(new Texture(basePath + "1.png"));
        frames[1] = new TextureRegion(new Texture(basePath + "2.png"));
        return new Animation<>(ANIMATION_SPEED, frames);
    }

    protected void createBodies(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
        bounds = new Rectangle(x, y, getWidth(), getHeight());
    }


    public void draw(SpriteBatch batch) {
        float x = position.x * PPM - getWidth() / 2;
        float y = position.y * PPM - getHeight() / 2;
        setPosition(x, y + 12);  // Offset sprite up from feet position
        super.draw(batch);
        skillManager.drawSkills(batch);
        if (isDead) {
            deathTimer += Gdx.graphics.getDeltaTime();
            TextureRegion currentFrame = deathAnimation.getKeyFrame(deathTimer, false);

            if (deathAnimation.isAnimationFinished(deathTimer) && !startFading) {
                startFading = true;
                fadeTimer = 0;
            }

            if (startFading) {
                fadeTimer += Gdx.graphics.getDeltaTime();
                float alpha = Math.min(1, fadeTimer / FADE_DURATION);
                batch.setColor(1, 1, 1, 1 - alpha); // Fade out player
            }

            batch.draw(currentFrame, getX(), getY());
            batch.setColor(1, 1, 1, 1); // Reset batch color

            // if (startFading && fadeTimer >= FADE_DURATION) {
            //     game.setScreen(new DeathScreen(game, game.getScreen()));
            // }
            return;
        }
        Color oldColor = batch.getColor();
        float finalAlpha = alpha;

        // If shield is active, use shield alpha instead
        if (shieldActive) {
            finalAlpha = SHIELD_ALPHA;
        }

        batch.setColor(oldColor.r, oldColor.g, oldColor.b, finalAlpha);
        batch.setColor(oldColor);

//        if (shieldActive) {
//            shieldStateTime += Gdx.graphics.getDeltaTime(); // Update state time here instead of update method
//            TextureRegion currentFrame = shieldAnimation.getKeyFrame(shieldStateTime, true);
//            float shieldScale = 0.45f; // Adjust this value to change shield size
//            float shieldX = getX() - (currentFrame.getRegionWidth() * shieldScale - getWidth()) / 2;
//            float shieldY = getY() - (currentFrame.getRegionHeight() * shieldScale - getHeight()) / 2;
//
//            // Save current color
//            oldColor = batch.getColor();
//            // Set transparent color
//            batch.setColor(1, 1, 1, SHIELD_ALPHA);
//
//            batch.draw(currentFrame,
//                shieldX,
//                shieldY,
//                currentFrame.getRegionWidth() * shieldScale,
//                currentFrame.getRegionHeight() * shieldScale);
//
//            // Restore original color
//            batch.setColor(oldColor);
//        }


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

    public Vector2 getPosition() {
        return new Vector2(position.x * PPM, position.y * PPM);
    }



    // Add this method to limit maximum velocity
    public void update(float delta) {
        if (isDead) {
            deathTimer += Gdx.graphics.getDeltaTime();
            if (deathAnimation.isAnimationFinished(deathTimer) && !startFading) {
                startFading = true;
                fadeTimer = 0;
            }

            if (startFading) {
                fadeTimer += Gdx.graphics.getDeltaTime();
                if (fadeTimer >= FADE_DURATION) {
                    // Only transition to death screen after fade completes
                    game.setScreen(new DeathScreen(game, game.getScreen()));
                    return;
                }
            }
            return;
        }

        // Auto-activate slash if not already activated
        if (!slashActivated) {
            skillManager.activateSkills(1);  // 1 is the MELEE skill ID
            slashActivated = true;
        }

        // Update skills
        skillManager.update(delta);

        // Only update position if there's actual velocity
        if (Math.abs(velocity.x) > 0.001f || Math.abs(velocity.y) > 0.001f) {
            float oldX = position.x;
            float oldY = position.y;

            // Test new position
            Rectangle testBounds = new Rectangle(
                position.x + velocity.x * delta,
                position.y + velocity.y * delta,
                width,
                height
            );

            // Only update position if no collision would occur
            if (!tileCollision.collidesWith(testBounds)) {
                position.add(velocity.x * delta, velocity.y * delta);
            } else {
                position.x = oldX;
                position.y = oldY;
            }
        }

        // Update bounds and sprite position only once
        float x = position.x * PPM - getWidth() / 2;
        float y = position.y * PPM - getHeight() / 2;
        setPosition(x, y);  // This updates both sprite and bounds positions

        // Remove any automatic position adjustments in draw method
        bounds.setPosition(x, y);

        updateAnimation(delta);

        // Update shield timer
        if (shieldActive) {
            shieldTimer += delta;
            if (shieldTimer >= SHIELD_DURATION) {
                shieldActive = false;
                shieldTimer = 0;
                System.out.println("Shield deactivated");
            }
        }
    }



    public void acquireEffect(int itemId) {
//        // Skip skill activation if skillManager is null
//        if (skillManager == null) {
//            // Handle shield effect directly
//            if (itemId == 5) { // Assuming 5 is the shield item ID
//                // activateShield();
//                skillManager.activateSkills(2);
//                return;
//            }
//            return;
//        }
//        skillManager.activateSkills(itemId);
        switch(itemId){
            case 1:;
            case 2: skillManager.applyBuff(itemId); break;
            case 4: heal(50); break;
            case 5: skillManager.activateSkills(2); break;
        }

    }

    public void setFacingDirection(boolean facingLeft){this.facingLeft=facingLeft;}
    public boolean isFacingLeft(){return facingLeft;}
    public float getPPM(){return PPM;}

    public void heal(float amount) {
        this.health = Math.min(this.health + amount, this.maxHealth);
    }

    public void stopMovement() {
        velocity.set(0, 0);  // Set velocity to zero instead of using Box2D body
    }


//    private void loadShieldAnimation() {
//        // Load all shield frames into array
//        shieldFrames = new TextureRegion[4];
//        for (int i = 0; i < 4; i++) {
//            Texture texture = new Texture(Gdx.files.internal("item/shield_effects/shield_effect_" + (i + 1) + ".png"));
//            shieldFrames[i] = new TextureRegion(texture);
//            Gdx.app.log("Shield", "Loaded shield frame " + (i + 1));
//        }
//        shieldAnimation = new Animation<>(SHIELD_ANIMATION_FRAME_DURATION, shieldFrames);
//        shieldStateTime = 0;
//    }

    //public void activateShield() {
        //shieldActive = true;
        //shieldTimeRemaining = SHIELD_DURATION;
        //shieldStateTime = 0;
    //}


    public void takeDamage(float damage) {
        if (!shieldActive && !isDead) {
            health = Math.max(0, health - damage);
            if (health <= 0) {
                isDead = true;
                ((Play)game.getScreen()).setGameOver();  // Freeze the game
                game.setScreen(new DeathScreen(game, game.getScreen()));
            }
        }
    }


    public boolean isKnockedBack() {
        return isKnockedBack;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    // Add this method to handle camera boundaries
    public Vector2 getCameraBoundedPosition(OrthographicCamera camera) {
        float cameraHalfWidth = camera.viewportWidth * camera.zoom / 2;
        float cameraHalfHeight = camera.viewportHeight * camera.zoom / 2;

        // Get current player position
        float x = position.x * PPM;
        float y = position.y * PPM;

        // Calculate bounded camera position
        float boundedX = Math.min(Math.max(x, cameraHalfWidth - CAMERA_PADDING),
                                mapWidth - cameraHalfWidth + CAMERA_PADDING);
        float boundedY = Math.min(Math.max(y, cameraHalfHeight - CAMERA_PADDING),
                                mapHeight - cameraHalfHeight + CAMERA_PADDING);

        return new Vector2(boundedX, boundedY);
    }

    private Animation<TextureRegion> createDeathAnimation() {
        TextureRegion[] frames = new TextureRegion[3];
        frames[0] = new TextureRegion(new Texture("sprites/Defeated1.png"));
        frames[1] = new TextureRegion(new Texture("sprites/Defeated2.png"));
        frames[2] = new TextureRegion(new Texture("sprites/Defeated3.png"));
        return new Animation<>(0.2f, frames); // 0.2s per frame
    }

    public void setMapBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void setVelocity(Vector2 vel) {
        if (vel.len2() > 0) {
            vel.nor().scl(MOVEMENT_SPEED);
        }
        velocity.set(vel.x / GameConfig.PPM, vel.y / GameConfig.PPM); // Scale velocity to match world units
    }

    public Rectangle getHitbox() {
        return new Rectangle(
            position.x - width/2,
            position.y - height/2,
            width,
            height
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);  // Update sprite position
        if (bounds != null) {
            bounds.setPosition(x, y); // Update collision bounds
        }
    }

    private void initializeAnimations() {
        // Load textures for walking animations
        TextureRegion[] leftFrames = new TextureRegion[FRAME_COUNT];
        TextureRegion[] rightFrames = new TextureRegion[FRAME_COUNT];

        // Load the individual frame textures
        leftFrames[0] = new TextureRegion(new Texture("sprites/WalkLeft1.png"));
        leftFrames[1] = new TextureRegion(new Texture("sprites/WalkLeft2.png"));
        rightFrames[0] = new TextureRegion(new Texture("sprites/WalkRight1.png"));
        rightFrames[1] = new TextureRegion(new Texture("sprites/WalkRight2.png"));

        // Create animations
        walkLeftAnimation = new Animation<>(ANIMATION_FRAME_DURATION, leftFrames);
        walkRightAnimation = new Animation<>(ANIMATION_FRAME_DURATION, rightFrames);

        // Set initial frame
        setRegion(rightFrames[0]);  // Default to first right-facing frame
    }

    private void updateAnimation(float delta) {
        stateTime += delta;

        // Update current animation based on movement
        if (velocity.x > 0) {
            facingRight = true;
            setRegion(walkRightAnimation.getKeyFrame(stateTime, true));
        } else if (velocity.x < 0) {
            facingRight = false;
            setRegion(walkLeftAnimation.getKeyFrame(stateTime, true));
        } else {
            // When idle, show first frame of current direction
            setRegion(facingRight ?
                walkRightAnimation.getKeyFrame(0) :
                walkLeftAnimation.getKeyFrame(0));
        }
    }


    public void dispose() {
        // Dispose of animation textures
        if (walkLeftAnimation != null) {
            for (TextureRegion frame : walkLeftAnimation.getKeyFrames()) {
                frame.getTexture().dispose();
            }
        }
        if (walkRightAnimation != null) {
            for (TextureRegion frame : walkRightAnimation.getKeyFrames()) {
                frame.getTexture().dispose();
            }
        }
        // ... dispose other resources ...
    }

    public void resetMovement() {
        velocity.set(0, 0);
        stateTime = 0;
    }

    public void applyKnockback(Vector2 direction) {
        isKnockedBack = true;
        knockbackTimer = KNOCKBACK_DURATION;
        velocity.set(direction.scl(KNOCKBACK_FORCE));
    }

    public void initCollision(TiledMap map) {
        tileCollision = new TileCollision(map);
    }

    public void setWorldPosition(float x, float y) {
        position.set(x / PPM, y / PPM);  // Store in world units
        float screenX = x - getWidth()/2;
        float screenY = y - getHeight()/2;
        setPosition(screenX, screenY);
        if (bounds != null) {
            bounds.setPosition(screenX, screenY);
        }
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public Object getScreen() {
        return game.getScreen();
    }

    public void increaseAttack(float amount) {
        this.attack += amount;
    }

    public void increaseSpeed(float amount) {
        this.speed += amount;
    }

    public void increaseDefense(float amount) {
        this.defense += amount;
    }

    public void activateShield() {
        shieldActive = true;
        shieldTimer = 0;
        System.out.println("Shield activated for " + SHIELD_DURATION + " seconds");
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public static void levelUp() {
        level++;
        System.out.println("Player leveled up to level " + level);
    }
}
