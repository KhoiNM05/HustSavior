package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.Play;
import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;

public class Slash extends Sprite implements Skills{

    TextureRegion[] animation;
    Animation<TextureRegion> cast;
    CooldownController cd;
    final static float DEFAULT_COOLDOWN=2.0f;
    private float stateTime=0;
    private float getAnimationTime;
    private boolean isFacingLeft;

    private Player player;

    private final static float SLASH_DAMAGE = 50.0f;
    private Rectangle slashBounds;
    private Array<AbstractMonster> hitMonsters = new Array<>();

    private Sound slashSound;
    private boolean isSoundLoaded = false;

    private float castingX;
    private float castingY;

    public Slash(Sprite sprite, Player player){
        super(sprite);
        this.cd = new CooldownController(DEFAULT_COOLDOWN);
        this.player = player;

        setOriginCenter();

        cast = createAnimation();
        getAnimationTime = cast.getAnimationDuration();

        // Initialize sound
        try {
            slashSound = Gdx.audio.newSound(Gdx.files.internal("sound/slash_sound_effect.mp3"));
            isSoundLoaded = true;
        } catch (Exception e) {
            Gdx.app.error("Slash", "Error loading slash sound", e);
        }
    }

    @Override
    public Animation<TextureRegion> createAnimation(){

        animation = new TextureRegion[3];
        animation[0] = new TextureRegion(new Texture("skills/Slash1.png"));
        animation[1] = new TextureRegion(new Texture("skills/Slash2.png"));
        animation[2] = new TextureRegion(new Texture("skills/Slash3.png"));
        //animation[3] = new TextureRegion(new Texture("skills/parabol4.png"));
        //animation[4] = new TextureRegion(new Texture("skills/parabol5.png"));
        //animation[5] = new TextureRegion(new Texture("skills/parabol6.png"));
        //animation[6] = new TextureRegion(new Texture("skills/parabol7.png"));

        return new Animation<TextureRegion>(1/30f, animation);

    }

    @Override
    public void draw(SpriteBatch batch){
        super.draw(batch);
    }

    @Override
    public void update(float delta) {
        if (!isReady()) {
            cd.cooldownTimer(delta);
            return;
        }

        if (stateTime == 0) {  // Just activated
            hitMonsters.clear();
            if (isSoundLoaded) {
                slashSound.play(1.0f);
            }
            // Create slash bounds

            // check if this cast turns left or right
            isFacingLeft= player.isFacingLeft();
            // change location of slash sprite depending on whether the skill is cast to the left
            //or the right

            if (player.isFacingLeft()) castingX = player.getX()-getRegionWidth();
            else castingX=player.getX()+player.getRegionWidth();
            //Align slash sprite with player sprite
            castingY = player.getY()+(player.getRegionHeight()-getRegionHeight())/2f;
            //slash bounds
            slashBounds = new Rectangle(
                castingX,
                castingY,
                getRegionWidth(),
                getRegionHeight()
            );
        }

        TextureRegion currFrame=cast.getKeyFrame(stateTime, false);
        if (isFacingLeft && !isFlipX()){
            currFrame.flip(true, false);
        }
        else if(!isFacingLeft && isFlipX()){
            currFrame.flip(true, false);
        }
        setRegion(currFrame);

        stateTime += delta;

        // During active frames of animation
        if (stateTime < getAnimationTime && slashBounds != null) {
            // Get monsters from the world
            for (AbstractMonster monster : ((Play)player.getScreen()).getMonsters()) {
                if (!hitMonsters.contains(monster, true) && monster.getBounds().overlaps(slashBounds)) {
                    monster.takeDamage(SLASH_DAMAGE);
                    hitMonsters.add(monster);
                }
            }
        }

        if (stateTime >= getAnimationTime) {
            cd.resetCooldown();
            slashBounds = null;
            if(isFlipX()) currFrame.flip(true, false);
            stateTime = 0;
        }
    }

    public boolean isReady(){
        return cd.isReady();
    }


    public void dispose() {
        if (isSoundLoaded) {
            slashSound.dispose();
        }

    }

    @Override
    public void setImprovedSize(float scale) {
        // increase hitbox size with scale
        setSize(getRegionWidth()*scale, getRegionHeight()*scale);
    }

    @Override
    public CooldownController getCooldown() {
        return cd;
    }
}
