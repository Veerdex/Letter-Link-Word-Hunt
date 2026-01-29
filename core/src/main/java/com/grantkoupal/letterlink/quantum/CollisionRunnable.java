package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;

public interface CollisionRunnable {
    public void run(Entity e, Contact contact, ContactImpulse impulse, boolean side);
}
