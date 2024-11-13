package io.github.HustSavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.HustSavior.entities.Player;

public class InputHandler extends InputAdapter {
    private Player player;
    private boolean left, right, up, down;
    private boolean prevLeft;
    private float stateTime;
    public InputHandler(Player player){
        this.player = player;
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
            prevLeft=true;

        } else if (keycode == Input.Keys.D) {
            right = false;

            prevLeft=false;
        } else if (keycode == Input.Keys.W) {
            up = false;
        } else if (keycode == Input.Keys.S) {
            down = false;
        }
        return true;
    }

    public void update(float delta) {
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

    }

};
