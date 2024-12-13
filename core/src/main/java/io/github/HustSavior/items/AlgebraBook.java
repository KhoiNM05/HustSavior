package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class AlgebraBook extends Item{
    public AlgebraBook(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        this.imagePath = "item/algebra.jpg";
        this.dialogMessage = "an Algebra Book!\nDefense +5";
        this.id = 2;
        this.defenseBoost = 5;
    }


}

