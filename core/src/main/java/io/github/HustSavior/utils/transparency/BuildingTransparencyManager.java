package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;

public class BuildingTransparencyManager {
    private static final float TRANSPARENT_ALPHA = 0.3f;
    private static final float OPAQUE_ALPHA = 1.0f;
    private static final String BOUNDS_SUFFIX = "_bounds";

    private final TiledMap map;
    private final MapLayer d3Layer;
    private final MapLayer d5Layer;
    private final MapLayer d35Layer;
    private final MapLayer libraryLayer;
    private final MapLayer roofLayer;
    private final MapLayer parkingLayer;

    public BuildingTransparencyManager(TiledMap map, MapLayer d3Layer, MapLayer d5Layer,
                                       MapLayer d35Layer, MapLayer libraryLayer,
                                       MapLayer roofLayer, MapLayer parkingLayer) {
        this.map = map;
        this.d3Layer = d3Layer;
        this.d5Layer = d5Layer;
        this.d35Layer = d35Layer;
        this.libraryLayer = libraryLayer;
        this.roofLayer = roofLayer;
        this.parkingLayer = parkingLayer;
    }

    public void update(Player player) {
        float playerX = player.getBody().getPosition().x * GameConfig.PPM;
        float playerY = player.getBody().getPosition().y * GameConfig.PPM;

        updateLayerTransparency(d3Layer, playerX, playerY);
        updateLayerTransparency(d5Layer, playerX, playerY);
        updateLayerTransparency(d35Layer, playerX, playerY);
        updateLayerTransparency(libraryLayer, playerX, playerY);
        updateLayerTransparency(roofLayer, playerX, playerY);
        updateLayerTransparency(parkingLayer, playerX, playerY);
    }

    private void updateLayerTransparency(MapLayer layer, float playerX, float playerY) {
        if (layer == null) return;

        String boundsLayerName = layer.getName() + BOUNDS_SUFFIX;
        MapLayer boundsLayer = map.getLayers().get(boundsLayerName);

        if (boundsLayer == null) {
            System.out.println("Warning: No bounds layer found for " + layer.getName());
            return;
        }

        if (!(boundsLayer instanceof MapLayer)) {
            System.out.println("Warning: Bounds layer is not an object layer for " + layer.getName());
            return;
        }

        Rectangle buildingBounds = getBuildingBoundsFromObjectLayer(boundsLayer);

        boolean isPlayerBehind = playerY < buildingBounds.y + buildingBounds.height &&
            playerY > buildingBounds.y &&
            playerX > buildingBounds.x &&
            playerX < buildingBounds.x + buildingBounds.width;

        layer.setOpacity(isPlayerBehind ? TRANSPARENT_ALPHA : OPAQUE_ALPHA);
    }

    private Rectangle getBuildingBoundsFromObjectLayer(MapLayer objectLayer) {
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

    public void printLayerNamesAndTypes() {
        for (MapLayer layer : map.getLayers()) {
            System.out.println("Layer name: " + layer.getName() + ", Type: " + layer.getClass().getSimpleName());
        }
    }
}
