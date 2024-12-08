package io.github.HustSavior.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class InventoryTray {
    private final Table container;
    private final Button[] slots;
    private static final float SLOT_SIZE = 64f;
    private static final float PADDING = 5f;
    private static final int MAX_SLOTS = 8;
    private int currentSlot = 0;
    
    public InventoryTray(Stage stage, Skin skin) {
        container = new Table();
        
        // Initialize slots array
        slots = new Button[MAX_SLOTS];
        
        // Create inventory slots
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new Button(skin, "default");
            container.add(slots[i]).size(SLOT_SIZE).pad(PADDING);
        }
        
        container.pack();
        container.setPosition(
            (stage.getWidth() - container.getWidth()) / 2,
            0
        );
        
        stage.addActor(container);
    }
    
    public void addItem(String imagePath) {
        if (currentSlot >= MAX_SLOTS) {
            return; // Inventory is full
        }
        
        Texture texture = new Texture(imagePath);
        TextureRegion region = new TextureRegion(texture);
        Image itemImage = new Image(region);
        
        // Scale the image to fit the slot while maintaining aspect ratio
        float scale = Math.min(
            (SLOT_SIZE - PADDING * 2) / texture.getWidth(),
            (SLOT_SIZE - PADDING * 2) / texture.getHeight()
        );
        itemImage.setSize(texture.getWidth() * scale, texture.getHeight() * scale);
        
        // Clear the button and add the new item
        slots[currentSlot].clear();
        slots[currentSlot].add(itemImage).size(SLOT_SIZE - PADDING * 2);
        
        currentSlot++;
    }
    
    public void dispose() {
        for (Button slot : slots) {
            if (slot.hasChildren()) {
                Image image = (Image) slot.getChildren().first();
                if (image.getDrawable() instanceof TextureRegionDrawable) {
                    ((TextureRegionDrawable) image.getDrawable()).getRegion().getTexture().dispose();
                }
            }
        }
    }
} 
