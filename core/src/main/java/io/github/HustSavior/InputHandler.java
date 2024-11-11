package io.github.HustSavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private Player player;
    private boolean left, right, up, down;
    public InputHandler(Player player){
        this.player = player;
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
        if (left) {
            player.setPosition(player.getX() - player.getSpeed() * delta, player.getY());
        }
        if (right) {
            player.setPosition(player.getX() + player.getSpeed() * delta, player.getY());
        }
        if (up) {
            player.setPosition(player.getX(), player.getY() + player.getSpeed() * delta);
        }
        if (down) {
            player.setPosition(player.getX(), player.getY() - player.getSpeed() * delta);
        }
    }

};
