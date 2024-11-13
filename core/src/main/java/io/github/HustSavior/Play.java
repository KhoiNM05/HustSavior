package io.github.HustSavior;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;
import com.badlogic.gdx.math.Polygon;
import io.github.HustSavior.input.InputHandler;
import java.io.IOException;

public class Play implements Screen {
    // for collision
    public static final float PPM = 100.0f;
    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_STATIC = 0x0002;
    public static final short MASK_PLAYER = CATEGORY_STATIC;
    public static final short MASK_STATIC = CATEGORY_PLAYER;


    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private Player player;
    private InputHandler inputHandler;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float mapWidth, mapHeight;

    @Override
    public void show() {
        // Initialize the Box2D World
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        // Set up the contact listener
        world.setContactListener(new CollisionListener());
        // Load the tmx file
        map = new TmxMapLoader().load("map/map.tmx");
        // Get map properties
        MapProperties properties = map.getProperties();
        int tileWidth = properties.get("tilewidth", Integer.class);
        int tileHeight = properties.get("tileheight", Integer.class);
        int mapWidthinTiles = properties.get("width", Integer.class);
        int mapHeightinTiles = properties.get("height", Integer.class);

        //Calculate map dimensions in pixel
        mapWidth = mapWidthinTiles * tileWidth;
        mapHeight = mapHeightinTiles * tileHeight;

        //Initialize the map renderer with the loaded map
        renderer = new OrthogonalTiledMapRenderer(map);
        // Initialize new camera (orthographic)
        camera = new OrthographicCamera();
        camera.zoom = -1.2f;

        // Set the camera to the size of the screen
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
       // initialize player
        player = new Player(new Sprite(new Texture("sprites/player.png")), world, 200,200);

       // initialize the input handler
        inputHandler = new InputHandler(player, mapWidth, mapHeight);

        // set input processor
        Gdx.input.setInputProcessor(inputHandler);

        //parse the collision layer and create box2d bodies
        parseCollisionLayer();
    }
    //parse collision
    private void parseCollisionLayer(){
        // get collisions layer from tmx file
        MapLayer collisionLayer = map.getLayers().get("collisions");
        if(collisionLayer != null){
            // iterate through the layer's set of objects
            for (MapObject object : collisionLayer.getObjects()) {
                // Object is in form of polygons or rectangle
                if(object instanceof RectangleMapObject){
                    Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                    createStaticBody(rectangle);
                }else if (object instanceof PolygonMapObject){
                    Polygon polygon = ((PolygonMapObject) object).getPolygon();
                    createStaticBody(polygon);
                }
            }
        }
    }
    // creating static body
    // for polygon
    private void createStaticBody(Polygon polygon){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(polygon.getX()/PPM, polygon.getY()/PPM);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        // get vertices ( lay dinh cua cac da giac)
        float[] vertices = polygon.getVertices();
        float[] worldVertices = new float[vertices.length];
        for(int i = 0 ; i < vertices.length ; i++){
            worldVertices[i] = vertices[i]/ PPM;
        }
        shape.set(worldVertices);
        if(vertices.length / 2 >= 3 && vertices.length / 2 <= 8){
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = CATEGORY_STATIC;
        fixtureDef.filter.maskBits = MASK_STATIC;

        body.createFixture(fixtureDef);
        } else {
            System.err.println("Invalid number of vertices for polygon");
        }
        shape.dispose();
    }

    // for rectangle (if exists)
    private void createStaticBody(Rectangle rectangle) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((rectangle.x + rectangle.width / 2) / PPM, (rectangle.y + rectangle.height / 2) / PPM);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rectangle.width / 2 / PPM, rectangle.height / 2 / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = CATEGORY_STATIC;
        fixtureDef.filter.maskBits = MASK_STATIC;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    @Override
    public void resize(int width, int height){
        camera.viewportHeight = height;
        camera.viewportWidth = width;
        camera.update();
    }
    @Override
    public void render(float delta){
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Step the physics world
        world.step(1/60f, 6, 2);
        // Update the player's position based on input
        inputHandler.update(delta);
        // Make the camera follow the player
        camera.position.set(player.getX() + player.getWidth()/2, player.getY() + player.getHeight()/2, 0);
        handleZoom();
        camera.update();
        //Update and applying camera
        renderer.setView(camera);
        // Render the map
        renderer.render();
        renderer.getBatch().begin();
        player.draw(renderer.getBatch());
        renderer.getBatch().end();

        //render box2d debug lines;
        debugRenderer.render(world, camera.combined);
    }

    //handling zoom in and zoom out
    private void handleZoom(){
        // press + or =  for zoom in
        if(Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)){
            camera.zoom -= 0.02;
        }
        // press - for zoom out
        if(Gdx.input.isKeyPressed(Input.Keys.MINUS)){
            camera.zoom += 0.02;
        }
        // avoid overzooming
        camera.zoom = Math.max(0.1f, Math.min(camera.zoom,10f));
    }

    @Override






    
    public void dispose(){
        
        // Dispose to free resources
        map.dispose();
        renderer.dispose();
        player.getTexture().dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void hide(){
        // Dispose to free resources
        dispose();
    }

    @Override
    public void pause(){
        // Pause the game
    }

    @Override
    public void resume(){

    }
}
