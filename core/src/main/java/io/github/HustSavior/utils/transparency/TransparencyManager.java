package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.HustSavior.entities.Player;
import static io.github.HustSavior.utils.GameConfig.PPM;

public abstract class TransparencyManager {
    protected static final float TRANSPARENT_ALPHA = 0.3f;
    protected static final float OPAQUE_ALPHA = 1.0f;
    protected static final float PLAYER_TRANSPARENT_ALPHA = 0.5f;
    protected static final float PLAYER_OPAQUE_ALPHA = 1.0f;
    protected static final String BOUNDS_SUFFIX = "_bounds";
    protected static final short TRANSPARENCY_BOUNDS_BITS = 0x0004;
    protected static final short MONSTER_CATEGORY = 0x0002;
    protected static final short PLAYER_CATEGORY = 0x0001;
    
    protected final TiledMap map;
    protected final World world;
    
    protected TransparencyManager(TiledMap map, World world) {
        this.map = map;
        this.world = world;
    }
    
    public abstract void update(Player player);
    
    protected void updateLayerTransparency(MapLayer layer, float playerX, float playerY, Player player) {
        if (layer == null) return;
        
        String boundsLayerName = layer.getName() + BOUNDS_SUFFIX;
        MapLayer boundsLayer = map.getLayers().get(boundsLayerName);
        
        if (boundsLayer == null) {
            System.out.println("Warning: No bounds layer found for " + layer.getName());
            return;
        }
        
        // Create Box2D fixtures for bounds
        for (RectangleMapObject rectangleObject : boundsLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = rectangleObject.getRectangle();
            // Create fixture with TRANSPARENCY_BOUNDS_BITS
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);
            
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(rect.width / 2 / PPM, rect.height / 2 / PPM);
            
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.isSensor = true;
            fixtureDef.filter.categoryBits = TRANSPARENCY_BOUNDS_BITS;
            fixtureDef.filter.maskBits = MONSTER_CATEGORY | PLAYER_CATEGORY;
            
            body.setUserData(boundsLayerName);
            body.createFixture(fixtureDef);
            shape.dispose();
        }
        
        boolean isInBounds = isPlayerInBounds(playerX, playerY, boundsLayer);
        
        // Update layer transparency
        layer.setOpacity(isInBounds ? TRANSPARENT_ALPHA : OPAQUE_ALPHA);
        
        // Update player transparency
        if (isInBounds) {
            player.setAlpha(PLAYER_TRANSPARENT_ALPHA);
        } else {
            player.setAlpha(PLAYER_OPAQUE_ALPHA);
        }
    }
    
    protected Rectangle getBoundsFromObjectLayer(MapLayer objectLayer) {
        MapObjects objects = objectLayer.getObjects();
        if (objects.getCount() == 0) return new Rectangle();
        
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        
        for (RectangleMapObject rectangleObject : objects.getByType(RectangleMapObject.class)) {
            Rectangle rect = rectangleObject.getRectangle();
            minX = Math.min(minX, rect.x);
            minY = Math.min(minY, rect.y);
            maxX = Math.max(maxX, rect.x + rect.width);
            maxY = Math.max(maxY, rect.y + rect.height);
        }
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
    
    protected boolean isPlayerInBounds(float playerX, float playerY, MapLayer boundsLayer) {
        if (boundsLayer == null) return false;
        
        for (RectangleMapObject rectangleObject : boundsLayer.getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = rectangleObject.getRectangle();
            if (playerX >= rect.x && playerX <= rect.x + rect.width &&
                playerY >= rect.y && playerY <= rect.y + rect.height) {
                return true;
            }
        }
        return false;
    }
} 