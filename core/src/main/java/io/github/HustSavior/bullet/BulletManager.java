package io.github.HustSavior.bullet;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import io.github.HustSavior.entities.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BulletManager {
    private final World world;
    private final Player player;
    private final List<Bullet> bullets;
    private float shootCooldown = 0.5f;
    private float timeSinceLastShot = 0;

    public BulletManager(World world, Player player) {
        this.world = world;
        this.player = player;
        this.bullets = new ArrayList<>();
    }

    public void update(float delta) {
        timeSinceLastShot += delta;
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(delta);
            if (bullet.getCollisionCount() >= 5) {
                iterator.remove();
            }
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        for (Bullet bullet : bullets) {
            bullet.render(batch);
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

                Bullet bullet = new Bullet(world, player.getX() + player.getWidth() / 2 + offsetX + radius * dx - 6,
                        player.getY() + player.getHeight() / 2 + offsetY + radius * dy - 3, bulletSpeed * dx,
                        bulletSpeed * dy, bulletWidth, bulletHeight);
                bullet.setRotation((float) Math.toDegrees(Math.atan2(dy, dx))); // Set the rotation of the bullet based
                                                                                // on its velocity
                bullets.add(bullet);
            }
        }
        timeSinceLastShot = 0;
    }
}
