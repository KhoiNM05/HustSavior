package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.entities.Player;
import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.Play;

import java.util.ArrayList;

import io.github.HustSavior.entities.AbstractMonster;

public class SkillManager {
    private final static int MELEE=1;
    //private final static int RANGED=2;
    private final static int SHIELD=2;
    Player player;
    private World world;
    ArrayList<Skills> skillList;
    private Array<AbstractMonster> monsters;
    public SkillManager(Player player, World world){
        this.player=player;
        this.world=world;
        this.monsters = new Array<>();
        skillList= new ArrayList<Skills>();
    }

    public SkillManager(Player player, Array<AbstractMonster> monsters) {
        this.player = player;
        this.monsters = monsters;
        skillList = new ArrayList<Skills>();
    }

    public void activateSkills(int id){
        if (id==MELEE){
            skillList.add(new Slash(new Sprite(new Texture("skills/Slash1.png")), player, monsters));
        }
        else if(id==SHIELD){
            skillList.add(new Shield(new Sprite(new Texture("item/shield.png")), player, world));
            System.out.println("activate");
        }
    }

    public void update(float delta){
        // Update monsters reference from Play screen
        Play playScreen = (Play)player.getScreen();
        if (playScreen != null) {
            this.monsters = playScreen.getMonsters();
            // Update monsters reference in all skills
            for (Skills skill : skillList) {
                if (skill instanceof Slash) {
                    ((Slash) skill).updateMonsters(monsters);
                }
            }
        }

        // Update skills
        for (Skills skill : skillList) {
            skill.update(delta);
        }
    }

    public void drawSkills(SpriteBatch batch) {
        for (Skills skill : skillList) {
            if (skill instanceof Slash) {
                ((Slash) skill).draw(batch);
            } else if (skill instanceof Shield) {
                ((Shield) skill).draw(batch);
            }
        }
    }

    public void applyBuff(int id){
        if (id==1){
            cooldownReduction(0.5f);
        }
        else if (id==2){
            increaseSkillSize(2.0f);
        }
    }

    private void cooldownReduction(float scale){
        for (int i=0; i<skillList.size(); i++){
            skillList.get(i).getCooldown().setCooldown(scale);
        }
    }

    private void increaseSkillSize(float scale){
        for (int i=0; i<skillList.size(); i++){
            if (skillList.get(i) instanceof Slash)
            skillList.get(i).setImprovedSize(scale);
        }
    }

    public void setMonsters(Array<AbstractMonster> monsters) {
        this.monsters = monsters;
    }
}
