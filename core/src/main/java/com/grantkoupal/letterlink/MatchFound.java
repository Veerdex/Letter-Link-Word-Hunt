package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.grantkoupal.letterlink.backend.MatchStatusResponse;
import com.grantkoupal.letterlink.backend.data.SessionData;
import com.grantkoupal.letterlink.quantum.core.*;
import com.grantkoupal.letterlink.quantum.paint.Textures;
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

    public MatchFound(MatchStatusResponse response) {
        opponentUsername = response.opponentUsername;
        background = new Texture(fl.getPNG("Background"));
        VS = new Texture(fl.getPNG("VS"));
        redBanner = new Texture(fl.getPNG("Red Player Banner"));
        blueBanner = new Texture(fl.getPNG("Blue Player Banner"));
        add(new BackgroundLayer());
        add(new Display());
        this.response = response;
    }

    @Override
    public void initialize() {}

    @Override
    public void restart() {}

    @Override
    public void frame() {
        be.setSpriteBatch(renderer.getSpriteBatch());
        be.setRatioX(.5f);
        be.setRatioY(.5f);
        add(new Timer(.05f, Timer.INDEFINITE, new TimeFrame() {
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
        VS.dispose();
        redBanner.dispose();
        blueBanner.dispose();
    }

    class BackgroundLayer extends Agent {
        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            sb.begin();
            sb.draw(background, 0, 0, Source.getScreenWidth(), Source.getScreenHeight());
            sb.end();
        }

        @Override
        public void dispose() {}
    }

    class Display extends Agent {

        private static final int PHASE_SLIDE    = 0;
        private static final int PHASE_DRIFT    = 1;
        private static final int PHASE_SPEED_UP = 2;

        private static final float SLIDE_SPEED       = 4.5f;
        private static final float DRIFT_SPEED       = 20f;
        private static final float DRIFT_DURATION    = 1.8f;
        private static final float SPEED_ACCEL       = 700f;
        private static final float FADE_SPEED        = 1.4f;
        private static final float VS_PUNCH_DURATION = 1.0f;

        private int phase = PHASE_SLIDE;

        private float blueX;
        private float redX;
        private float blueVel = 0;
        private float redVel  = 0;
        private float driftTimer  = 0;
        private float fadeAlpha   = 0;
        private float vsPunchTime = -1f;
        private float vsScale     = 1f;

        private final BitmapFont font;
        private final GlyphLayout layout = new GlyphLayout();
        private final Texture whitePixel = Textures.getWhitePixel();

        public Display() {
            font = Source.generateFont(
                DataManager.fontName != null ? DataManager.fontName : "Coiny", 240);

            float scale = Source.getScale();
            blueX = Source.getScreenWidth() + blueBanner.getWidth() * scale;
            redX  = -(redBanner.getWidth() * scale);

            add(new Animation(Animation.INDEFINITE, new Action() {
                @Override
                public void run(float delta) {
                    update(delta);
                }
            }));
        }

        @Override
        public void frame() {
            setViewOrder(1);
        }

        private void update(float delta) {
            float centerX = Source.getScreenWidth() / 2f;

            if (phase == PHASE_SLIDE) {
                blueX += (centerX - blueX) * Math.min(delta * SLIDE_SPEED, 1f);
                redX  += (centerX - redX)  * Math.min(delta * SLIDE_SPEED, 1f);
                if (Math.abs(blueX - centerX) < 3f) {
                    blueX = redX = centerX;
                    blueVel = -DRIFT_SPEED;
                    redVel  =  DRIFT_SPEED;
                    vsPunchTime = 0f;
                    phase = PHASE_DRIFT;
                }
            } else if (phase == PHASE_DRIFT) {
                blueX += blueVel * delta;
                redX  += redVel  * delta;
                driftTimer += delta;
                if (driftTimer >= DRIFT_DURATION) {
                    phase = PHASE_SPEED_UP;
                }
            } else if (phase == PHASE_SPEED_UP) {
                blueVel -= SPEED_ACCEL * delta;
                redVel  += SPEED_ACCEL * delta;
                blueX += blueVel * delta;
                redX  += redVel  * delta;
                fadeAlpha = Math.min(fadeAlpha + FADE_SPEED * delta, 1f);
            }

            if (vsPunchTime >= 0f && vsPunchTime < VS_PUNCH_DURATION) {
                vsPunchTime += delta;
                float t = Math.min(vsPunchTime / VS_PUNCH_DURATION, 1f);
                vsScale = 1f + 0.15f * (float) Math.sin(Math.PI * t);
            } else if (vsPunchTime >= VS_PUNCH_DURATION) {
                vsScale = 1f;
            }
        }

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            float screenWidth  = Source.getScreenWidth();
            float screenHeight = Source.getScreenHeight();
            float scale        = Source.getScale();

            float blueW = blueBanner.getWidth()  * scale * 1.5f;
            float blueH = blueBanner.getHeight() * scale * 1.5f;
            float redW  = redBanner.getWidth()   * scale * 1.5f;
            float redH  = redBanner.getHeight()  * scale * 1.5f;

            float blueY = screenHeight * 0.70f;
            float redY  = screenHeight * 0.30f;

            float vsW = VS.getWidth()  * scale * vsScale;
            float vsH = VS.getHeight() * scale * vsScale;

            sb.begin();

            sb.setColor(Color.WHITE);
            sb.draw(blueBanner, blueX - blueW / 2f, blueY - blueH / 2f, blueW, blueH);
            sb.draw(redBanner,  redX  - redW  / 2f, redY  - redH  / 2f, redW,  redH);

            sb.draw(VS, screenWidth / 2f - vsW / 2f, screenHeight / 2f - vsH / 2f, vsW, vsH);

            drawBannerText(sb, opponentUsername != null && !opponentUsername.isEmpty() ? opponentUsername : "Opponent", blueX, blueY, blueW);
            drawBannerText(sb, SessionData.username != null && !SessionData.username.isEmpty() ? SessionData.username : "You", redX, redY, redW);

            if (fadeAlpha > 0) {
                sb.setColor(0, 0, 0, fadeAlpha);
                sb.draw(whitePixel, 0, 0, screenWidth, screenHeight);
                sb.setColor(Color.WHITE);
            }

            sb.end();
        }

        private void drawBannerText(SpriteBatch sb, String text, float centerX, float centerY, float bannerWidth) {
            float targetWidth = bannerWidth * 0.9f;
            font.getData().setScale(1f);
            layout.setText(font, "AAAAAAAAAAAAAAAAAAAA");
            float fixedScale = (targetWidth / layout.width) * 2f;
            layout.setText(font, text);
            float scale = Math.min(fixedScale, targetWidth / layout.width);
            font.getData().setScale(scale);
            layout.setText(font, text);
            font.setColor(Color.WHITE);
            font.draw(sb, text, centerX - layout.width / 2f, centerY + layout.height / 2f);
        }

        @Override
        public void dispose() {}
    }

    static class AshParticle extends Particle {

        private final int multiplier;

        protected AshParticle(boolean top) {
            super(ShapeType.Circle, ParticleType.Shape);
            setAnchorX(.5f);
            enableAlpha();
            if (top) {
                setColor(.25f, .5f, 1, 1);
            } else {
                setColor(1, 0, 0, 1);
            }
            setSpeed(5);
            setDuration(Particle.INDEFINITE);
            multiplier = top ? -1 : 1;
        }

        protected AshParticle me() {
            return this;
        }

        @Override
        protected ParticleRunnable makeParticleRunnable() {
            return (pi, delta, speed, time) -> {
                pi.ym += 10 * delta * multiplier;
                pi.xm += (MathUtils.random() - .5f) * delta * 100;
                pi.y += pi.ym * delta;
                pi.x += MathUtils.clamp(pi.xm * delta, -10 * delta, 10 * delta);
                if (multiplier == 1) {
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
            for (int i = 0; i < 5; i++) {
                ParticleSetup p = new ParticleSetup();
                p.x = Source.getWorldWidth() * MathUtils.random() - Source.getWorldWidth() / 2;
                if (multiplier == 1) {
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
