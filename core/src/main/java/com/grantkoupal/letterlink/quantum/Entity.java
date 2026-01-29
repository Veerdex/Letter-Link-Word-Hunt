package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Entity extends Agent {

    public BodyDef bodyDef = new BodyDef();
    public Body body;
    protected Color color;

    private CollisionRunnable startCollision;
    private CollisionRunnable endCollision;

    /**
     * @param bodyType
     * @param spawnX
     * @param spawnY
     * @param physicalObject
     */
    public Entity(BodyDef.BodyType bodyType, float spawnX, float spawnY, FixtureDef physicalObject, Color c) {

        isEntity = true;
        bodyDef.type = bodyType;
        bodyDef.position.set(spawnX / Manager.PPM, spawnY / Manager.PPM);

        physicalObject.filter.categoryBits = Manager.CONTACT_CHANNEL;

        color = c;

        body = Manager.mainWorld.createBody(bodyDef);
        body.createFixture(physicalObject);
        body.setUserData(this);

        if(bodyType == BodyDef.BodyType.DynamicBody){
            body.setAngularVelocity(1f);
        }
    }

    public void setLightChannel(short channel){
        if(body != null){
            for(Fixture fixture : body.getFixtureList()){
                fixture.getFilterData().categoryBits = channel;
            }
        }
    }

    public void setStartCollision(CollisionRunnable r){
        startCollision = r;
    }

    public void setEndCollision(CollisionRunnable r){
        endCollision = r;
    }

    public void onCollisionStart(Entity target, Contact contact, ContactImpulse impulse, boolean side){
        if(startCollision != null){
            startCollision.run(target, contact, impulse, side);
        }
    }

    public void onCollisionEnd(Entity target, Contact contact, ContactImpulse impulse, boolean side){
        if(endCollision != null){
            endCollision.run(target, contact, impulse, side);
        }
    }

    @Override
    public void delete() {
        dispose();
    }

    @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if (body == null) {
            return;
        }
        Transform transform = body.getTransform();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(color);
        for(int k = 0; k < body.getFixtureList().size; k++){
            if(body.getFixtureList().get(k).isSensor()){
                continue;
            }
            switch (body.getFixtureList().get(k).getType()) {
                case Polygon:
                    PolygonShape polygon = (PolygonShape) body.getFixtureList().get(k).getShape();
                    Vector2[] vertices = new Vector2[polygon.getVertexCount()];
                    Vector2 temp = new Vector2();
                    Vector2 center = new Vector2();

                    for (int i = 0; i < polygon.getVertexCount(); i++) {
                        polygon.getVertex(i, temp);
                        transform.mul(temp); // world space
                        vertices[i] = new Vector2(temp);
                        center.add(vertices[i]);
                    }

                    center.scl(1f / vertices.length);

                    for (int i = 0; i < vertices.length; i++) {
                        Vector2 v1 = vertices[i];
                        Vector2 v2 = vertices[(i + 1) % vertices.length];
                        sr.triangle(center.x * Manager.PPM, center.y * Manager.PPM, v1.x * Manager.PPM, v1.y * Manager.PPM, v2.x * Manager.PPM, v2.y * Manager.PPM);
                    }
                    break;

                case Circle:
                    CircleShape circle = (CircleShape) body.getFixtureList().get(k).getShape();
                    Vector2 centerCircle = new Vector2(circle.getPosition());
                    transform.mul(centerCircle);
                    float radius = circle.getRadius();

                    sr.circle(centerCircle.x * Manager.PPM, centerCircle.y * Manager.PPM, radius * Manager.PPM, 30); // 30 segments
                    break;

                case Edge:
                    EdgeShape edge = (EdgeShape) body.getFixtureList().get(k).getShape();
                    Vector2 v0 = new Vector2();
                    Vector2 v1 = new Vector2();
                    edge.getVertex1(v0);
                    edge.getVertex2(v1);
                    transform.mul(v0);
                    transform.mul(v1);

                    sr.line(v0.scl(Manager.PPM), v1.scl(Manager.PPM));
                    break;

                case Chain:
                    ChainShape chain = (ChainShape) body.getFixtureList().get(k).getShape();
                    int count = chain.getVertexCount();
                    Vector2 a = new Vector2();
                    Vector2 b = new Vector2();

                    for (int i = 0; i < count - 1; i++) {
                        chain.getVertex(i, a);
                        chain.getVertex(i + 1, b);
                        transform.mul(a);
                        transform.mul(b);
                        sr.line(a.scl(Manager.PPM), b.scl(Manager.PPM));
                    }
                    break;
            }
        }
        sr.end();
    }
}
