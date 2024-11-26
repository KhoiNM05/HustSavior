package io.github.HustSavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private final Player player;
    private boolean left, right, up, down;
    private boolean facingLeft;
    private float stateTime;

    public InputHandler(Player player) {
        this.player = player;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A: left = true; break;
            case Input.Keys.D: right = true; break;
            case Input.Keys.W: up = true; break;
            case Input.Keys.S: down = true; break;
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
            case Input.Keys.W: up = false; break;
            case Input.Keys.S: down = false; break;
        }
        return true;
    }

    public void update(float delta) {
        stateTime += Gdx.graphics.getDeltaTime();
        Vector2 velocity = calculateVelocity();
        updatePlayerAnimation(velocity);
        player.getBody().setLinearVelocity(velocity);
    }

    private Vector2 calculateVelocity() {
        Vector2 velocity = new Vector2();
        if (left) velocity.x = -player.getSpeed();
        if (right) velocity.x = player.getSpeed();
        if (up) velocity.y = player.getSpeed();
        if (down) velocity.y = -player.getSpeed();
        return velocity;
    }

    private void updatePlayerAnimation(Vector2 velocity) {
        if (velocity.x < 0) {
            player.setRegion(player.walkLeft.getKeyFrame(stateTime, true));
        } else if (velocity.x > 0) {
            player.setRegion(player.walkRight.getKeyFrame(stateTime, true));
        } else if (velocity.y != 0) {
            player.setRegion((facingLeft ? player.walkLeft : player.walkRight)
                           .getKeyFrame(stateTime, true));
        }
    }
}
