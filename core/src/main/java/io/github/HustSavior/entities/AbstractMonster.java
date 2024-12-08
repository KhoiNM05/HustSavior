package io.github.HustSavior.entities;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static io.github.HustSavior.utils.GameConfig.PPM;

public abstract class AbstractMonster {
    protected static final short MONSTER_CATEGORY = 0x0002;
    protected static final short MONSTER_MASK = -1;
    
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
    protected static final float ATTACK_COOLDOWN_TIME = 1.0f; // 1 second between attacks
    
    // Abstract methods that must be implemented by specific monsters
    protected abstract void initializeAnimations();
    protected abstract void updateAnimation(float delta);
    
    // Common methods
    public void draw(SpriteBatch batch) {
        if (batch == null || getCurrentAnimation() == null) return;
        
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
        
        body = world.createBody(bodyDef);
        body.setUserData(this);
        
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = MONSTER_CATEGORY;
        fixtureDef.filter.maskBits = MONSTER_MASK;
        
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
    
    protected abstract void renderMonster(SpriteBatch batch);
    
    public void dispose() {
        disposeMonster();  // Abstract method for specific monster cleanup
    }
    
    protected abstract void disposeMonster();
    
    public void init() {
        initializeAnimations();
    }
    
    public void update(float delta, Player player) {
        stateTime += delta;
        updateAnimation(delta);
        
        // Update attack cooldown
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        // If colliding with player and cooldown is ready, attack
        if (isCollidingWithPlayer && attackCooldown <= 0) {
            currentState = MonsterState.ATTACKING;
            attackCooldown = ATTACK_COOLDOWN_TIME;
        } else if (!isCollidingWithPlayer) {
            // Normal movement/chase logic when not colliding
            moveTowardsPlayer(player);
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
    
    private void moveTowardsPlayer(Player player) {
        if (!isAlive() || currentState == MonsterState.ATTACKING) return;
        
        Vector2 playerPos = player.getBody().getPosition();
        Vector2 monsterPos = body.getPosition();
        Vector2 direction = playerPos.cpy().sub(monsterPos).nor();
        
        // Only move if not too close to player
        float distance = monsterPos.dst(playerPos);
        if (distance > 1.5f) {  // Adjust this value as needed
            body.setLinearVelocity(direction.x * speed, direction.y * speed);
            currentState = MonsterState.RUNNING;
            isFlipped = direction.x < 0;
        } else {
            body.setLinearVelocity(0, 0);
            currentState = MonsterState.IDLE;
        }
    }
    
    public MonsterState getCurrentState() {
        return currentState;
    }
} 