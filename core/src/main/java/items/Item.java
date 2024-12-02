package items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Item {
    static int rectX=20, rectY=10;
    Body body;
    Sprite sprite;

    public Item(int x, int y, float PPM, World world){
        body=createStaticBody(x, y, PPM, world);
    }

    public Body createStaticBody(int x, int y, float PPM, World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((x+sprite.getRegionWidth()/2f)/PPM, (y+sprite.getRegionHeight()/2f)/PPM);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape=new PolygonShape();
        shape.setAsBox(sprite.getRegionWidth()/2f/PPM, sprite.getRegionHeight()/PPM);

        FixtureDef fixtureDef= new FixtureDef();
        fixtureDef.shape=shape;
        fixtureDef.isSensor=true;

        ItemBody.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        return ItemBody;
    }

    public void draw(SpriteBatch batch){
        sprite.draw(batch);
    }

}
