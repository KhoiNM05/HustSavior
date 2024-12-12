package io.github.HustSavior.utils.transparency;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;

public class BuildingTransparencyManager extends TransparencyManager implements Disposable {
    private final MapLayer d3Layer;
    private final MapLayer d5Layer;
    private final MapLayer d35Layer;
    private final MapLayer libraryLayer;
    private final MapLayer roofLayer;
    private final MapLayer parkingLayer;
    private final World world;
    private final List<AbstractMonster> monsters;
    private String playerCurrentBounds = null;
    
    public BuildingTransparencyManager(World world, TiledMap map, MapLayer d3Layer, MapLayer d5Layer, 
                                     MapLayer d35Layer, MapLayer libraryLayer,
                                     MapLayer roofLayer, MapLayer parkingLayer) {
        super(map, world);
        this.world = world;
        this.d3Layer = d3Layer;
        this.d5Layer = d5Layer;
        this.d35Layer = d35Layer;
        this.libraryLayer = libraryLayer;
        this.roofLayer = roofLayer;
        this.parkingLayer = parkingLayer;
        this.monsters = new ArrayList<>();
    }
    
   
    
    public void update(Vector2 position) {
        if (position == null) {
          
            return;
        }

        // Convert position to world coordinates (multiply by PPM since position is in physics units)
        float playerX = position.x * GameConfig.PPM;
        float playerY = position.y * GameConfig.PPM;
        
      
      

     
     
        
        // Update each building layer based on its corresponding bounds layer
        checkAndUpdateLayer(d3Layer, playerX, playerY, "D3_bounds", "D3");
        checkAndUpdateLayer(d5Layer, playerX, playerY, "D5_bounds", "D5");
        checkAndUpdateLayer(d35Layer, playerX, playerY, "D35_bounds", "D35");
        checkAndUpdateLayer(libraryLayer, playerX, playerY, "Library_bounds", "Library");
        checkAndUpdateLayer(roofLayer, playerX, playerY, "Roof_bounds", "Roof");
        checkAndUpdateLayer(parkingLayer, playerX, playerY, "Parking_bounds", "Parking");
    }

    private void checkAndUpdateLayer(MapLayer layer, float x, float y, String boundsLayerName, String layerName) {
        // First check if the layer exists
        if (layer == null) {
          
            return;
        }

        // Get and check the bounds layer
        MapLayer boundsLayer = map.getLayers().get(boundsLayerName);
        if (boundsLayer == null) {
            
            return;
        }

      
       

        // Check if player is in bounds
        boolean isInBounds = isPlayerInBuildingBounds(x, y, boundsLayer);
        
        // Set layer opacity based on player position
        float currentOpacity = layer.getOpacity();
        float targetOpacity = isInBounds ? TRANSPARENT_ALPHA : OPAQUE_ALPHA;
        
        if (Math.abs(currentOpacity - targetOpacity) > 0.01f) {
            layer.setOpacity(targetOpacity);
           
        }
    }

    protected boolean isPlayerInBuildingBounds(float playerX, float playerY, MapLayer boundsLayer) {
        // Convert player position from physics units to pixel units
        float playerXInPixels = playerX / GameConfig.PPM;
        float playerYInPixels = playerY / GameConfig.PPM;

        for (MapObject object : boundsLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectangleObject = (RectangleMapObject) object;
                Rectangle rect = rectangleObject.getRectangle();
                
                // Debug the bounds check
         
                
                if (rect.contains(playerXInPixels, playerYInPixels)) {
                
                    return true;
                }
            }
        }
        return false;
    }

    public void onPlayerEnter(Fixture fixture) {
        // Add logic for when player enters a building zone
    }

    public void onPlayerExit(Fixture fixture) {
        // Add logic for when player exits a building zone
    }

    public void onPlayerEnterBounds(Fixture boundsFixture) {
        String layerName = (String) boundsFixture.getBody().getUserData();
        playerCurrentBounds = layerName;
        MapLayer layer = map.getLayers().get(layerName.replace("_bounds", ""));
        if (layer != null) {
            layer.setOpacity(TRANSPARENT_ALPHA);
        }
        
        // Make monsters in the same bounds visible
        for (AbstractMonster monster : monsters) {
            if (layerName.equals(monster.getCurrentBoundsLayer())) {
                monster.setVisible(true);
            }
        }
    }
    
    public void onPlayerExitBounds(Fixture boundsFixture) {
        String layerName = (String) boundsFixture.getBody().getUserData();
        playerCurrentBounds = null;
        MapLayer layer = map.getLayers().get(layerName.replace("_bounds", ""));
        if (layer != null) {
            layer.setOpacity(OPAQUE_ALPHA);
        }
        
        // Hide monsters in the exited bounds
        for (AbstractMonster monster : monsters) {
            if (layerName.equals(monster.getCurrentBoundsLayer())) {
                monster.setVisible(false);
            }
        }
    }
    
    public void onMonsterEnterBounds(AbstractMonster monster, Fixture boundsFixture) {
        String layerName = (String) boundsFixture.getBody().getUserData();
        Gdx.app.log("Transparency", "Monster entering bounds: " + layerName);
        monster.setCurrentBoundsLayer(layerName);
        boolean shouldBeVisible = layerName.equals(playerCurrentBounds);
        monster.setVisible(shouldBeVisible);
        Gdx.app.log("Transparency", "Monster entering bounds: " + layerName + ", visible: " + shouldBeVisible);
    }
    
    public void onMonsterExitBounds(AbstractMonster monster, Fixture boundsFixture) {
        Gdx.app.log("Transparency", "Monster exiting bounds");
        monster.setCurrentBoundsLayer(null);
        monster.setVisible(true);
    }
    
    public void addMonster(AbstractMonster monster) {
        monsters.add(monster);
    }

    @Override
    public void dispose() {
        // Clean up any resources
    }


    public void update(Player player) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
} 
    