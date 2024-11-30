package io.github.HustSavior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.HustSavior.entities.Bullet;
import io.github.HustSavior.entities.Player;

import io.github.HustSavior.utils.CollisionListener;
import io.github.HustSavior.utils.GameConfig;

public class Play implements Screen {
    private final float PPM = GameConfig.PPM;
    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;
    private static final float WORLD_STEP_TIME = 1/60f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Player player;
    private final InputHandler inputHandler;
    private final World world;
    // Bullet
    private List<Bullet> bullets;
    private float shootCooldown = 0.5f;
    private float timeSinceLastShot = 0;
    // Health bar stat
    private Texture healthBarTexture;
    private static final float HEALTH_BAR_WIDTH = 30f;
    private static final float HEALTH_BAR_HEIGHT = 3f;
    private static final float HEALTH_BAR_OFFSET_X = 6.5f;
    private static final float HEALTH_BAR_OFFSET_Y = 37f;
    // XP bar stat
    private Texture xpBarTexture;
    private static final float XP_BAR_WIDTH = 750f;
    private static final float XP_BAR_HEIGHT = 10f;
    private static final float XP_BAR_OFFSET_X = 25f;
    private static final float XP_BAR_OFFSET_Y = 15f;

    public Play() {
        map = new TmxMapLoader().load("map/map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
        world = setupWorld();
        player = new Player(new Sprite(new Texture("sprites/WalkRight1.png")),
            500, 500, world);
        inputHandler = new InputHandler(player);
        // New list of bullets
        bullets = new ArrayList<>();
        // Health bar and XP bar
        healthBarTexture = new Texture("HP & XP/health_bar.png");
        xpBarTexture = new Texture("HP & XP/xp_bar.png");
        createCollisionBodies();
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
        for (MapObject object : map.getLayers().get("collisions").getObjects()) {
            if (object instanceof RectangleMapObject) {
                createStaticBody((RectangleMapObject) object);
            }else if (object instanceof PolygonMapObject) {
                createStaticBody((PolygonMapObject) object);
            }
        }
    }

    private void createStaticBody(RectangleMapObject rectangleObject) {
        Rectangle rect = rectangleObject.getRectangle();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((rect.x + rect.width / 2)/PPM, (rect.y + rect.height / 2)/PPM);

        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2 / PPM , rect.height / 2 /PPM);

        body.createFixture(shape, 0.0f);
        shape.dispose();
    }
    private void createStaticBody(PolygonMapObject polygonObject) {
        try {
            // Get polygon vertices
            Polygon polygon = polygonObject.getPolygon();
            float[] vertices = polygon.getTransformedVertices();
            Vector2[] worldVertices = new Vector2[vertices.length / 2];

            // Convert vertices to Box2D coordinates
            for (int i = 0; i < vertices.length / 2; i++) {
                worldVertices[i] = new Vector2(
                    vertices[i * 2] / GameConfig.PPM,
                    vertices[i * 2 + 1] / GameConfig.PPM
                );
            }

            // Create body definition
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(0, 0); // Position is already in transformed vertices

            // Create body and shape
            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.set(worldVertices);

            // Create fixture
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0.4f;
            fixtureDef.restitution = 0.0f;

            body.createFixture(fixtureDef);
            shape.dispose();

            Gdx.app.log("Play", "Created polygon body with " + worldVertices.length + " vertices");
        } catch (Exception e) {
            Gdx.app.error("Play", "Failed to create polygon body", e);
        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        clearScreen();
        updateGame(delta);
        drawGame();
        world.step(WORLD_STEP_TIME, 6, 2);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void updateGame(float delta) {
        inputHandler.update(delta);
        timeSinceLastShot += delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            shootBullet();
        }
        updateCamera();
    }

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
        map.dispose();
        renderer.dispose();
        player.getTexture().dispose();
        world.dispose();
        healthBarTexture.dispose();
        xpBarTexture.dispose();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();
    }

    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}


}
