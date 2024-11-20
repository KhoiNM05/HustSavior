package skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import io.github.HustSavior.entities.Player;

public class Calculus extends Sprite {

    TextureRegion[] animation;
    Animation<TextureRegion> cast;
    CooldownController cd;
    final static float DEFAULT_COOLDOWN=2.0f;
    private float stateTime=0;
    private float animationTime;
    private float getAnimationTime;

    private float castingX;
    private float castingY;


    Player player;

    public Calculus(Sprite sprite, Player player){
        super(sprite);
        this.cd=new CooldownController(DEFAULT_COOLDOWN);
        this.player=player;

        animation= new TextureRegion[7];
        animation[0]= new TextureRegion(new Texture("skills/parabol1.png"));
        animation[1]= new TextureRegion(new Texture("skills/parabol2.png"));
        animation[2]= new TextureRegion(new Texture("skills/parabol3.png"));
        animation[3]= new TextureRegion(new Texture("skills/parabol4.png"));
        animation[4]= new TextureRegion(new Texture("skills/parabol5.png"));
        animation[5]= new TextureRegion(new Texture("skills/parabol6.png"));
        animation[6]= new TextureRegion(new Texture("skills/parabol7.png"));

        cast=new Animation<TextureRegion>(1/60f, animation);
        getAnimationTime=cast.getAnimationDuration();
    }

    public void draw(SpriteBatch batch){
        super.draw(batch);
    }

    public void update(float delta){
        stateTime+=delta;

        if (isReady()){

            setRegion(cast.getKeyFrame(stateTime, true));
            //get initial position of skill shots
            if (animationTime==getAnimationTime){
                castingX=player.getX();
                castingY=player.getY();
            }
            setPosition(castingX, castingY);
            animationTime--;
            if (animationTime<=0){
                cd.resetCooldown();
                animationTime=getAnimationTime;
            }
        }
        else{
            cd.cooldownTimer(delta);
        }

    }

    public boolean isReady(){
        return cd.isReady();
    }
}
