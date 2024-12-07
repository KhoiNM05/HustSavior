package io.github.HustSavior.utils.transparency;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;

public class BuildingTransparencyManager extends TransparencyManager {
    private final MapLayer d3Layer;
    private final MapLayer d5Layer;
    private final MapLayer d35Layer;
    private final MapLayer libraryLayer;
    private final MapLayer roofLayer;
    private final MapLayer parkingLayer;
    
    public BuildingTransparencyManager(TiledMap map, MapLayer d3Layer, MapLayer d5Layer, 
                                     MapLayer d35Layer, MapLayer libraryLayer,
                                     MapLayer roofLayer, MapLayer parkingLayer) {
        super(map);
        this.d3Layer = d3Layer;
        this.d5Layer = d5Layer;
        this.d35Layer = d35Layer;
        this.libraryLayer = libraryLayer;
        this.roofLayer = roofLayer;
        this.parkingLayer = parkingLayer;
    }
    
    @Override
    public void update(Player player) {
        float playerX = player.getBody().getPosition().x * GameConfig.PPM;
        float playerY = player.getBody().getPosition().y * GameConfig.PPM;
        
        updateLayerTransparency(d3Layer, playerX, playerY);
        updateLayerTransparency(d5Layer, playerX, playerY);
        updateLayerTransparency(d35Layer, playerX, playerY);
        updateLayerTransparency(libraryLayer, playerX, playerY);
        updateLayerTransparency(roofLayer, playerX, playerY);
        updateLayerTransparency(parkingLayer, playerX, playerY);
    }

    public void onPlayerEnter(Fixture fixture) {
        // Add logic for when player enters a building zone
    }

    public void onPlayerExit(Fixture fixture) {
        // Add logic for when player exits a building zone
    }
} 
