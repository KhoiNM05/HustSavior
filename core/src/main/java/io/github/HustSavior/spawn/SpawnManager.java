package io.github.HustSavior.spawn;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.items.Item;


public class SpawnManager {
    private TiledMap map;
    private Array<Rectangle> collisionAreas;
    private Array<BoundedSpawnArea> boundedSpawnAreas;
    private Array<Item> managedItems;

    public SpawnManager(TiledMap map) {
        this.map = map;
        this.collisionAreas = new Array<>();
        this.boundedSpawnAreas = new Array<>();
        this.managedItems = new Array<>();
        loadCollisionAreas();
        loadBoundedAreas();
    }

    private void loadCollisionAreas() {
        MapLayer collisionLayer = map.getLayers().get("collisions");
        if (collisionLayer != null) {
            for (MapObject object : collisionLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    collisionAreas.add(rect);
                }
            }
        }
    }

    private void loadBoundedAreas() {
        // Load each building's bounds layer separately
        String[] boundLayers = {
            "D3_bounds", "D5_bounds", "D35_bounds", 
            "Library_bounds", "Roof_bounds", "Parking_bounds"
        };

        for (String layerName : boundLayers) {
            MapLayer boundsLayer = map.getLayers().get(layerName);
            if (boundsLayer != null) {
                boundedSpawnAreas.add(new BoundedSpawnArea(
                    getBuildingBoundsFromObjectLayer(boundsLayer),
                    layerName
                ));
            }
        }
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

    public Vector2 getValidSpawnPosition(float itemWidth, float itemHeight) {
        Vector2 position = new Vector2();
        boolean validPosition = false;
        int maxAttempts = 100;
        int attempts = 0;

        while (!validPosition && attempts < maxAttempts) {
            position.x = (float) (Math.random() * map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class));
            position.y = (float) (Math.random() * map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class));

            if (isValidSpawnPosition(position, itemWidth, itemHeight)) {
                validPosition = true;
            }
            attempts++;
        }

        return position;
    }

    private boolean isValidSpawnPosition(Vector2 position, float width, float height) {
        Rectangle itemBounds = new Rectangle(position.x, position.y, width, height);

        // Check collision areas
        for (Rectangle collision : collisionAreas) {
            if (collision.overlaps(itemBounds)) {
                return false;
            }
        }

        return true;
    }

    public void registerItem(Item item) {
        managedItems.add(item);
        updateItemVisibility(item);
    }

    public void updateItemVisibilities(Vector2 playerPosition) {
        for (Item item : managedItems) {
            updateItemVisibility(item, playerPosition);
        }
    }

    private void updateItemVisibility(Item item) {
        boolean shouldBeVisible = true;
        Vector2 itemPosition = new Vector2(item.getX(), item.getY());

        // Check if item is in any bounded area
        for (BoundedSpawnArea area : boundedSpawnAreas) {
            if (area.contains(itemPosition)) {
                shouldBeVisible = false;
                break;
            }
        }

        item.setVisible(shouldBeVisible);
    }

    private void updateItemVisibility(Item item, Vector2 playerPosition) {
        boolean shouldBeVisible = false;
        Vector2 itemPosition = new Vector2(item.getX(), item.getY());
        boolean itemInAnyBounds = false;

        for (BoundedSpawnArea area : boundedSpawnAreas) {
            if (area.contains(itemPosition)) {
                itemInAnyBounds = true;
                // Item is in a bounded area, only show if player is also in the area
                if (area.contains(playerPosition)) {
                    shouldBeVisible = true;
                    break;
                }
            }
        }

        // If item is not in any bounds, it should always be visible
        if (!itemInAnyBounds) {
            shouldBeVisible = true;
        }

        item.setVisible(shouldBeVisible);
    }

    private static class BoundedSpawnArea {
        private Rectangle bounds;
        private String layerName;

        public BoundedSpawnArea(Rectangle bounds, String layerName) {
            this.bounds = bounds;
            this.layerName = layerName;
        }

        public boolean contains(Vector2 position) {
            return position.x > bounds.x && 
                   position.x < bounds.x + bounds.width &&
                   position.y > bounds.y && 
                   position.y < bounds.y + bounds.height;
        }
    }
} 