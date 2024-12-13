package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Skeleton extends AbstractMonster {
    public Skeleton(float x, float y, Player player) {
        super(player);
        this.hp = 80;
        this.attack = 10;
        this.speed = 1f;
        this.DETECTION_RANGE = 450f;
        this.ATTACK_RANGE = 15f;
        this.ATTACK_COOLDOWN = 1.2f;
        this.CHASE_SPEED = 60f;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
        initializeAnimations();
    }

    @Override
    public void initializeAnimations() {
        Texture idleSheet = new Texture("sprites/monster/Skeleton/Idle.png");
        Texture walkSheet = new Texture("sprites/monster/Skeleton/Walk.png");
        Texture attackSheet = new Texture("sprites/monster/Skeleton/Attack.png");
        Texture hitSheet = new Texture("sprites/monster/Skeleton/Take Hit.png");
        Texture deathSheet = new Texture("sprites/monster/Skeleton/Death.png");
        
        idleAnimation = createAnimation(idleSheet, 4, 0.1f);
        runAnimation = createAnimation(walkSheet, 4, 0.5f);
        attack1Animation = createAnimation(attackSheet, 8, 0.6f);
        takeHitAnimation = createAnimation(hitSheet, 4, 0.5f);
        deathAnimation = createAnimation(deathSheet, 4, 1f);
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

    @Override
    protected void disposeMonster() {
        if (idleAnimation != null) idleAnimation.getKeyFrame(0).getTexture().dispose();
        if (runAnimation != null) runAnimation.getKeyFrame(0).getTexture().dispose();
        if (attack1Animation != null) attack1Animation.getKeyFrame(0).getTexture().dispose();
        if (takeHitAnimation != null) takeHitAnimation.getKeyFrame(0).getTexture().dispose();
        if (deathAnimation != null) deathAnimation.getKeyFrame(0).getTexture().dispose();
    }
} 