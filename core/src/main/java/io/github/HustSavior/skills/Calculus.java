package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

        cast=createAnimation();
        getAnimationTime=cast.getAnimationDuration();
    }
    public Animation<TextureRegion> createAnimation(){
        animation= new TextureRegion[7];
        animation[0]= new TextureRegion(new Texture("skills/parabol1.png"));
        animation[1]= new TextureRegion(new Texture("skills/parabol2.png"));
        animation[2]= new TextureRegion(new Texture("skills/parabol3.png"));
        animation[3]= new TextureRegion(new Texture("skills/parabol4.png"));
        animation[4]= new TextureRegion(new Texture("skills/parabol5.png"));
        animation[5]= new TextureRegion(new Texture("skills/parabol6.png"));
        animation[6]= new TextureRegion(new Texture("skills/parabol7.png"));

        return new Animation<TextureRegion>(1/60f, animation);
    }
    public void draw(SpriteBatch batch){
        super.draw(batch);
    }

    public void update(float delta){
            // Start animation if cooldown is ready
            if (isReady()) {
                // Play animation
                setRegion(cast.getKeyFrame(stateTime, false)); // 'false' ensures animation stops at the last frame
                if (stateTime<=0) {
                    // Get initial position of skill shots
                    castingX = player.getX();
                    castingY = player.getY();
                }
                setPosition(castingX, castingY);

                // Check if animation has finished
                if (stateTime >= getAnimationTime) {
                    cd.resetCooldown(); // Reset cooldown only after animation completes
                    stateTime = 0; // Reset stateTime for the next animation cycle
                }
                else{
                    stateTime += delta;
                }
            } else {
                cd.cooldownTimer(delta); // Update cooldown
            }



    }

    public boolean isReady(){
        return cd.isReady();
    }
}
