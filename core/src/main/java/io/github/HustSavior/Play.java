package io.github.HustSavior;

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
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
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
import io.github.HustSavior.skills.SkillManager;
import io.github.HustSavior.spawn.SpawnManager;
import io.github.HustSavior.spawner.MonsterSpawnManager;
import io.github.HustSavior.ui.GameTimer;
import io.github.HustSavior.ui.InventoryTray;
import io.github.HustSavior.ui.PauseButton;
import io.github.HustSavior.utils.GameConfig;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;
import io.github.HustSavior.utils.transparency.TreeTransparencyManager;

public class Play implements Screen {
    private final float PPM = GameConfig.PPM;
//    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 20f;
    private static final float WORLD_STEP_TIME = 1 / 60f;
    private static final float WARNING_COOLDOWN_TIME = 2f; // Cooldown time in seconds
    private static final float TRANSPARENCY_UPDATE_INTERVAL = 1/30f; // Update 30 times per second

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameMap gameMap;
    private final Player player;
    private BulletManager bulletManager;
    // Skill
    private final AssetSetter assetSetter;
    private final SkillManager skillManager;
    private final InputHandler inputHandler;
    private final World world;

    private Stage uiStage;
    private PauseButton pauseButton;
    private boolean isPaused = false;
    private BuildingTransparencyManager transparencyManager;
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
    private Array<AbstractMonster> monsters;
    private MonsterSpawnManager monsterSpawnManager;

    public Play(Game game) {
        // Set logging level to show debug messages
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();

        world = setupWorld();
        collisionBodyFactory = new CollisionBodyFactory(world, PPM);
        gameMap = new GameMap("map/map.tmx", collisionBodyFactory);

        // Initialize SpawnManager before loading items
        spawnManager = new SpawnManager(gameMap.getTiledMap());

        // Initialize highground manager
        highgroundManager = new HighgroundManager(gameMap.getTiledMap());

        // Initialize transparency manager
        transparencyManager = new BuildingTransparencyManager(
            gameMap.getTiledMap(),
            gameMap.getLayer("D3"),
            gameMap.getLayer("D5"),
            gameMap.getLayer("D35"),
            gameMap.getLayer("Library"),
            gameMap.getLayer("Roof"),
            gameMap.getLayer("Parking")
        );

        // Add tree transparency manager initialization
        treeTransparencyManager = new TreeTransparencyManager(gameMap.getTiledMap());

        player = new Player(
            new Sprite(new Texture("sprites/WalkRight1.png")),
            500,    // Multiply by PPM to convert to world coordinates
            500,    // Multiply by PPM to convert to world coordinates
            world
        );
        inputHandler = new InputHandler(player);
        bulletManager = new BulletManager(world, player);
        assetSetter = new AssetSetter();
        skillManager = new SkillManager(player, world);
        skillManager.activateSkills(1);

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

        // Initialize monster array and spawn manager
        monsters = new Array<>();
        monsterSpawnManager = new MonsterSpawnManager(world, player, monsters, camera, gameMap.getTiledMap());
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
            ((Bullet) fixtureA.getBody().getUserData()).incrementCollisionCount();
        } else if (fixtureB.getBody().getUserData() instanceof Bullet) {
            ((Bullet) fixtureB.getBody().getUserData()).incrementCollisionCount();
        }
    }

    private void createCollisionBodies() {
        // Only create collision bodies for the general collisions layer
        MapLayer collisionsLayer = gameMap.getTiledMap().getLayers().get("collisions");
        if (collisionsLayer != null) {
            createCollisionBodiesForLayer(collisionsLayer);
        }
    }

    private void createCollisionBodiesForLayer(MapLayer layer) {
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                collisionBodyFactory.createStaticBody((RectangleMapObject) object);
            } else if (object instanceof PolygonMapObject) {
                collisionBodyFactory.createStaticBody((PolygonMapObject) object);
            }
        }
    }

    private void loadItems() {
        assetSetter.createObject(500, 500, 1, PPM, world);
        assetSetter.createObject(400, 400, 2, PPM, world);
        assetSetter.createObject(300, 300, 3, PPM, world);
        assetSetter.createObject(250, 250, 4, PPM, world);
        assetSetter.createObject(400, 250, 5, PPM, world);

        // Register all items with the SpawnManager
        for (Item item : assetSetter.objectList) {
            spawnManager.registerItem(item);
        }
    }

    @Override
    public void show() {
        // Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        clearScreen();
        if (!isPaused && !dialogManager.isDialogActive()) {
            updateGame(delta);
            world.step(WORLD_STEP_TIME, 6, 2);

            transparencyUpdateTimer += delta;
            if (transparencyUpdateTimer >= TRANSPARENCY_UPDATE_INTERVAL) {
                transparencyManager.update(player);
                treeTransparencyManager.update(player);
                transparencyUpdateTimer = 0;
            }
        } else {
            player.stopMovement();
        }

        drawGame();

        uiStage.act(delta);
        uiStage.draw();

        checkCollisions();

        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stage.act(delta);
        stage.draw();

        dialogManager.update(delta);
        stage.act(delta);
        stage.draw();

        gameTimer.update(delta);

        Vector2 playerPos = new Vector2(
            player.getBody().getPosition().x * PPM,
            player.getBody().getPosition().y * PPM
        );
        spawnManager.updateItemVisibilities(playerPos);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateGame(float delta) {
        inputHandler.update(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            bulletManager.shootBullet();
        }
        updateCamera();
        bulletManager.update(delta);
        skillManager.update(delta);

        // Update player position based on highground
        Vector2 currentPos = player.getBody().getPosition();
        Vector2 adjustedPos = highgroundManager.updatePosition(currentPos.x * PPM, currentPos.y * PPM);
        adjustedPos = lowgroundManager.updatePosition(adjustedPos.x,adjustedPos.y);
        // Convert back to Box2D coordinates (divide by PPM) and set the body position
        player.getBody().setTransform(adjustedPos.x / PPM, adjustedPos.y / PPM, player.getBody().getAngle());

        // Update item visibility based on player position
        spawnManager.updateItemVisibilities(new Vector2(player.getX(), player.getY()));

        // Update monster spawner and monsters
        monsterSpawnManager.update(delta);
        
        // Update all monsters
        for (AbstractMonster monster : monsters) {
            monster.update(delta, player);
        }
        
        // Remove dead monsters
        for (int i = monsters.size - 1; i >= 0; i--) {
            if (!monsters.get(i).isAlive()) {
                AbstractMonster monster = monsters.removeIndex(i);
                monster.dispose(); // Make sure to clean up resources
                world.destroyBody(monster.getBody());
            }
        }
    }

    private void updateCamera() {
        camera.position.set(
                player.getX() + player.getWidth() / 2,
                player.getY() + player.getHeight() / 2,
                0);
        handleZoom();
        camera.update();
    }

    private void drawGame() {
        OrthogonalTiledMapRenderer renderer = gameMap.getRenderer();
        renderer.setView(camera);
        renderer.render();
        renderer.getBatch().begin();
        skillManager.drawSkills((SpriteBatch) renderer.getBatch());
        assetSetter.drawObject((SpriteBatch) renderer.getBatch());
        player.draw((SpriteBatch) renderer.getBatch(), camera);
        bulletManager.render((SpriteBatch) renderer.getBatch(), camera);

        // Draw monsters
        for (AbstractMonster monster : monsters) {
            monster.render((SpriteBatch) renderer.getBatch());
        }

        renderer.getBatch().end();

        // Draw debug outline
        shapeRenderer.setProjectionMatrix(camera.combined);
        player.drawDebug(shapeRenderer, camera);
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
        gameMap.dispose();
        player.getTexture().dispose();
        world.dispose();
        uiStage.dispose();
        shapeRenderer.dispose();
        stage.dispose();
        gameTimer.dispose();
        inventoryTray.dispose();

        // Dispose monsters
        for (AbstractMonster monster : monsters) {
            monster.dispose();
        }
        monsters.clear();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();

        // Update both UI stages
        uiStage.getViewport().update(width, height, true);
        stage.getViewport().update(width, height, true);

        pauseButton.updatePosition();

        // Log viewport sizes
        Gdx.app.log("Play", String.format("Resize - Window: %dx%d, Stage viewport: %dx%d",
            width, height,
            (int)stage.getViewport().getWorldWidth(),
            (int)stage.getViewport().getWorldHeight()));
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void checkCollisions() {
        if (gameMap.getTiledMap().getLayers().get("warnings") == null) {
            Gdx.app.error("Play", "Warnings layer not found!");
            return;
        }

        if (warningCooldown > 0) {
            warningCooldown -= Gdx.graphics.getDeltaTime();
            return;
        }

        Vector2 playerPos = player.getBody().getPosition();
        com.badlogic.gdx.math.Rectangle playerRect = new com.badlogic.gdx.math.Rectangle(
            playerPos.x * PPM,  // Convert Box2D coordinates to pixels
            playerPos.y * PPM,
            player.getBody().getFixtureList().get(0).getShape().getRadius() * 2 * PPM,  // Use body size
            player.getBody().getFixtureList().get(0).getShape().getRadius() * 2 * PPM
        );

        for (MapObject object : gameMap.getTiledMap().getLayers().get("warnings").getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectObject = (RectangleMapObject) object;
                com.badlogic.gdx.math.Rectangle rect = rectObject.getRectangle();

                if (playerRect.overlaps(rect)) {
                    Gdx.app.log("Play", "Collision detected with warning area");
                    player.stopMovement();
                    dialogManager.showWarningDialog("Anh hen em pickleball", () -> {
                        Gdx.app.log("Play", "Dialog closed callback");
                        player.resetMovement();
                        warningCooldown = WARNING_COOLDOWN_TIME;
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

    // Update your collision listener to use this method
    private void beginContact(Contact contact) {
        Object userDataA = contact.getFixtureA().getBody().getUserData();
        Object userDataB = contact.getFixtureB().getBody().getUserData();

        if (userDataA instanceof Player && userDataB instanceof Item) {
            handleItemCollision((Item) userDataB, contact.getFixtureB());
        } else if (userDataB instanceof Player && userDataA instanceof Item) {
            handleItemCollision((Item) userDataA, contact.getFixtureA());
        }
    }

    private void endContact(Contact contact) {
        Object userDataA = contact.getFixtureA().getBody().getUserData();
        Object userDataB = contact.getFixtureB().getBody().getUserData();

        // Handle transparency zone exits
        if (userDataA instanceof Player) {
            transparencyManager.onPlayerExit(contact.getFixtureB());
        } else if (userDataB instanceof Player) {
            transparencyManager.onPlayerExit(contact.getFixtureA());
        }
    }
}











