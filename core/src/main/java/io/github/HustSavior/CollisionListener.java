package io.github.HustSavior;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionListener  implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        System.out.println("Collision detected: " + contact.getFixtureA().getBody().getUserData() + " with " + contact.getFixtureB().getBody().getUserData());

    }
    @Override
    public void endContact(Contact contact) {
        System.out.println("Collision ended: " + contact.getFixtureA().getBody().getUserData() + " with " + contact.getFixtureB().getBody().getUserData());
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Handle pre-solve collision logic
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Handle post-solve collision logic
    }
}
