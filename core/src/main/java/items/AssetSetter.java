package items;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class AssetSetter {

    ArrayList<Item> objectList;

    public AssetSetter(){
        objectList= new ArrayList<Item>();
    }

    public void createObject(int x, int y, int id, World world){
        switch (id) {
            case 1: objectList.add(new CalcBook(x, y, world));
                    break;
            default:
        }
    }

    public void drawObject(SpriteBatch batch){
        for (int i=0; i<objectList.size(); i++){
            objectList.get(i).draw(batch);
        }
    }
}
