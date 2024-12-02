package io.github.HustSavior.skills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;

public class Calculus extends Sprite {

    TextureRegion[] animation;
    Animation<TextureRegion> cast;
    CooldownController cd;
    final static float DEFAULT_COOLDOWN=2.0f;
    private float stateTime=0;
    private float animationTime;
    private float getAnimationTime;

    private World world;
    public Body hitbox;

    private float castingX;
    private float castingY;

    Player player;

    public Calculus(Sprite sprite, Player player, World world){
        super(sprite);
        this.cd=new CooldownController(DEFAULT_COOLDOWN);
        this.player=player;
        this.world=world;

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
                    castingX = player.getX()+2*player.getRegionWidth()/2;
                    castingY = player.getY();
                    hitbox=createHitbox((int)castingX, (int)castingY, player.getPPM(), world);
                }
                setPosition(castingX, castingY);

                // Check if animation has finished
                if (stateTime >= getAnimationTime) {
                    cd.resetCooldown(); // Reset cooldown only after animation completes
                    world.destroyBody(hitbox);
                    stateTime = 0; // Reset stateTime for the next animation cycle
                }
                else{
                    stateTime += delta;
                }
            } else {
                cd.cooldownTimer(delta); // Update cooldown
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

    public boolean isReady(){
        return cd.isReady();
    }
}
