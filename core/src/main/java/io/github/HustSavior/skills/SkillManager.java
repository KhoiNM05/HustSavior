package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import io.github.HustSavior.entities.Player;

import java.util.ArrayList;

public class SkillManager {
    private final static int MELEE=1;
    //private final static int RANGED=2;
    private final static int SHIELD=2;
    Player player;
    private World world;
    ArrayList<Skills> skillList;
    public SkillManager(Player player, World world){
        this.player=player;
        this.world=world;
        skillList= new ArrayList<Skills>();
    }

    public void activateSkills(int id){
        if (id==MELEE){
            skillList.add(new Slash(new Sprite(new Texture("skills/Slash1.png")),player, world));
        }
        else if(id==SHIELD){
            skillList.add(new Shield(new Sprite(new Texture("item/shield.png")), player, world));
            System.out.println("activate");
        }
    }

    public void update(float delta){
        for (int i=0; i<skillList.size(); i++){
                skillList.get(i).update(delta);

        }
    }

    public void drawSkills(SpriteBatch batch){
        for (int i=0; i<skillList.size(); i++){
            if (skillList.get(i).isReady()){
                skillList.get(i).draw(batch);
            }
        }
    }

    public void applyBuff(int id){

    }
}
