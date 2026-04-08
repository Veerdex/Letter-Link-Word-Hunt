package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.backend.data.SessionData;
import com.grantkoupal.letterlink.quantum.core.*;
import com.grantkoupal.letterlink.quantum.core.Process;
import com.grantkoupal.letterlink.quantum.paint.Textures;

public class MainMenu extends Page {

    private final Texture playTexture;
    private final Texture hamburgerTexture;
    private final Texture gemTexture;
    private final Texture gearTexture;
    private final Texture gamemodeTexture;
    private final Texture coinTexture;
    private final Texture backgroundTexture;
    private final Texture squircleTexture;
    private final Texture rankBackgroundTexture;
    private final Texture usernameBackgroundTexture;
    private Texture rankTexture;
    private Texture iconTexture;
    private Texture board_4x4;
    private Texture board_5x5;
    private Texture board_4x5;
    private Texture board_5x4;
    private Texture casual;
    private Texture competitive;
    private Texture practice;
    private Texture gamemodeDisplay;
    private final Texture whitePixel = Textures.getWhitePixel();
    private final Graphic Play;
    private final Graphic Hamburger;
    private final Graphic Gem;
    private final Graphic Gear;
    private final Graphic Gamemode;
    private final Graphic Coin;
    private final Graphic Background;
    private final Graphic Squircle;
    private final Graphic RankBackground;
    private final Graphic UsernameBackground;
    private final Graphic Icon;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private final Display d;

    public MainMenu(){

        playTexture = getTexture("Play");
        hamburgerTexture = getTexture("Hamburger");
        gemTexture = getTexture("Gem");
        gearTexture = getTexture("Gear");
        gamemodeTexture = getTexture("Gamemode");
        coinTexture = getTexture("Coin");
        backgroundTexture = getTexture("Diamond Background");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        squircleTexture = getTexture("Squircle");
        rankBackgroundTexture = getTexture("Rank Background");
        usernameBackgroundTexture = getTexture("UsernameBackground");
        board_4x4 = getTexture("4x4 Board");
        board_5x5 = getTexture("5x5 Board");
        board_4x5 = getTexture("4x5 Board");
        board_5x4 = getTexture("5x4 Board");
        casual = getTexture("Casual");
        competitive = getTexture("Competitive");
        practice = getTexture("Practice");
        gamemodeDisplay = getTexture("GamemodeDisplay");
        rankTexture = RankHandler.getTextureBasedOffRank();
        iconTexture = DataManager.iconTexture;

        Play = new Graphic(playTexture);
        Hamburger = new Graphic(hamburgerTexture);
        Gem = new Graphic(gemTexture);
        Gear = new Graphic(gearTexture);
        Gamemode = new Graphic(gamemodeTexture);
        Coin = new Graphic(coinTexture);
        Background = new Graphic(backgroundTexture);
        Squircle = new Graphic(squircleTexture);
        RankBackground = new Graphic(rankBackgroundTexture);
        UsernameBackground = new Graphic(usernameBackgroundTexture);
        Icon = new Graphic(iconTexture);

        font = Source.generateFont(DataManager.fontName, 128);
        layout = new GlyphLayout();

        d = new Display();
        add(d);
    }

    @Override
    public void initialize() {

    }

    private Texture getTexture(String name){
        Texture t = new Texture(Source.getAsset("MainMenu/" + name + ".png"));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    @Override
    public void restart() {
        add(d);
    }

    @Override
    public void dispose() {
        playTexture.dispose();
        hamburgerTexture.dispose();
        gemTexture.dispose();
        gearTexture.dispose();
        gamemodeTexture.dispose();
        coinTexture.dispose();
        rankBackgroundTexture.dispose();
        usernameBackgroundTexture.dispose();
        squircleTexture.dispose();
    }

    class Display extends Agent {

        private float centerX = 0;
        private float centerY = 0;
        private float scale = 1;
        private boolean mouseDown = false;
        private float burgerX = 0;
        private float burgerY = 0;
        private float gearX = 0;
        private float gearY = 0;
        private float playX = 0;
        private float playY = 0;
        private float gamemodeX = 0;
        private float gamemodeY = 0;
        private float burgerScale = 1;
        private float gearScale = 1;
        private float playScale = 1;
        private float gamemodeScale = 1;
        private String name = DataManager.userName;
        private float fontScale = 1;

        public Display(){

            if(name.length() > 13){
                name = name.substring(0, 11) + "...";
            }

            float yScale = Source.getScreenHeight() / 3000f;
            float xScale = Source.getScreenWidth() / 1500f;
            scale = Math.min(xScale, yScale);
            centerX = Source.getScreenWidth() / 2f;
            centerY = Source.getScreenHeight() / 2f;
            updatePosition();
            updateScale();
            updateFont();

            addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
                @Override
                public void run(float delta) {

                    buttonUpdate(isClick(), delta);
                }
            }));

            addResize(new Process(){
                @Override
                public boolean run() {
                    float yScale = Source.getScreenHeight() / 3000f;
                    float xScale = Source.getScreenWidth() / 1500f;
                    scale = Math.min(xScale, yScale);
                    centerX = Source.getScreenWidth() / 2f;
                    centerY = Source.getScreenHeight() / 2f;

                    updateFont();
                    updatePosition();
                    updateScale();
                    return true;
                }
            });
        }

        private void buttonUpdate(boolean click, float delta){

            if(isHover(burgerX, burgerY, 300 * scale, 300 * scale)){
                if(burgerScale < 1.1f) {
                    burgerScale += delta * 2;
                } else {
                    burgerScale = 1.1f;
                }
            } else {
                if(burgerScale > 1) {
                    burgerScale -= delta * 2;
                } else {
                    burgerScale = 1;
                }
            }

            if(isHover(gearX, gearY, 300 * scale, 300 * scale)){
                if(gearScale < 1.1f) {
                    gearScale += delta * 2;
                } else {
                    gearScale = 1.1f;
                }
            } else {
                if(gearScale > 1) {
                    gearScale -= delta * 2;
                } else {
                    gearScale = 1;
                }
            }

            if(isHover(playX, playY, 800 * scale, 250 * scale)){
                if(playScale < 1.1f) {
                    playScale += delta * 2;
                } else {
                    playScale = 1.1f;
                }
            } else {
                if(playScale > 1) {
                    playScale -= delta * 2;
                } else {
                    playScale = 1;
                }
            }

            if(isHover(gamemodeX, gamemodeY, 950 * scale, 200 * scale)){
                if(click){
                    //GamemodeSelection gs = new GamemodeSelection();
                    //Source.loadNewPage(gs);
                }
                if(gamemodeScale < 1.1f) {
                    gamemodeScale += delta * 2;
                } else {
                    gamemodeScale = 1.1f;
                }
            } else {
                if(gamemodeScale > 1) {
                    gamemodeScale -= delta * 2;
                } else {
                    gamemodeScale = 1;
                }
            }


            Play.setScale(playScale * 250 * scale / playTexture.getHeight());
            Gamemode.setScale(gamemodeScale * 200 * scale / gamemodeTexture.getHeight());
            Hamburger.setScale(burgerScale * 200 * scale / hamburgerTexture.getHeight());
            Gear.setScale(gearScale * 200 * scale / gearTexture.getHeight());
        }

        private boolean isHover(float x, float y, float width, float height){
            if(Math.abs(Source.getScreenMouseX() - x) < width / 2 && Math.abs(Source.getScreenMouseY() - y) < height / 2){
                return true;
            }
            return false;
        }

        private void updateFont(){
            font.getData().setScale(1);
            layout.setText(font, name);
            font.getData().setScale(scale * 500 / layout.width);
            layout.setText(font, name);
            fontScale = font.getData().scaleX;
        }

        private boolean isClick() {
            if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                if(!mouseDown){
                    mouseDown = true;
                    return true;
                }
            } else if(mouseDown){
                mouseDown = false;
            }
            return false;
        }

        private void updatePosition(){
            gearX = centerX * 2 - 200 * scale;
            gearY = centerY * 2 - 750 * scale;
            burgerX = centerX * 2 - 200 * scale;
            burgerY = centerY * 2 - 425 * scale;
            playX = centerX;
            playY = centerY - 750 * scale;
            gamemodeX = centerX;
            gamemodeY = centerY - 1000 * scale;

            Play.setCenter(playX, playY);
            Gamemode.setCenter(centerX, gamemodeY);
            Hamburger.setCenter(burgerX, burgerY);
            Gear.setCenter(gearX, gearY);
            UsernameBackground.setCenter(centerX, centerY * 2 - 400 * scale);
            RankBackground.setCenter(centerX, centerY * 2 - 750 * scale);
        }

        private void updateScale() {
            UsernameBackground.setScale(225 * scale / usernameBackgroundTexture.getHeight());
            RankBackground.setScale(400 * scale / rankBackgroundTexture.getHeight());
        }

        private void drawSquircles(SpriteBatch sb){
            sb.setColor(.75f, .75f, .75f, 1);
            sb.draw(squircleTexture, gearX - 150 * scale * gearScale, gearY - 150 * scale * gearScale, 300 * scale * gearScale, 300 * scale * gearScale);
            sb.draw(squircleTexture, burgerX - 150 * scale * burgerScale, burgerY - 150 * scale * burgerScale, 300 * scale * burgerScale, 300 * scale * burgerScale);
            sb.setColor(Color.WHITE);
        }

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            sb.begin();
            drawBackground(sb);
            borderGradient(sb);
            drawSquircles(sb);
            Play.draw(sb);
            Gamemode.draw(sb);
            Hamburger.draw(sb);
            Gear.draw(sb);
            UsernameBackground.draw(sb);
            RankBackground.draw(sb);
            drawRank(sb);
            drawFont(sb);
            drawGamemodeDisplay(sb);
            sb.end();
        }

        private void drawGamemodeDisplay(SpriteBatch sb){
            drawBoard(sb);
            drawEmblem(sb);
            Color c;
            switch(SessionData.currentGamemode){
                case "casual" :c = Color.GREEN; break;
                case "competitive" :c = Color.RED; break;
                default:c = Color.CYAN;
            }
            sb.setColor(c);
            sb.draw(gamemodeDisplay, centerX - gamemodeDisplay.getWidth() / 2f * scale, centerY - gamemodeDisplay.getHeight() / 2f * scale, gamemodeDisplay.getWidth() * scale, gamemodeDisplay.getHeight() * scale);
            drawText(sb);
        }

        private void drawText(SpriteBatch sb){
            String text;
            switch(SessionData.currentBoardHeight + SessionData.currentBoardWidth){
                case 8 :text = "4x4"; break;
                case 9 :text = "4x5"; break;
                default:text = "5x5";
            }
            font.getData().setScale(scale * 1.4f);
            font.setColor(Color.BLACK);
            layout.setText(font, text);
            font.draw(sb, text, centerX - layout.width / 2, centerY + layout.height / 2 + 375 * scale);
        }

        private void drawBoard(SpriteBatch sb){
            Texture temp;
            switch(SessionData.currentBoardWidth + SessionData.currentBoardHeight){
                case 8 :temp = board_4x4; break;
                case 9 :temp = board_4x5; break;
                default:temp = board_5x5; break;
            }
            sb.draw(temp, centerX - 400 * scale, centerY - 425 * scale, 800 * scale, 800 * scale);
        }

        private void drawEmblem(SpriteBatch sb){
            Texture temp;
            switch(SessionData.currentGamemode){
                case "practice" :temp = practice; break;
                case "competitive" :temp = competitive; break;
                default : temp = casual;
            }
            sb.draw(temp, centerX - 65.5f * scale, centerY - 416.5f * scale - 65 * scale, 130 * scale, 130 * scale);
        }

        private void drawBackground(SpriteBatch sb){
            sb.setColor(.8f, 1f, 1f, 1);
            sb.draw(backgroundTexture,
                0, 0, 0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                1, 1, 0,
                0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                false, false);
            sb.setColor(Color.WHITE);
        }

        private void drawRank(SpriteBatch sb){
            float s = Math.min(300 * scale / rankTexture.getWidth(), 300 * scale / rankTexture.getHeight());
            sb.draw(rankTexture, centerX - rankTexture.getWidth() * s / 2, centerY * 2 - 750 * scale - rankTexture.getHeight() * s / 2, rankTexture.getWidth() * s, rankTexture.getHeight() * s);
        }

        private void drawFont(SpriteBatch sb){
            font.setColor(Color.WHITE);
            font.getData().setScale(fontScale);
            layout.setText(font, name);
            font.draw(sb, name, centerX - layout.width / 2, centerY * 2 - 400 * scale + layout.height / 2);
        }

        private void borderGradient(SpriteBatch sb) {
            float borderSize = 100 * scale;
            float screenWidth = Source.getScreenWidth();
            float screenHeight = Source.getScreenHeight();

            int steps = 20;
            float stepSize = borderSize / steps;

            for (int i = 0; i < steps; i++) {
                float alpha = 1.0f - (i / (float) steps);
                float offset = i * stepSize;

                sb.setColor(0, 0, 0, alpha);

                // Left border
                sb.draw(whitePixel,
                    offset, 0,
                    stepSize, screenHeight);

                // Right border
                sb.draw(whitePixel,
                    screenWidth - offset - stepSize, 0,
                    stepSize, screenHeight);

                // Bottom border
                sb.draw(whitePixel,
                    0, offset,
                    screenWidth, stepSize);

                // Top border
                sb.draw(whitePixel,
                    0, screenHeight - offset - stepSize,
                    screenWidth, stepSize);
            }

            sb.setColor(Color.WHITE);
        }

        @Override
        public void dispose() {

        }
    }
}
