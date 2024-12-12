package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public abstract class TransparencyManager {
    protected static final float TRANSPARENT_ALPHA = 0.3f;
    protected static final float OPAQUE_ALPHA = 1.0f;
    protected static final float PLAYER_TRANSPARENT_ALPHA = 0.5f;
    protected static final float PLAYER_OPAQUE_ALPHA = 1.0f;
    protected static final String BOUNDS_SUFFIX = "_bounds";
    protected static final short TRANSPARENCY_BOUNDS_BITS = 0x0004;
    protected static final short MONSTER_CATEGORY = 0x0002;
    protected static final short PLAYER_CATEGORY = 0x0001;
    
    protected final TiledMap map;
    protected final World world;
    
    protected TransparencyManager(TiledMap map, World world) {
        this.map = map;
        this.world = world;
    }
    
    public abstract void update(Vector2 position);
    
    protected void updateLayerTransparency(MapLayer layer, float x, float y, String boundsName) {
        if (layer == null) return;
        
        // Check if player is in bounds
        boolean isInBounds = checkBounds(x, y, boundsName);
        
        // Update layer opacity
        layer.setOpacity(isInBounds ? 0.5f : 1.0f);  // Adjust transparency values as needed
    }
    
    protected Rectangle getBoundsFromObjectLayer(MapLayer objectLayer) {
        MapObjects objects = objectLayer.getObjects();
        if (objects.getCount() == 0) return new Rectangle();
        
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        
        for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
            Rectangle rect = rectangleObject.getRectangle();
            minX = Math.min(minX, rect.x);
            minY = Math.min(minY, rect.y);
            maxX = Math.max(maxX, rect.x + rect.width);
            maxY = Math.max(maxY, rect.y + rect.height);
        }
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
    
    protected boolean isPlayerInBounds(float playerX, float playerY, MapLayer boundsLayer) {
        if (boundsLayer == null) return false;
        
        for (RectangleMapObject rectangleObject : boundsLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = rectangleObject.getRectangle();
            if (playerX >= rect.x && playerX <= rect.x + rect.width &&
                playerY >= rect.y && playerY <= rect.y + rect.height) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean checkBounds(float x, float y, String boundsName) {
        if (boundsName == null) return false;
        MapLayer boundsLayer = map.getLayers().get(boundsName);
        return isPlayerInBounds(x, y, boundsLayer);
    }
} 