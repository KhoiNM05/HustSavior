package io.github.HustSavior.utils;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        // Handle the beginning of a collision
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
