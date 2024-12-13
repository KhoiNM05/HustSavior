package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public interface Skills {

    Animation<TextureRegion> createAnimation();

    void draw(SpriteBatch batch);

    void update(float delta);

    boolean isReady();

    CooldownController getCooldown();

    void setImprovedSize(float scale);

}
