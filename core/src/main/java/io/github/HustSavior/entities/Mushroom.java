package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Mushroom extends AbstractMonster {
    public Mushroom(float x, float y, Player player) {
        super(player);
        this.hp = 120;
        this.attack = 20;
        this.speed = 0.5f;
        this.DETECTION_RANGE = 400f;
        this.ATTACK_RANGE = 40f;
        this.ATTACK_COOLDOWN = 2.0f;
        this.CHASE_SPEED = 60f;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
        initializeAnimations();
    }

    @Override
    public void initializeAnimations() {
        Texture idleSheet = new Texture("sprites/monster/Mushroom/Idle.png");
        Texture runSheet = new Texture("sprites/monster/Mushroom/Run.png");
        Texture attackSheet = new Texture("sprites/monster/Mushroom/Attack.png");
        Texture hitSheet = new Texture("sprites/monster/Mushroom/Take Hit.png");
        Texture deathSheet = new Texture("sprites/monster/Mushroom/Death.png");
        
        idleAnimation = createAnimation(idleSheet, 4, 0.1f);
        runAnimation = createAnimation(runSheet, 8, 0.5f);
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