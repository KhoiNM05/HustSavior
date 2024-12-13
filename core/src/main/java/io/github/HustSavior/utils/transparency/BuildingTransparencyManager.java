package io.github.HustSavior.utils.transparency;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;

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
            Gdx.app.debug("Transparency", "Position is null, skipping update");
            return;
        }

        // Debug all layers at start of update
     
       

        float playerX = position.x;
        float playerY = position.y;
        
        

        // Update each layer
        checkAndUpdateLayer(d3Layer, playerX, playerY, "D3_bounds", "D3");
        checkAndUpdateLayer(d5Layer, playerX, playerY, "D5_bounds", "D5");
        checkAndUpdateLayer(d35Layer, playerX, playerY, "D35_bounds", "D35");
        checkAndUpdateLayer(libraryLayer, playerX, playerY, "Library_bounds", "Library");
        checkAndUpdateLayer(roofLayer, playerX, playerY, "Roof_bounds", "Roof");
        checkAndUpdateLayer(parkingLayer, playerX, playerY, "Parking_bounds", "Parking");

      
    
    }

    private void checkAndUpdateLayer(MapLayer layer, float x, float y, String boundsLayerName, String layerName) {
        if (layer == null) {
            Gdx.app.debug("Transparency", "Layer is null: " + layerName);
            return;
        }

        // Get the bounds layer for checking position
        MapLayer boundsLayer = map.getLayers().get(boundsLayerName);
        if (boundsLayer == null) {
            Gdx.app.debug("Transparency", "Bounds layer not found: " + boundsLayerName);
            return;
        }

        // Get the actual building layer that we want to make transparent
        MapLayer buildingLayer = map.getLayers().get(layerName); // Use layerName without "_bounds"
        if (buildingLayer == null) {
            Gdx.app.debug("Transparency", "Building layer not found: " + layerName);
            return;
        }

        boolean isInBounds = isPlayerInBuildingBounds(x, y, boundsLayer);
        
      

        // Set opacity on the actual building layer
        if (isInBounds) {
            buildingLayer.setOpacity(0.2f);
            
            // If it's a TiledMapTileLayer, update it directly
            if (buildingLayer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) buildingLayer;
                tileLayer.setOpacity(0.2f);
                
                // Force update all cells
                for (int cellX = 0; cellX < tileLayer.getWidth(); cellX++) {
                    for (int cellY = 0; cellY < tileLayer.getHeight(); cellY++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(cellX, cellY);
                        if (cell != null) {
                            cell.setTile(cell.getTile());
                        }
                    }
                }
            }
        } else {
            buildingLayer.setOpacity(1.0f);
        }

        
        
    }

    protected boolean isPlayerInBuildingBounds(float playerX, float playerY, MapLayer boundsLayer) {
        // Keep player coordinates in world units (don't divide by PPM yet)
       

        for (MapObject object : boundsLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectangleObject = (RectangleMapObject) object;
                Rectangle rect = rectangleObject.getRectangle();
                
                // Create a scaled rectangle for comparison
                Rectangle scaledRect = new Rectangle(
                    rect.x,        // Keep original x
                    rect.y,        // Keep original y
                    rect.width,    // Keep original width
                    rect.height    // Keep original height
                );
                
                
                
                // Check if player position (in world units) is within the bounds
                if (playerX >= scaledRect.x && 
                    playerX <= scaledRect.x + scaledRect.width &&
                    playerY >= scaledRect.y && 
                    playerY <= scaledRect.y + scaledRect.height) {
                   
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
    