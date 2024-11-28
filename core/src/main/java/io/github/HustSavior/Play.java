package io.github.HustSavior;

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
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.skills.SkillManager;
import io.github.HustSavior.utils.CollisionListener;
import io.github.HustSavior.utils.GameConfig;
import items.AssetSetter;

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
    private final AssetSetter assetSetter;
    private final SkillManager skillManager;
    private final World world;

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
        assetSetter= new AssetSetter();
        inputHandler = new InputHandler(player);
        skillManager= new SkillManager(player);
        skillManager.activateSkills(1);
        loadItems();
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
        world.setContactListener(new CollisionListener());
        return world;
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

    private void loadItems(){
        assetSetter.createObject(700, 700, 1, PPM, world);
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
        skillManager.update(delta);
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
        renderer.setView(camera);
        renderer.render();
        renderer.getBatch().begin();
        player.draw((SpriteBatch)renderer.getBatch());
        skillManager.drawSkills((SpriteBatch)renderer.getBatch());
        assetSetter.drawObject((SpriteBatch) renderer.getBatch());
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
