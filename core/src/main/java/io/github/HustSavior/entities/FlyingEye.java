package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import static io.github.HustSavior.utils.GameConfig.PPM;

public class FlyingEye extends AbstractMonster {
    private static final float ATTACK_RANGE = 0.1f;
    private static final float DETECTION_RANGE = 10f;
    private static final float ATTACK_COOLDOWN = 1.0f;
    private static final float FLYING_EYE_SPEED = 1f;
    private float attackTimer = 0;

    public FlyingEye(World world, float x, float y) {
        this.world = world;
        this.hp = 60;           // Weaker but faster
        this.attack = 10;
        this.speed = FLYING_EYE_SPEED;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
    }

    @Override
    protected void initializeAnimations() {
        Texture idleSheet = new Texture("sprites/monster/Flying eye/Flight.png");
        Texture attackSheet = new Texture("sprites/monster/Flying eye/Attack.png");
        Texture hitSheet = new Texture("sprites/monster/Flying eye/Take Hit.png");
        Texture deathSheet = new Texture("sprites/monster/Flying eye/Death.png");
        
        idleAnimation = createAnimation(idleSheet, 8, 0.1f);
        runAnimation = idleAnimation; // Flying eye uses same animation for idle/run
        attack1Animation = createAnimation(attackSheet, 8, 1.0f);
        takeHitAnimation = createAnimation(hitSheet, 4, 0.2f);
        deathAnimation = createAnimation(deathSheet, 4, 0.3f);
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

        isFlipped = direction.x < 0;

        if (distance <= DETECTION_RANGE) {
            direction.nor();
            
            if (distance <= ATTACK_RANGE) {
                if (attackTimer <= 0) {
                    currentState = MonsterState.ATTACKING;
                    player.takeDamage(attack);
                    attackTimer = ATTACK_COOLDOWN;
                }
                body.setLinearVelocity(0, 0);
            } else {
                currentState = MonsterState.RUNNING;
                body.setLinearVelocity(direction.x * speed, direction.y * speed);
            }
        } else {
            currentState = MonsterState.IDLE;
            body.setLinearVelocity(0, 0);
        }
    }

    @Override
    protected void updateAnimation(float delta) {
        stateTime += delta;
        
        if (currentState == MonsterState.ATTACKING) {
            // Don't change state until animation fully completes
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

    @Override
    protected void renderMonster(SpriteBatch batch) {
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
    }

    @Override
    protected void disposeMonster() {
        if (idleAnimation != null) idleAnimation.getKeyFrame(0).getTexture().dispose();
        if (runAnimation != null) runAnimation.getKeyFrame(0).getTexture().dispose();
        if (attack1Animation != null) attack1Animation.getKeyFrame(0).getTexture().dispose();
        if (takeHitAnimation != null) takeHitAnimation.getKeyFrame(0).getTexture().dispose();
        if (deathAnimation != null) deathAnimation.getKeyFrame(0).getTexture().dispose();
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frameCount, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(sheet,
            sheet.getWidth() / frameCount,
            sheet.getHeight()
        );
        
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = tmp[0][i];
        }
        
        return new Animation<>(frameDuration, frames);
    }
} 