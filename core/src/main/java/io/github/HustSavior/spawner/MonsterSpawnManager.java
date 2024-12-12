package io.github.HustSavior.spawner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;
import static io.github.HustSavior.utils.GameConfig.PPM;

public class MonsterSpawnManager {
    private final World world;
    private final Player player;
    private Array<AbstractMonster> monsters;
    private float spawnTimer = 0;
    private static final float SPAWN_INTERVAL = 5f;
    private static final short MONSTER_CATEGORY = 0x0002; // Example category bits for monsters
    private final Camera camera;
    private static final float MIN_SPAWN_DISTANCE = 12f; // Just outside camera view
    private static final float MAX_SPAWN_DISTANCE = 15f; // Not too far from player
    private final TiledMap map;
    private final Array<Rectangle> spawnRectangles = new Array<>();
    private final Array<Polygon> spawnPolygons = new Array<>();
    private final MonsterPool monsterPool;
    private int totalSpawned = 0;
    
    public MonsterSpawnManager(World world, Player player, Array<AbstractMonster> monsters, Camera camera, TiledMap map) {
        this.world = world;
        this.player = player;
        this.monsters = monsters;
        this.camera = camera;
        this.map = map;
        this.monsterPool = new MonsterPool();
        loadSpawnAreas();
    }
    
    public void update(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnMonsters(2, 3); // 2 monsters, 3 meters radius
            spawnTimer = 0;
        }
    }
    
    private void loadSpawnAreas() {
        MapLayer spawnLayer = map.getLayers().get("spawning");
        if (spawnLayer == null) {
            Gdx.app.error("Spawn", "No spawning layer found in map!");
            return;
        }
        
        // Load all spawn areas from the map
        for (MapObject object : spawnLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                spawnRectangles.add(rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();
                spawnPolygons.add(poly);
            }
        }
    }
    
    private boolean isInSpawnArea(float x, float y) {
        // Check rectangles
        for (Rectangle rect : spawnRectangles) {
            if (rect.contains(x * PPM, y * PPM)) {  // Convert Box2D coordinates to pixels
                return true;
            }
        }
        
        // Check polygons
        for (Polygon poly : spawnPolygons) {
            if (poly.contains(x * PPM, y * PPM)) {  // Convert Box2D coordinates to pixels
                return true;
            }
        }
        
        return false;
    }
    
    private void spawnMonsters(int count, float radius) {
        // Get all spawn rectangles
        MapLayer spawnLayer = map.getLayers().get("spawning");
        if (spawnLayer == null || (spawnRectangles.isEmpty() && spawnPolygons.isEmpty())) {
            Gdx.app.error("Spawn", "No spawn areas defined!");
            return;
        }

        for (int i = 0; i < count; i++) {
            int attempts = 0;
            int maxAttempts = 50;  // Increased for better chance of finding valid position
            boolean spawned = false;

            while (!spawned && attempts < maxAttempts) {
                Rectangle spawnRect = spawnRectangles.random();
                if (spawnRect != null) {
                    float x = spawnRect.x + MathUtils.random(spawnRect.width);
                    float y = spawnRect.y + MathUtils.random(spawnRect.height);
                    
                    x /= PPM;
                    y /= PPM;

                    if (isInSpawnArea(x, y) && isOutsideCamera(x, y) && 
                        isPositionClear(x, y) && isWithinSpawnDistance(x, y)) {
                        AbstractMonster monster = createRandomMonster(x, y);
                        if (monster != null) {
                            monsters.add(monster);
                            spawned = true;
                            totalSpawned++;
                            Gdx.app.log("MonsterSpawn", "Monster spawned! Total spawned: " + totalSpawned + 
                                ", Current active: " + monsters.size);
                        }
                    }
                }
                attempts++;
            }
        }
    }
    
    private boolean isPositionClear(float x, float y) {
        final boolean[] isClear = {true};
        float checkRadius = 0.5f; // Adjust based on monster size
        
        // Query the world for any fixtures in the spawn area
        world.QueryAABB(
            fixture -> {
                // Ignore sensors and other monster fixtures
                if (!fixture.isSensor() && fixture.getFilterData().categoryBits != MONSTER_CATEGORY) {
                    isClear[0] = false;
                    return false; // Stop querying
                }
                return true; // Continue querying
            },
            x - checkRadius, y - checkRadius,
            x + checkRadius, y + checkRadius
        );
        
        return isClear[0];
    }
    
    private boolean isOutsideCamera(float x, float y) {
        float cameraLeft = camera.position.x - camera.viewportWidth/2;
        float cameraRight = camera.position.x + camera.viewportWidth/2;
        float cameraBottom = camera.position.y - camera.viewportHeight/2;
        float cameraTop = camera.position.y + camera.viewportHeight/2;
        
        // Add a small margin to ensure monsters are completely outside view
        float margin = 1f;
        return x < (cameraLeft - margin) || x > (cameraRight + margin) ||
               y < (cameraBottom - margin) || y > (cameraTop + margin);
    }
    
    private AbstractMonster createRandomMonster(float x, float y) {
        try {
            int monsterType = MathUtils.random(3);
            return monsterPool.obtain(monsterType, x, y);
        } catch (Exception e) {
            Gdx.app.error("Spawn", "Failed to spawn monster: " + e.getMessage());
            return null;
        }
    }
    
    private boolean isWithinSpawnDistance(float x, float y) {
        Vector2 playerPos = player.getPosition();
        float distance = Vector2.dst(playerPos.x, playerPos.y, x, y);

        return distance >= MIN_SPAWN_DISTANCE && distance <= MAX_SPAWN_DISTANCE;
    }
} 