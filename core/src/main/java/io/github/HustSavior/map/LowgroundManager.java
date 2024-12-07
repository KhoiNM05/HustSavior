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

public class LowgroundManager {
    private static final float LOWGROUND_OFFSET_X = -5f;
    private static final float LOWGROUND_OFFSET_Y = -5f;

    private final Array<Object> lowgroundAreas;
    private boolean isInLowground;

    public LowgroundManager(TiledMap map) {
        lowgroundAreas = new Array<>();
        loadLowgroundAreas(map);
    }

    private void loadLowgroundAreas(TiledMap map) {
        MapLayer lowgroundLayer = map.getLayers().get("lowground");
        if (lowgroundLayer == null) {
            Gdx.app.error("LowgroundManager", "No lowground layer found!");
            return;
        }

        for (MapObject object : lowgroundLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                lowgroundAreas.add(rect);
                Gdx.app.log("LowgroundManager", "Added rectangle lowground: " + rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                lowgroundAreas.add(poly);
                Gdx.app.log("LowgroundManager", "Added polygon lowground");
            }
        }

        Gdx.app.log("LowgroundManager", "Total lowground areas loaded: " + lowgroundAreas.size);
    }

    public Vector2 updatePosition(float x, float y) {
        boolean wasInLowground = isInLowground;
        Vector2 position = new Vector2(x, y);

        isInLowground = false;
        for (Object area : lowgroundAreas) {
            if (area instanceof Rectangle) {
                Rectangle rect = (Rectangle) area;
                if (rect.contains(x, y)) {
                    isInLowground = true;
                    break;
                }
            } else if (area instanceof Polygon) {
                Polygon poly = (Polygon) area;
                if (poly.contains(x, y)) {
                    isInLowground = true;
                    break;
                }
            }
        }

        if (isInLowground && !wasInLowground) {
            position.x += LOWGROUND_OFFSET_X;
            position.y += LOWGROUND_OFFSET_Y;
            Gdx.app.log("LowgroundManager", "Entering lowground! New position: " + position);
        } else if (!isInLowground && wasInLowground) {
            position.x -= LOWGROUND_OFFSET_X;
            position.y -= LOWGROUND_OFFSET_Y;
            Gdx.app.log("LowgroundManager", "Leaving lowground! New position: " + position);
        }

        return position;
    }

    public boolean isInLowground() {
        return isInLowground;
    }
} 