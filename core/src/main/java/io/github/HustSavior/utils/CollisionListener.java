package io.github.HustSavior.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.entities.NormalMonster;

public class CollisionListener implements ContactListener {
    private static final String TAG = "CollisionListener";

    @Override
    public void beginContact(Contact contact) {
        if (contact == null) {
            Gdx.app.error(TAG, "Null contact received");
            return;
        }

        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA == null || fixtureB == null) {
            Gdx.app.error(TAG, "Null fixture in contact");
            return;
        }

        // Check if the collision involves the player and a monster
        if (isMonsterMonsterCollision(fixtureA, fixtureB)) {
            handleMonsterMonsterCollision(fixtureA, fixtureB);
            return;
        }

        // Xử lý va chạm giữa người chơi và quái vật
        if (isPlayerMonsterCollision(fixtureA, fixtureB)) {
            handlePlayerMonsterCollision(fixtureA, fixtureB);
        }
    }

    private boolean isPlayerMonsterCollision(Fixture fixtureA, Fixture fixtureB) {
        return (fixtureA.getUserData() instanceof Player && fixtureB.getUserData() instanceof NormalMonster) ||
            (fixtureB.getUserData() instanceof Player && fixtureA.getUserData() instanceof NormalMonster);
    }

    private boolean isMonsterMonsterCollision(Fixture fixtureA, Fixture fixtureB) {
        return fixtureA.getUserData() instanceof NormalMonster &&
            fixtureB.getUserData() instanceof NormalMonster;
    }

    private void handlePlayerMonsterCollision(Fixture fixtureA, Fixture fixtureB) {
        Player player = null;
        NormalMonster monster = null;

        // Determine which fixture is the player and which is the monster
        if (fixtureA.getUserData() instanceof Player) {
            player = (Player) fixtureA.getUserData();
            monster = (NormalMonster) fixtureB.getUserData();
        } else {
            player = (Player) fixtureB.getUserData();
            monster = (NormalMonster) fixtureA.getUserData();
        }

        if (player != null && monster != null && !monster.isHitted()) {
            // Trigger monster's hitted state
            monster.hitByPlayer(player);

            // Optional: Add player interaction logic
            Gdx.app.log(TAG, "Player collided with monster, triggering hitted state");
        }
    }

    private void handleMonsterMonsterCollision(Fixture fixtureA, Fixture fixtureB) {
        NormalMonster monsterA = (NormalMonster) fixtureA.getUserData();
        NormalMonster monsterB = (NormalMonster) fixtureB.getUserData();

        if (monsterA != null && monsterB != null) {
            // Xử lý logic khi quái vật va chạm nhau (ví dụ: bật ngược hoặc thay đổi hướng)
            Vector2 direction = new Vector2(monsterA.getBody().getPosition()).sub(monsterB.getBody().getPosition()).nor();
            monsterA.getBody().applyLinearImpulse(direction.scl(50f), monsterA.getBody().getWorldCenter(), true);
            monsterB.getBody().applyLinearImpulse(direction.scl(-50f), monsterB.getBody().getWorldCenter(), true);

            Gdx.app.log(TAG, "Monsters collided, applying impulse");
        }
    }
    // Other methods remain the same
    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
