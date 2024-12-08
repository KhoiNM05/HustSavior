package io.github.HustSavior.collision;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import io.github.HustSavior.Play;

public class CollisionListener implements ContactListener {
    private final Play play;

    public CollisionListener(Play play) {
        this.play = play;
    }

    @Override
    public void beginContact(Contact contact) {
        play.handleItemCollision(contact);
        play.handleBulletCollision(contact);
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
