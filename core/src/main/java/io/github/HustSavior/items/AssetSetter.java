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
        System.out.println("Creating item with ID: " + id + " at position: " + x + "," + y);
        Item newItem = null;
        
        switch (id) {
            case 1:
                newItem = new CalcBook(new Sprite(new Texture("item/calculus1.jpg")), x, y);
                break;
            case 2:
                newItem = new AlgebraBook(new Sprite(new Texture("item/algebra.jpg")), x, y);
                break;
            case 3:
                newItem = new PhysicBook(new Sprite(new Texture("item/physic1.jpg")), x, y);
                break;
            case 4:
                newItem = new HPPotion(new Sprite(new Texture("item/hp_potion.png")), x, y);
                break;
            case 5:
                newItem = new Shield(new Sprite(new Texture("item/shield.png")), x, y);
                break;
        }
        
        if (newItem != null) {
            newItem.setVisible(true);  // Ensure item starts visible
            objectList.add(newItem);
            System.out.println("Added item to list. New size: " + objectList.size());
        } else {
            System.out.println("Failed to create item with ID: " + id);
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
        System.out.println("Drawing items. Total items: " + objectList.size());
        for (Item item : objectList) {
            System.out.println("Item position: " + item.getX() + "," + item.getY() + 
                             " Visible: " + item.isVisible() + 
                             " Collected: " + item.isCollected());
            item.draw(batch);  // Draw regardless of visibility for testing
        }
    }

    public List<Item> getItems() {
        return objectList;
    }

    public void updateItemVisibility(Vector2 playerPos, TiledMap map) {
        for (Item item : objectList) {
            Vector2 itemPos = new Vector2(item.getX(), item.getY());
            boolean isInBoundsLayer = false;
            boolean shouldBeVisible = true;

            System.out.println("Checking item at: " + itemPos.x + "," + itemPos.y);
            System.out.println("Player at: " + playerPos.x + "," + playerPos.y);
            
            // Check if item is in any _bounds layer
            for (MapLayer layer : map.getLayers()) {
                String layerName = layer.getName();
                if (layerName.endsWith("_bounds")) {
                    for (MapObject object : layer.getObjects()) {
                        if (object instanceof RectangleMapObject) {
                            Rectangle bounds = ((RectangleMapObject) object).getRectangle();
                            if (bounds.contains(itemPos.x, itemPos.y)) {
                                isInBoundsLayer = true;
                                shouldBeVisible = bounds.contains(playerPos.x, playerPos.y);
                                System.out.println("Item in bounds layer: " + layerName);
                                System.out.println("Player in same bounds: " + shouldBeVisible);
                                break;
                            }
                        }
                    }
                }
                if (isInBoundsLayer) break;
            }

            // If item is not in any _bounds layer, it should always be visible
            if (!isInBoundsLayer) {
                shouldBeVisible = true;
                System.out.println("Item not in any bounds layer, setting visible");
            }
            
            item.setVisible(shouldBeVisible);
            System.out.println("Final item visibility: " + shouldBeVisible);
        }
    }

    public Item checkCollisions(Rectangle playerBounds) {
        for (Item item : objectList) {
            if (item.checkCollision(playerBounds)) {
                return item;
            }
        }
        return null;
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

    public List<Item> getObjectList() {
        return objectList;
    }
}
    