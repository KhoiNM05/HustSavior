package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class Shield extends Item{
    public Shield(Sprite sprite, int x, int y, float PPM, World world){
        super(sprite, x, y, PPM, world);
        sprite.setSize(sprite.getRegionWidth() * 0.03f, sprite.getRegionHeight() * 0.04f);
        this.imagePath = "item/shield.png";
        this.dialogMessage = "a shield!\nLet's enjoy the game!";
    }
}

