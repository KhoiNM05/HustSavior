package io.github.HustSavior.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.input.InputHandler;

public class DialogManager implements Disposable {
    private Stage stage;
    private Skin skin;
    private InputHandler inputHandler;
    private WarningDialog warningDialog;
    private ItemPickupDialog itemPickupDialog;
    private boolean dialogActive = false;
    
    public DialogManager(Stage stage, Skin skin, InputHandler inputHandler) {
        this.stage = stage;
        this.skin = new Skin(Gdx.files.internal("UI/dialogue/dialog.json"));
        this.inputHandler = inputHandler;
        
        this.warningDialog = new WarningDialog(stage, this.skin, inputHandler);
        this.itemPickupDialog = new ItemPickupDialog(stage, this.skin, inputHandler);
    }

    public void showWarningDialog(String message, Runnable onClose) {
        warningDialog.createDialog(message, onClose);
    }

    public void showItemPickupDialog(String message, String imagePath, Runnable onClose) {
        Gdx.app.log("DialogManager", "Showing item pickup dialog: " + message);
        dialogActive = true;
        itemPickupDialog.show(message, imagePath, () -> {
            dialogActive = false;
            if (onClose != null) {
                onClose.run();
            }
        });
    }
    public void update(float delta) {
        if (warningDialog != null) {
            warningDialog.update(delta);
        }
        if (itemPickupDialog != null) {
            itemPickupDialog.update(delta);
        }
        
        // Add debug logging
        if (dialogActive) {
            Gdx.app.log("DialogManager", "Dialog is active");
        }
    }

    public boolean isDialogActive() {
        return dialogActive;
    }

    @Override
    public void dispose() {
        if (warningDialog != null) {
            warningDialog.dispose();
        }
        if (itemPickupDialog != null) {
            itemPickupDialog.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
} 