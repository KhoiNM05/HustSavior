package items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Item {
    Body body;
    Sprite sprite;
    private static float DEFAULT_WIDTH=80f;

    public Item(Sprite sprite, int x, int y, float PPM, World world){
        this.sprite=sprite;
        sprite.setPosition(x, y);
        body=createStaticBody(x, y, PPM, world);
    }

    public Body createStaticBody(int x, int y, float PPM, World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((x+sprite.getRegionWidth()/2f)/PPM, (y+sprite.getRegionHeight()/2f)/PPM);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape=new PolygonShape();
        shape.setAsBox(sprite.getRegionWidth()/2f/PPM, sprite.getRegionHeight()/2f/PPM);

        FixtureDef fixtureDef= new FixtureDef();
        fixtureDef.shape=shape;
        fixtureDef.isSensor=true;

        ItemBody.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        return ItemBody;
    }



    public void draw(SpriteBatch batch){
        int originalWidth=sprite.getRegionWidth();
        int originalHeight=sprite.getRegionHeight();
        float ratio=DEFAULT_WIDTH/(originalWidth*1.0f);
        batch.draw(sprite, sprite.getX(), sprite.getY(), originalWidth*ratio, originalHeight*ratio);

    }

}
