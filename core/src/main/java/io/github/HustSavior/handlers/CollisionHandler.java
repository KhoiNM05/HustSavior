package io.github.HustSavior.handlers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.utils.transparency.BuildingTransparencyManager;

public class CollisionHandler implements ContactListener {
    private final BuildingTransparencyManager transparencyManager;
    
    public CollisionHandler(BuildingTransparencyManager transparencyManager) {
        this.transparencyManager = transparencyManager;
    }
    
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        // Check for monster-bounds collisions
        if (isMonsterBoundsCollision(fixtureA, fixtureB)) {
            AbstractMonster monster;
            Fixture boundsFixture;
            
            if (fixtureA.getBody().getUserData() instanceof AbstractMonster) {
                monster = (AbstractMonster) fixtureA.getBody().getUserData();
                boundsFixture = fixtureB;
            } else {
                monster = (AbstractMonster) fixtureB.getBody().getUserData();
                boundsFixture = fixtureA;
            }
            
            transparencyManager.onMonsterEnterBounds(monster, boundsFixture);
        }
        
        // Only handle player-monster collisions
        if (isPlayerMonsterCollision(fixtureA, fixtureB)) {
            handlePlayerMonsterCollision(fixtureA, fixtureB);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        if (isMonsterBoundsCollision(fixtureA, fixtureB)) {
            AbstractMonster monster;
            Fixture boundsFixture;
            
            if (fixtureA.getBody().getUserData() instanceof AbstractMonster) {
                monster = (AbstractMonster) fixtureA.getBody().getUserData();
                boundsFixture = fixtureB;
            } else {
                monster = (AbstractMonster) fixtureB.getBody().getUserData();
                boundsFixture = fixtureA;
            }
            
            transparencyManager.onMonsterExitBounds(monster, boundsFixture);
        }
        
        // Handle monster collision end
        if (isPlayerMonsterCollision(fixtureA, fixtureB)) {
            AbstractMonster monster;
            if (fixtureA.getBody().getUserData() instanceof AbstractMonster) {
                monster = (AbstractMonster) fixtureA.getBody().getUserData();
            } else {
                monster = (AbstractMonster) fixtureB.getBody().getUserData();
            }
            monster.onPlayerCollisionEnd();
        }
    }

    @Override
    public void preSolve(Contact cntct, Manifold mnfld) {
        // Not needed for now
    }

    @Override
    public void postSolve(Contact cntct, ContactImpulse ci) {
        // Not needed for now
    }

    private boolean isPlayerMonsterCollision(Fixture a, Fixture b) {
        Object userDataA = a.getBody().getUserData();
        Object userDataB = b.getBody().getUserData();
        
        // Only check for player-monster collisions
        return (userDataA instanceof Player && userDataB instanceof AbstractMonster) ||
               (userDataA instanceof AbstractMonster && userDataB instanceof Player);
    }

    private void handlePlayerMonsterCollision(Fixture a, Fixture b) {
        Player player;
        AbstractMonster monster;
        
        if (a.getBody().getUserData() instanceof Player) {
            player = (Player) a.getBody().getUserData();
            monster = (AbstractMonster) b.getBody().getUserData();
        } else {
            player = (Player) b.getBody().getUserData();
            monster = (AbstractMonster) a.getBody().getUserData();
        }
        
        monster.onPlayerCollisionStart();
        if (!player.isKnockedBack()) {
            player.applyKnockback(monster.getBody().getPosition());
            monster.handlePush(player.getBody().getLinearVelocity());
        }
    }

    private boolean isMonsterBoundsCollision(Fixture a, Fixture b) {
        Object userDataA = a.getBody().getUserData();
        Object userDataB = b.getBody().getUserData();
        
        return (userDataA instanceof AbstractMonster && userDataB instanceof String && ((String)userDataB).endsWith("_bounds")) ||
               (userDataB instanceof AbstractMonster && userDataA instanceof String && ((String)userDataA).endsWith("_bounds"));
    }

    private void handleMonsterBoundsCollision(Fixture monsterFixture, Fixture boundsFixture) {
        AbstractMonster monster = (AbstractMonster) monsterFixture.getBody().getUserData();
        // Assuming you have a method to handle transparency
        monster.setTransparent(true);
    }
} 