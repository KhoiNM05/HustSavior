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
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.input.InputHandler;

public class ItemPickupDialog extends BaseDialog implements Disposable {
    public ItemPickupDialog(Stage stage, Skin skin, InputHandler inputHandler) {
        super(stage, skin, inputHandler);
    }

    public void show(String itemName, String imagePath, Runnable onClose) {
        createDialog("You got " + itemName + "!", imagePath, onClose);
    }

    protected void createDialog(String message, String imagePath, Runnable onClose) {
        if (!isActive) {
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
                    if (onClose != null) onClose.run();
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
            
            Table contentTable = new Table();
            
            Texture itemTexture = new Texture(Gdx.files.internal(imagePath));
            Image itemImage = new Image(itemTexture);
            float aspectRatio = (float)itemTexture.getWidth() / itemTexture.getHeight();
            float targetHeight = 64f;
            float targetWidth = targetHeight * aspectRatio;
            
            contentTable.add(itemImage).size(targetWidth, targetHeight).padRight(10f);
            
            messageLabel = new Label("", skin);
            messageLabel.setWrap(true);
            messageLabel.setFontScale(3.0f);
            messageLabel.setAlignment(Align.right);
            contentTable.add(messageLabel).width(300f).pad(2f);
            
            dialog.getContentTable().add(contentTable);
            dialog.getTitleTable().remove();
            dialog.show(stage);
            
            dialog.setPosition(
                stage.getWidth() - dialog.getWidth() - 20,
                stage.getHeight() - dialog.getHeight() - 80
            );

            setupAutoClose(onClose);
        }
    }

    @Override
    public void dispose() {
        if (dialog != null) {
            dialog.remove();
        }
    }
} 