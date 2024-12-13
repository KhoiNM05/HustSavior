package io.github.HustSavior.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

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
    private boolean isFacingLeft;

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

        // Verify monsters array is not null and print initial size
        if (monsters != null) {
            System.out.println("Slash initialized with " + monsters.size + " monsters");
        } else {
            System.out.println("Warning: monsters array is null!");
            this.monsters = new Array<>();  // Create empty array to prevent null pointer
        }

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
        Texture slashTexture = new Texture("skills/Slash1.png");
        animation[0] = new TextureRegion(slashTexture);
        animation[1] = new TextureRegion(new Texture("skills/Slash2.png"));
        animation[2] = new TextureRegion(new Texture("skills/Slash3.png"));

        // Slower animation and no looping
        Animation<TextureRegion> anim = new Animation<>(0.15f, animation);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }

  
    

    @Override
    public void update(float delta) {
        // Only update cooldown if we're not currently animating
        if (stateTime == 0) {
            cd.cooldownTimer(delta);
           
        }

        // If animation is not running and skill is not ready, return
        if (stateTime == 0 && !isReady()) {
         
            return;
        }

        if (stateTime == 0) {  // Start of animation
            System.out.println("Starting animation");
            hitMonsters.clear();
            if (isSoundLoaded) {
                slashSound.play(1.0f);
            }
            
            // Debug hitbox creation
            float hitboxWidth = getRegionWidth() * 2.5f / GameConfig.PPM;
            float hitboxHeight = getRegionHeight() * 2.0f / GameConfig.PPM;
            
            if (player.isFacingLeft()) {
                castingX = player.getPosition().x - hitboxWidth;
            } else {
                castingX = player.getPosition().x + player.getWidth() / GameConfig.PPM;
            }
            castingY = player.getPosition().y;
            
            slashBounds = new Rectangle(
                castingX,
                castingY - hitboxHeight/2,
                hitboxWidth,
                hitboxHeight
            );
            
            // Debug print for slash hitbox
            System.out.println("Slash hitbox: x=" + slashBounds.x + 
                              ", y=" + slashBounds.y + 
                              ", width=" + slashBounds.width + 
                              ", height=" + slashBounds.height);
        }

        stateTime += delta;
        
        // Check for hits during active frames
        if (stateTime < getAnimationTime && slashBounds != null) {
            // Convert slashBounds to world units
            Rectangle worldSlashBounds = new Rectangle(
                slashBounds.x / GameConfig.PPM,
                slashBounds.y / GameConfig.PPM,
                slashBounds.width / GameConfig.PPM,
                slashBounds.height / GameConfig.PPM
            );

            System.out.println("Number of monsters: " + monsters.size);
            System.out.println("Slash bounds (world units): x=" + worldSlashBounds.x + ", y=" + worldSlashBounds.y + ", width=" + worldSlashBounds.width + ", height=" + worldSlashBounds.height);
            for (AbstractMonster monster : monsters) {
                Rectangle monsterBounds = monster.getBounds();
                System.out.println("Monster bounds: x=" + monsterBounds.x + ", y=" + monsterBounds.y + ", width=" + monsterBounds.width + ", height=" + monsterBounds.height);
                
                if (!hitMonsters.contains(monster, true) && monsterBounds.overlaps(worldSlashBounds)) {
                    System.out.println("Hit detected!");
                    monster.takeDamage(SLASH_DAMAGE);
                    hitMonsters.add(monster);
                    
                    Vector2 knockbackDir = new Vector2(
                        monster.getPosition().x - player.getPosition().x,
                        monster.getPosition().y - player.getPosition().y
                    ).nor();
                    monster.handlePush(knockbackDir.scl(5f));
                }
            }
        }

        if (stateTime >= getAnimationTime) {
            stateTime = 0;
            slashBounds = null;
            cd.resetCooldown();
        }
    }

    public boolean isReady(){
        boolean ready = cd.isReady();
        System.out.println("Checking if ready: Timer=" + cd.getCurrentTimer() + 
                          " Cooldown=" + cd.getCooldownValue() + 
                          " Ready=" + ready);
        return ready;
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
        return cd;  // Implement this method properly
    }

    public void draw(SpriteBatch batch) {
        if (!isReady() && stateTime == 0) return;
        
        TextureRegion currentFrame = cast.getKeyFrame(stateTime, false);
        float scale = 1.2f;  // Reduced scale
        
        // Calculate dimensions
        float slashWidth = currentFrame.getRegionWidth() * scale;
        float slashHeight = currentFrame.getRegionHeight() * scale;
        
        // Position slash based on player facing direction
        if (player.isFacingLeft()) {
            castingX = player.getX() - slashWidth * 0.8f;  // Closer to player
            if (!currentFrame.isFlipX()) currentFrame.flip(true, false);
        } else {
            castingX = player.getX() + player.getWidth() * 0.8f;  // Closer to player
            if (currentFrame.isFlipX()) currentFrame.flip(true, false);
        }
        
        // Center vertically with player
        castingY = player.getY() + (player.getHeight() - slashHeight) / 2;
        
        batch.draw(currentFrame, castingX, castingY, slashWidth, slashHeight);
    }

    // Add method to update monsters reference if needed
    public void updateMonsters(Array<AbstractMonster> newMonsters) {
        this.monsters = newMonsters;
        System.out.println("Updated monsters array, new size: " + monsters.size);
    }
}
