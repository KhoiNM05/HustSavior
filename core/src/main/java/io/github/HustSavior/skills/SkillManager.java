package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.HustSavior.entities.Player;

import java.util.ArrayList;

public class SkillManager {
    private final static int MELEE=1;
    private final static int RANGED=2;
    Player player;
    ArrayList<Calculus> skillList;
    public SkillManager(Player player){
        this.player=player;
        skillList= new ArrayList<Calculus>();
    }

    public void activateSkills(int id){
        if (id==MELEE){
            skillList.add(new Calculus(new Sprite(new Texture("skills/parabol7.png")),player));
        }
    }

    public void update(float delta){
        for (int i=0; i<skillList.size(); i++){
            if (skillList.get(i).isReady()){
                skillList.get(i).update(delta);
            }
        }
    }

    public void drawSkills(SpriteBatch batch){
        for (int i=0; i<skillList.size(); i++){
            if (skillList.get(i).isReady()){
                skillList.get(i).draw(batch);
            }
        }
    }
}
