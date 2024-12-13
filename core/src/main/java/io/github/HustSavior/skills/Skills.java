package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface Skills {

    Animation<TextureRegion> createAnimation();

    void update(float delta);

    void setAOE(float aoe);

    CooldownController getCooldown();

    boolean isReady();

    void draw(SpriteBatch batch);

}
