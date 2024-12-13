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
import io.github.HustSavior.entities.FlyingEye;
import io.github.HustSavior.entities.Goblin;
import io.github.HustSavior.entities.Mushroom;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.entities.Skeleton;

import static io.github.HustSavior.utils.GameConfig.PPM;

public class MonsterSpawnManager {
    private final Player player;
    private final Array<AbstractMonster> monsters;
    private final Camera camera;
    private final Array<Rectangle> spawnAreas = new Array<>();
    private static final int MAX_MONSTERS = 50;
    private static final float MIN_SPAWN_DISTANCE = 2000f;
    private static final float MAX_SPAWN_DISTANCE = 5000f;

    public MonsterSpawnManager(Player player, Array<AbstractMonster> monsters, Camera camera, TiledMap map) {
        this.player = player;
        this.monsters = monsters;
        this.camera = camera;
        loadSpawnAreas(map);
    }

    public void update(float delta) {
        // Only keep this if you need periodic spawning
        if (monsters.size < MAX_MONSTERS) {
            trySpawnMonster();
        }
    }

    public void trySpawnMonster() {
        System.out.println("Attempting to spawn monster...");
        if (monsters.size >= MAX_MONSTERS) {
            System.out.println("Max monsters reached: " + monsters.size);
            return;
        }
        
        if (spawnAreas.isEmpty()) {
            System.out.println("No spawn areas available!");
            return;
        }
        
        Rectangle spawnArea = spawnAreas.random();
        float x = spawnArea.x + MathUtils.random(spawnArea.width);
        float y = spawnArea.y + MathUtils.random(spawnArea.height);
        
       
        
        if (!isValidSpawnPosition(x, y)) {
            Gdx.app.debug("Spawn", "Invalid spawn position");
            return;
        }
        
        if (!isPositionClear(x, y)) {
            Gdx.app.debug("Spawn", "Position not clear");
            return;
        }
        
        AbstractMonster monster = createMonster(x, y);
        if (monster != null) {
            monsters.add(monster);
         
        } else {
            Gdx.app.error("Spawn", "Failed to create monster");
        }
    }

    public AbstractMonster createMonster(float x, float y) {
        int type = MathUtils.random(3);
        switch (type) {
            case 0: return new Skeleton(x, y, player);
            case 1: return new FlyingEye(x, y, player);
            case 2: return new Mushroom(x, y, player);
            case 3: return new Goblin(x, y, player);
            default: return new Skeleton(x, y, player);
        }
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
        
        System.out.println("Loaded spawn areas: " + spawnAreas.size);
    }

    private boolean isValidSpawnPosition(float x, float y) {
        Vector2 playerPos = player.getPosition();
        // Convert everything to world units (divide by PPM)
        float playerX = playerPos.x;  // Already in world units
        float playerY = playerPos.y;  // Already in world units
        float spawnX = x ;       // Convert spawn position to world units
        float spawnY = y ;       // Convert spawn position to world units
        
        float distance = Vector2.dst(playerX, playerY, spawnX, spawnY);

        
        // Convert spawn distance limits to world units
        float minDistance = MIN_SPAWN_DISTANCE ;
        float maxDistance = MAX_SPAWN_DISTANCE ;
        
        if (distance < minDistance || distance > maxDistance) {
       
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

    public Vector2 getRandomSpawnPoint() {
        if (spawnAreas.isEmpty()) return null;
        Rectangle spawnArea = spawnAreas.random();
        float x = spawnArea.x + MathUtils.random(spawnArea.width);
        float y = spawnArea.y + MathUtils.random(spawnArea.height);
        return isValidSpawnPosition(x, y) ? new Vector2(x, y) : null;
    }
} 