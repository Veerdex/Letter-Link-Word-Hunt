package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.grantkoupal.letterlink.backend.MatchStatusResponse;
import com.grantkoupal.letterlink.quantum.core.*;
import com.grantkoupal.letterlink.quantum.particle.Particle;
import com.grantkoupal.letterlink.quantum.particle.ParticleRunnable;

import java.util.LinkedList;

public class MatchFound extends Page {

    private final BatchElement be = new BatchElement();
    private final FileLocator fl = new FileLocator("FindMatch");
    private final MatchStatusResponse response;
    private final String opponentUsername;
    private final Texture background;
    private final Texture VS;
    private final Texture redBanner;
    private final Texture blueBanner;

    public MatchFound(MatchStatusResponse response){
        opponentUsername = response.opponentUsername;
        background = new Texture(fl.getPNG("Background"));
        VS = new Texture(fl.getPNG("VS"));
        redBanner = new Texture(fl.getPNG("Red Player Banner"));
        blueBanner = new Texture(fl.getPNG("Blue Player Banner"));
        add(new Display());
        this.response = response;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void frame() {
        be.setSpriteBatch(renderer.getSpriteBatch());
        be.setRatioX(.5f);
        be.setRatioY(.5f);
        add(new Timer(.05f, Timer.INDEFINITE, new TimeFrame(){
            @Override
            public void run(long iteration) {
                add(new AshParticle(true));
                add(new AshParticle(false));

            }
        }));
    }

    @Override
    public void dispose() {
        background.dispose();
    }

    class Display extends Agent {
        private float bannerOffset = 1000;
        public Display(){

        }

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            float screenWidth = Source.getScreenWidth();
            float screenHeight = Source.getScreenHeight();
            float scale = Source.getScale();
            sb.begin();
            sb.draw(background, 0, 0, screenWidth, screenHeight);
            be.draw(VS, 0, 0);
            be.draw(redBanner, 0, 100);
            sb.end();
        }

        @Override
        public void dispose() {

        }
    }

    static class AshParticle extends Particle {

        private final int multiplier;

        protected AshParticle(boolean top) {
            super(ShapeType.Circle, ParticleType.Shape);
            setAnchorX(.5f);
            enableAlpha();
            if(top) {
                setColor(.25f, .5f, 1, 1);
            } else {
                setColor(1, 0, 0, 1);
            }
            setSpeed(5);
            setDuration(Particle.INDEFINITE);

            multiplier = top ? -1 : 1;
        }

        protected AshParticle me(){
            return this;
        }

        @Override
        protected ParticleRunnable makeParticleRunnable() {
            return (pi, delta, speed, time) -> {
                pi.ym += 10 * delta * multiplier;
                pi.xm += (MathUtils.random() - .5f) * delta * 100;
                pi.y += pi.ym * delta;
                pi.x += MathUtils.clamp(pi.xm * delta, -10 * delta, 10 * delta);
                if(multiplier == 1) {
                    pi.setAlpha((short) (255 - pi.y * 255 / (Source.getWorldHeight() / 4)));
                } else {
                    pi.setAlpha((short) (255 - (Source.getWorldHeight() - pi.y) * 255 / (Source.getWorldHeight() / 4)));
                }

                return (multiplier == -1 && pi.y < -10) ||
                    (multiplier == 1 && pi.y > Source.getWorldHeight() + 10) ||
                    pi.x < -Source.getWorldWidth() / 2 - 10 ||
                    pi.x > Source.getWorldWidth() / 2 + 10;

            };
        }

        @Override
        protected LinkedList<ParticleSetup> makeParticleList() {
            LinkedList<ParticleSetup> particles = new LinkedList<>();
            for(int i = 0; i < 5; i++){
                ParticleSetup p = new ParticleSetup();
                p.x = Source.getWorldWidth() * MathUtils.random() - Source.getWorldWidth() / 2;
                if(multiplier == 1){
                    p.y = -10;
                } else {
                    p.y = Source.getWorldHeight() + 10;
                }
                p.xm = MathUtils.random() - .5f;
                p.ym = MathUtils.random() * multiplier;
                p.radius = 5 * (MathUtils.random() + .5f);
                particles.add(p);
            }

            return particles;
        }
    }
}
