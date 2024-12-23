package io.github.HustSavior.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Goblin extends AbstractMonster {
    public Goblin(float x, float y, Player player) {
        super(player);
        this.hp = 70;           // Medium health
        this.attack = 12;       // Medium attack
        this.speed = 1f;
        this.DETECTION_RANGE = 600f;
        this.ATTACK_RANGE = 30f;
        this.ATTACK_COOLDOWN = 1.0f;
        this.CHASE_SPEED = 65f;
        this.currentState = MonsterState.IDLE;
        
        createBody(x, y);
        initializeAnimations();
    }

    @Override
    public void initializeAnimations() {
        Texture idleSheet = new Texture("sprites/monster/Goblin/Idle.png");
        Texture runSheet = new Texture("sprites/monster/Goblin/Run.png");
        Texture attackSheet = new Texture("sprites/monster/Goblin/Attack.png");
        Texture hitSheet = new Texture("sprites/monster/Goblin/Take Hit.png");
        Texture deathSheet = new Texture("sprites/monster/Goblin/Death.png");
        
        idleAnimation = createAnimation(idleSheet, 4, 0.1f);
        runAnimation = createAnimation(runSheet, 8, 0.3f);
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