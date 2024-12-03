package io.github.HustSavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.ui.PauseButton;
import io.github.HustSavior.utils.GameConfig;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;
import io.github.HustSavior.collision.CollisionBodyFactory;
import io.github.HustSavior.collision.CollisionListener;
import io.github.HustSavior.map.GameMap;
import io.github.HustSavior.map.HighgroundManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.Application;

public class Play implements Screen {
    private final float PPM = GameConfig.PPM;
    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;
    private static final float WORLD_STEP_TIME = 1/60f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GameMap gameMap;
    private final Player player;
    private final InputHandler inputHandler;
    private final World world;
    private Stage uiStage;
    private PauseButton pauseButton;
    private boolean isPaused = false;
    private BuildingTransparencyManager transparencyManager;
    private ShapeRenderer shapeRenderer;
    private CollisionBodyFactory collisionBodyFactory;
    private HighgroundManager highgroundManager;

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
        
        // Add debug logging before creating HighgroundManager
        Gdx.app.log("Play", "=== Map Layers Debug ===");
        if (gameMap.getTiledMap() == null) {
            Gdx.app.error("Play", "TiledMap is null!");
        } else {
            for (MapLayer layer : gameMap.getTiledMap().getLayers()) {
                Gdx.app.log("Play", "Found layer: " + layer.getName());
            }
        }
        
        highgroundManager = new HighgroundManager(gameMap.getTiledMap());
        // Add debug call after initialization
        highgroundManager.debugPrintAreas();
        
        createCollisionBodies();
        
        player = new Player(new Sprite(new Texture("sprites/WalkRight1.png")),
                          500, 500, world);
        inputHandler = new InputHandler(player);
        
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
            gameMap.getLayer("Parking")
        );
        shapeRenderer = new ShapeRenderer();
    }

    private OrthographicCamera setupCamera() {
        OrthographicCamera cam = new OrthographicCamera();
        cam.zoom = INITIAL_ZOOM;
        cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return cam;
    }

    private World setupWorld() {
        World world = new World(new Vector2(0, 0), true);
        world.setContactListener(new CollisionListener());
        return world;
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
            world.step(WORLD_STEP_TIME, 6, 2);
        }
        drawGame();
        
        uiStage.act(delta);
        uiStage.draw();
        
        // Update building transparency
        transparencyManager.update(player);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateGame(float delta) {
        inputHandler.update(delta);
        
        // Update player position based on highground
        Vector2 currentPos = player.getBody().getPosition();
        Vector2 adjustedPos = highgroundManager.updatePosition(currentPos.x * PPM, currentPos.y * PPM);
        // Convert back to Box2D coordinates (divide by PPM) and set the body position
        player.getBody().setTransform(adjustedPos.x / PPM, adjustedPos.y / PPM, player.getBody().getAngle());
        
        updateCamera();
    }

    private void updateCamera() {
        camera.position.set(
            player.getX() + player.getWidth()/2,
            player.getY() + player.getHeight()/2,
            0
        );
        handleZoom();
        camera.update();
    }

    private void drawGame() {
        OrthogonalTiledMapRenderer renderer = gameMap.getRenderer();
        renderer.setView(camera);
        renderer.render();
        renderer.getBatch().begin();
        player.draw((SpriteBatch)renderer.getBatch());
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

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
        uiStage.getViewport().update(width, height, true);
        pauseButton.updatePosition();
    }

    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
}
