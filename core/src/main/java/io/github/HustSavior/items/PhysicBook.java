package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicBook extends Item{
    public PhysicBook(Sprite sprite, int x, int y, float PPM, World world){
        super(sprite, x, y, PPM, world);
    }


}