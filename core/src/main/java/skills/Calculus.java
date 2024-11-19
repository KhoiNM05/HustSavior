package skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Calculus extends Sprite {

    TextureRegion[] animation;
    Animation<TextureRegion> cast;
    public Calculus(Sprite sprite){
        super(sprite);

        animation= new TextureRegion[7];
        animation[0]= new TextureRegion(new Texture(""))
    }

    public void draw(SpriteBatch batch){
        super.draw(batch);
    }
}
