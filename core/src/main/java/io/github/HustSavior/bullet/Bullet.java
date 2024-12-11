package io.github.HustSavior.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import io.github.HustSavior.utils.GameConfig;

public class Bullet {
    private static final float PPM = GameConfig.PPM;
    // Add sound effects
    private Sound shootSound;
    private Sound impactSound;
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private float width;
    private float height;
    private float rotation;
    private Body body;
    private int collisionCount = 0;

    public Bullet(World world, float x, float y, float vx, float vy, float width, float height) {
        // Load sound effects
        // shootSound = Gdx.audio.newSound(Gdx.files.internal("sound/bullet_effect.mp3"));
        impactSound = Gdx.audio.newSound(Gdx.files.internal("sound/bullet_effect.mp3"));

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

        // Create physics body for bullet
        createBody(world, x / PPM, y / PPM);

        // Play shoot sound when bullet is created
        // shootSound.play(0.1f);
    }

    private void createBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.bullet = true; // Enable bullet physics for better collision detection

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(width / (2 * PPM)); // Use half width as radius

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        // Set collision filtering
        fixtureDef.filter.categoryBits = 0x0004; // Bullet category
        fixtureDef.filter.maskBits = 0x0001 | 0x0002; // Collide with world objects and players

        body.createFixture(fixtureDef);
        body.setLinearVelocity(velocity.x, velocity.y);
        body.setUserData(this);

        shape.dispose();
    }

    public Body getBody() {
        return body;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void update(float deltaTime) {
        position.set(body.getPosition().x * PPM, body.getPosition().y * PPM);
        rotation = (float) Math.toDegrees(Math.atan2(body.getLinearVelocity().y, body.getLinearVelocity().x));
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width / 2, height / 2, width, height, 1, 1, rotation, 0, 0,
            texture.getWidth(), texture.getHeight(), false, false);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void incrementCollisionCount() {
        collisionCount++;
        // Play impact sound on collision
        impactSound.play(0.05f);
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    // Add dispose method or modify existing one
    public void dispose() {
        // if (shootSound != null) shootSound.dispose();
        if (impactSound != null) impactSound.dispose();
        if (texture != null) texture.dispose();
    }
}
