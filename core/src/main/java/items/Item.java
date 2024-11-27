package items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Item {
    static int rectX=50, rectY=50;
    Body body;
    Sprite sprite;
    String filePath;

    public Item(int x, int y, World world){
        body=createStaticBody(x, y, world);
    }

    public Body createStaticBody(int x, int y, World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x+rectX/2, y+rectY/2);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape=new PolygonShape();
        shape.setAsBox(rectX/2, rectY/2);

        ItemBody.createFixture(shape, 0.0f);
        shape.dispose();

        return ItemBody;
    }

    public void draw(SpriteBatch batch){
        sprite.draw(batch);
    }

}
