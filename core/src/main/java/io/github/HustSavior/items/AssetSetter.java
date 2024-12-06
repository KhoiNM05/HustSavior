package io.github.HustSavior.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class AssetSetter {

    public ArrayList<Item> objectList;

    public AssetSetter(){
        objectList= new ArrayList<Item>();
    }

    public void createObject(int x, int y, int id, float PPM, World world){
        switch (id) {
            case 1:
                objectList.add(new CalcBook(new Sprite(new Texture("item/calculus1.jpg")), x, y, PPM, world));
                break;
            case 2:
                objectList.add(new AlgebraBook(new Sprite(new Texture("item/algebra.jpg")), x, y, PPM, world));
                break;
            case 3:
                objectList.add(new PhysicBook(new Sprite(new Texture("item/physic1.jpg")), x, y, PPM, world));
                break;
            case 4:
                objectList.add(new HPPotion(new Sprite(new Texture("item/hp_potion.png")), x, y, PPM, world));
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
}
