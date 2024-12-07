package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;

public class TreeTransparencyManager extends TransparencyManager {
    private static final int NUMBER_OF_TREE_LAYERS = 5;
    private static final String BOUNDS_LAYER = "_bounds";
    
    private final MapLayer[] treeLayers;
    private final MapLayer[] boundLayers;
    
    public TreeTransparencyManager(TiledMap map) {
        super(map);
        this.treeLayers = new MapLayer[NUMBER_OF_TREE_LAYERS];
        this.boundLayers = new MapLayer[NUMBER_OF_TREE_LAYERS];
        
        for (int i = 0; i < NUMBER_OF_TREE_LAYERS; i++) {
            treeLayers[i] = map.getLayers().get("Tree" + (i + 1));
            boundLayers[i] = map.getLayers().get("Tree" + (i + 1) + "_bounds");
        }
    }
    
    @Override
    public void update(Player player) {
        // Get the position from the player's physics body
        Vector2 position = player.getBody().getPosition();
        // Convert Box2D coordinates to world coordinates
        float playerX = position.x * GameConfig.PPM;
        float playerY = position.y * GameConfig.PPM;
        
        for (int i = 0; i < NUMBER_OF_TREE_LAYERS; i++) {
            if (treeLayers[i] != null && boundLayers[i] != null) {
                updateLayerTransparencyWithBounds(treeLayers[i], boundLayers[i], playerX, playerY);
            }
        }
    }
    
    private void updateLayerTransparencyWithBounds(MapLayer treeLayer, MapLayer boundsLayer, float playerX, float playerY) {
        boolean shouldBeTransparent = false;
        
        for (MapObject boundObject : boundsLayer.getObjects()) {
            if (!(boundObject instanceof RectangleMapObject)) continue;
            
            Rectangle bounds = ((RectangleMapObject) boundObject).getRectangle();
            // Add a small buffer to the bounds check if needed
            float buffer = 2f; // Adjust this value as needed
            if (playerX >= bounds.x - buffer && 
                playerX <= bounds.x + bounds.width + buffer &&
                playerY >= bounds.y - buffer && 
                playerY <= bounds.y + bounds.height + buffer) {
                shouldBeTransparent = true;
                break;
            }
        }
        
        updateObjectTransparency(treeLayer, shouldBeTransparent);
    }
    
    private void updateObjectTransparency(MapLayer layer, boolean transparent) {
        float alpha = transparent ? 0.5f : 1.0f;  // Adjust alpha values as needed
        layer.setOpacity(alpha);
    }
} 