package io.github.HustSavior.dialog;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import io.github.HustSavior.input.InputHandler;

public abstract class BaseDialog {
    protected Stage stage;
    protected Skin skin;
    protected Dialog dialog;
    protected boolean isActive;
    protected InputHandler inputHandler;
    protected Label messageLabel;
    protected String fullMessage;
    protected int currentCharIndex;
    protected float charTimer;
    protected static final float AUTO_CLOSE_DELAY = 2f;
    protected static final float CHAR_DELAY = 0.02f;

    public BaseDialog(Stage stage, Skin skin, InputHandler inputHandler) {
        this.stage = stage;
        this.skin = skin;
        this.inputHandler = inputHandler;
        this.isActive = false;
    }

    protected void createDialog(String message, Runnable onClose) {
        this.fullMessage = message;
        this.currentCharIndex = 0;
        this.charTimer = 0;
        this.isActive = true;
        inputHandler.setDialogActive(true);
    }

    public boolean update(float delta) {
        if (!isActive) return false;

        if (currentCharIndex < fullMessage.length()) {
            charTimer += delta;
            if (charTimer >= CHAR_DELAY) {
                charTimer = 0;
                currentCharIndex++;
                messageLabel.setText(fullMessage.substring(0, currentCharIndex));
            }
        }
        return true;
    }

    public void skipTypewriter() {
        if (isActive) {
            currentCharIndex = fullMessage.length();
            messageLabel.setText(fullMessage);
        }
    }

    public void close() {
        isActive = false;
        inputHandler.setDialogActive(false);
        if (dialog != null) {
            dialog.hide();
        }
    }

    protected void setupAutoClose(Runnable onClose) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                isActive = false;
                inputHandler.setDialogActive(false);
                if (onClose != null) onClose.run();
                dialog.hide();
            }
        }, AUTO_CLOSE_DELAY);
    }
}
