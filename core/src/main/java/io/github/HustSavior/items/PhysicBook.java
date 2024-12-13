package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class PhysicBook extends Item{
    public PhysicBook(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        this.imagePath = "item/physic1.jpg";
        this.dialogMessage = "a Physics Book!\nSpeed +5";
        this.id = 3;
        this.speedBoost = 5;
    }


}
