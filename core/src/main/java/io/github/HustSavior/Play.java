package io.github.HustSavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        world.setContactListener(new CollisionListener() {
            @Override
            public void beginContact(Contact contact) {
                super.beginContact(contact);
                // Set collisionCount for bullet
                handleBulletCollision(contact);
            }
        });
        return world;
    }

    private void handleBulletCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if (fixtureA.getBody().getUserData() instanceof Bullet) {
            ((Bullet) fixtureA.getBody().getUserData()).incrementCollisionCount();
        } else if (fixtureB.getBody().getUserData() instanceof Bullet) {
            ((Bullet) fixtureB.getBody().getUserData()).incrementCollisionCount();
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
        


    private void shootBullet() {
        if (timeSinceLastShot < shootCooldown) {
            return;
        }
        float bulletSpeed = 5;
        float bulletWidth = 12;
        float bulletHeight = 5;
        int numAngles = 6;
        int bulletsPerAngle = 2;
        float angleStep = 360f / numAngles;
        float bulletSpacing = 100;
        float radius = 20;
        float angleRandom = (float) (Math.random() * 360);

        for (int i = 0; i < numAngles; i++) {
            float angle = i * angleStep + angleRandom;
            float radians = (float) Math.toRadians(angle);
            float dx = (float) Math.cos(radians);
            float dy = (float) Math.sin(radians);

            for (int j = 0; j < bulletsPerAngle; j++) {
                float offsetX = j * bulletSpacing * dx;
                float offsetY = j * bulletSpacing * dy;

                Bullet bullet = new Bullet(world, player.getX() + player.getWidth() / 2 + offsetX + radius * dx - 6,
                    player.getY() + player.getHeight() / 2 + offsetY + radius * dy - 3, bulletSpeed * dx,
                    bulletSpeed * dy, bulletWidth, bulletHeight
                );
                bullet.setRotation((float) Math.toDegrees(Math.atan2(dy, dx))); // Set the rotation of the bullet based on its velocity
                bullets.add(bullet);
            }
        }
        timeSinceLastShot = 0;
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
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(Gdx.graphics.getDeltaTime());
            bullet.render((SpriteBatch) renderer.getBatch());
            // can change 4 to another number
            if (bullet.getPosition().x < camera.position.x - camera.viewportWidth / 2 ||
                bullet.getPosition().x > camera.position.x + camera.viewportWidth / 2 ||
                bullet.getPosition().y < camera.position.y - camera.viewportHeight / 2 ||
                bullet.getPosition().y > camera.position.y + camera.viewportHeight / 2 ||
                bullet.getCollisionCount() >= 5 /*Can collide 5 times before disappear*/) {
                iterator.remove();
            }
        }
        SpriteBatch batch = (SpriteBatch) renderer.getBatch();
        float healthPercentage = player.getHealth() / player.getMaxHealth();
        // Draw health bar
        float healthBarX = player.getX() - HEALTH_BAR_OFFSET_X;
        float healthBarY = player.getY() - player.getHeight() + HEALTH_BAR_OFFSET_Y;
        // Draw health bar border
        batch.setColor(0, 0, 0, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY - 1, HEALTH_BAR_WIDTH + 2, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY + HEALTH_BAR_HEIGHT, HEALTH_BAR_WIDTH + 2, 1);
        batch.draw(healthBarTexture, healthBarX - 1, healthBarY - 1, 1, HEALTH_BAR_HEIGHT + 2);
        batch.draw(healthBarTexture, healthBarX + HEALTH_BAR_WIDTH, healthBarY - 1, 1, HEALTH_BAR_HEIGHT + 2);
        batch.setColor(1, 1, 1, 1);
        batch.draw(healthBarTexture, healthBarX, healthBarY, HEALTH_BAR_WIDTH * healthPercentage, HEALTH_BAR_HEIGHT);
        float xpPercentage = player.getXp() / player.getMaxXp();
        float xpBarX = camera.position.x - camera.viewportWidth / 2 + XP_BAR_OFFSET_X;
        float xpBarY = camera.position.y + camera.viewportHeight / 2 - XP_BAR_OFFSET_Y;
        // Draw XP bar border
        batch.setColor(0, 0, 0, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY - 1, XP_BAR_WIDTH + 2, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY + XP_BAR_HEIGHT, XP_BAR_WIDTH + 2, 1);
        batch.draw(xpBarTexture, xpBarX - 1, xpBarY - 1, 1, XP_BAR_HEIGHT + 2);
        batch.draw(xpBarTexture, xpBarX + XP_BAR_WIDTH, xpBarY - 1, 1, XP_BAR_HEIGHT + 2);
        batch.setColor(1, 1, 1, 1); // Reset color to white
        batch.draw(xpBarTexture, xpBarX, xpBarY, XP_BAR_WIDTH * xpPercentage, XP_BAR_HEIGHT);
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
