package com.grantkoupal.letterlink.quantum;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class EntityContactListener implements ContactListener {

    private int beginSensorAmount = 0;
    private List<Object> beginContactSensorData = new ArrayList<Object>();
    private List<SensorRunnable> beginContactActions = new ArrayList<SensorRunnable>();
    private int endSensorAmount = 0;
    private List<Object> endContactSensorData = new ArrayList<Object>();
    private List<SensorRunnable> endContactActions = new ArrayList<SensorRunnable>();

    public void addBeginContact(Object userData, SensorRunnable action) {
        if(userData == null || action == null){
            return;
        }
        beginContactSensorData.add(userData);
        beginContactActions.add(action);
        beginSensorAmount++;
    }

    public void removeBeginContact(Object userData) {
        for(int i = 0; i < beginContactSensorData.size(); i++){
            if(userData.equals(beginContactSensorData.get(i))){
                beginContactSensorData.remove(i);
                beginContactActions.remove(i);
                beginSensorAmount--;
                return;
            }
        }
    }

    // Called when a collision happens
    @Override
    public void beginContact(Contact contact) {
        if (beginSensorAmount == 0)
            return;
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        for(int i = 0; i < beginContactSensorData.size(); i++){
            Object userData = beginContactSensorData.get(i);
            if (a.getUserData() != null && a.getUserData().equals(userData)) {
                beginContactActions.get(i).run(b);
                return;
            } else if(b.getUserData() != null && b.getUserData().equals(userData)){
                beginContactActions.get(i).run(a);
                return;
            }
        }
    }

    public void addEndContact(Object userData, SensorRunnable action) {
        if(userData == null || action == null){
            return;
        }
        endContactSensorData.add(userData);
        endContactActions.add(action);
        endSensorAmount++;
    }

    public void removeEndContact(Object userData) {
        for(int i = 0; i < endContactSensorData.size(); i++){
            if(userData.equals(endContactSensorData.get(i))){
                endContactSensorData.remove(i);
                endContactActions.remove(i);
                endSensorAmount--;
                return;
            }
        }
    }

    // Called when a collision ends
    @Override
    public void endContact(Contact contact) {
        if (endSensorAmount == 0)
            return;
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        for(int i = 0; i < endContactSensorData.size(); i++){
            Object userData = endContactSensorData.get(i);
            if (a.getUserData() != null && a.getUserData().equals(userData)) {
                endContactActions.get(i).run(b);
                return;
            } else if(b.getUserData() != null && b.getUserData().equals(userData)){
                endContactActions.get(i).run(a);
                return;
            }
        }
    }

    // Called just before a collision
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    // Called just after a collision
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        Entity objA = (Entity) contact.getFixtureA().getBody().getUserData();
        Entity objB = (Entity) contact.getFixtureB().getBody().getUserData();

        if (objA != null && objB != null) {
            objA.onCollisionStart(objB, contact, impulse, true);
            objB.onCollisionStart(objA, contact, impulse, false);
        }
    }

}
