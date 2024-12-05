package io.github.HustSavior.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;

import static io.github.HustSavior.utils.GameConfig.PPM;

public class NormalMonster extends Sprite {

    // Monster attributes
    private int hp;
    private int attack;
    private int speed;
    private String spriteSheetPath;
    // Animation-related attributes
    private Texture spriteSheet;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> hittedAnimation;

    private float stateTime = 0f;
    private Vector2 knockbackVector = new Vector2();
    private static final float KNOCKBACK_FORCE = 150f;

    // State management
    public enum MonsterState {
        IDLE, MOVING, HITTED
    }

    private MonsterState state = MonsterState.IDLE;
    private boolean movingRight = true;
    private float hittedDuration = 0.5f;
    private float hittedTimer = 0f;

    private Body body;
    private boolean isHitted = false;

    // Constructor
    public NormalMonster(float x, float y, int hp, int attack, int speed, String spriteSheetPath) {

        this.hp = hp;
        this.attack = attack;
        this.speed = speed;
        this.spriteSheetPath = spriteSheetPath;
        setPosition(x / PPM, y / PPM);

        try {
            spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
            if (spriteSheet == null) {
                Gdx.app.error("NormalMonster", "Texture is null for path: " + spriteSheetPath);
                return;
            }
            setSize(spriteSheet.getWidth() / 8f, spriteSheet.getHeight() / 6f);
            initializeAnimations();
        } catch (Exception e) {
            Gdx.app.error("NormalMonster", "Failed to load texture: " + spriteSheetPath, e);
        }

    }



    private Animation<TextureRegion> extractAnimation(Texture spriteSheet, int row, int frameCount, float frameDuration) {
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet,
            spriteSheet.getWidth() / frameCount, // Chia theo số frame trên mỗi hàng
            spriteSheet.getHeight() / 6 // Giả định sprite sheet có 3 hàng
        );

        // Lấy hàng chỉ định
        TextureRegion[] animationFrames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            animationFrames[i] = tmpFrames[row][i];
        }

        return new Animation<>(frameDuration, animationFrames);
    }


//    // Initialize animations
//    private void initializeAnimations(String spritePath) {
//        spriteSheet = new Texture(Gdx.files.internal(spritePath));
//
//        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet,
//            spriteSheet.getWidth() / 8,
//            spriteSheet.getHeight() / 6
//        );
//
//        // Initialize animations (example, you should adjust based on your sprite sheet)
//        walkAnimationRight = new Animation<>(0.1f, tmpFrames[0]);
//        walkAnimationLeft = new Animation<>(0.1f, tmpFrames[1]);
//        hittedAnimationRight = new Animation<>(0.1f, tmpFrames[2]);
//        hittedAnimationLeft = new Animation<>(0.1f, tmpFrames[3]);
//    }

    private void initializeAnimations() {
        if (spriteSheet == null) {
            Gdx.app.error("NormalMonster", "Sprite sheet is null, cannot initialize animations");
            return;
        }

        try {
            walkAnimation = extractAnimation(spriteSheet, 0, 8, 0.1f); // Hàng 0: Di chuyển
            attackAnimation = extractAnimation(spriteSheet, 2, 6, 0.15f); // Hàng 2: Tấn công
            hittedAnimation = extractAnimation(spriteSheet, 4, 4, 0.2f); // Hàng 4: Bị đánh
        } catch (Exception e) {
            Gdx.app.error("NormalMonster", "Error initializing animations", e);
        }
    }



    public void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX() * PPM, getY() * PPM);
        bodyDef.fixedRotation = true; // Prevent rotation

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(8f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.0f;

        // Set collision filtering
        fixtureDef.filter.categoryBits = 0x0004; // Monster category
        fixtureDef.filter.maskBits = 0x0001 | 0x0002 | 0x0004; // Collide with world objects and player

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this); // Gán `this` (NormalMonster) cho `userData`
        shape.dispose();
    }

    public void hitByPlayer(Player player) {
        if (!isHitted) {
            isHitted = true;
            state = MonsterState.HITTED;
            hittedTimer = hittedDuration;
            knockbackVector.set(player.getBody().getLinearVelocity()).nor().scl(KNOCKBACK_FORCE);
            body.applyLinearImpulse(knockbackVector, body.getWorldCenter(), true);
        }
    }

    public void update(float delta, Player player) {
        stateTime += delta;

        if (!isHitted) {
            state = MonsterState.MOVING; // Chuyển sang trạng thái di chuyển
            moveTowardsPlayer(delta, player);
        }
        // Lấy vị trí quái vật và người chơi
        Vector2 monsterPos = body.getPosition();
        Vector2 playerPos = new Vector2(player.getX() / PPM, player.getY() / PPM);

        // Tính hướng di chuyển đến người chơi
        Vector2 direction = playerPos.sub(monsterPos).nor();
        body.setLinearVelocity(direction.scl(speed / PPM)); // `speed` là
    }

    private void handleHittedState(float delta) {
        hittedTimer -= delta;
        if (hittedTimer <= 0) {
            isHitted = false;
            state = MonsterState.IDLE;
        }
    }

    private void moveTowardsPlayer(float delta, Player player) {
        if (state == MonsterState.MOVING) {
            Vector2 playerPosition = player.getBody().getPosition();
            Vector2 monsterPosition = body.getPosition();

            Vector2 direction = playerPosition.sub(monsterPosition).nor();
            body.setLinearVelocity(direction.scl(speed / PPM));
        }
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case HITTED:
                return hittedAnimation;
            case MOVING:
                return walkAnimation;
            case IDLE:
            default:
                return walkAnimation; // Quay lại hoạt ảnh di chuyển như trạng thái mặc định
        }
    }

    public void draw(SpriteBatch batch) {
        if (batch == null) {
            Gdx.app.error("NormalMonster", "Cannot draw: batch is null");
            return;
        }

        if (body == null) {
            Gdx.app.error("NormalMonster", "Cannot draw: body is null");
            return;
        }

        stateTime += Gdx.graphics.getDeltaTime(); // Cập nhật thời gian hoạt ảnh
        TextureRegion currentFrame = getCurrentAnimation().getKeyFrame(stateTime, true);

        // Vẽ frame hiện tại
        batch.draw(
            currentFrame,
            body.getPosition().x * PPM - getWidth() / 2,
            body.getPosition().y * PPM - getHeight() / 2,
            getWidth(),
            getHeight()
        );
    }



    public void dispose() {
        spriteSheet.dispose();
    }

    public Vector2 getPosition() {
        return new Vector2(getX(), getY());
    }

    public MonsterState getState() {
        return state;
    }

    public boolean isHitted() {
        return isHitted;
    }

    public Body getBody() {
        return body;
    }

}
