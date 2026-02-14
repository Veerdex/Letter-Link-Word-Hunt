package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.*;

public class MainMenu extends Page {

    private final Texture playTexture;
    private final Texture leaderboardTexture;
    private final Texture statsTexture;
    private final Texture settingsTexture;
    private final Texture exitTexture;
    private final Texture portraitTexture;
    private final Texture landscapeTexture;
    private final Texture frameTexture;
    private final Texture titleTexture;
    private final Graphic playButton;
    private final Graphic leaderboardButton;
    private final Graphic statsButton;
    private final Graphic settingsButton;
    private final Graphic exitButton;
    private final Graphic portraitBackground;
    private final Graphic landscapeBackground;
    private final Graphic frame;
    private final Graphic title;

    private final Display d;

    public MainMenu(){
        playTexture = getTexture("Play");
        leaderboardTexture = getTexture("Leaderboard");
        statsTexture = getTexture("Stats");
        settingsTexture = getTexture("Settings");
        exitTexture = getTexture("Exit");
        portraitTexture = getTexture("BackgroundPortrait");
        landscapeTexture = getTexture("BackgroundLandscape");
        frameTexture = getTexture("Frame");
        titleTexture = getTexture("Title");

        playButton = new Graphic(playTexture);
        leaderboardButton = new Graphic(leaderboardTexture);
        statsButton = new Graphic(statsTexture);
        settingsButton = new Graphic(settingsTexture);
        exitButton = new Graphic(exitTexture);
        portraitBackground = new Graphic(portraitTexture);
        landscapeBackground = new Graphic(landscapeTexture);
        frame = new Graphic(frameTexture);
        title = new Graphic(titleTexture);

        d = new Display();
        add(d);
    }

    @Override
    public void initialize() {

    }

    private Texture getTexture(String name){
        return new Texture(Source.getAsset("MainMenu/" + name + ".png"));
    }

    @Override
    public void restart() {
        add(d);
    }

    @Override
    public void dispose() {
        exitTexture.dispose();
        leaderboardTexture.dispose();
        playTexture.dispose();
        settingsTexture.dispose();
        statsTexture.dispose();
        frameTexture.dispose();
        portraitTexture.dispose();
        landscapeTexture.dispose();
        titleTexture.dispose();
    }

    class Display extends Agent {

        private float centerX = 0;
        private float centerY = 0;
        private float scale = 1;

        public Display(){
            addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
                @Override
                public void run(float delta) {
                    centerX = Source.getScreenWidth() / 2f;
                    centerY = Source.getScreenHeight() / 2f;

                    updateScale();
                    updatePosition();
                }
            }));
        }

        private void updatePosition(){
            playButton.setCenter(centerX, centerY + 600 * scale);
            leaderboardButton.setCenter(centerX, centerY + 200 * scale);
            statsButton.setCenter(centerX, centerY - 150 * scale);
            settingsButton.setCenter(centerX, centerY - 450 * scale);
            exitButton.setCenter(centerX, centerY - 700 * scale);

            landscapeBackground.setCenter(centerX, centerY);
            portraitBackground.setCenter(centerX, centerY);
            frame.setCenter(centerX, centerY);
            title.setCenter(centerX, centerY + 1000 * scale);
        }

        private void updateScale() {
            float yScale = Source.getScreenHeight() / 3000f;
            float xScale = Source.getScreenWidth() / 1500f;
            scale = Math.min(xScale, yScale);

            playButton.setScale(scale * 1.5f);
            leaderboardButton.setScale(scale * 1.25f);
            exitButton.setScale(scale);
            settingsButton.setScale(scale * 1.1f);
            statsButton.setScale(scale * 1.25f);
            frame.setScale(2000 * scale / frameTexture.getHeight());
            title.setScale(scale * 1.5f);

            if(Source.getScreenHeight() > Source.getScreenWidth() * 1.5f) {
                portraitBackground.setScale((float) Source.getScreenHeight() / portraitTexture.getHeight());
            } else {
                portraitBackground.setScale((float) Source.getScreenWidth() / portraitTexture.getWidth());
            }
            if(Source.getScreenWidth() > Source.getScreenHeight() * 1.5f) {
                landscapeBackground.setScale((float) Source.getScreenWidth() / landscapeTexture.getWidth());
            } else {
                landscapeBackground.setScale((float) Source.getScreenHeight() / landscapeTexture.getHeight());
            }
        }

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            sb.begin();
            if(Source.getScreenWidth() > Source.getScreenHeight()){
                landscapeBackground.draw(sb);
            } else {
                portraitBackground.draw(sb);
            }
            frame.draw(sb);
            title.draw(sb);
            playButton.draw(sb);
            leaderboardButton.draw(sb);
            if(Source.isOnDesktop()) {
                exitButton.draw(sb);
            }
            settingsButton.draw(sb);
            statsButton.draw(sb);
            sb.end();
        }

        @Override
        public void dispose() {

        }
    }
}
