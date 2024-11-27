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
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.CollisionListener;
import io.github.HustSavior.skills.Calculus;
import items.AssetSetter;

public class Play implements Screen {
    private static final float INITIAL_ZOOM = -1.2f;
    private static final float ZOOM_SPEED = 0.02f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10f;
    private static final float WORLD_STEP_TIME = 1/60f;

    private final OrthographicCamera camera;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Player player;
    private final InputHandler inputHandler;
    private final World world;
    private final AssetSetter assetSetter;

    private final Calculus calculus;

    public Play() {
        map = new TmxMapLoader().load("map/map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);
        camera = setupCamera();
        world = setupWorld();
        player = new Player(new Sprite(new Texture("sprites/WalkRight1.png")),
                          500, 500, world);
        calculus= new Calculus(new Sprite(new Texture("skills/parabol7.png")), player);
        inputHandler = new InputHandler(player);
        assetSetter= new AssetSetter();
        assetSetter.createObject(700, 700, 1, world);
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
            }
        }
    }

    private void createStaticBody(RectangleMapObject rectangleObject) {
        Rectangle rect = rectangleObject.getRectangle();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(rect.x + rect.width / 2, rect.y + rect.height / 2);

        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2, rect.height / 2);

        body.createFixture(shape, 0.0f);
        shape.dispose();
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
        calculus.update(delta);
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
        if(calculus.isReady()) calculus.draw((SpriteBatch)renderer.getBatch());
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
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}
}
