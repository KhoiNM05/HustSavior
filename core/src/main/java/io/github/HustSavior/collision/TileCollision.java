package io.github.HustSavior.collision;

import com.badlogic.gdx.Gdx;
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

    public TileCollision(TiledMap map) {
        this.map = map;
        this.collisionLayer = map.getLayers().get("collisions");
        if (collisionLayer == null) {
            Gdx.app.error("TileCollision", "No 'collisions' layer found in map!");
        }
    }

    public boolean collidesWith(Rectangle bounds) {
        if (collisionLayer == null) return false;

        MapObjects objects = collisionLayer.getObjects();
        Rectangle scaledBounds = new Rectangle(
            bounds.x * GameConfig.PPM,
            bounds.y * GameConfig.PPM,
            bounds.width * GameConfig.PPM,
            bounds.height * GameConfig.PPM
        );

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                if (scaledBounds.overlaps(rect)) {
                    return true;
                }
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                Polygon rectPoly = new Polygon(new float[] {
                    scaledBounds.x, scaledBounds.y,
                    scaledBounds.x + scaledBounds.width, scaledBounds.y,
                    scaledBounds.x + scaledBounds.width, scaledBounds.y + scaledBounds.height,
                    scaledBounds.x, scaledBounds.y + scaledBounds.height
                });
                if (Intersector.overlapConvexPolygons(rectPoly, poly)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(Rectangle bounds) {
        return collidesWith(bounds);
    }
} 