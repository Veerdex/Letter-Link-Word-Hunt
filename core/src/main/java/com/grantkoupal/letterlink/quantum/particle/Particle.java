package com.grantkoupal.letterlink.quantum.particle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.grantkoupal.letterlink.quantum.core.Action;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Animation;
import com.grantkoupal.letterlink.quantum.core.Manager;
import com.grantkoupal.letterlink.quantum.paint.Painter;

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

    public static final int INDEFINITE = -1;
    private Runnable onEnd;
    private float totalTime = 0;
    private long duration = 3000;
    private float speed = 1;
    private float scale = 1;
    private final ShapeType shapeType;
    private final ParticleType particleType;
    private final List<ParticleInstance> particleInstances = new ArrayList<ParticleInstance>();
    private final ParticleRunnable runnable;
    private float particleX = 0;
    private float particleY = 0;
    private float anchorX = 0;
    private float anchorY = 0;
    private Color color = Color.WHITE;
    private boolean alphaEnabled = false;
    private boolean finished = false;

    /**
     * Only called by child, must call separate methods to set up
     */
    protected Particle(ShapeType st, ParticleType pt) {
        shapeType = st;
        particleType = pt;

        runnable = makeParticleRunnable();
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

    public void setAnchorX(float x){
        anchorX = x;
    }

    public float getAnchorX(){
        return anchorX;
    }

    public void setAnchorY(float y){
        anchorY = y;
    }

    public float getAnchorY(){
        return anchorY;
    }

    public void setColor(Color c) {
        color = c;
    }

    public void setColor(float r, float g, float b, float a){
        color = new Color(r, g, b, a);
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

    public void enableAlpha(){
        alphaEnabled = true;
    }

    public void disableAlpha(){
        alphaEnabled = false;
    }

    public void stop(){
        finished = true;
    }

    public Animation getAnimation() {

        ListIterator<ParticleSetup> iter = makeParticleList().listIterator();
        while (iter.hasNext()) {
            particleInstances.add(new ParticleInstance(iter.next()));
        }

        Animation a = new Animation(duration, new Action() {
            @Override
            public void run(float delta) {
                totalTime += delta;
                if(finished){
                    stop();
                    return;
                }
                for (int i = 0; i < particleInstances.size(); i++) {
                    if (particleInstances.get(i).update(delta, speed, totalTime)) {
                        particleInstances.remove(i);
                        i--;
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

    private final Color tempColor = new Color(1, 1, 1, 1);
    private short colorID = 255;

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if (particleType == ParticleType.Shape && !particleInstances.isEmpty()) {
            if(alphaEnabled){
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            sr.begin(ShapeRenderer.ShapeType.Filled);
            mixColor(particleInstances.get(0));
            sr.setColor(tempColor);
            for (int i = 0; i < particleInstances.size(); i++) {
                if(particleInstances.get(i).colorID != colorID){
                    mixColor(particleInstances.get(i));
                    colorID = particleInstances.get(i).colorID;
                    sr.setColor(tempColor);
                }
                particleInstances.get(i).paint(sr);
            }
            sr.end();
        } else {
            stop();
            parent.remove(this);
        }
    }

    private void mixColor(ParticleInstance p){
        tempColor.r = MathUtils.clamp(p.red / 255f * color.r, 0, 1);
        tempColor.g = MathUtils.clamp(p.green / 255f * color.g, 0, 1);
        tempColor.b = MathUtils.clamp(p.blue / 255f * color.b, 0, 1);
        tempColor.a = MathUtils.clamp(p.alpha / 255f * color.a, 0, 1);
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
        protected short red;
        protected short green;
        protected short blue;
        protected short alpha;
        protected short colorID;

        public ParticleInstance(ParticleSetup p) {
            this.radius = p.radius;
            this.xm = p.xm;
            this.ym = p.ym;
            this.x = p.x;
            this.y = p.y;
            this.r = p.r;
            this.rm = p.rm;
            this.data = p.data;
            this.red = p.red;
            this.green = p.green;
            this.blue = p.blue;
            this.alpha = p.alpha;
            this.colorID = p.colorID;

            if(p.points != null){
                distances = new float[p.points.length / 2];
                rotations = new float[p.points.length / 2];

                for (int i = 0; i < p.points.length; i += 2) {
                    distances[i / 2] = (float) Math.sqrt(Math.pow(p.points[i], 2) + Math.pow(p.points[i + 1], 2));
                    rotations[i / 2] = (float) (Math.atan2(p.points[i + 1], p.points[i]) + Math.PI / 2);
                }
            }
        }

        public void setRed(short r){
            red = r;
            recalculateColorID();
        }

        public short getRed(){
            return red;
        }

        public void setGreen(short g){
            green = g;
            recalculateColorID();
        }

        public short getGreen(){
            return green;
        }

        public void setBlue(short b){
            blue = b;
            recalculateColorID();
        }

        public short getBlue(){
            return blue;
        }

        public void setAlpha(short a){
            alpha = a;
            recalculateColorID();
        }

        public short getAlpha(){
            return alpha;
        }

        public float getParticleX() {
            return particleX;
        }

        public float getParticleY() {
            return particleY;
        }

        private void recalculateColorID(){
            colorID = (short)((red + green + blue + alpha) / 4);
        }

        public boolean update(float delta, float speed, float time) {
            return runnable.run(this, delta * speed, speed, time);
        }

        public void paint(ShapeRenderer sr) {
            float screenScale = Manager.getScale();
            if (shapeType == ShapeType.Polygon) {
                Painter.paintSolid(sr, (x * scale + particleX + (anchorX * Manager.getWorldWidth())) * screenScale, (y * scale + particleY + (anchorY * Manager.getWorldHeight())) * screenScale, r, rotations,
                        distances, scale);
            } else {
                sr.circle((x * scale + particleX + (anchorX * Manager.getWorldWidth())) * screenScale, (y * scale + particleY + (anchorY * Manager.getWorldHeight())) * screenScale, radius * scale * screenScale);
            }
        }
    }

    public static class ParticleSetup {
        protected float[] points;
        public float radius;
        public float xm;
        public float ym;
        public float x;
        public float y;
        public float r;
        public float rm;
        protected short red = 255;
        protected short green = 255;
        protected short blue = 255;
        protected short alpha = 255;
        protected short colorID = 63;
        public List<Float> data = new ArrayList<>();

        public void setPoints(float... points){
            if(points.length % 2 != 0)
                throw new IllegalArgumentException("Must provide an even number of points");

            this.points = points;
        }

        public void setColor(short r, short g, short b, short a){
            red = r;
            green = g;
            blue = b;
            alpha = a;
            colorID = (short)((red + green + blue + alpha) / 4);
        }
    }
}
