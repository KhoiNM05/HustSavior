package io.github.HustSavior.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import io.github.HustSavior.input.InputHandler;

public class DialogManager {
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

    public void showItemPickupDialog(String itemName, String imagePath, Runnable onClose) {
        itemPickupDialog.show(itemName, imagePath, onClose);
    }
    public void update(float delta) {
        warningDialog.update(delta);
        itemPickupDialog.update(delta);
    }

    public boolean isDialogActive() {
        return dialogActive;
    }
} 