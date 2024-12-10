package io.github.HustSavior.entities;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

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
    protected Body body;
    protected World world;
    
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
    
    // Abstract methods that must be implemented by specific monsters
    protected abstract void initializeAnimations();
    protected abstract void disposeMonster();
    
    // Common methods
    public void draw(SpriteBatch batch) {
        if (!visible || batch == null || getCurrentAnimation() == null) return;
        
        TextureRegion currentFrame = getCurrentAnimation().getKeyFrame(stateTime, true);
        if (currentFrame == null) return;
        
        float x = body.getPosition().x * PPM - currentFrame.getRegionWidth() / 2;
        float y = body.getPosition().y * PPM - currentFrame.getRegionHeight() / 2;
        
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
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        
        body = world.createBody(bodyDef);
        body.setUserData(this);
        
        CircleShape shape = new CircleShape();
        shape.setRadius(0.3f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = MONSTER_CATEGORY;
        fixtureDef.filter.maskBits = (short)(MONSTER_MASK | COLLISION_LAYER_BITS | TRANSPARENCY_BOUNDS_BITS);
        
        body.createFixture(fixtureDef);
        shape.dispose();
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void takeDamage(float damage) {
        hp -= damage;
        if (hp <= 0) {
            currentState = MonsterState.DEATH;
        } else {
            currentState = MonsterState.TAKE_HIT;
        }
    }
    
    // Getters
    public Body getBody() { return body; }
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
            float x = body.getPosition().x * PPM - currentFrame.getRegionWidth() / 2f;
            float y = body.getPosition().y * PPM - currentFrame.getRegionHeight() / 2f;
            
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
    
    public void update(float delta, Player player) {
        if (!isAlive()) {
            currentState = MonsterState.DEATH;
            return;
        }

        // Update position based on ground height
        positionUpdateTimer += delta;
        if (positionUpdateTimer >= POSITION_UPDATE_INTERVAL) {
            positionUpdateTimer = 0;
            Vector2 currentPos = body.getPosition();
            
            // Check and update highground position
            Vector2 highgroundPos = highgroundManager.updatePosition(currentPos.x * PPM, currentPos.y * PPM);
            // Check and update lowground position
            Vector2 lowgroundPos = lowgroundManager.updatePosition(currentPos.x * PPM, currentPos.y * PPM);
            
            // Apply position updates if needed
            if (highgroundManager.isInHighground() || lowgroundManager.isInLowground()) {
                body.setTransform(
                    highgroundManager.isInHighground() ? highgroundPos.x / PPM : lowgroundPos.x / PPM,
                    highgroundManager.isInHighground() ? highgroundPos.y / PPM : lowgroundPos.y / PPM,
                    0
                );
            }
        }

        // Debug: Check visibility status
        boolean visibilityStatus = isInSameTransparencyBounds(player);
        System.out.println("Debug: Visibility status for monster: " + visibilityStatus);
        setVisible(visibilityStatus);

        updateAnimation(delta);
        updateTimers(delta);
        
        if (stateTransitionTimer > 0) {
            handleStateTransition(delta);
            return;
        }

        // Handle attack and movement separately
        handleAttack(player);
        if (currentState != MonsterState.ATTACKING) {
            handleMovement(player);
        }
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
            Vector2 toPlayer = player.getBody().getPosition().cpy().sub(body.getPosition());
            float distanceToPlayer = toPlayer.len();

            if (distanceToPlayer <= DAMAGE_RADIUS && attackTimer <= 0) {
                body.setLinearVelocity(0, 0);
                changeState(MonsterState.ATTACKING);
                isFinishingAttack = true;
                stateTime = 0;
                attackTimer = ATTACK_COOLDOWN;
                player.takeDamage(attack);
            }
        } else {
            // Keep monster completely stationary during attack
            body.setLinearVelocity(0, 0);
        }
    }
    
    private void handleMovement(Player player) {
        if (currentState == MonsterState.ATTACKING || isFinishingAttack) {
            body.setLinearVelocity(0, 0);
            return;
        }

        Vector2 toPlayer = player.getBody().getPosition().cpy().sub(body.getPosition());
        float distanceToPlayer = toPlayer.len();
        
        if (distanceToPlayer > PERSONAL_SPACE) {
            Vector2 moveDirection = toPlayer.nor();
            
            // Check for obstacles in the direct path
            final boolean[] hit = {false}; // Use array to store hit status
            RayCastCallback rayCastCallback = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                    if ((fixture.getFilterData().categoryBits & COLLISION_LAYER_BITS) != 0) {
                        hit[0] = true;
                        return 0;
                    }
                    return -1;
                }
            };
            
            world.rayCast(rayCastCallback, body.getPosition(), player.getBody().getPosition());
            
            // If there's an obstacle, try to find alternative path
            if (hit[0]) {
                // Try moving along the wall
                Vector2 leftPath = moveDirection.cpy().rotate90(1);
                Vector2 rightPath = moveDirection.cpy().rotate90(-1);
                
                // Check which direction has fewer obstacles
                if (checkPath(leftPath)) {
                    moveDirection = leftPath;
                } else if (checkPath(rightPath)) {
                    moveDirection = rightPath;
                }
            }
            
            body.setLinearVelocity(moveDirection.x * speed, moveDirection.y * speed);
            if (currentState != MonsterState.RUNNING) {
                changeState(MonsterState.RUNNING);
            }
            isFlipped = moveDirection.x < 0;
        } else {
            body.setLinearVelocity(0, 0);
        }
    }
    
    private boolean checkPath(Vector2 direction) {
        final boolean[] clear = {true};
        RayCastCallback pathCheck = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if ((fixture.getFilterData().categoryBits & COLLISION_LAYER_BITS) != 0) {
                    clear[0] = false;
                    return 0;
                }
                return -1;
            }
        };
        
        Vector2 start = body.getPosition();
        Vector2 end = start.cpy().add(direction.cpy().scl(1.0f));
        world.rayCast(pathCheck, start, end);
        
        return clear[0];
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
            body.setLinearVelocity(pushForce);
            pushRecoveryTimer = PUSH_RECOVERY_TIME;
        }
    }
    
    private boolean isInSameTransparencyBounds(Player player) {
        System.out.println("Debug: Starting bounds check");
        
        if (map == null || world == null) {
            System.out.println("Debug: Map or World is null");
            return true;
        }

        final boolean[] inSameBounds = {true};
        float checkRadius = 0.5f; // Adjust this value based on your needs

        System.out.println("Debug: Monster position: " + body.getPosition());
        System.out.println("Debug: Player position: " + player.getBody().getPosition());

        world.QueryAABB(fixture -> {
            if ((fixture.getFilterData().categoryBits & TRANSPARENCY_BOUNDS_BITS) != 0) {
                boolean monsterInBounds = fixture.testPoint(body.getPosition());
                boolean playerInBounds = fixture.testPoint(player.getBody().getPosition());
                
                System.out.println("Debug: Found transparency bounds");
                System.out.println("Debug: Monster in bounds: " + monsterInBounds);
                System.out.println("Debug: Player in bounds: " + playerInBounds);

                if (monsterInBounds != playerInBounds) {
                    inSameBounds[0] = false;
                    return false;
                }
            }
            return true;
        }, 
        body.getPosition().x - checkRadius, 
        body.getPosition().y - checkRadius,
        body.getPosition().x + checkRadius, 
        body.getPosition().y + checkRadius);

        System.out.println("Debug: Final result: " + inSameBounds[0]);
        return inSameBounds[0];
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
   
} 