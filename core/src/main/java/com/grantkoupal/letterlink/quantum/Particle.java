package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public abstract class Particle extends Agent {

    public static enum ShapeType {
        Polygon, Circle
    };

    public static enum ParticleType {
        Shape, Sprite
    };

    private Runnable onEnd;
    private float totalTime = 0;
    private long duration = 3000;
    private float speed = 1;
    private float scale = 1;
    private ShapeType shapeType;
    private ParticleType particleType;
    private List<ParticleInstance> particleInstances = new ArrayList<ParticleInstance>();
    private ParticleRunnable runnable;
    private float particleX = 0;
    private float particleY = 0;
    private Color color = Color.WHITE;

    /**
     * Only called by child, must call separate methods to set up
     */
    protected Particle(ShapeType st, ParticleType pt) {
        shapeType = st;
        particleType = pt;

        runnable = makeParticleRunnable();

        ListIterator<ParticleSetup> iter = makeParticleList().listIterator();
        while (iter.hasNext()) {
            ParticleSetup setup = iter.next();
            particleInstances
                    .add(new ParticleInstance(setup.x, setup.y, setup.xm, setup.ym, setup.r, setup.rm, setup.radius, setup.data));
        }
    }

    protected abstract ParticleRunnable makeParticleRunnable();

    protected abstract LinkedList<ParticleSetup> makeParticleList();

    public void onEnd(Runnable r){
        onEnd = r;
    }

    @Override
    public void setX(float x) {
        particleX = x;
    }

    @Override
    public void setY(float y) {
        particleY = y;
    }

    public float getX() {
        return particleX;
    }

    public float getY() {
        return particleY;
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    public void setDuration(long l) {
        duration = l;
    }

    public void setSpeed(float f) {
        speed = f;
    }

    public void setScale(float f) {
        scale = f;
    }

    public float getScale() {
        return scale;
    }

    public Particle self(){
        return this;
    }

    public Animation getAnimation() {

        Animation a = new Animation(System.nanoTime(), duration, new Action() {
            @Override
            public void run(float delta) {
                totalTime += delta;
                for (int i = 0; i < particleInstances.size(); i++) {
                    if (particleInstances.get(i).update(delta, speed, totalTime)) {
                        particleInstances.remove(i);
                    }
                }
            }
        });
        if (onEnd != null) {
            a.onEnd(onEnd);
        }
        return a;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if (particleType == ParticleType.Shape) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(color);
            for (int i = 0; i < particleInstances.size(); i++) {
                particleInstances.get(i).paint(sr);
            }
            sr.end();
        }
    }

    public class ParticleInstance {
        public float[] distances;
        public float[] rotations;
        public List<Float> data;
        public float radius;
        public float xm;
        public float ym;
        public float x;
        public float y;
        public float r;
        public float rm;

        public ParticleInstance(float x, float y, float xm, float ym, float r, float rm, float radius, List<Float> data) {
            this.radius = radius;
            this.xm = xm;
            this.ym = ym;
            this.x = x;
            this.y = y;
            this.r = r;
            this.rm = rm;
            this.data = data;
        }

        public ParticleInstance(float x, float y, float xm, float ym, float r, float rm, float[] points, List<Float> data) {
            this.xm = xm;
            this.ym = ym;
            this.x = x;
            this.y = y;
            this.r = r;
            this.rm = rm;
            this.data = data;

            distances = new float[points.length / 2];
            rotations = new float[points.length / 2];

            for (int i = 0; i < points.length; i += 2) {
                distances[i / 2] = (float) Math.sqrt(Math.pow(points[i], 2) + Math.pow(points[i + 1], 2));
                rotations[i / 2] = (float) (Math.atan2(points[i + 1], points[i]) + Math.PI / 2);
            }
        }

        public float getParticleX() {
            return particleX;
        }

        public float getParticleY() {
            return particleY;
        }

        public boolean update(float delta, float speed, float time) {
            return runnable.run(this, delta * speed, speed, time);
        }

        public void paint(ShapeRenderer sr) {
            if (shapeType == ShapeType.Polygon) {
                Painter.paintSolid(sr, x * scale + particleX, y * scale + particleY, r, rotations,
                        distances, scale);
            } else {
                sr.circle(x * scale + particleX, y * scale + particleY, radius * scale);
            }
        }
    }

    public static class ParticleSetup {
        public float[] points;
        public float radius;
        public float xm;
        public float ym;
        public float x;
        public float y;
        public float r;
        public float rm;
        public List<Float> data;

        public ParticleSetup(float x, float y, float xm, float ym, float r, float rm, float radius, List<Float> data) {
            this.radius = radius;
            this.x = x;
            this.y = y;
            this.xm = xm;
            this.ym = ym;
            this.r = r;
            this.rm = rm;
            this.data = data;
        }

        public ParticleSetup(float x, float y, float xm, float ym, float r, float rm, float[] points, List<Float> data) {
            this.points = points;
            this.x = x;
            this.y = y;
            this.xm = xm;
            this.ym = ym;
            this.r = r;
            this.rm = rm;
            this.data = data;
        }
    }
}
