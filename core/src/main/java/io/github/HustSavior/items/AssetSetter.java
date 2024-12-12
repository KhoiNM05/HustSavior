package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;

import static io.github.HustSavior.utils.GameConfig.PPM;

public class AssetSetter implements Disposable {

    private List<Item> objectList = new ArrayList<>();

    public AssetSetter(){
        objectList= new ArrayList<Item>();
    }

    public void createObject(int x, int y, int id) {
        switch (id) {
            case 1:
                objectList.add(new CalcBook(new Sprite(new Texture("item/calculus1.jpg")), x, y));
                break;
            case 2:
                objectList.add(new AlgebraBook(new Sprite(new Texture("item/algebra.jpg")), x, y));
                break;
            case 3:
                objectList.add(new PhysicBook(new Sprite(new Texture("item/physic1.jpg")), x, y));
                break;
            case 4:
                objectList.add(new HPPotion(new Sprite(new Texture("item/hp_potion.png")), x, y));
                break;
            case 5:
                objectList.add(new Shield(new Sprite(new Texture("item/shield.png")), x, y));
                break;
            default:
        }
    }

    public void objectAcquired(Item item){
        objectList.remove(item);
    }

    public void drawObject(SpriteBatch batch){
        for (int i=0; i<objectList.size(); i++){
            objectList.get(i).draw(batch);
        }
    }
    public void drawVisibleObjects(SpriteBatch batch, Rectangle viewBounds) {
        for (Item item : objectList) {
            if (!item.isCollected() && item.isVisible() && viewBounds.contains(item.getBounds().x, item.getBounds().y)) {
                item.draw(batch);
            }
        }
    }

    public List<Item> getItems() {
        return objectList;
    }

    public void updateItemVisibility(Vector2 playerPos, TiledMap map) {
        String[] boundsLayers = {"D3_bounds", "D5_bounds", "D35_bounds", "Library_bounds", "Roof_bounds", "Parking_bounds"};
        
        for (Item item : objectList) {
            boolean shouldBeVisible = true;
            Vector2 itemPos = new Vector2(item.getX(), item.getY());
            
            for (String layerName : boundsLayers) {
                MapLayer boundsLayer = map.getLayers().get(layerName);
                if (boundsLayer != null) {
                    for (MapObject object : boundsLayer.getObjects()) {
                        if (object instanceof RectangleMapObject) {
                            Rectangle bounds = ((RectangleMapObject) object).getRectangle();
                            if (bounds.contains(itemPos.x, itemPos.y)) {
                                shouldBeVisible = bounds.contains(playerPos.x, playerPos.y);
                                break;
                            }
                        }
                    }
                }
            }
            
            item.setVisible(shouldBeVisible);
        }
    }

    @Override
    public void dispose() {
        for (Item item : objectList) {
            if (item != null) {
                item.dispose();
            }
        }
        objectList.clear();
    }
}
