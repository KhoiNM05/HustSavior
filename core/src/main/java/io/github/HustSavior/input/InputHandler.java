package io.github.HustSavior.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private Player player;
    private boolean left, right, up, down;
    private float mapWidth, mapHeight;
    public InputHandler(Player player, float mapWidth, float mapHeight) {
        this.player = player;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
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
        } else if (keycode == Input.Keys.D) {
            right = false;
        } else if (keycode == Input.Keys.W) {
            up = false;
        } else if (keycode == Input.Keys.S) {
            down = false;
        }
        return true;
    }

    public void update(float delta) {
        float newX = player.getX();
        float newY = player.getY();

        if (left) {
            newX -= player.getSpeed() * delta;
        }
        if (right) {
            newX += player.getSpeed() * delta;
        }
        if (up) {
            newY += player.getSpeed() * delta;
        }
        if (down) {
            newY -= player.getSpeed() * delta;
        }

        // Clamp the player's position within the map boundaries
        newX = Math.max(0, Math.min(newX, mapWidth - player.getWidth()));
        newY = Math.max(0, Math.min(newY, mapHeight - player.getHeight()));
        player.setPosition(newX, newY);
    }

};
