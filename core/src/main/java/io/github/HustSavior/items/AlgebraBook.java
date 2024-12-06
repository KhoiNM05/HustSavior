package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class AlgebraBook extends Item{
    public AlgebraBook(Sprite sprite, int x, int y, float PPM, World world){
        super(sprite, x, y, PPM, world);
        this.imagePath = "item/algebra.jpg";
        this.dialogMessage = "You got an Algebra Book!\nMatrix operations unlocked!";
    }


}

