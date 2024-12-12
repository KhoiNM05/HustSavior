package io.github.HustSavior.collision;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.HustSavior.entities.Player;
import io.github.HustSavior.entities.AbstractMonster;


public class SimpleCollisionHandler {
    public static void checkCollisions(Player player, List<AbstractMonster> monsters) {
        Rectangle playerBounds = player.getBounds();
        
        for (AbstractMonster monster : monsters) {
            if (playerBounds.overlaps(monster.getBounds())) {
                handleCollision(player, monster);
            }
        }
    }
    
    private static void handleCollision(Player player, AbstractMonster monster) {
        // Calculate knockback direction
        Vector2 knockbackDir = new Vector2(
            player.getPosition().x - monster.getPosition().x,
            player.getPosition().y - monster.getPosition().y
        ).nor();
        
        // Apply knockback to player
        if (!player.isKnockedBack()) {
            player.applyKnockback(knockbackDir);
            player.takeDamage(monster.getAttack());
        }
        
        // Apply push to monster
        monster.handlePush(player.getVelocity());
    }
} 