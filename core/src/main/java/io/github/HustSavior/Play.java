package io.github.HustSavior;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.Screen;
import io.github.HustSavior.entities.Player;
import net.dermetfan.gdx.physics.box2d.PositionController;

public class Play implements Screen {
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private Player player;
    private InputHandler inputHandler;
    @Override
    public void show() {
        // Load the tmx file
        map = new TmxMapLoader().load("map/map.tmx");

        //Initialize the map renderer with the loaded map
        renderer = new OrthogonalTiledMapRenderer(map);
        // Initialize new camera (orthographic)
        camera = new OrthographicCamera();
        camera.zoom = -1.2f;
        // Set the camera to the size of the screen
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        player = new Player(new Sprite(new Texture("sprites/WalkRight1.png")), 500,500);
        inputHandler = new InputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);
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


    }

    //handling zoom in and zoom out
    private void handleZoom(){
        if(Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)){
            camera.zoom -= 0.02;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.MINUS)){
            camera.zoom += 0.02;
        }

        camera.zoom = Math.max(0.1f, Math.min(camera.zoom,10f));
    }

    @Override
    public void dispose(){
        // Dispose to free resources
        map.dispose();
        renderer.dispose();
        player.getTexture().dispose();
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
