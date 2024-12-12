package io.github.HustSavior.dialog;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.input.InputHandler;

public class WarningDialog extends BaseDialog implements Disposable {
    private static final float COOLDOWN_DURATION = 2f;
    private float cooldownTimer = 0f;
    private boolean isOnCooldown = false;

    public WarningDialog(Stage stage, Skin skin, InputHandler inputHandler) {
        super(stage, skin, inputHandler);
    }

    @Override
    protected void createDialog(String message, Runnable onClose) {
        if (isOnCooldown || isActive) {
            return;
        }
        
        isActive = true;
        inputHandler.setDialogActive(true);
        fullMessage = message;
        currentCharIndex = 0;
        charTimer = 0;
        
        dialog = new Dialog("", skin) {
            @Override
            protected void result(Object obj) {
                isActive = false;
                inputHandler.setDialogActive(false);
                isOnCooldown = true;
                cooldownTimer = COOLDOWN_DURATION;
                if (onClose != null) {
                    onClose.run();
                }
                hide();
            }

            @Override
            public void setStyle(WindowStyle style) {
                super.setStyle(style);
                padTop(2f);
                padBottom(2f);
                padLeft(2f);
                padRight(2f);
            }
        };
        
        messageLabel = new Label("", skin);
        messageLabel.setWrap(true);
        messageLabel.setFontScale(3.0f);
        dialog.getContentTable().add(messageLabel).width(300f).pad(2f);
        dialog.getTitleTable().remove();
        dialog.show(stage);
        
        dialog.setPosition(
            stage.getWidth() - dialog.getWidth() - 20,
            stage.getHeight() - dialog.getHeight() - 80
        );

        setupAutoClose(onClose);
    }

    @Override
    public boolean update(float delta) {
        if (isOnCooldown) {
            cooldownTimer -= delta;
            if (cooldownTimer <= 0) {
                isOnCooldown = false;
                cooldownTimer = 0;
            }
            return true;
        }
        return super.update(delta);
    }

    public boolean isOnCooldown() {
        return isOnCooldown;
    }

    @Override
    public void dispose() {
        if (dialog != null) {
            dialog.remove();
        }
    }
} 