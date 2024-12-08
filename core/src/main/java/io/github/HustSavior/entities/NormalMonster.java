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

public class NormalMonster extends Sprite implements MonsterBehavior{

    // Monster attributes
    private float hp;
    private float attack;
    private float speed;
    private String spriteSheetPath;
    // Animation-related attributes
    private Texture spriteSheet;
    private Animation<TextureRegion> movingAnimation;
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

    // New constructor for single texture
    public NormalMonster(Texture texture, float x, float y, World world) {
        setPosition(x/PPM, y/PPM);

        // Use the provided texture directly
        this.spriteSheet = texture;
        Gdx.app.log("NormalMonster", "Texture loaded: " + (this.spriteSheet != null)); // Log here
        setSize(texture.getWidth()/PPM, texture.getHeight()/PPM);

        // Create a simple animation from the single texture
        TextureRegion[] singleFrameAnimation = new TextureRegion[] { new TextureRegion(texture) };
        movingAnimation = new Animation<>(1f, singleFrameAnimation);
        hittedAnimation = movingAnimation;
        createBody(world);
    }

    // Constructor
    /*
    public NormalMonster(String spriteSheetPath, float x, float y, World world) {
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

        Texture spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        movingAnimation = extractAnimation(spriteSheet, 0, 8, 0.1f);  // Hàng 0: MOVING
        hittedAnimation = extractAnimation(spriteSheet, 4, 4, 0.2f);
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


    private void initializeAnimations() {
        if (spriteSheet == null) {
            Gdx.app.error("NormalMonster", "Sprite sheet is null, cannot initialize animations");
            return;
        }

        try {
            movingAnimation = extractAnimation(spriteSheet, 0, 8, 0.1f); // Hàng 0: Di chuyển
            Animation<TextureRegion> attackAnimation = extractAnimation(spriteSheet, 2, 6, 0.15f); // Hàng 2: Tấn công
            hittedAnimation = extractAnimation(spriteSheet, 4, 4, 0.2f); // Hàng 4: Bị đánh
        } catch (Exception e) {
            Gdx.app.error("NormalMonster", "Error initializing animations", e);
        }
    }

     */

    public void createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());
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

            // Tính hướng đẩy lùi (knockback)
            Vector2 direction = body.getPosition().sub(player.getBody().getPosition()).nor();
            knockbackVector.set(direction).scl(KNOCKBACK_FORCE);
            body.applyLinearImpulse(knockbackVector, body.getWorldCenter(), true);
        }
    }

    public void update(float delta, Player player) {
        stateTime += delta;

        switch (state) {
            case HITTED:
                handleHittedState(delta);
                break;

            case MOVING:
                moveTowardsPlayer(delta, player);
                break;

            case IDLE:
               handleIdleState(delta);
                break;
        }

        // Đồng bộ vị trí vật lý và sprite
        setPosition(
            body.getPosition().x - getWidth() / 2,
            body.getPosition().y - getHeight() / 2
        );
    }

    private void handleHittedState(float delta) {
        hittedTimer -= delta;
        if (hittedTimer > 0) {
            // Đẩy lùi quái vật
            body.setLinearVelocity(knockbackVector);
        } else {
            // Hết thời gian bị đánh, quay lại di chuyển
            isHitted = false;
            state = MonsterState.MOVING;
        }
    }

    private void handleIdleState(float delta) {
        body.setLinearVelocity(0, 0); // Không di chuyển
    }

    private void moveTowardsPlayer(float delta, Player player) {
        if (isHitted) return; // Nếu đang ở trạng thái HITTED, không di chuyển

        Vector2 playerPos = player.getBody().getPosition();
        Vector2 direction = playerPos.sub(body.getPosition()).nor();
        body.setLinearVelocity(direction.scl(speed));
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case HITTED:
                return hittedAnimation;
            case MOVING:
                return movingAnimation;
            case IDLE:
            default:
                return movingAnimation; // Quay lại hoạt ảnh di chuyển như trạng thái mặc định
        }
    }

    public void draw(SpriteBatch batch) {
        if (body == null) {
            Gdx.app.error("NormalMonster.draw", "Body chưa được khởi tạo.");
            return;
        }
        if (this.getTexture() == null) {
            Gdx.app.error("NormalMonster.draw", "Texture không hợp lệ.");
            return;
        }
        // Lấy vị trí từ Box2D body
        float pixelX = body.getPosition().x * GameConfig.PPM;
        float pixelY = body.getPosition().y * GameConfig.PPM;
        batch.draw(getTexture(), pixelX, pixelY, getWidth() * GameConfig.PPM, getHeight() * GameConfig.PPM);

        // Ghi log để kiểm tra
        Gdx.app.log("NormalMonster.draw", "Đang vẽ quái vật tại (mét): (" + pixelX + ", " + pixelY + ")");
    }

    @Override
    public void takeDamage(float damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            // Chuyển trạng thái sang DEAD nếu cần
        } else {
            // Chuyển sang trạng thái bị đánh
            state = MonsterState.HITTED;
            isHitted = true;
            hittedTimer = hittedDuration;
        }
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

    public boolean isAlive(){
        if (hp > 0) return true;
        else return false;
    }

    public float getSpeed(){
        return speed;
    }

    public void dispose() {
        spriteSheet.dispose();
    }
//    @Override
//    public float getX() {
//        return body.getPosition().x * GameConfig.PPM; // Chuyển đổi từ Box2D sang pixel
//    }
//
//    @Override
//    public float getY() {
//        return body.getPosition().y * GameConfig.PPM; // Chuyển đổi từ Box2D sang pixel
//    }

}
