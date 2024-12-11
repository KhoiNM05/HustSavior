package io.github.HustSavior.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class HighgroundManager {
    private static final float HIGHGROUND_OFFSET_X = 5f;
    private static final float HIGHGROUND_OFFSET_Y = 5f;

    private final Array<Object> highgroundAreas;
    private boolean isInHighground;

    public HighgroundManager(TiledMap map) {
        highgroundAreas = new Array<>();
        loadHighgroundAreas(map);
    }

    private void loadHighgroundAreas(TiledMap map) {
        MapLayer highgroundLayer = map.getLayers().get("highground");
        if (highgroundLayer == null) {
            Gdx.app.error("HighgroundManager", "No highground layer found!");
            return;
        }

        for (MapObject object : highgroundLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                highgroundAreas.add(rect);
                Gdx.app.log("HighgroundManager", "Added rectangle highground: " + rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                highgroundAreas.add(poly);
                Gdx.app.log("HighgroundManager", "Added polygon highground");
            }
        }

        Gdx.app.log("HighgroundManager", "Total highground areas loaded: " + highgroundAreas.size);
    }

    public Vector2 updatePosition(float x, float y) {
        boolean wasInHighground = isInHighground;
        Vector2 position = new Vector2(x, y);

        isInHighground = false;
        for (Object area : highgroundAreas) {
            if (area instanceof Rectangle) {
                Rectangle rect = (Rectangle) area;
                if (rect.contains(x, y)) {
                    isInHighground = true;
                    break;
                }
            } else if (area instanceof Polygon) {
                Polygon poly = (Polygon) area;
                if (poly.contains(x, y)) {
                    isInHighground = true;
                    break;
                }
            }
        }

        if (isInHighground && !wasInHighground) {
            position.x += HIGHGROUND_OFFSET_X;
            position.y += HIGHGROUND_OFFSET_Y;
            Gdx.app.log("HighgroundManager", "Entering highground! New position: " + position);
        } else if (!isInHighground && wasInHighground) {
            position.x -= HIGHGROUND_OFFSET_X;
            position.y -= HIGHGROUND_OFFSET_Y;
            Gdx.app.log("HighgroundManager", "Leaving highground! New position: " + position);
        }

        return position;
    }

    public boolean isInHighground() {
        return isInHighground;
    }

    public void debugPrintAreas() {
        Gdx.app.log("HighgroundManager", "=== Loaded Highground Areas ===");
        for (Object area : highgroundAreas) {
            if (area instanceof Rectangle) {
                Rectangle rect = (Rectangle) area;
                Gdx.app.log("HighgroundManager", "Rectangle: x=" + rect.x + ", y=" + rect.y +
                           ", width=" + rect.width + ", height=" + rect.height);
            } else if (area instanceof Polygon) {
                Polygon poly = (Polygon) area;
                Gdx.app.log("HighgroundManager", "Polygon: vertices=" + poly.getVertices().length/2);
            }
        }
    }
}
