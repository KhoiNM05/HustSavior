package io.github.HustSavior;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.HustSavior.entities.NormalMonster;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.CollisionListener;
import io.github.HustSavior.utils.GameConfig;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;

public class Play implements Screen {
    private static final float PPM = GameConfig.PPM;
    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;
    private static final float WORLD_STEP_TIME = 1 / 60f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Player player;
    private final NormalMonster normalMonster;
    private final World world;
    private final InputHandler inputHandler;

    private final Array<NormalMonster> monsters = new Array<>();
    public Play() {
        // Load map
        map = new TmxMapLoader().load("map/map.tmx");
        if (map == null) {
            throw new RuntimeException("Failed to load map");
        }
        renderer = new OrthogonalTiledMapRenderer(map);

        // Setup camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT, camera);
        camera.position.set(GameConfig.GAME_WIDTH / 2, GameConfig.GAME_HEIGHT / 2, 0);
        camera.update();

        // Setup physics world
        world = setupWorld();

        // Initialize player
        player = new Player("sprites/WalkRight1.png", 500, 500, world);
        if (player == null || !player.isTextureLoaded()) {
            Gdx.app.error("Play", "Player texture loading failed. Check file paths.");
            throw new RuntimeException("Failed to initialize player");
        }

        // Initialize monster with sprite sheet
        normalMonster = new NormalMonster(300, 300, 100, 10, 5, "sprites/blueMonster.png");
        if (normalMonster == null) {
            throw new RuntimeException("Failed to initialize monster");
        }
        normalMonster.createBody(world);
        inputHandler = new InputHandler(player);

        // Setup collision bodies from map
        createCollisionBodies();
        spawnMonsters(5);
    }

    private World setupWorld() {
        World world = new World(new Vector2(0, 0), true);
        world.setContactListener(new CollisionListener());
        return world;
    }

    private void spawnMonsters(int count) {
        float radius = 200f; // Bán kính vòng tròn spawn quái vật
        for (int i = 0; i < count; i++) {
            // Tính vị trí x, y dựa trên góc
            float angle = MathUtils.random(0, 360);
            float x = player.getX() + MathUtils.cosDeg(angle) * radius;
            float y = player.getY() + MathUtils.sinDeg(angle) * radius;

            // Tạo quái vật
            NormalMonster monster = new NormalMonster(x, y, 100, 10, 5, "sprites/blueMonster.png");
            monster.createBody(world); // Tạo body vật lý
            monsters.add(monster); // Thêm vào danh sách quản lý
        }
    }

    private void createCollisionBodies() {
        // Lấy object group từ map
        MapObjects objects = map.getLayers().get("collisions").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                createStaticBody((RectangleMapObject) object); // Xử lý Rectangle
            } else if (object instanceof PolygonMapObject) {
                createStaticBody((PolygonMapObject) object); // Xử lý Polygon
            } else if (object.getProperties().containsKey("gid")) {
                Gdx.app.log("Play", "Skipping TileMap Object with gid: " + object.getProperties().get("gid"));
            } else {
                Gdx.app.log("Play", "Skipping unsupported object type: " + object.getClass().getName());
            }
        }
    }

    private void createStaticBody(RectangleMapObject rectangleObject) {
        Rectangle rect = rectangleObject.getRectangle();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((rect.x + rect.width / 2) / PPM, (rect.y + rect.height / 2) / PPM);

        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2 / PPM, rect.height / 2 / PPM);

        body.createFixture(shape, 0.0f);
        shape.dispose();
    }

    private void createStaticBody(PolygonMapObject polygonObject) {
        try {
            Polygon polygon = polygonObject.getPolygon();
            float[] vertices = polygon.getTransformedVertices();
            Vector2[] worldVertices = new Vector2[vertices.length / 2];
            for (int i = 0; i < vertices.length / 2; i++) {
                worldVertices[i] = new Vector2(vertices[i * 2] / PPM, vertices[i * 2 + 1] / PPM);
            }

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;

            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.set(worldVertices);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1.0f;

            body.createFixture(fixtureDef);
            shape.dispose();
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
        player.update(delta);
        for (NormalMonster monster : monsters) {
            monster.update(delta, player);
        }
        updateCamera();
    }

    private void updateCamera() {
        camera.position.set(
            player.getX() + player.getWidth() / 2,
            player.getY() + player.getHeight() / 2,
            0
        );
        handleZoom();
        camera.update();
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

    private void drawGame() {
        if (renderer == null || renderer.getBatch() == null) {
            Gdx.app.error("Play", "Renderer or batch is null");
            return;
        }
        renderer.setView(camera);
        renderer.render();
        renderer.getBatch().begin();

        // Vẽ người chơi
        if (player != null) {
            player.draw(renderer.getBatch());
        }

        // Draw monsters with null checks
        for (NormalMonster monster : monsters) {
            if (monster != null) {
                monster.draw(renderer.getBatch());
            }
        }

        renderer.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        player.dispose();
        normalMonster.dispose();
        world.dispose();
    }
}
