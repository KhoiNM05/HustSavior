package io.github.HustSavior.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import io.github.HustSavior.utils.GameConfig;

public class TileCollision {
    private TiledMap map;
    private MapLayer collisionLayer;
    private ShapeRenderer shapeRenderer;

    public TileCollision(TiledMap map) {
        this.map = map;
        this.collisionLayer = map.getLayers().get("collisions");
        this.shapeRenderer = new ShapeRenderer();
        if (collisionLayer == null) {
            Gdx.app.error("TileCollision", "No 'collisions' layer found in map!");
        }
    }

    public boolean collidesWith(Rectangle bounds) {
        if (collisionLayer == null) return false;

        MapObjects objects = collisionLayer.getObjects();
        Rectangle monsterBounds = bounds;

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                Rectangle wallBounds = new Rectangle(
                    rect.x / GameConfig.PPM,
                    rect.y / GameConfig.PPM,
                    rect.width / GameConfig.PPM,
                    rect.height / GameConfig.PPM
                );

                if (monsterBounds.overlaps(wallBounds)) {
                    System.out.println("Colliding");
                    return true;
                }
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                Polygon rectPoly = new Polygon(new float[] {
                    monsterBounds.x, monsterBounds.y,
                    monsterBounds.x + monsterBounds.width, monsterBounds.y,
                    monsterBounds.x + monsterBounds.width, monsterBounds.y + monsterBounds.height,
                    monsterBounds.x, monsterBounds.y + monsterBounds.height
                });
                if (Intersector.overlapConvexPolygons(rectPoly, poly)) {
                    System.out.print("Colliding");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(Rectangle bounds) {
        return collidesWith(bounds);
    }

    public boolean isColliding(Rectangle bounds) {
        // Get collision layer from map
        MapLayer collisionLayer = map.getLayers().get("collisions");
        if (collisionLayer == null) return false;

        // Check collision with all objects in layer
        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                if (bounds.overlaps(rect)) {
                    return true;
                }
            }
        }
        return false;
    }

   

    // Add this method to render debug lines
    public void renderDebug(ShapeRenderer shapeRenderer) {
        if (collisionLayer == null) return;

        // Draw collision objects
        shapeRenderer.setColor(Color.RED); // Red for wall bounds
        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                Rectangle scaledRect = new Rectangle(
                    rect.x / GameConfig.PPM,
                    rect.y / GameConfig.PPM,
                    rect.width / GameConfig.PPM,
                    rect.height / GameConfig.PPM
                );
                // Draw wall bounds
                shapeRenderer.rect(
                    scaledRect.x * GameConfig.PPM, 
                    scaledRect.y * GameConfig.PPM,
                    scaledRect.width * GameConfig.PPM,
                    scaledRect.height * GameConfig.PPM
                );
                
                // Debug text for wall position
               
            }
        }
    }

    // Add to dispose method or create one if it doesn't exist
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
} 