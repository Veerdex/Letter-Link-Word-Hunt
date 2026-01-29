package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class FixtureMaker{
    public static FixtureDef makeRect(float width, float height){
        width /= 2 * Manager.PPM;
        height /= 2 * Manager.PPM;
        PolygonShape shape = new PolygonShape();
        shape.set(new float[]{
            -width, -height,
            -width, height,
            width, height,
            width, -height
        });


        FixtureDef fixture = new FixtureDef();
        fixture.density = 1f;
        fixture.friction = .5f;
        fixture.restitution = .5f;
        fixture.shape = shape;

        return fixture;
    }

    public static FixtureDef makeRect(float width, float height, float density, float friction, float restitution){
        width /= 2 * Manager.PPM;
        height /= 2 * Manager.PPM;
        PolygonShape shape = new PolygonShape();
        shape.set(new float[]{
            -width, -height,
            -width, height,
            width, height,
            width, -height
        });

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = density;
        fixture.friction = friction;
        fixture.restitution = restitution;

        return fixture;
    }

    public static FixtureDef makePoly(float[] points, float density, float friction, float restitution){
        for(int i = 0; i < points.length; i++){
            points[i] /= Manager.PPM;
        }
        PolygonShape shape = new PolygonShape();
        shape.set(points);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = density;
        fixture.friction = friction;
        fixture.restitution = restitution;

        return fixture;
    }

    public static FixtureDef makePoly(float[] points){
        for(int i = 0; i < points.length; i++){
            points[i] /= Manager.PPM;
        }
        PolygonShape shape = new PolygonShape();
        shape.set(points);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = .5f;
        fixture.restitution = .5f;

        return fixture;
    }

    public static FixtureDef makeCircle(float radius){
        CircleShape shape = new CircleShape();

        shape.setRadius(radius / Manager.PPM);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = .5f;
        fixture.restitution = .5f;

        return fixture;
    }

    public static FixtureDef makeCircle(float radius, float density, float friction, float restitution){
        CircleShape shape = new CircleShape();

        shape.setRadius(radius / Manager.PPM);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = density;
        fixture.friction = friction;
        fixture.restitution = restitution;

        return fixture;
    }
}
