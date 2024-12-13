package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Shield extends Item{
    public Shield(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        sprite.setSize(sprite.getRegionWidth() * 0.03f, sprite.getRegionHeight() * 0.04f);
        this.imagePath = "item/shield.png";
        this.dialogMessage = "a shield!\nYou can now block attacks for 10s";
        this.id = 5;
        this.defenseBoost = 10;
    }
}

