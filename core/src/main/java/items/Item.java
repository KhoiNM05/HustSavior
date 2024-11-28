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
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((x+rectX/2f)/PPM, (y+rectY/2f)/PPM);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape=new PolygonShape();
        shape.setAsBox(rectX/2.0f/PPM, rectY/2.0f/PPM);

        FixtureDef fixtureDef= new FixtureDef();
        fixtureDef.shape=shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.0f;
        fixtureDef.filter.categoryBits=0x0001;

        ItemBody.createFixture(fixtureDef);
        shape.dispose();

        return ItemBody;
    }

    public void draw(SpriteBatch batch){
        sprite.draw(batch);
    }

}
