package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class SpecificMonster extends AbstractMonster {
    private static final float ATTACK_RANGE = 1f; // In meters
    private static final float DETECTION_RANGE = 10f; // In meters
    private static final float ATTACK_COOLDOWN = 1.5f; // Seconds between attacks
    private float attackTimer = 0;
    
    public SpecificMonster(World world, float x, float y) {
        this.world = world;
        this.hp = 100;
        this.attack = 10;
        this.speed = 2;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
        initializeAnimations();
    }
    
    @Override
    protected void initializeAnimations() {
        // Load specific monster's sprite sheets and initialize animations
        TextureAtlas atlas = new TextureAtlas("monsters/specific_monster.atlas");
        
        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"));
        runAnimation = new Animation<>(0.1f, atlas.findRegions("run"));
        // ... initialize other animations
    }
    
    @Override
    public void update(float delta, Player player) {
        if (!isAlive()) {
            currentState = MonsterState.DEATH;
            return;
        }
        
        updateAnimation(delta);
        attackTimer -= delta;
        
        Vector2 playerPos = player.getBody().getPosition();
        Vector2 monsterPos = body.getPosition();
        Vector2 direction = new Vector2(playerPos).sub(monsterPos);
        float distance = direction.len();
        
        // Update facing direction
        isFlipped = direction.x < 0;
        
        if (distance <= DETECTION_RANGE) {
            // Normalize direction and apply movement
            direction.nor();
            
            if (distance <= ATTACK_RANGE) {
                // In attack range
                if (attackTimer <= 0) {
                    currentState = MonsterState.ATTACKING;
                    attackPlayer(player);
                    attackTimer = ATTACK_COOLDOWN;
                }
                // Stop moving when attacking
                body.setLinearVelocity(0, 0);
            } else {
                // Move towards player
                currentState = MonsterState.RUNNING;
                body.setLinearVelocity(direction.x * speed, direction.y * speed);
            }
        } else {
            // Out of detection range, stop moving
            currentState = MonsterState.IDLE;
            body.setLinearVelocity(0, 0);
        }
    }
    
    private void attackPlayer(Player player) {
        // Check if player has shield before dealing damage
        // You'll need to add a method in Player class to check shield status
        player.takeDamage(attack);
    }
    
    @Override
    protected void updateAnimation(float delta) {
        stateTime += delta;
        
        // Reset state if attack animation is finished
        if (currentState == MonsterState.ATTACKING && 
            attack1Animation.isAnimationFinished(stateTime)) {
            currentState = MonsterState.IDLE;
        }
        
        // Reset state if take hit animation is finished
        if (currentState == MonsterState.TAKE_HIT && 
            takeHitAnimation.isAnimationFinished(stateTime)) {
            currentState = MonsterState.IDLE;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void renderMonster(SpriteBatch batch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void disposeMonster() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
} 