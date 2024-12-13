package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class HPPotion extends Item {
    public HPPotion(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        sprite.setSize(sprite.getRegionWidth() * 0.05f, sprite.getRegionHeight() * 0.05f);
        this.imagePath = "item/hp_potion.png";
        this.dialogMessage = "a Health Potion!\nRestores 50 HP";
        this.id = 4;
        this.healthBoost = 50;
    }
}

