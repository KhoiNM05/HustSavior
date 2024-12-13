package io.github.HustSavior.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

import io.github.HustSavior.bullet.BulletManager;
import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private final Player player;
    private boolean left, right, up, down;
    private boolean facingLeft;
    private float stateTime;
    private boolean dialogActive = false;
    private BulletManager bulletManager;

    public InputHandler(Player player, BulletManager bulletManager) {
        this.player = player;
        this.bulletManager = bulletManager;
    }

    public void setDialogActive(boolean active) {
        this.dialogActive = active;
        if (active) {
            left = false;
            right = false;
            up = false;
            down = false;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (dialogActive) return false;
        
        switch (keycode) {
            case Input.Keys.A:
                left = true;
                player.setFacingDirection(true);
                break;
            case Input.Keys.D:
                right = true;
                player.setFacingDirection(false);
                break;
            case Input.Keys.W:
                up = true;
                break;
            case Input.Keys.S:
                down = true;
                break;
            case Input.Keys.SPACE:
                bulletManager.shootBullet();
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                left = false;
                facingLeft = true;
                break;
            case Input.Keys.D:
                right = false;
                facingLeft = false;
                break;
            case Input.Keys.W:
                up = false;
                break;
            case Input.Keys.S:
                down = false;
                break;
        }
        return true;
    }

    public void update(float delta) {
        if (dialogActive) {
            player.setVelocity(new Vector2(0, 0));
            return;
        }

        Vector2 velocity = calculateVelocity();
        player.setVelocity(velocity);
    }

    private Vector2 calculateVelocity() {
        Vector2 velocity = new Vector2();
        if (left)
            velocity.x = -player.getSpeed();
        if (right)
            velocity.x = player.getSpeed();
        if (up)
            velocity.y = player.getSpeed();
        if (down)
            velocity.y = -player.getSpeed();
        return velocity;
    }
}
