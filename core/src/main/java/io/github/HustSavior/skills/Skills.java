package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface Skills {

    Animation<TextureRegion> createAnimation();

    void update(float delta);

    boolean isReady();

    CooldownController getCooldown();

    void setImprovedSize(float scale);

}
