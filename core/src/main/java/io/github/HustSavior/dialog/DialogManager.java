package io.github.HustSavior.dialog;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;

public class DialogManager {
    private Stage stage;
    private Skin skin;
    private Dialog currentDialog;
    private boolean isDialogActive;
    private static final float AUTO_CLOSE_DELAY = 1f; // 1 second

    public DialogManager(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        this.isDialogActive = false;
    }

    public void showWarningDialog(String message, Runnable onClose) {
        if (!isDialogActive) {
            isDialogActive = true;
            currentDialog = new Dialog("Warning", skin) {
                @Override
                protected void result(Object obj) {
                    isDialogActive = false;
                    if (onClose != null) onClose.run();
                    hide();
                }
            };
            
            currentDialog.text(message);
            currentDialog.show(stage);
            centerDialog();

            // Auto-close after delay
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    isDialogActive = false;
                    if (onClose != null) onClose.run();
                    currentDialog.hide();
                }
            }, AUTO_CLOSE_DELAY);
        }
    }

    private void centerDialog() {
        currentDialog.setPosition(
            (stage.getWidth() - currentDialog.getWidth()) / 2,
            (stage.getHeight() - currentDialog.getHeight()) / 2
        );
    }

    public boolean update(float delta) {
        return isDialogActive;
    }
} 