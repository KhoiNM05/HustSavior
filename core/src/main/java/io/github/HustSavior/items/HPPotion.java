package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class HPPotion extends Item {
    public HPPotion(Sprite sprite, int x, int y, float PPM, World world) {
        super(sprite, x, y, PPM, world);
        sprite.setSize(sprite.getRegionWidth() * 0.05f, sprite.getRegionHeight() * 0.05f);
    }
}

