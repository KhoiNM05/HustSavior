package io.github.HustSavior.utils;

import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.entities.Player;
import io.github.HustSavior.skills.Calculus;
import items.AssetSetter;
import items.CalcBook;
import items.Item;

public class CollisionListener implements ContactListener {
    AssetSetter assetSetter;
    public CollisionListener(AssetSetter assetSetter){
        this.assetSetter=assetSetter;
    }
    @Override
    public void beginContact(Contact contact) {
        // Handle the beginning of a collision
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();

        if (userDataA instanceof Item && userDataB instanceof Player){
            assetSetter.objectAcquired((Item)userDataA);
        }

    }

    @Override
    public void endContact(Contact contact) {
        // Handle the end of a collision
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Handle before collision resolution
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Handle after collision resolution
    }
}
