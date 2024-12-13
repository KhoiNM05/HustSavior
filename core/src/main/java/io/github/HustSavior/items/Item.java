package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.entities.Player;

public abstract class Item implements Disposable {
    protected Rectangle bounds;
    protected Sprite sprite;
    private boolean collected = false;
    protected String imagePath;
    protected String dialogMessage;
    private boolean visible = true;
    int id;
    protected float attackBoost = 0;
    protected float healthBoost = 0;
    protected float speedBoost = 0;
    protected float defenseBoost = 0;

    public Item(Sprite sprite, int x, int y) {
        this.sprite = sprite;
        sprite.setPosition(x, y);
        sprite.setSize(sprite.getRegionWidth() * 0.1f, sprite.getRegionHeight() * 0.1f);
        
        // Create collision bounds
        bounds = new Rectangle(
            x, 
            y, 
            sprite.getWidth(), 
            sprite.getHeight()
        );
        
        this.dialogMessage = "You got an item!";
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void draw(SpriteBatch batch) {
        if (visible && !collected) {
            sprite.draw(batch);
        }
    }

    @Override
    public void dispose() {
        if (sprite != null && sprite.getTexture() != null) {
            sprite.getTexture().dispose();
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getId() {
        return id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public boolean checkCollision(Rectangle playerBounds) {
        if (!collected && visible) {
            return bounds.overlaps(playerBounds);
        }
        return false;
    }

    public void applyEffects(Player player) {
        if (healthBoost > 0) {
            player.heal(healthBoost);
        }
        if (attackBoost > 0) {
            player.increaseAttack(attackBoost);
        }
        if (speedBoost > 0) {
            player.increaseSpeed(speedBoost);
        }
        if (defenseBoost > 0) {
            player.increaseDefense(defenseBoost);
        }
    }
}
