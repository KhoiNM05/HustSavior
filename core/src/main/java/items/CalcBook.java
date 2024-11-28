package items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;

public class CalcBook extends Item{
    public CalcBook(int x, int y, float PPM, World world){
        super(x, y, PPM, world);
        sprite=new Sprite(new Texture("skills/parabol7.png"));
        sprite.setPosition(x, y);
    }
}
