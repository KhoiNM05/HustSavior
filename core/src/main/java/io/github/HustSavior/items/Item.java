package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Item {
    Body body;
    Sprite sprite;
    private boolean collected = false;

    public Item(Sprite sprite, int x, int y, float PPM, World world){
        this.sprite = sprite;
        sprite.setPosition(x, y);
        sprite.setSize(sprite.getRegionWidth() * 0.1f, sprite.getRegionHeight() * 0.1f);
        body = createStaticBody(x, y, PPM, world);
    }

    public Body createStaticBody(int x, int y, float PPM, World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((x+sprite.getWidth()/2f)/PPM, (y+sprite.getHeight()/2f)/PPM);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(sprite.getWidth()/2f/PPM, sprite.getHeight()/2f/PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = false;  // Changed to false to enable collision
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;

        ItemBody.createFixture(fixtureDef).setUserData(this);
        ItemBody.setUserData(this);  // Set user data for the body as well
        shape.dispose();

        return ItemBody;
    }

    public void draw(SpriteBatch batch){
        if (!collected) {
            sprite.draw(batch);
        }
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public boolean isCollected() {
        return collected;
    }
}
