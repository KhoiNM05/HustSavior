package io.github.HustSavior.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class Bullet implements Disposable {
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

    public Bullet(float x, float y, float directionX, float directionY) {
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

    public void handleCollision() {
        collisionCount++;
        if (collisionCount >= MAX_COLLISIONS) {
            active = false;
        }
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
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
