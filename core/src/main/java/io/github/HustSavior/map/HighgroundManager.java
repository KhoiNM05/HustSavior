package io.github.HustSavior.map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class HighgroundManager implements Disposable {
    private static final float STEP_HEIGHT = 8f;
    private static final float X_OFFSET = 5f;
    private final Array<Object> highgroundAreas;
    private boolean wasOnHighground = false;

    public HighgroundManager(TiledMap map) {
        highgroundAreas = new Array<>();
        loadHighgroundAreas(map);
    }

    private void loadHighgroundAreas(TiledMap map) {
        MapLayer highgroundLayer = map.getLayers().get("highground");
        if (highgroundLayer == null) {
          //  Gdx.app.error("HighgroundManager", "No highground layer found!");
            return;
        }

        for (MapObject object : highgroundLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                highgroundAreas.add(rect);
              //  Gdx.app.log("HighgroundManager", "Added rectangle highground: " + rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                highgroundAreas.add(poly);
              //  Gdx.app.log("HighgroundManager", "Added polygon highground");
            }
        }

      //  Gdx.app.log("HighgroundManager", "Total highground areas loaded: " + highgroundAreas.size);
    }

    public Vector2 getStepPosition(float x, float y) {
        boolean onHighground = false;
        for (Object area : highgroundAreas) {
            if ((area instanceof Rectangle && ((Rectangle) area).contains(x, y)) ||
                (area instanceof Polygon && ((Polygon) area).contains(x, y))) {
                onHighground = true;
               // System.out.println("Stepped on highground at: " + x + ", " + y);
                break;
            }
        }

        if (onHighground != wasOnHighground) {
            wasOnHighground = onHighground;
           // System.out.println("State changed! New height: " + (onHighground ? "up" : "down"));
            float newX = x + (onHighground ? X_OFFSET : -X_OFFSET);
            float newY = y + (onHighground ? STEP_HEIGHT : -STEP_HEIGHT);
            return new Vector2(newX, newY);
        }
        return null;
    }

    public void printAreas() {
        System.out.println("Number of highground areas: " + highgroundAreas.size);
        for (Object area : highgroundAreas) {
            if (area instanceof Rectangle) {
                Rectangle r = (Rectangle) area;
                System.out.println("Rectangle: " + r.x + ", " + r.y + ", " + r.width + ", " + r.height);
            }
        }
    }

    @Override
    public void dispose() {
        // Clean up any resources if needed
    }
}
