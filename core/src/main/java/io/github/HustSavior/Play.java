package io.github.HustSavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.badlogic.gdx.utils.TimeUtils;
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
import io.github.HustSavior.items.AlgebraBook;
import io.github.HustSavior.items.AssetSetter;
import io.github.HustSavior.items.CalcBook;
import io.github.HustSavior.items.HPPotion;
import io.github.HustSavior.items.Item;
import io.github.HustSavior.items.PhysicBook;
import io.github.HustSavior.items.Shield;
import io.github.HustSavior.map.GameMap;
import io.github.HustSavior.map.HighgroundManager;
import io.github.HustSavior.map.LowgroundManager;
import io.github.HustSavior.skills.Slash;
import io.github.HustSavior.sound.MusicPlayer;
import io.github.HustSavior.spawn.SpawnManager;
import io.github.HustSavior.spawner.MonsterPool;
import io.github.HustSavior.ui.GameTimer;
import io.github.HustSavior.ui.InventoryTray;
import io.github.HustSavior.ui.PauseButton;
import io.github.HustSavior.utils.GameConfig;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;
import io.github.HustSavior.utils.transparency.TreeTransparencyManager;

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
    private List<AbstractMonster> monsters = new ArrayList<>();

    private Rectangle mapBounds;
    private MusicPlayer musicPlayer;
    private long lastVolumeCheck = 0;
    private long lastGCCheck = 0; // Add this field as well
    private long lastPerformanceLog = 0;
    private long lastCleanupTime = 0;

    private final Game game;

    private CollisionHandler collisionHandler;

    private final SpriteBatch batch;

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

    private MonsterPool monsterPool;
    private float spawnTimer = 0;
    private static final float SPAWN_INTERVAL = 2f;
    private static final int SPAWN_COUNT = 2;

    private static final int MAX_MONSTERS = 10;  // Limit max monsters
    private static final float CLEANUP_DISTANCE = 1000f;  // Distance to remove monsters

    private float cleanupTimer = 0;

    public Play(Game game) {
        this.game = game;
        musicPlayer = MusicPlayer.getInstance();
        // Set logging level to show debug messages
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        // Initialize camera with proper starting position
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        
        // Force initial camera position and zoom
        camera.position.set(500f, 150f, 0f);
        camera.zoom = 0.5f;
        camera.update();
        
        // Force viewport update immediately
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        world = setupWorld();
        collisionBodyFactory = new CollisionBodyFactory(world, PPM);
        gameMap = new GameMap("map/map.tmx", collisionBodyFactory);
        initializeMapBounds();

        // Initialize SpawnManager before loading items
        spawnManager = new SpawnManager(gameMap.getTiledMap());

        // Initialize highground manager
        highgroundManager = new HighgroundManager(gameMap.getTiledMap());
        // Initialize transparency manager
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
        treeTransparencyManager = new TreeTransparencyManager(world, gameMap.getTiledMap());

        player = new Player(
            new Sprite(new Texture("sprites/WalkRight1.png")),
            450,    // x coordinate
            500,    // y coordinate
            world,
            game,
            gameMap.getTiledMap()  // Pass the TiledMap from gameMap
        );
        player.setCamera(camera);
        inputHandler = new InputHandler(player);
        bulletManager = new BulletManager(player, monsters);
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

        // Initialize monster pool
        monsterPool = new MonsterPool();
        monsters = new ArrayList<>();
    
        collisionHandler = new CollisionHandler();

        this.batch = new SpriteBatch();
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
        spawnInitialMonsters();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) {
            update(delta);
            updateGame(delta);
        }

        camera.update();
        
        // Use drawGame instead of separate render calls
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
        profiler.reset();
        
        // Limit frame time to prevent spiral of death
        float frameTime = Math.min(delta, MAX_FRAME_TIME);
        accumulator += frameTime;
        
        long physicsStartTime = TimeUtils.nanoTime();
        
        // Physics simulation with fixed time step
        while (accumulator >= FIXED_TIME_STEP) {
            long startTime = TimeUtils.nanoTime();
            
            // Step physics world
            world.step(FIXED_TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            profiler.physicsTime = TimeUtils.nanoTime() - startTime;
            
            accumulator -= FIXED_TIME_STEP;
        }
        
        profiler.physicsUpdateTime = TimeUtils.nanoTime() - physicsStartTime;
        
        // Non-physics updates
        long startTime = TimeUtils.nanoTime();
        inputHandler.update(delta);
        profiler.inputTime = TimeUtils.nanoTime() - startTime;
        
        startTime = TimeUtils.nanoTime();
        updateCamera();
        profiler.cameraTime = TimeUtils.nanoTime() - startTime;
        
        // Position updates - consolidated ground management here
        startTime = TimeUtils.nanoTime();
        Vector2 currentPos = player.getPosition();
        if (currentPos != null) {
            // First apply highground adjustments
            Vector2 adjustedPos = highgroundManager.getStepPosition(currentPos.x, currentPos.y);
            if (adjustedPos != null) {
                // Then apply lowground adjustments to the already adjusted position
                adjustedPos = lowgroundManager.updatePosition(adjustedPos.x, adjustedPos.y);
                // Update player's world position with both adjustments
                player.setWorldPosition(adjustedPos.x, adjustedPos.y);
            } else {
                // If no highground adjustment, still check for lowground
                Vector2 lowgroundPos = lowgroundManager.updatePosition(currentPos.x, currentPos.y);
                player.setWorldPosition(lowgroundPos.x, lowgroundPos.y);
            }
        }
        profiler.positionTime = TimeUtils.nanoTime() - startTime;
        
        // Update player
        player.update(delta);
        
        logPerformanceMetrics();
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
        if (playerPos == null || mapBounds == null) {
            Gdx.app.error("Play", "Player position or map bounds is null!");
            return;
        }

        // Use lerp for smooth camera following
        float lerp = 0.1f;
        camera.position.x += (playerPos.x - camera.position.x) * lerp;
        camera.position.y += (playerPos.y - camera.position.y) * lerp;

        // Clamp camera to map bounds with margin
        float viewportHalfWidth = (camera.viewportWidth * camera.zoom) / 2;
        float viewportHalfHeight = (camera.viewportHeight * camera.zoom) / 2;
        float margin = 100f;

        camera.position.x = Math.max(viewportHalfWidth + margin, 
            Math.min(mapBounds.width - viewportHalfWidth - margin, camera.position.x));
        camera.position.y = Math.max(viewportHalfHeight + margin, 
            Math.min(mapBounds.height - viewportHalfHeight - margin, camera.position.y));

        camera.update();
    }

    private void drawGame() {
        OrthogonalTiledMapRenderer renderer = gameMap.getRenderer();
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
        
        // Draw monsters
        SpriteBatch batch = (SpriteBatch) renderer.getBatch();
        batch.begin();
        for (AbstractMonster monster : monsters) {
            if (isInView(monster.getPosition().x, monster.getPosition().y, viewBounds)) {
                monster.render(batch);
            }
        }
        batch.end();
        
        // Render remaining layers
        int[] remainingLayers = new int[gameMap.getTiledMap().getLayers().getCount() - 3];
        for (int i = 3; i < gameMap.getTiledMap().getLayers().getCount(); i++) {
            remainingLayers[i - 3] = i;
        }
        renderer.render(remainingLayers);
        
        // Draw other game objects
        batch.begin();
        bulletManager.render(batch, viewBounds);
        assetSetter.drawVisibleObjects(batch, viewBounds);
        player.draw(batch);
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
        batch.dispose();
        // Dispose physics world and bodies first
        if (world != null) {
            // Remove all bodies safely
            Array<Body> bodies = new Array<>();
            world.getBodies(bodies);
            for (Body body : bodies) {
                if (body != null) {
                    world.destroyBody(body);
                }
            }
            world.dispose();
            world = null;
        }

        // Dispose monsters
        if (monsters != null) {
            for (AbstractMonster monster : monsters) {
                monsterPool.free(monster);
            }
            monsters.clear();
        }

        // Dispose graphics resources
        if (batch != null) batch.dispose();
        if (gameMap != null) gameMap.dispose();
        if (player != null && player.getTexture() != null) player.getTexture().dispose();
        if (uiStage != null) uiStage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (stage != null) stage.dispose();
        if (gameTimer != null) gameTimer.dispose();
        if (inventoryTray != null) inventoryTray.dispose();
        if (musicPlayer != null) musicPlayer.dispose();
        if (skin != null) skin.dispose();
        if (bulletManager != null) bulletManager.dispose();
        if (assetSetter != null) assetSetter.dispose();
        if (dialogManager != null) dialogManager.dispose();
        if (buildingTransparencyManager != null) buildingTransparencyManager.dispose();
        if (treeTransparencyManager != null) treeTransparencyManager.dispose();
        if (highgroundManager != null) highgroundManager.dispose();
        if (lowgroundManager != null) lowgroundManager.dispose();

        // Force garbage collection
        cleanupUnusedResources();
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
        MapLayer warningsLayer = gameMap.getTiledMap().getLayers().get("warnings");
        if (warningsLayer == null) {
            Gdx.app.error("Play", "Warnings layer not found!");
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

        // Debug log player position and bounds
        Gdx.app.debug("Play", "Player position: " + playerX + ", " + playerY);
        Gdx.app.debug("Play", "Player bounds: " + playerRect);

        for (MapObject object : warningsLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                
                // Debug log warning area bounds
                Gdx.app.debug("Play", "Warning area bounds: " + rect);
                
                if (playerRect.overlaps(rect)) {
                    Gdx.app.log("Play", "Collision detected with warning area!");
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

    private String getItemName(Item item) {
        if (item instanceof CalcBook) return "Calculus Book";
        if (item instanceof AlgebraBook) return "Algebra Book";
        if (item instanceof PhysicBook) return "Physics Book";
        if (item instanceof HPPotion) return "Health Potion";
        if (item instanceof Shield) return "Shield";
        return "Unknown Item";
    }

    
    

    private void updateMonsters(float delta) {
        // Update all monsters
        for (AbstractMonster monster : monsters) {
            monster.update(delta, player);
            monster.render(batch);
        }
        
        for (int i = monsters.size() - 1; i >= 0; i--) {
            if (!monsters.get(i).isAlive()) {
                AbstractMonster monster = monsters.remove(i);
                monster.dispose();
            }
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

    private void update(float delta) {
        if (!isPaused) {
            player.update(delta);
            checkItemPickup();
            updateCamera();
            
            // Add monster spawning/updating here
            spawnTimer += delta;
            if (spawnTimer >= SPAWN_INTERVAL) {
                spawnMonster(MathUtils.random(100, 1000), MathUtils.random(100, 1000));
                spawnTimer = 0;
            }
            
            // Update existing monsters
            for (AbstractMonster monster : monsters) {
                monster.update(delta);
            }
            
            // Get player position in world coordinates
            Vector2 playerPos = player.getPosition();
            
            if (playerPos != null) {
                buildingTransparencyManager.update(playerPos);
                treeTransparencyManager.update(playerPos);
                // Update item visibility based on player position
                assetSetter.updateItemVisibility(playerPos, gameMap.getTiledMap());
            }
            
            cleanupMonsters();  // Clean up far away monsters
            
            // Periodic garbage collection
            cleanupTimer += delta;
            if (cleanupTimer >= CLEANUP_INTERVAL) {
                cleanupUnusedResources();
                cleanupTimer = 0;
            }
        }
    }

    private void checkItemPickup() {
        Rectangle playerBounds = player.getBounds();
        for (Item item : assetSetter.getItems()) {
            if (!item.isCollected() && item.isVisible() && playerBounds.overlaps(item.getBounds())) {
                // Set dialog active state
                inputHandler.setDialogActive(true);
                
                // Show dialog with proper parameters
                dialogManager.showItemPickupDialog(
                    item.getDialogMessage(),  // Pass the message
                    item.getImagePath(),      // Pass the image path
                    () -> {
                        // Callback when dialog is closed
                        item.setCollected(true);
                        player.acquireEffect(item.getId());
                        assetSetter.objectAcquired(item);
                        inventoryTray.addItem(item.getImagePath());
                        inputHandler.setDialogActive(false);
                    }
                );
                // Break after showing dialog for first overlapping item
                break;
            }
        }
    }

    private void spawnInitialMonsters() {
        spawnMonster(500, 300);  // First monster
        spawnMonster(600, 400);  // Second monster
        spawnMonster(700, 500);  // Third monster
    }

    private void spawnMonster(float x, float y) {
        if (monsters.size() >= MAX_MONSTERS) {
            return; // Don't spawn if at max capacity
        }
        
        int monsterType = MathUtils.random(3);
        AbstractMonster monster = monsterPool.obtain(monsterType, x, y);
        if (monster != null) {
            monster.initializeAnimations();
            monsters.add(monster);
        }
    }

    private void cleanupMonsters() {
        Vector2 playerPos = player.getPosition();
        Iterator<AbstractMonster> iter = monsters.iterator();
        
        while (iter.hasNext()) {
            AbstractMonster monster = iter.next();
            // Remove monsters that are too far from player
            if (Vector2.dst(playerPos.x, playerPos.y, 
                monster.getPosition().x, monster.getPosition().y) > CLEANUP_DISTANCE) {
                monsterPool.free(monster);
                iter.remove();
            }
        }
    }
}











