package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CalcBook extends Item{
    public CalcBook(Sprite sprite, int x, int y) {
        super(sprite, x, y);
        this.imagePath = "item/calculus1.jpg";
        this.dialogMessage = "a Calculus Book!\nAttack +5";
        this.id = 1;
        this.attackBoost = 5;
    }


}
