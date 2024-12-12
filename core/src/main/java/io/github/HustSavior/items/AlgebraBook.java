package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class AlgebraBook extends Item{
    public AlgebraBook(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        this.imagePath = "item/algebra.jpg";
        this.dialogMessage = "an Algebra Book!\nMatrix operations unlocked";
        this.id = 2;
    }


}

