package io.github.HustSavior.bullet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.HustSavior.collision.TileCollision;
import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;

public class BulletManager implements Disposable {
    private final List<Bullet> bullets;
    private final List<AbstractMonster> monsters;
    private float shootCooldown = 0.5f;
    private float timeSinceLastShot = 0;
    private static final int MAX_BULLETS = 1000;
    private final Player player;
    private final TileCollision tileCollision;

    public BulletManager(Player player, List<AbstractMonster> monsters, TiledMap tiledMap) {
        this.bullets = new ArrayList<>();
        this.player = player;
        this.monsters = monsters;
        this.tileCollision = new TileCollision(tiledMap);
    }

    public void update(float delta) {
        timeSinceLastShot += delta;
        
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            Vector2 oldPos = bullet.getPosition().cpy();
            bullet.update(delta);
            
            Rectangle hitbox = bullet.getHitbox();
            
            // Single collision check
            if (tileCollision.collidesWith(hitbox)) {
                Gdx.app.debug("BulletManager", "Collision detected at: " + bullet.getPosition());
                
                // Reset to old position
                bullet.getPosition().set(oldPos);
                
                // Determine collision direction
                Rectangle horizontalTest = new Rectangle(
                    oldPos.x - bullet.getWidth()/2,
                    hitbox.y,
                    bullet.getWidth(),
                    bullet.getHeight()
                );
                
                boolean hitVertical = !tileCollision.collidesWith(horizontalTest);
                bullet.handleCollision(hitVertical);
            }
            
            if (!bullet.isActive()) {
                iterator.remove();
                Gdx.app.debug("BulletManager", "Bullet removed");
            }
        }
    }

    public void render(SpriteBatch batch, Rectangle viewBounds) {
        for (Bullet bullet : bullets) {
            Vector2 pos = bullet.getPosition();
            if (viewBounds.contains(pos.x, pos.y)) {
                bullet.render(batch);
            }
        }
    }

    public void shootBullet() {
        if (timeSinceLastShot < shootCooldown) {
            return;
        }
        float bulletSpeed = 5;
        float bulletWidth = 12;
        float bulletHeight = 5;
        int numAngles = 6;
        int bulletsPerAngle = 2;
        float angleStep = 360f / numAngles;
        float bulletSpacing = 100;
        float radius = 20;
        float angleRandom = (float) (Math.random() * 360);

        for (int i = 0; i < numAngles; i++) {
            float angle = i * angleStep + angleRandom;
            float radians = (float) Math.toRadians(angle);
            float dx = (float) Math.cos(radians);
            float dy = (float) Math.sin(radians);

            for (int j = 0; j < bulletsPerAngle; j++) {
                float offsetX = j * bulletSpacing * dx;
                float offsetY = j * bulletSpacing * dy;

                Bullet bullet = new Bullet(
                    player.getX() + player.getWidth() / 2 + offsetX + radius * dx - 6,
                    player.getY() + player.getHeight() / 2 + offsetY + radius * dy - 3,
                    bulletSpeed * dx,
                    bulletSpeed * dy
                );
                bullet.setRotation((float) Math.toDegrees(Math.atan2(dy, dx))); // Set the rotation of the bullet based
                                                                                // on its velocity
                bullets.add(bullet);
            }
        }
        timeSinceLastShot = 0;
    }

    public void checkCollisions(Rectangle mapObject) {
        
        
        for (Bullet bullet : bullets) {
            Rectangle bulletHitbox = bullet.getHitbox();
            if (bulletHitbox.overlaps(mapObject)) {
                Gdx.app.debug("BulletManager", "COLLISION DETECTED!");
                
                // Calculate collision normal
                float dx = bullet.getPosition().x - mapObject.x;
                float dy = bullet.getPosition().y - mapObject.y;
                boolean hitVertical = Math.abs(dx) > Math.abs(dy);
                
                Gdx.app.debug("BulletManager", "Hit " + (hitVertical ? "vertical" : "horizontal") + 
                    " wall. Velocity before: " + bullet.getVelocity());
                
                bullet.handleCollision(hitVertical);
                
                Gdx.app.debug("BulletManager", "Velocity after bounce: " + bullet.getVelocity());
            }
        }
    }

    @Override
    public void dispose() {
        // Dispose of any resources (textures, etc.) used by bullets
        for (Bullet bullet : bullets) {
            bullet.dispose();
        }
        bullets.clear();
    }
}
