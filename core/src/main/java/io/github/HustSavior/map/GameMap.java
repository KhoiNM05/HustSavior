package io.github.HustSavior.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import io.github.HustSavior.collision.CollisionBodyFactory;

public class GameMap {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final CollisionBodyFactory collisionBodyFactory;

    public GameMap(String mapPath, CollisionBodyFactory collisionBodyFactory) {
        this.collisionBodyFactory = collisionBodyFactory;
        
        FileHandle mapFile = Gdx.files.internal(mapPath);
        if (!mapFile.exists()) {
            Gdx.app.error("GameMap", "Map file not found: " + mapFile.path());
            throw new RuntimeException("Map file not found!");
        }
        
        try {
            map = new TmxMapLoader().load(mapPath);
        } catch (Exception e) {
            Gdx.app.error("GameMap", "Failed to load map: " + e.getMessage(), e);
            throw e;
        }
        
        renderer = new OrthogonalTiledMapRenderer(map);
        createCollisionBodies();
    }

    private void createCollisionBodies() {
        if (map.getLayers().get("collisions") == null) {
            Gdx.app.error("GameMap", "Collisions layer not found in map!");
            return;
        }
        
        for (MapObject object : map.getLayers().get("collisions").getObjects()) {
            if (object instanceof RectangleMapObject) { 
                collisionBodyFactory.createStaticBody((RectangleMapObject) object);
            } else if (object instanceof PolygonMapObject) {
                collisionBodyFactory.createStaticBody((PolygonMapObject) object);
            }
        }
    }

    public TiledMap getTiledMap() {
        return map;
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    public MapLayer getLayer(String layerName) {
        return map.getLayers().get(layerName);
    }

    public void dispose() {
        if (map != null) {
            map.dispose();
        }
        if (renderer != null) {
            renderer.dispose();
        }
    }
} 