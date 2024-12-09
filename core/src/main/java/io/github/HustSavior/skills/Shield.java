package io.github.HustSavior.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.GameConfig;


public class Shield extends Sprite implements Skills{

    CooldownController cd;
    final static float DEFAULT_COOLDOWN=3f;
    final static float SHIELD_ANIMATION_FRAME_DURATION=0.1f;
    private final static float SHIELD_ALPHA=0.5f;
    private final static float SHIELD_DURATION=3f;
    private final static float PPM= GameConfig.PPM;
    boolean shieldActive;
    private float shieldTimeRemaining;

    Animation<TextureRegion> shieldAnimation;
    private float shieldStateTime;
    private TextureRegion[] shieldFrames;

    private World world;
    public Body hitbox;

    Player player;

    public Shield(Sprite sprite, Player player, World world){
        super(sprite);
        this.cd=new CooldownController(DEFAULT_COOLDOWN);
        this.player=player;
        this.world=world;

        shieldActive=true;
        shieldAnimation=createAnimation();


    }


    @Override
    public Animation<TextureRegion> createAnimation() {
        // Load all shield frames into array
        shieldFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture texture = new Texture(Gdx.files.internal("item/shield_effects/shield_effect_" + (i + 1) + ".png"));
            shieldFrames[i] = new TextureRegion(texture);
            Gdx.app.log("Shield", "Loaded shield frame " + (i + 1));
        }
        Animation<TextureRegion> animation = new Animation<TextureRegion>(SHIELD_ANIMATION_FRAME_DURATION, shieldFrames);
        shieldStateTime = 0;
        return animation;
    }
    @Override
    public void draw(SpriteBatch batch){
        shieldStateTime += Gdx.graphics.getDeltaTime(); // Update state time here instead of update method
        TextureRegion currentFrame = shieldAnimation.getKeyFrame(shieldStateTime, true);
        float shieldScale = 0.45f; // Adjust this value to change shield size
        float shieldX = (player.getX() - (currentFrame.getRegionWidth() * shieldScale - player.getWidth()) / 2);
        float shieldY = (player.getY() - (currentFrame.getRegionHeight() * shieldScale - player.getHeight()) / 2);

        // Save current color
        Color oldColor = batch.getColor();
        // Set transparent color
        batch.setColor(1, 1, 1, SHIELD_ALPHA);

        batch.draw(currentFrame,
            shieldX,
            shieldY,
            currentFrame.getRegionWidth() * shieldScale,
            currentFrame.getRegionHeight() * shieldScale);

        // Restore original color
        batch.setColor(oldColor);
    }
    @Override
    public void update(float delta){

        if(shieldActive){
            shieldTimeRemaining -= delta;
            if (shieldTimeRemaining <= 0) {
                shieldActive = false;
                cd.resetCooldown();
            }
        }

        else{
            cd.cooldownTimer(delta);
            if (cd.isReady()){
                shieldActive=true;
                shieldTimeRemaining=SHIELD_DURATION;
            }
        }

    }

    public Body createHitbox(int x, int y, float PPM, World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((x+getRegionWidth()/2f)/PPM, (y+getRegionHeight()/2f)/PPM);

        Body ItemBody = world.createBody(bodyDef);
        PolygonShape shape=new PolygonShape();
        shape.setAsBox(getRegionWidth()/2.0f/PPM, getRegionHeight()/2.0f/PPM);

        FixtureDef fixtureDef= new FixtureDef();
        fixtureDef.shape=shape;
        fixtureDef.isSensor=true;

        ItemBody.createFixture(fixtureDef).setUserData(this);
        shape.dispose();

        return ItemBody;
    }

    public boolean isReady(){return cd.isReady();}
}
