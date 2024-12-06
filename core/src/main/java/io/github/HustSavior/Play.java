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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.HustSavior.bullet.Bullet;
import io.github.HustSavior.bullet.BulletManager;
import io.github.HustSavior.collision.CollisionBodyFactory;
import io.github.HustSavior.collision.CollisionListener;
import io.github.HustSavior.entities.MonsterBehavior;
import io.github.HustSavior.entities.NormalMonster;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.map.GameMap;
import io.github.HustSavior.map.HighgroundManager;
import io.github.HustSavior.ui.PauseButton;
import io.github.HustSavior.utils.GameConfig;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;

public class Play implements Screen {
    private final float PPM = GameConfig.PPM;
//    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;
    private static final float WORLD_STEP_TIME = 1 / 60f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameMap gameMap;
    private final Player player;
    private BulletManager bulletManager;
    private final InputHandler inputHandler;
    private final World world;

    private Stage uiStage;
    private PauseButton pauseButton;
    private boolean isPaused = false;
    private BuildingTransparencyManager transparencyManager;
    private ShapeRenderer shapeRenderer;
    private CollisionBodyFactory collisionBodyFactory;
    private HighgroundManager highgroundManager;

    private Array<MonsterBehavior> monsters = new Array<>();
    private NormalMonster normalMonster = null;
    private SpriteBatch batch;


    public Play(Game game) {
        // Set logging level to show debug messages
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();

        world = setupWorld();
        collisionBodyFactory = new CollisionBodyFactory(world, PPM);
        
        try {
            gameMap = new GameMap("map/map.tmx", collisionBodyFactory);
            Gdx.app.log("GameMap", "Map loaded successfully.");
            
            player = new Player(new Sprite(new Texture("sprites/WalkRight1.png")), 500, 500, world);
            inputHandler = new InputHandler(player);
            bulletManager = new BulletManager(world, player);
            normalMonster = new NormalMonster("sprites/blueMonster.png", 300, 300, world);
            normalMonster.createBody(world);
            
        } catch (Exception e) {
            Gdx.app.error("GameMap", "Could not load map.", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize game", e);
        }

        // Add debug logging for map layers
        Gdx.app.log("Play", "=== Map Layers Debug ===");
        for (MapLayer layer : gameMap.getTiledMap().getLayers()) {
            Gdx.app.log("Play", "Found layer: " + layer.getName());
        }

        highgroundManager = new HighgroundManager(gameMap.getTiledMap());
        highgroundManager.debugPrintAreas();

        createCollisionBodies();

        // Add UI stage and pause button
        uiStage = new Stage(new ScreenViewport());
        pauseButton = new PauseButton(uiStage, game, this);
        uiStage.addActor(pauseButton);

        // Add UI stage to input multiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        // Initialize transparency manager with map layers
        transparencyManager = new BuildingTransparencyManager(
            gameMap.getTiledMap(),
            gameMap.getLayer("D3"),
            gameMap.getLayer("D5"),
            gameMap.getLayer("D35"),
            gameMap.getLayer("Library"),
            gameMap.getLayer("Roof"),
            gameMap.getLayer("Parking"));
        shapeRenderer = new ShapeRenderer();

        // Spawn initial monsters
        spawnMonsters(5, 100);
    }

//    private OrthographicCamera setupCamera() {
//        OrthographicCamera cam = new OrthographicCamera();
//        cam.zoom = INITIAL_ZOOM;
//        cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        return cam;
//    }

    private World setupWorld() {
        World world = new World(new Vector2(0, 0), true);
        world.setContactListener(new CollisionListener() {
            @Override
            public void beginContact(Contact contact) {
                super.beginContact(contact);
                // Manage collision for bullet
                handleBulletCollision(contact);
            }
        });
        return world;
    }

    private boolean isValidSpawnLocation(float x, float y) {
        MapLayer collisionLayer = gameMap.getTiledMap().getLayers().get("collisions");
        if (collisionLayer == null) {
            return true; // Nếu không có layer `collisions`, mọi vị trí đều hợp lệ
        }

        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectangle = (RectangleMapObject) object;
                if (rectangle.getRectangle().contains(x, y)) {
                    return false; // Vị trí nằm trong khu vực va chạm
                }
            } else if (object instanceof PolygonMapObject) {
                PolygonMapObject polygon = (PolygonMapObject) object;
                if (polygon.getPolygon().contains(x, y)) {
                    return false; // Vị trí nằm trong khu vực va chạm
                }
            }
        }

        return true; // Không nằm trong khu vực va chạm
    }

    private void spawnMonsters(int numberOfMonsters, float spawnRadius) {
        for (int i = 0; i < numberOfMonsters; i++) {
            int maxAttempts = 5; // Số lần thử tối đa
            boolean validLocationFound = false;

            float x = 0;
            float y = 0;

            for (int attempts = 0; attempts < maxAttempts; attempts++) {
                // Tạo vị trí ngẫu nhiên trong vòng tròn xung quanh người chơi
                float angle = MathUtils.random(0, 360);
                x = player.getX() + MathUtils.cosDeg(angle) * spawnRadius;
                y = player.getY() + MathUtils.sinDeg(angle) * spawnRadius;

                // Kiểm tra vị trí có hợp lệ không
                if (isValidSpawnLocation(x, y)) {
                    validLocationFound = true;
                    break; // Thoát vòng lặp khi tìm được vị trí hợp lệ
                }
            }

            // Nếu tìm được vị trí hợp lệ, tạo quái vật
            if (validLocationFound) {
                NormalMonster monster = new NormalMonster("sprites/blueMonster.png", x, y, world);
                monster.createBody(world);
                monsters.add(monster); // Thêm vào danh sách quản lý
            }
        }
    }

    private void updateMonsters(float delta) {
        for (int i = monsters.size - 1; i >= 0; i--) { // Duyệt ngược để loại bỏ quái chết
            MonsterBehavior monster = monsters.get(i);

            if (!monster.isAlive()) {
                // Xóa quái vật nếu đã chết
                monsters.removeIndex(i);
                continue;
            }

            // Cập nhật hành vi quái vật
            monster.update(delta, player);
        }
    }


    private void handleBulletCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        if (userDataA instanceof Bullet) {
            ((Bullet) userDataA).incrementCollisionCount();
        } else if (userDataB instanceof Bullet) {
            ((Bullet) userDataB).incrementCollisionCount();
        }
    }

    private void createCollisionBodies() {
        if (gameMap.getTiledMap().getLayers().get("collisions") == null) {
            Gdx.app.error("Play", "Collisions layer not found in map!");
            return;
        }

        for (MapObject object : gameMap.getTiledMap().getLayers().get("collisions").getObjects()) {
            if (object instanceof RectangleMapObject) {
                collisionBodyFactory.createStaticBody((RectangleMapObject) object);
            } else if (object instanceof PolygonMapObject) {
                collisionBodyFactory.createStaticBody((PolygonMapObject) object);
            }

        }
    }

    @Override
    public void show() {
        // Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        clearScreen();

        if (!isPaused) {
            updateGame(delta);
            updateMonsters(delta);
            world.step(WORLD_STEP_TIME, 6, 2);
        }

        batch.setProjectionMatrix(camera.combined); // Đồng bộ hóa camera với batch
        drawMonster(); // Vẽ quái vật
        drawGame();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawMonster() {
        batch.begin(); // Bắt đầu vẽ
        for (MonsterBehavior monster : monsters) {
            if (monster instanceof NormalMonster) {
                ((NormalMonster) monster).draw(batch); // Vẽ từng quái vật
            }
        }
        batch.end(); // Kết thúc vẽ
    }

    private void updateGame(float delta) {
        inputHandler.update(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            bulletManager.shootBullet();
        }
        updateCamera();
        bulletManager.update(delta);

        // Update player position based on highground
        Vector2 currentPos = player.getBody().getPosition();
        Vector2 adjustedPos = highgroundManager.updatePosition(currentPos.x * PPM, currentPos.y * PPM);
        player.getBody().setTransform(adjustedPos.x / PPM, adjustedPos.y / PPM, player.getBody().getAngle());

        // Cập nhật độ trong suốt của building
        transparencyManager.update(player);
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
        player.draw((SpriteBatch) renderer.getBatch(), camera);
        bulletManager.render((SpriteBatch) renderer.getBatch(), camera);
        renderer.getBatch().end();

        // Draw debug outline
        shapeRenderer.setProjectionMatrix(camera.combined);
        player.drawDebug(shapeRenderer);
    }

    private void handleZoom() {
        if (Gdx.input.isKeyPressed(Input.Keys.PLUS)) {
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
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
        uiStage.getViewport().update(width, height, true);
        pauseButton.updatePosition();
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

}
