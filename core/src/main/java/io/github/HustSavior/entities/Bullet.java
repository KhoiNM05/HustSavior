package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.HustSavior.Play;

public class Bullet {
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private float width;
    private float height;
    private float rotation;
    private Body body;

    public Bullet(World world, float x, float y, float vx, float vy, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(vx, vy);
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            this.texture = new Texture("bullets/pen.png");
        } else if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            this.texture = new Texture("bullets/pencil.png");
        } else {
            this.texture = new Texture("bullets/pencil.png");
        }
        this.width = width;
        this.height = height;

        // Create bullet body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Play.PPM, y / Play.PPM);
        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(width / 2 / Play.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = Play.CATEGORY_PLAYER;
        fixtureDef.filter.maskBits = Play.MASK_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setLinearVelocity(vx / Play.PPM, vy / Play.PPM);
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    // public float getRotation() {
    //    return rotation;
    //}

    public void update(float deltaTime) {
        position.set(body.getPosition().x * Play.PPM, body.getPosition().y * Play.PPM);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width / 2, height / 2, width, height, 1, 1, rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    //public void dispose() {
    //    texture.dispose();
    //}

    public Vector2 getPosition() {
        return position;
    }

    // public void setPosition(Vector2 position) {
    //    this.position = position;
    //   body.setTransform(position.x / Play.PPM, position.y / Play.PPM, body.getAngle());
    //}

    //public Vector2 getVelocity() {
    //    return velocity;
    //}

    //public void setVelocity(Vector2 velocity) {
    //    this.velocity = velocity;
    //    body.setLinearVelocity(velocity.x / Play.PPM, velocity.y / Play.PPM);
    //}
}
