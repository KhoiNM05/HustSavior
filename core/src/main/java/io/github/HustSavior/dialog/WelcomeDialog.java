package io.github.HustSavior.dialog;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.input.InputHandler;

public class WelcomeDialog extends BaseDialog implements Disposable {
    public WelcomeDialog(Stage stage, Skin skin, InputHandler inputHandler) {
        super(stage, skin, inputHandler);
    }

    @Override
    protected void createDialog(String message, Runnable onClose) {
        super.createDialog(message, onClose);
        
        dialog = new Dialog("", skin);
        messageLabel = new Label("Find the secret then escape the campus before too late", skin);
        messageLabel.setWrap(true);
        messageLabel.setFontScale(3.0f);
        dialog.getContentTable().add(messageLabel).width(400f).pad(2f);
        dialog.getTitleTable().remove();
        dialog.show(stage);
        
        // Center the dialog
        dialog.setPosition(
            (stage.getWidth() - dialog.getWidth()) / 2,
            (stage.getHeight() - dialog.getHeight()) / 2
        );

        setupAutoClose(onClose);
    }

    @Override
    public void dispose() {
        if (dialog != null) {
            dialog.remove();
        }
    }
} 