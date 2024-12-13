package io.github.HustSavior;

import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.HustSavior.bullet.Bullet;
import io.github.HustSavior.bullet.BulletManager;
import io.github.HustSavior.collision.CollisionBodyFactory;
import io.github.HustSavior.collision.CollisionListener;
import io.github.HustSavior.dialog.DialogManager;
import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;

import io.github.HustSavior.handlers.CollisionHandler;
import io.github.HustSavior.input.InputHandler;
import io.github.HustSavior.items.AssetSetter;
import io.github.HustSavior.items.HPPotion;
import io.github.HustSavior.items.Item;
import io.github.HustSavior.items.Shield;
import io.github.HustSavior.map.GameMap;
import io.github.HustSavior.map.HighgroundManager;
import io.github.HustSavior.map.LowgroundManager;
import io.github.HustSavior.skills.Slash;
import io.github.HustSavior.sound.MusicPlayer;
import io.github.HustSavior.spawn.SpawnManager;
import io.github.HustSavior.spawner.MonsterPool;
import io.github.HustSavior.spawner.MonsterSpawnManager;
import io.github.HustSavior.ui.GameTimer;
import io.github.HustSavior.ui.InventoryTray;
import io.github.HustSavior.ui.PauseButton;
import io.github.HustSavior.utils.GameConfig;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;
import io.github.HustSavior.utils.transparency.TreeTransparencyManager;
import io.github.HustSavior.collision.TileCollision;
public class Play implements Screen {
    private static final float PPM = GameConfig.PPM;
//    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 20f;
    private static final float WORLD_STEP_TIME = 1 / 60f;
    private static final float WARNING_COOLDOWN_TIME = 2f; // Cooldown time in seconds
    private static final float TRANSPARENCY_UPDATE_INTERVAL = 1/30f; // Update 30 times per second
    private static final long GC_CHECK_INTERVAL = 60000; // Check every 60 seconds
    private static final int VELOCITY_ITERATIONS = 2;  // Reduced from 3
    private static final int POSITION_ITERATIONS = 1;
    private static final float PHYSICS_STEP = 1/60f;
    private static final float CLEANUP_INTERVAL = 5000f; // 5 seconds
    private static final float FIXED_TIME_STEP = 1/60f;
    private static final float MAX_FRAME_TIME = 0.25f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private  GameMap gameMap;
    private final Player player;
    private BulletManager bulletManager;
    // Skill
    private final AssetSetter assetSetter;
    //private final SkillManager skillManager;
    private final InputHandler inputHandler;
    private  World world;

    private Stage uiStage;
    private PauseButton pauseButton;
    private boolean isPaused = false;
    private BuildingTransparencyManager buildingTransparencyManager;
    private ShapeRenderer shapeRenderer;
    private CollisionBodyFactory collisionBodyFactory;
    private HighgroundManager highgroundManager;
    private Skin skin;

    private Stage stage;
    private float warningCooldown = 0;
    private DialogManager dialogManager;
    private SpawnManager spawnManager;
    private float transparencyUpdateTimer = 0;
    private TreeTransparencyManager treeTransparencyManager;
    private LowgroundManager lowgroundManager;
    private GameTimer gameTimer;
    private InventoryTray inventoryTray;
    private TileCollision tileCollision;

    private Rectangle mapBounds;
    private MusicPlayer musicPlayer;
    private long lastVolumeCheck = 0;
    private long lastGCCheck = 0; // Add this field as well
    private long lastPerformanceLog = 0;
    private long lastCleanupTime = 0;
    
    private final Game game;

    private CollisionHandler collisionHandler;

    private SpriteBatch batch;

    private static class ProfilerInfo {
        long physicsTime;
        long inputTime;
        long cameraTime;
        long positionTime;
        long bulletTime;
        long itemTime;
        long physicsUpdateTime;
        
        void reset() {
            physicsTime = inputTime = cameraTime = positionTime = bulletTime = itemTime = physicsUpdateTime = 0;
        }
    }

    private final ProfilerInfo profiler = new ProfilerInfo();
    private float accumulator = 0;


    private float spawnTimer = 0;
    private static final float SPAWN_INTERVAL = 2f;
    private static final int SPAWN_COUNT = 2;

    private static final int MAX_MONSTERS = 10;  // Limit max monsters
    private static final float CLEANUP_DISTANCE = 1000f;  // Distance to remove monsters

    private float cleanupTimer = 0;

    private Array<AbstractMonster> monsters;
    private MonsterPool monsterPool;
    private MonsterSpawnManager monsterSpawnManager;

    private boolean isGameOver = false;
    private boolean isDisposed = false;

    public Play(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        
        // Initialize core components
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        
        // Initialize world and map
        world = setupWorld();
        collisionBodyFactory = new CollisionBodyFactory(world, PPM);
        gameMap = new GameMap("map/map.tmx", collisionBodyFactory);
        
        // Initialize player
        player = new Player(
            new Sprite(new Texture("sprites/WalkRight1.png")),
            450,    // x coordinate
            500,    // y coordinate
            world,
            game,
            gameMap.getTiledMap()
        );
        player.setCamera(camera);
        
        // Initialize bullet manager
        bulletManager = new BulletManager(player, new ArrayList<>(), gameMap.getTiledMap());
        
        // Set logging level to show debug messages
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        // Initialize camera with proper starting position
        camera.position.set(500f, 150f, 0f);
        camera.zoom = 0.5f;
        camera.update();
        
        // Force viewport update immediately
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Initialize SpawnManager before loading items
        spawnManager = new SpawnManager(gameMap.getTiledMap());

        // Initialize highground manager
        highgroundManager = new HighgroundManager(gameMap.getTiledMap());
        // Initialize transparency manager
        // Add tree transparency manager initialization
        treeTransparencyManager = new TreeTransparencyManager(world, gameMap.getTiledMap());

        inputHandler = new InputHandler(player, bulletManager);
        assetSetter = new AssetSetter();
        //skillManager = new SkillManager(player, world);
        //skillManager.activateSkills(1);

        // Load items after SpawnManager is initialized
        loadItems();

        // Add UI stage and pause button
        uiStage = new Stage(new ScreenViewport());
        pauseButton = new PauseButton(uiStage, game, this);
        uiStage.addActor(pauseButton);

        // Initialize DialogManager before the input multiplexer setup
        dialogManager = new DialogManager(uiStage, new Skin(Gdx.files.internal("UI/dialogue/dialog.json")), inputHandler);

        // Initialize stage with proper viewport
        stage = new Stage(new ScreenViewport());
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        gameTimer = new GameTimer(stage);

        // Make sure input processor is set up correctly
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);  // Add stage first to handle UI events
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        Gdx.app.log("Play", String.format("Initial stage viewport: %dx%d",
            (int)stage.getViewport().getWorldWidth(),
            (int)stage.getViewport().getWorldHeight()));

        // Initialize transparency manager with map layers
        shapeRenderer = new ShapeRenderer();
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        lowgroundManager = new LowgroundManager(gameMap.getTiledMap());

        Skin inventorySkin = new Skin(Gdx.files.internal("UI/itemtray/itemtray.json"));
        inventoryTray = new InventoryTray(stage, inventorySkin);

        initMonsterSystem();
        initializeMapBounds();

        // Initialize transparency managers with proper layers
        buildingTransparencyManager = new BuildingTransparencyManager(
            world,
            gameMap.getTiledMap(),
            gameMap.getLayer("D3"),
            gameMap.getLayer("D5"),
            gameMap.getLayer("D35"),
            gameMap.getLayer("Library"),
            gameMap.getLayer("Roof"),
            gameMap.getLayer("Parking")
        );

        // Add tree transparency manager initialization
        treeTransparencyManager = new TreeTransparencyManager(
            world, 
            gameMap.getTiledMap()
        );


    }

//    private OrthographicCamera setupCamera() {
//        OrthographicCamera cam = new OrthographicCamera();
//        cam.zoom = INITIAL_ZOOM;
//        cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        return cam;
//    }

    private World setupWorld() {
        World world = new World(new Vector2(0, 0), true);
        world.setContactListener(new CollisionListener(this));
        return world;
    }

    public void handleBulletCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if (fixtureA.getBody().getUserData() instanceof Bullet) {
            Bullet bullet = (Bullet) fixtureA.getBody().getUserData();
            bullet.incrementCollisionCount();
            if (fixtureB.getBody().getUserData() instanceof AbstractMonster) {
                AbstractMonster monster = (AbstractMonster) fixtureB.getBody().getUserData();
                monster.takeDamage(10); // Adjust damage value as needed
            }
        } else if (fixtureB.getBody().getUserData() instanceof Bullet) {
            Bullet bullet = (Bullet) fixtureB.getBody().getUserData();
            bullet.incrementCollisionCount();
            if (fixtureA.getBody().getUserData() instanceof AbstractMonster) {
                AbstractMonster monster = (AbstractMonster) fixtureA.getBody().getUserData();
                monster.takeDamage(10); // Adjust damage value as needed
            }
        }
    }


    public void handleSkillCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if (fixtureA.getBody().getUserData() instanceof Slash) {
            if (fixtureB.getBody().getUserData() instanceof AbstractMonster) {
                AbstractMonster monster = (AbstractMonster) fixtureB.getBody().getUserData();
                monster.takeDamage(30); // Adjust damage value as needed
            }
        } else if (fixtureB.getBody().getUserData() instanceof Slash) {
            if (fixtureA.getBody().getUserData() instanceof AbstractMonster) {
                AbstractMonster monster = (AbstractMonster) fixtureA.getBody().getUserData();
                monster.takeDamage(30); // Adjust damage value as needed
            }
        }
    }


    private void loadItems() {
        assetSetter.createObject(500, 500, 1);
        assetSetter.createObject(400, 400, 2);
        assetSetter.createObject(300, 300, 3);
        assetSetter.createObject(250, 250, 4);
        assetSetter.createObject(400, 250, 5);
    }

    @Override
    public void show() {
        // Initialize basic components
        world = setupWorld();
        collisionBodyFactory = new CollisionBodyFactory(world, PPM);
        
        
        // Force camera position reset
        camera.position.set(500f, 150f, 0f);
        camera.zoom = 0.5f;
        camera.update();
        
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        
        Gdx.app.log("Play", "Show method - Camera position reset to: " + camera.position);

        // Initialize music player
        musicPlayer = MusicPlayer.getInstance();
        try {
            musicPlayer.playGameplayMusic();
            musicPlayer.updateVolume();
        } catch (Exception e) {
            Gdx.app.error("Play", "Failed to initialize gameplay music", e);
        }

        // Load map
        TiledMap map = new TmxMapLoader().load("map/map.tmx");
        player.initCollision(map);
        tileCollision = new TileCollision(map);
    }

    @Override
    public void render(float delta) {
        if (isDisposed) return;
        
        if (isGameOver) {
            player.update(delta);
            
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            batch.begin();
            player.draw(batch);
            batch.end();
            return;
        }
        if(gameMap != null) {
            if (!isPaused) {
                update(delta);
                updateGame(delta);
                if (monsters == null) {
                    Gdx.app.log("Play", "Monsters array is null!");
                    return;
                }
                // Debug each monster in the array
                if(monsters != null) {
                    for (int i = 0; i < monsters.size; i++) {
                        AbstractMonster monster = monsters.get(i);
                        if (monster != null) {
                            
                            
                            monster.update(delta, player);
                            
                            
                        } else {
                            Gdx.app.debug("Play", "Monster[" + i + "] is null!");
                        }
                    }
                }           
                // Debug spawn system
                spawnTimer += delta;
                if (spawnTimer >= SPAWN_INTERVAL) {
                    
                    monsterSpawnManager.update(delta);  // Make sure this is called
                    spawnTimer = 0;
                    
                   // Debug monster count after spawn attempt
                    
                }

                // Single update location for monsters
                for (AbstractMonster monster : monsters) {
                    if (monster != null && monster.isAlive()) {
                        monster.update(delta, player);
                    }
                }
                
                // Other updates
               
            }

            camera.update();
            
            drawGame();
            
            checkCollisions();
            dialogManager.update(delta);

            if (stage != null) {
                stage.act(delta);
                stage.draw();
            }
            if (uiStage != null) {
                uiStage.act(delta);
                uiStage.draw();
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(camera.combined);
            
            // Render collision bounds
            tileCollision.renderDebug(shapeRenderer);
            
            // Render monster bounds
            for (AbstractMonster monster : monsters) {
                monster.renderDebug(shapeRenderer);
            }
            
            shapeRenderer.end();
        }
    }

    private Rectangle getViewBounds() {
        float w = camera.viewportWidth * camera.zoom;
        float h = camera.viewportHeight * camera.zoom;
        return new Rectangle(
            camera.position.x - w/2,
            camera.position.y - h/2,
            w,
            h
        );
    }

   
    private void updateGame(float delta) {
        float frameTime = Math.min(delta, MAX_FRAME_TIME);
        accumulator += frameTime;
        while (accumulator >= FIXED_TIME_STEP) {
            world.step(FIXED_TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= FIXED_TIME_STEP;
        }
        inputHandler.update(delta);
        updateCamera();
        Vector2 currentPos = player.getPosition();
        if (currentPos != null) {
            Vector2 adjustedPos = highgroundManager.getStepPosition(currentPos.x, currentPos.y);
            if (adjustedPos != null) {
                adjustedPos = lowgroundManager.updatePosition(adjustedPos.x, adjustedPos.y);
                player.setWorldPosition(adjustedPos.x, adjustedPos.y);
            } else {
                Vector2 lowgroundPos = lowgroundManager.updatePosition(currentPos.x, currentPos.y);
                player.setWorldPosition(lowgroundPos.x, lowgroundPos.y);
            }
        }
        player.update(delta);

        // // Debug transparency state
        // if (gameMap != null && gameMap.getTiledMap() != null) {
        //     for (MapLayer layer : gameMap.getTiledMap().getLayers()) {
        //         if (layer.getName().equals("D3") || 
        //             layer.getName().equals("D5") || 
        //             layer.getName().equals("D35") || 
        //             layer.getName().equals("Library") || 
        //             layer.getName().equals("Roof") || 
        //             layer.getName().equals("Parking")) {
                    
        //             Gdx.app.debug("Play", String.format(
        //                 "Layer %s opacity: %.2f",
        //                 layer.getName(),
        //                 layer.getOpacity()
        //             ));
        //         }
        //     }
        // }
    }


    private void logPerformanceMetrics() {
        Gdx.app.log("Profiler", String.format(
            "Physics: %.2fms, Physics Update: %.2fms, Input: %.2fms, Camera: %.2fms, Position: %.2fms, " +
            "Bullets: %.2fms, Items: %.2fms",
            profiler.physicsTime / 1000000f,
            profiler.physicsUpdateTime / 1000000f,
            profiler.inputTime / 1000000f,
            profiler.cameraTime / 1000000f,
            profiler.positionTime / 1000000f,
            profiler.bulletTime / 1000000f,
            profiler.itemTime / 1000000f
        ));
    }

    private void updateCamera() {
        // Get player position (now in pixels)
        Vector2 playerPos = player.getPosition();
        if (playerPos == null) {
            return;
        }

        // Use lerp for smooth camera following
        float lerp = 0.1f;
        camera.position.x += (playerPos.x - camera.position.x) * lerp;
        camera.position.y += (playerPos.y - camera.position.y) * lerp;

        // Clamp camera to map bounds if needed
        if (mapBounds != null) {
            float viewportHalfWidth = (camera.viewportWidth * camera.zoom) / 2;
            float viewportHalfHeight = (camera.viewportHeight * camera.zoom) / 2;
            float margin = 100f;

            camera.position.x = Math.max(viewportHalfWidth + margin, 
                Math.min(mapBounds.width - viewportHalfWidth - margin, camera.position.x));
            camera.position.y = Math.max(viewportHalfHeight + margin, 
                Math.min(mapBounds.height - viewportHalfHeight - margin, camera.position.y));
        }

        camera.update();
    }

    private void drawGame() {
        if (isDisposed || gameMap == null || batch == null) return;

        OrthogonalTiledMapRenderer renderer = gameMap.getRenderer();
        if (renderer == null) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderer.setView(camera); 
        // Get camera frustum for culling
        float w = camera.viewportWidth * camera.zoom;
        float h = camera.viewportHeight * camera.zoom;
        Rectangle viewBounds = new Rectangle(
            camera.position.x - w/2,
            camera.position.y - h/2,
            w,
            h
        );
        // Render first 3 layers
        int[] firstLayers = {0, 1, 2};
        renderer.render(firstLayers);        
        // Draw monsters and player
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (monsters != null) {
            for (AbstractMonster monster : monsters) {
                if (monster != null) {
                    monster.draw(batch);
                }
            }
        }
        
        if (bulletManager != null) {
            bulletManager.render(batch, viewBounds);
        }
        batch.end();    
        if (buildingTransparencyManager != null && player != null) {
            buildingTransparencyManager.update(player.getPosition());
        }
        if (treeTransparencyManager != null && player != null) {
            treeTransparencyManager.update(player.getPosition());
        }
        // Render remaining layers
        if (gameMap.getTiledMap() != null) {
            int[] remainingLayers = new int[gameMap.getTiledMap().getLayers().getCount() - 3];
            for (int i = 3; i < gameMap.getTiledMap().getLayers().getCount(); i++) {
                remainingLayers[i - 3] = i;
            }
            renderer.render(remainingLayers);
        }   
        // Draw other game objects
        batch.begin();
        
        if (assetSetter != null) {
            assetSetter.drawVisibleObjects(batch, viewBounds);
        }
        if (player != null) {
            player.draw(batch);
        }
        batch.end();
     
    }

    private boolean isInView(float x, float y, Rectangle viewBounds) {
        return viewBounds.contains(x, y);
    }

    private void handleZoom() {
        if (Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            camera.zoom -= ZOOM_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            camera.zoom += ZOOM_SPEED;
        }
        camera.zoom = Math.max(MIN_ZOOM, Math.min(camera.zoom, MAX_ZOOM));
    }

    @Override
    public void dispose() {
        isDisposed = true;
        // Dispose physics world and bodies first
        if (world != null) {
            world.dispose();
            world = null;
        }

        // Dispose monsters safely
        if (monsters != null) {
            for (AbstractMonster monster : new Array.ArrayIterator<>(monsters)) {
                if (monster != null) {
                    monster.dispose();
                }
            }
            monsters.clear();
            monsters = null;
        }

        // Dispose graphics resources safely
        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        // Dispose map resources
        if (gameMap != null) {
            gameMap.dispose();
            gameMap = null;
        }

        // Dispose UI resources
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (uiStage != null) {
            uiStage.dispose();
            uiStage = null;
        }

        // Dispose other resources
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport while maintaining camera position
        float oldX = camera.position.x;
        float oldY = camera.position.y;
        
        viewport.update(width, height, false);
        
        // Restore camera position
        camera.position.set(oldX, oldY, 0);
        camera.update();

        // Update UI stages
        uiStage.getViewport().update(width, height, true);
        stage.getViewport().update(width, height, true);
        pauseButton.updatePosition();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {
        if (musicPlayer != null) {
            Gdx.app.log("MusicPlayer", "Pausing current music");
            musicPlayer.pause();
        }
    }

    @Override
    public void resume() {
        if (musicPlayer != null) {
            Gdx.app.log("MusicPlayer", "Resuming current music");
            musicPlayer.resume();
        }
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void checkCollisions() {
        if (gameMap == null) return;

        MapLayer warningsLayer = gameMap.getTiledMap().getLayers().get("warnings");
        if (warningsLayer == null) {
            
            return;
        }

        if (warningCooldown > 0) {
            warningCooldown -= Gdx.graphics.getDeltaTime();
            return;
        }

        // Get player position and convert to world coordinates
        Vector2 playerPos = player.getPosition();
        float playerX = playerPos.x;
        float playerY = playerPos.y;
        
        // Create player bounds rectangle
        Rectangle playerRect = new Rectangle(
            playerX - player.getWidth() / 2,
            playerY - player.getHeight() / 2,
            player.getWidth(),
            player.getHeight()
        );


        for (MapObject object : warningsLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                
                
                
                if (playerRect.overlaps(rect)) {
                    player.stopMovement();
                    // Make sure dialog is shown on the UI thread
                    Gdx.app.postRunnable(() -> {
                        dialogManager.showWarningDialog("Warning: Anh hen em pickleball", () -> {
                            Gdx.app.log("Play", "Warning dialog closed");
                            player.resetMovement();
                            warningCooldown = WARNING_COOLDOWN_TIME;
                        });
                    });
                    break;
                }
            }
        }
    }

    public void handleItemCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Object userDataA = contact.getFixtureA().getBody().getUserData();
        Object userDataB = contact.getFixtureB().getBody().getUserData();

        if (userDataA instanceof Player && userDataB instanceof Item) {
            handleItemCollision((Item) userDataB, fixtureB);
        } else if (userDataB instanceof Player && userDataA instanceof Item) {
            handleItemCollision((Item) userDataA, fixtureA);
        }
    }

    private void handleItemCollision(Item item, Fixture fixture) {
        if (!item.isCollected()) {
            inputHandler.setDialogActive(true);
            dialogManager.showItemPickupDialog(item.getDialogMessage(), item.getImagePath(), () -> {
                item.setCollected(true);
                fixture.setSensor(true);
                player.acquireEffect(item.getId());
                assetSetter.objectAcquired(item);
                if (item instanceof HPPotion) {
                    player.heal(50);
                } else if (item instanceof Shield) {
                    player.activateShield();
                }
                inventoryTray.addItem(item.getImagePath());
                inputHandler.setDialogActive(false);
            });
        }
    }

    private void initializeMapBounds() {
        MapLayer boundsLayer = gameMap.getTiledMap().getLayers().get("map_bounds");
        if (boundsLayer != null) {
            for (MapObject object : boundsLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    mapBounds = ((RectangleMapObject) object).getRectangle();
                    break;
                }
            }
        }
    }

    private void cleanupUnusedResources() {
        System.gc();
        Gdx.app.log("Play", "Garbage collection triggered");
    }

    // Add this method to clean up inactive physics bodies
    private void cleanupPhysicsBodies() {
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (!body.isActive() || body.getUserData() == null) {
                world.destroyBody(body);
            }
        }
    }

    private void initMonsterSystem() {
        Gdx.app.debug("Play", "Initializing monster system");
        monsters = new Array<AbstractMonster>(false, 16);
        monsterPool = new MonsterPool();
        monsterSpawnManager = new MonsterSpawnManager(
            player,
            monsters,  // Same array reference
            camera,
            gameMap.getTiledMap(),
            monsterPool
        );
        Gdx.app.debug("Play", "Monster system initialized with array: " + monsters.toString());
    }

    private void update(float delta) {
        if (isDisposed || gameMap == null) return;      
        if (!isPaused) {
            // Update player
            if (player != null) {
                player.update(delta);
                
                // Get player position once
               
            }          
            updateCamera();           
            if (bulletManager != null) {
                bulletManager.update(delta);
            }
            if (monsterSpawnManager != null) {
                monsterSpawnManager.update(delta);
            }           
            if (gameMap.getTiledMap() != null) {
                MapLayer collisionLayer = gameMap.getTiledMap().getLayers().get("collisions");
                if (collisionLayer != null) {
                    // Debug collision layer
                    
                    for (MapObject object : collisionLayer.getObjects()) {
                        if (object instanceof RectangleMapObject) {
                            Rectangle rect = ((RectangleMapObject) object).getRectangle();
                            // Scale rectangle if using PPM
                            rect.x /= GameConfig.PPM;
                            rect.y /= GameConfig.PPM;
                            rect.width /= GameConfig.PPM;
                            rect.height /= GameConfig.PPM;
                            bulletManager.checkCollisions(rect);
                        }
                    }
                } else {
                    Gdx.app.debug("Play", "No collision layer found!");
                }               
                checkItemPickup();
                updateCamera();               
                if (!isPaused) {
            
                    for (AbstractMonster monster : monsters) {
                        monster.update(delta, player);
                    }
                }       
               
                // Periodic garbage collection
                cleanupTimer += delta;
                if (cleanupTimer >= CLEANUP_INTERVAL) {
                    cleanupUnusedResources();
                    cleanupTimer = 0;
                }                            
            }
        }
    }
    
    private void checkItemPickup() {
        if (gameMap == null) return;

        Rectangle playerBounds = player.getBounds();
        for (Item item : assetSetter.getItems()) {
            if (!item.isCollected() && item.isVisible() && playerBounds.overlaps(item.getBounds())) {
                // Set dialog active state
                inputHandler.setDialogActive(true);
                
                // Handle specific item effects
                handleItemEffect(item);
                
                // Show dialog with proper parameters
                dialogManager.showItemPickupDialog(
                    item.getDialogMessage(),
                    item.getImagePath(),
                    () -> {
                        // Callback when dialog is closed
                        item.setCollected(true);
                        assetSetter.objectAcquired(item);
                        inventoryTray.addItem(item.getImagePath());
                        inputHandler.setDialogActive(false);
                    }
                );
                break;
            }
        }
    }

    private void handleItemEffect(Item item) {
        switch (item.getId()) {
            case 1: // CalcBook
                player.acquireEffect(1);
                break;
            case 2: // AlgebraBook
                player.acquireEffect(2);
                break;
            case 3: // PhysicBook
                player.acquireEffect(3);
                break;
            case 4: // HPPotion
                player.heal(50);
                break;
            case 5: // Shield
                player.activateShield();
                break;
        }
    }

    public void setGameOver() {
        isGameOver = true;
    }

    
    public Array<AbstractMonster> getMonsters() {
        return monsters;
    }
}