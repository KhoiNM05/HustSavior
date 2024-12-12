package io.github.HustSavior.entities;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.HustSavior.collision.TileCollision;
import io.github.HustSavior.map.HighgroundManager;
import io.github.HustSavior.map.LowgroundManager;
import static io.github.HustSavior.utils.GameConfig.PPM;

public abstract class AbstractMonster {
    protected static final short COLLISION_LAYER_BITS = 0x0001;
    protected static final short MONSTER_CATEGORY = 0x0002;
    protected static final short TRANSPARENCY_BOUNDS_BITS = 0x0004;
    protected static final short MONSTER_MASK = COLLISION_LAYER_BITS | TRANSPARENCY_BOUNDS_BITS;
    
    public enum MonsterState {
        IDLE, RUNNING, ATTACKING, TAKE_HIT, DEATH
    }
    
    // Base stats
    protected float hp;
    protected float attack;
    protected float speed;
    protected MonsterState currentState = MonsterState.IDLE;
    protected boolean isFlipped = false;
    
    // Physics
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle bounds;
    protected float width;
    protected float height;
    
    // Animation components
    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> attack1Animation;
    protected Animation<TextureRegion> attack2Animation;
    protected Animation<TextureRegion> attack3Animation;
    protected Animation<TextureRegion> takeHitAnimation;
    protected Animation<TextureRegion> deathAnimation;
    protected float stateTime = 0f;
    
    protected boolean isCollidingWithPlayer = false;
    protected float attackCooldown = 0;
    protected static final float ATTACK_COOLDOWN_TIME = 2.0f; // 1 second between attacks
    
    // Add these new class variables at the top of the class
    protected static final float CONTACT_DAMAGE_COOLDOWN = 0.5f; // Time between contact damage
    protected static final float SWARM_RADIUS = 2.0f; // Radius for swarm behavior
    protected static final float PERSONAL_SPACE = 0.3f; // Minimum distance between monsters
    protected static final float DAMAGE_RADIUS = 0.5f; // Radius for damage dealing
    protected float contactDamageTimer = 0;
    
    // Add these constants at the top of the class
    protected static final float ATTACK_ANIMATION_DURATION = 2f; // Slower attack animation
    protected static final float ATTACK_COOLDOWN = 2f; // Time between attacks
    protected static final float STATE_TRANSITION_DELAY = 0.6f; // Delay between state changes
    protected float stateTransitionTimer = 0.5f;
    protected MonsterState nextState = null;
    protected float attackTimer = ATTACK_COOLDOWN; // Initialize with cooldown
    
    protected boolean isFinishingAttack = false;
    
    // Add these constants at the top
    protected static final float PUSH_FORCE = 2.0f;
    protected static final float PUSH_RECOVERY_TIME = 0.2f;
    protected float pushRecoveryTimer = 0;
    
    protected TiledMap map;
    
    // Add visibility field
    private boolean visible = true;
    private String currentBoundsLayer = null;
    
    private boolean isTransparent = false;
    
    // Add getter/setter for visibility
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setCurrentBoundsLayer(String layer) {
        this.currentBoundsLayer = layer;
    }
    
    public String getCurrentBoundsLayer() {
        return currentBoundsLayer;
    }
    
    public void setTransparent(boolean transparent) {
        this.isTransparent = transparent;
    }
    
    public boolean isTransparent() {
        return isTransparent;
    }
    
    protected static final float DETECTION_RANGE = 8f;  // Default detection range
    protected static final float ATTACK_RANGE = 0.8f;   // Reduced from 1.2f to 0.8f for closer attacks
    
    // Abstract methods that must be implemented by specific monsters
    public abstract void initializeAnimations();
    protected abstract void disposeMonster();
    
    // Common methods
    public void draw(SpriteBatch batch) {
        if (!visible || batch == null || getCurrentAnimation() == null) return;
        
        TextureRegion currentFrame = getCurrentAnimation().getKeyFrame(stateTime, true);
        if (currentFrame == null) return;
        
        float x = position.x - currentFrame.getRegionWidth() / 2;
        float y = position.y - currentFrame.getRegionHeight() / 2;
        
        if (isFlipped != currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        
        batch.draw(currentFrame, x, y);
    }
    
    protected Animation<TextureRegion> getCurrentAnimation() {
        switch (currentState) {
            case IDLE: return idleAnimation;
            case RUNNING: return runAnimation;
            case ATTACKING: return attack1Animation; // Default to attack1
            case TAKE_HIT: return takeHitAnimation;
            case DEATH: return deathAnimation;
            default: return idleAnimation;
        }
    }
    
    protected void createBody(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        bounds = new Rectangle(x - width/2, y - height/2, width, height);
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void takeDamage(float damage) {
        hp -= damage;
        if (hp <= 0) {
            changeState(MonsterState.DEATH);
        } else {
            changeState(MonsterState.TAKE_HIT);
        }
    }
    
    // Getters
    public Rectangle getBounds() { return new Rectangle(
        position.x - bounds.width/2,
        position.y - bounds.height/2,
        bounds.width,
        bounds.height
    ); }
    public float getHp() { return hp; }
    public float getAttack() { return attack; }
    
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        renderMonster(batch);  // Abstract method for specific monster rendering
    }
    
  
    protected  void renderMonster(SpriteBatch batch){
        if (isTransparent) {
            batch.setColor(1, 1, 1, 0.5f); // Example: 50% transparency
        }
        
        TextureRegion currentFrame = null;
        
        switch (currentState) {
            case IDLE:
                currentFrame = idleAnimation.getKeyFrame(stateTime, true);
                break;
            case RUNNING:
                currentFrame = runAnimation.getKeyFrame(stateTime, true);
                break;
            case ATTACKING:
                currentFrame = attack1Animation.getKeyFrame(stateTime, false);
                break;
            case TAKE_HIT:
                currentFrame = takeHitAnimation.getKeyFrame(stateTime, false);
                break;
            case DEATH:
                currentFrame = deathAnimation.getKeyFrame(stateTime, false);
                break;
        }
        
        if (currentFrame != null) {
            float x = position.x - currentFrame.getRegionWidth() / 2f;
            float y = position.y - currentFrame.getRegionHeight() / 2f;
            
            if (isFlipped) {
                batch.draw(currentFrame, 
                    x + currentFrame.getRegionWidth(), y,
                    -currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
            } else {
                batch.draw(currentFrame, x, y);
            }
        }
        
        batch.setColor(1, 1, 1, 1); // Reset color
    }
    
    public void dispose() {
        disposeMonster();  // Abstract method for specific monster cleanup
    }
    
    
    public void init() {
        initializeAnimations();
    }
    
    // Add these fields
    protected HighgroundManager highgroundManager;
    protected LowgroundManager lowgroundManager;
    protected static final float POSITION_UPDATE_INTERVAL = 1/60f; // 60 times per second
    protected float positionUpdateTimer = 0;
    
    protected TileCollision tileCollision;
    
    public void setTileCollision(TileCollision tileCollision) {
        this.tileCollision = tileCollision;
    }
    
    public void update(float delta, Player player) {
        if (!isAlive()) {
            currentState = MonsterState.DEATH;
            velocity.setZero();
            return;
        }

        // Update position based on velocity
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x - width/2, position.y - height/2);
        
        updateAnimation(delta);
    }
    
    protected boolean isPlayerInRange(Player player) {
        float distance = Vector2.dst(
            position.x, position.y,
            player.getPosition().x, player.getPosition().y
        );
        return distance <= DETECTION_RANGE * PPM;
    }
    
    private void updateTimers(float delta) {
        attackTimer -= delta;
        contactDamageTimer -= delta;
    }
    
    private void handleStateTransition(float delta) {
        stateTransitionTimer -= delta;
        if (stateTransitionTimer <= 0 && nextState != null) {
            currentState = nextState;
            nextState = null;
            stateTime = 0;
        }
    }
    
    private void handleAttack(Player player) {
        // Only check for new attacks if not already attacking or finishing attack
        if (currentState != MonsterState.ATTACKING && !isFinishingAttack) {
            Vector2 toPlayer = player.getPosition().cpy().sub(position);
            float distanceToPlayer = toPlayer.len();

            if (distanceToPlayer <= DAMAGE_RADIUS && attackTimer <= 0) {
                changeState(MonsterState.ATTACKING);
                isFinishingAttack = true;
                stateTime = 0;
                attackTimer = ATTACK_COOLDOWN;
                player.takeDamage(attack);
            }
        } else {
            // Keep monster completely stationary during attack
            velocity.set(0, 0);
        }
    }
    
    
    public void onPlayerCollisionStart() {
        isCollidingWithPlayer = true;
    }
    
    public void onPlayerCollisionEnd() {
        isCollidingWithPlayer = false;
        if (currentState == MonsterState.ATTACKING) {
            currentState = MonsterState.IDLE;
        }
    }
    
    public MonsterState getCurrentState() {
        return currentState;
    }
    
    // Add this new method to handle state changes
    protected void changeState(MonsterState newState) {
        if (currentState == newState) return;
        
        // Complete current animation if it's an important state
        if (currentState == MonsterState.ATTACKING || currentState == MonsterState.TAKE_HIT) {
            nextState = newState;
            stateTransitionTimer = STATE_TRANSITION_DELAY;
        } else {
            currentState = newState;
            stateTime = 0;
        }
    }
    
    // Add method to handle being pushed
    public void handlePush(Vector2 playerVelocity) {
        if (currentState != MonsterState.ATTACKING && pushRecoveryTimer <= 0) {
            Vector2 pushForce = playerVelocity.cpy().scl(PUSH_FORCE);
            velocity.set(pushForce);
            pushRecoveryTimer = PUSH_RECOVERY_TIME;
        }
    }
    
   
    
    public void setMap(TiledMap map) {
        this.map = map;
    }
    
    protected void updateAnimation(float delta) {
        stateTime += delta;
        
        if (currentState == MonsterState.ATTACKING) {
            if (stateTime >= attack1Animation.getAnimationDuration()) {
                isFinishingAttack = false;
                changeState(MonsterState.RUNNING);
                stateTime = 0;
                attackTimer = ATTACK_COOLDOWN;
            }
        }
        
        if (currentState == MonsterState.TAKE_HIT && 
            takeHitAnimation.isAnimationFinished(stateTime)) {
            currentState = MonsterState.IDLE;
        }
    }
    
    //Add setter for managers
    public void setGroundManagers(HighgroundManager highgroundManager, LowgroundManager lowgroundManager) {
        this.highgroundManager = highgroundManager;
        this.lowgroundManager = lowgroundManager;
    }
    
    // In constructor or after setting initial stats:
   
    public Vector2 getPosition() {
        return position;
    }
    
    public void setPosition(float x, float y) {
        position.set(x, y);
        bounds.setPosition(x, y);
    }
    
    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }
    
    public void update(float delta) {
        // Update position based on velocity
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);
        
        // Update animation
        updateAnimation(delta);
    }
} 