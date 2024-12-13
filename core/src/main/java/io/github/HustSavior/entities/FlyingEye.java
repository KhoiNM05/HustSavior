package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FlyingEye extends AbstractMonster {
    public FlyingEye(float x, float y, Player player) {
        super(player);
        this.hp = 60;
        this.attack = 10;
        this.speed = 1.2f;
        this.DETECTION_RANGE = 500f;
        this.ATTACK_RANGE = 10f;
        this.ATTACK_COOLDOWN = 1.0f;
        this.CHASE_SPEED = 65f;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
        initializeAnimations();
    }

    @Override
    public void initializeAnimations() {
        Texture flightSheet = new Texture("sprites/monster/Flying eye/Flight.png");
        Texture attackSheet = new Texture("sprites/monster/Flying eye/Attack2.png");
        Texture hitSheet = new Texture("sprites/monster/Flying eye/Take Hit.png");
        Texture deathSheet = new Texture("sprites/monster/Flying eye/Death.png");
        
        idleAnimation = createAnimation(flightSheet, 8, 0.1f);
        runAnimation = idleAnimation;
        attack1Animation = createAnimation(attackSheet, 8, 0.6f);
        takeHitAnimation = createAnimation(hitSheet, 4, 0.5f);
        deathAnimation = createAnimation(deathSheet, 4, 1f);
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
            float x = position.x * PPM - currentFrame.getRegionWidth() / 2f;
            float y = position.y * PPM - currentFrame.getRegionHeight() / 2f;
            
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