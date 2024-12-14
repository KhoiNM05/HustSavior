package io.github.HustSavior.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class Bullet implements Disposable {
    private Sound impactSound;
    private Vector2 position;
    private Vector2 velocity;
    private float width = 12;  // Adjust based on your bullet sprite
    private float height = 5;
    private float rotation;
    private Texture texture;
    private boolean active = true;
    private int collisionCount = 0;
    private static final float BULLET_SPEED = 300f; // Pixels per second
    private static final int MAX_COLLISIONS = 3;
    private static final float BOUNCE_FACTOR = 1.0f; // Controls how much speed is retained after bounce

    public Bullet(float x, float y, float directionX, float directionY) {
        // impactSound = Gdx.audio.newSound(Gdx.files.internal("sound/bullet_effect.mp3"));
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(BULLET_SPEED);

        // Load appropriate texture based on input
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            this.texture = new Texture("bullets/pen.png");
        } else {
            this.texture = new Texture("bullets/pencil.png");
        }

        this.rotation = (float) Math.toDegrees(Math.atan2(directionY, directionX));
    }

    public void update(float delta) {
        // Update position based on velocity
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public Rectangle getHitbox() {
        return new Rectangle(
            position.x - width/2,
            position.y - height/2,
            width,
            height
        );
    }

    public boolean isActive() {
        return active && collisionCount < MAX_COLLISIONS;
    }

    public void handleCollision(boolean hitVertical) {
        collisionCount++;

        if (collisionCount >= MAX_COLLISIONS) {
            active = false;
            return;
        }

        // Store old velocity
        Vector2 oldVel = velocity.cpy();

        // Reflect velocity and normalize speed
        if (hitVertical) {
            velocity.x = -oldVel.x;
            velocity.nor().scl(BULLET_SPEED);
            // Move bullet out of collision area more significantly
            position.x += Math.signum(velocity.x) * width;
        } else {
            velocity.y = -oldVel.y;
            velocity.nor().scl(BULLET_SPEED);
            // Move bullet out of collision area more significantly
            position.y += Math.signum(velocity.y) * height;
        }

        active = true;
        rotation = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));

        Gdx.app.debug("Bullet", "Collision handled: pos=" + position + ", vel=" + velocity);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture,
            position.x - width/2, position.y - height/2,
            width/2, height/2,
            width, height,
            1, 1,
            rotation,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false);
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void incrementCollisionCount() {
        collisionCount++;
        // impactSound.play(0.05f);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public void dispose() {
        // if (impactSound != null) impactSound.dispose();
        if (texture != null) {
            texture.dispose();
        }
    }
}
