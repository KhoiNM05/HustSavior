package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.HustSavior.Play;
import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;

import io.github.HustSavior.utils.GameConfig;

public class Slash extends Sprite implements Skills{

    TextureRegion[] animation;
    Animation<TextureRegion> cast;
    CooldownController cd;
    final static float DEFAULT_COOLDOWN=2.0f;
    private float stateTime=0;
    private float getAnimationTime;

    private Player player;

    private final static float SLASH_DAMAGE = 50.0f;
    private Rectangle slashBounds;
    private Array<AbstractMonster> hitMonsters = new Array<>();

    private Sound slashSound;
    private boolean isSoundLoaded = false;

    private float castingX;
    private float castingY;

    private Array<AbstractMonster> monsters;

    public Slash(Sprite sprite, Player player, Array<AbstractMonster> monsters) {
        super(sprite);
        this.cd = new CooldownController(DEFAULT_COOLDOWN);
        this.player = player;
        this.monsters = monsters;
        cd.resetCooldown();

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
    public void update(float delta) {
        System.out.println("code go here");
        if (!isReady()) {
            System.out.println("Not ready, cooldown active");
            cd.cooldownTimer(delta);
            return;
        }

        if (stateTime == 0) {  // Just activated
            System.out.println("Initializing slash attack");
            hitMonsters.clear();
            if (isSoundLoaded) {
                slashSound.play(1.0f);
            }
            // Create slash bounds based on player position and facing direction
            boolean facingLeft = player.isFacingLeft();
            castingX = player.getPosition().x;
            castingY = player.getPosition().y;
            
            float slashWidth = getRegionWidth() / GameConfig.PPM;
            float slashHeight = getRegionHeight() / GameConfig.PPM;
            
            if (facingLeft) {
                castingX -= slashWidth;
            }
            
            slashBounds = new Rectangle(
                castingX / GameConfig.PPM, 
                castingY / GameConfig.PPM - slashHeight/2,
                slashWidth,
                slashHeight
            );
            System.out.println("Created slash bounds: " + slashBounds);
        }

        stateTime += delta;
        System.out.println("stateTime: " + stateTime);
        System.out.println("animationTime: " + getAnimationTime);
        
        // During active frames of animation
        if (stateTime < getAnimationTime && slashBounds != null) {
            System.out.println("Checking for hits");
            for (AbstractMonster monster : monsters) {
                Rectangle monsterBounds = monster.getBounds();
                System.out.println("Checking monster bounds: " + monsterBounds);
                
                if (!hitMonsters.contains(monster, true) && monsterBounds.overlaps(slashBounds)) {
                    System.out.println("Hit detected!");
                    monster.takeDamage(SLASH_DAMAGE);
                    hitMonsters.add(monster);
                    
                    Vector2 knockbackDir = new Vector2(
                        monster.getPosition().x - player.getPosition().x,
                        monster.getPosition().y - player.getPosition().y
                    ).nor();
                    monster.handlePush(knockbackDir.scl(10f));
                }
            }
        }

        if (stateTime >= getAnimationTime) {
            System.out.println("Resetting slash");
            cd.resetCooldown();
            slashBounds = null;
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
    public void setAOE(float aoe) {
        // Implement AOE setting logic here
    }

    @Override
    public CooldownController getCooldown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (stateTime < getAnimationTime) {
            TextureRegion currentFrame = cast.getKeyFrame(stateTime, false);
            
            boolean facingLeft = player.isFacingLeft();
            float x = castingX;
            
            if (facingLeft) {
                // Flip texture if player faces left
                currentFrame.flip(true, false);
                x -= getWidth();  // Adjust position for left facing
            }
            
            batch.draw(currentFrame, 
                x, 
                castingY,
                getWidth(),
                getHeight()
            );
            
            // Reset flip for next frame
            if (facingLeft) {
                currentFrame.flip(true, false);
            }
        }
    }
}
