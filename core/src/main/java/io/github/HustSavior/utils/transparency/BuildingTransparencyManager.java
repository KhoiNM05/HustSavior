package io.github.HustSavior.utils.transparency;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;

public class BuildingTransparencyManager extends TransparencyManager {
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
    
    @Override
    public void update(Player player) {
        float playerX = player.getBody().getPosition().x * GameConfig.PPM;
        float playerY = player.getBody().getPosition().y * GameConfig.PPM;
        
        updateLayerTransparency(d3Layer, playerX, playerY, player);
        updateLayerTransparency(d5Layer, playerX, playerY, player);
        updateLayerTransparency(d35Layer, playerX, playerY, player);
        updateLayerTransparency(libraryLayer, playerX, playerY, player);
        updateLayerTransparency(roofLayer, playerX, playerY, player);
        updateLayerTransparency(parkingLayer, playerX, playerY, player);
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
} 
    