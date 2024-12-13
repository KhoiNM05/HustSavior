package io.github.HustSavior.spawner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;
import static io.github.HustSavior.utils.GameConfig.PPM;

public class MonsterSpawnManager {
    private final Player player;
    private final Array<AbstractMonster> monsters;
    private float spawnTimer = 0;
    private static final float BASE_SPAWN_INTERVAL = 5f;
    private float currentSpawnInterval = BASE_SPAWN_INTERVAL;
    private static final float MIN_SPAWN_INTERVAL = 2f;
    private static final float MAX_SPAWN_INTERVAL = 10f;
    private int spawnCount = 2;
    private static final int MAX_MONSTERS = 50;
    private final Camera camera;
    private static final float MIN_SPAWN_DISTANCE = 200f;
    private static final float MAX_SPAWN_DISTANCE = 400f;
    private final Array<Rectangle> spawnAreas = new Array<>();
    private final MonsterPool monsterPool;

    public MonsterSpawnManager(Player player, Array<AbstractMonster> monsters, Camera camera, TiledMap map, MonsterPool monsterPool) {
        this.player = player;
        this.monsters = monsters;
        this.camera = camera;
        this.monsterPool = monsterPool;
        loadSpawnAreas(map);
    }

    private void loadSpawnAreas(TiledMap map) {
        MapLayer spawnLayer = map.getLayers().get("spawning");
        if (spawnLayer == null) {
            Gdx.app.error("Spawn", "No spawning layer found in map!");
            return;
        }

        for (MapObject object : spawnLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                spawnAreas.add(rect);
              
            }
        }
        
      
    }

    public void setSpawnRate(float multiplier) {
        currentSpawnInterval = MathUtils.clamp(
            BASE_SPAWN_INTERVAL / multiplier,
            MIN_SPAWN_INTERVAL,
            MAX_SPAWN_INTERVAL
        );
     
    }

    public void setSpawnCount(int count) {
        this.spawnCount = MathUtils.clamp(count, 1, 5);
        
    }

    public void update(float delta) {
        if (monsters.size < MAX_MONSTERS) {
            Gdx.app.debug("Spawn", String.format(
                "Spawning monsters. Current count: %d, Max: %d",
                monsters.size,
                MAX_MONSTERS
            ));
            spawnMonsters(spawnCount);
        }
    }

    private void spawnMonsters(int count) {
        if (spawnAreas.isEmpty()) {
            Gdx.app.error("Spawn", "Cannot spawn - no spawn areas defined!");
            return;
        }

        for (int i = 0; i < count; i++) {
            Rectangle spawnArea = spawnAreas.random();
            float x = spawnArea.x + MathUtils.random(spawnArea.width);
            float y = spawnArea.y + MathUtils.random(spawnArea.height);

          

            int monsterType = MathUtils.random(3);
            AbstractMonster monster = monsterPool.obtain(monsterType, x, y);
            
            if (monster != null) {
                monsterPool.resetMonster(monster, x, y);
                monsters.add(monster);
            }
        }
    }

    private boolean isValidSpawnPosition(float x, float y) {
        // Calculate camera bounds with some padding in screen units
        float padding = 32f;
        float cameraLeft = camera.position.x * PPM - camera.viewportWidth/2 - padding;
        float cameraRight = camera.position.x * PPM + camera.viewportWidth/2 + padding;
        float cameraBottom = camera.position.y * PPM - camera.viewportHeight/2 - padding;
        float cameraTop = camera.position.y * PPM + camera.viewportHeight/2 + padding;

        // Debug spawn position attempt
        Gdx.app.debug("Spawn", String.format(
            "Trying spawn at (%.1f, %.1f)", x, y
        ));

        // Check if position is too close to camera view
        if (x >= cameraLeft && x <= cameraRight && y >= cameraBottom && y <= cameraTop) {
            return false;
        }

        Vector2 playerPos = player.getPosition();
        float playerX = playerPos.x * PPM;  // Convert to screen units
        float playerY = playerPos.y * PPM;  // Convert to screen units
        float distance = Vector2.dst(playerX, playerY, x, y);
        
        // Check distance in screen units
        if (distance < MIN_SPAWN_DISTANCE || distance > MAX_SPAWN_DISTANCE) {
            return false;
        }

        return true;
    }

    private boolean isPositionClear(float x, float y) {
        Rectangle newMonsterRect = new Rectangle(x - 16, y - 16, 32, 32);
        
        for (AbstractMonster monster : monsters) {
            Vector2 pos = monster.getPosition();
            Rectangle monsterRect = new Rectangle(pos.x - 16, pos.y - 16, 32, 32);
            if (monsterRect.overlaps(newMonsterRect)) {
                return false;
            }
        }
        
        return true;
    }
} 