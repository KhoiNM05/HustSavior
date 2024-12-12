package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.utils.GameConfig;

public class TreeTransparencyManager extends TransparencyManager implements Disposable {
    private static final int NUMBER_OF_TREE_LAYERS = 5;
    private static final String BOUNDS_LAYER = "_bounds";
    
    private final MapLayer[] treeLayers;
    private final MapLayer[] boundLayers;
    
    public TreeTransparencyManager(World world, TiledMap map) {
        super(map, world);
        this.treeLayers = new MapLayer[NUMBER_OF_TREE_LAYERS];
        this.boundLayers = new MapLayer[NUMBER_OF_TREE_LAYERS];
        
        for (int i = 0; i < NUMBER_OF_TREE_LAYERS; i++) {
            treeLayers[i] = map.getLayers().get("Tree" + (i + 1));
            boundLayers[i] = map.getLayers().get("Tree" + (i + 1) + "_bounds");
        }
    }

    public void update(Vector2 position) {
        float playerX = position.x;
        float playerY = position.y;
        
        for (int i = 0; i < NUMBER_OF_TREE_LAYERS; i++) {
            updateLayerTransparencyWithBounds(treeLayers[i], boundLayers[i], playerX, playerY);
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
    
    @Override
    public void dispose() {
        // Clean up any resources if needed
    }
} 