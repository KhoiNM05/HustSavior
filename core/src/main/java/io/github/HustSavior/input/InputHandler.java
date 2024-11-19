package io.github.HustSavior.input;

import static org.junit.jupiter.api.Assumptions.abort;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private Player player;
    private boolean left, right, up, down;
    private float mapWidth, mapHeight;
    private boolean prevLeft;
    private float stateTime;
    public InputHandler(Player player, float mapWidth, float mapHeight) {
        this.player = player;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
    int counter=0;
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A) {
            left = true;
        } else if (keycode == Input.Keys.D) {
            right = true;
        } else if (keycode == Input.Keys.W) {
            up = true;
        } else if (keycode == Input.Keys.S) {
            down = true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) {
            left = false;
            prevLeft = true;
        } else if (keycode == Input.Keys.D) {
            right = false;
            prevLeft = false;
        } else if (keycode == Input.Keys.W) {
            up = false;
        } else if (keycode == Input.Keys.S) {
            down = false;
        }
        return true;
    }

    public void update(float delta) {
        // float newX = player.getX();
        // float newY = player.getY();

        // if (left) {
        //     newX -= player.getSpeed() * delta;
        // }
        // if (right) {
        //     newX += player.getSpeed() * delta;
        // }
        // if (up) {
        //     newY += player.getSpeed() * delta;
        // }
        // if (down) {
        //     newY -= player.getSpeed() * delta;
        // }

        // // Clamp the player's position within the map boundaries
        // newX = Math.max(0, Math.min(newX, mapWidth - player.getWidth()));
        // newY = Math.max(0, Math.min(newY, mapHeight - player.getHeight()));
        // player.setPosition(newX, newY);

        stateTime+=Gdx.graphics.getDeltaTime();
        if (left) {
            player.setPosition(player.getX() - player.getSpeed() * delta, player.getY());
            player.setRegion(player.walkLeft.getKeyFrame(stateTime, true));
        }
        if (right) {
            player.setPosition(player.getX() + player.getSpeed() * delta, player.getY());
            player.setRegion(player.walkRight.getKeyFrame(stateTime, true));
        }
        if (up) {
            player.setPosition(player.getX(), player.getY() + player.getSpeed() * delta);
            if (prevLeft) player.setRegion(player.walkLeft.getKeyFrame(stateTime, true));
            else player.setRegion(player.walkRight.getKeyFrame(stateTime, true));
        }
        if (down) {
            player.setPosition(player.getX(), player.getY() - player.getSpeed() * delta);
            if (prevLeft) player.setRegion(player.walkLeft.getKeyFrame(stateTime, true));
            else player.setRegion(player.walkRight.getKeyFrame(stateTime, true));
        }

        Vector2 velocity = new Vector2();
        if (left) {
            velocity.x = -player.getSpeed();
        }
        if (right) {
            velocity.x = player.getSpeed();
        }
        if (up) {
            velocity.y = player.getSpeed();
        }
        if (down) {
            velocity.y = -player.getSpeed();
        }
        System.out.println(velocity);
        player.setVelocity(velocity);
    }

};
