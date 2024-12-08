package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

import io.github.HustSavior.entities.Player;

public abstract class TransparencyManager {
    protected static final float TRANSPARENT_ALPHA = 0.3f;
    protected static final float OPAQUE_ALPHA = 1.0f;
    protected static final String BOUNDS_SUFFIX = "_bounds";
    
    protected final TiledMap map;
    
    protected TransparencyManager(TiledMap map) {
        this.map = map;
    }
    
    public abstract void update(Player player);
    
    protected void updateLayerTransparency(MapLayer layer, float playerX, float playerY) {
        if (layer == null) return;
        
        String boundsLayerName = layer.getName() + BOUNDS_SUFFIX;
        MapLayer boundsLayer = map.getLayers().get(boundsLayerName);
        
        if (boundsLayer == null) {
            System.out.println("Warning: No bounds layer found for " + layer.getName());
            return;
        }
        
        Rectangle bounds = getBoundsFromObjectLayer(boundsLayer);
        
        boolean isPlayerBehind = playerY < bounds.y + bounds.height &&
                               playerY > bounds.y &&
                               playerX > bounds.x &&
                               playerX < bounds.x + bounds.width;
        
        layer.setOpacity(isPlayerBehind ? TRANSPARENT_ALPHA : OPAQUE_ALPHA);
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
} 