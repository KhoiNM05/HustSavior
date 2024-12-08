package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class CalcBook extends Item{
    public CalcBook(Sprite sprite, int x, int y, float PPM, World world){
        super(sprite, x, y, PPM, world);
        this.imagePath = "item/calculus1.jpg";
        this.dialogMessage = "a Calculus Book!\nNow you can solve integrals";
    }


}
