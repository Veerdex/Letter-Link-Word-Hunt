package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.*;

import java.util.ArrayList;
import java.util.List;

public class WordOverview extends Page {

    public enum WordState {FOUND, NOTFOUND, MISSED}

    private BitmapFont font;
    private Texture backTexture;
    private Texture backgroundTexture;
    private Texture wordBackgroundTexture;
    private Texture leftArrowTexture;
    private Texture rightArrowTexture;
    private final Graphic backButton;
    private final Graphic wordBackground;
    private final Graphic leftArrow;
    private final Graphic rightArrow;
    private final GlyphLayout layout;
    private int mode = 0; // 0 - Found Words, 1 - Missed Words, 2 - Not Found Words

    public WordOverview(){
        instantiateArrows();
        instantiateTexture();
        instantiateBackgroundTexture();
        instantiateWordBackground();
        backButton = new Graphic(backTexture);
        wordBackground = new Graphic(wordBackgroundTexture);
        leftArrow = new Graphic(leftArrowTexture);
        rightArrow = new Graphic(rightArrowTexture);
        layout = new GlyphLayout();

        add(new Display());
    }

    private void instantiateArrows(){
        leftArrowTexture = new Texture(Source.getAsset("Misc/LeftMedievalArrow.png"));
        rightArrowTexture = new Texture(Source.getAsset("Misc/RightMedievalArrow.png"));
        leftArrowTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        rightArrowTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private void instantiateTexture() {
        backTexture = DataManager.backButtonTexture;
    }

    private void instantiateBackgroundTexture(){
        backgroundTexture = DataManager.backgroundTexture;
    }

    private void instantiateWordBackground(){
        wordBackgroundTexture = new Texture(Source.getAsset("Misc/WordBackground.png"));
        wordBackgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void initialize() {
        font = Source.generateFont("Cinzel", 128);
    }

    @Override
    public void restart() {

    }

    @Override
    public void dispose() {
        wordBackgroundTexture.dispose();
    }

    class Display extends Agent {

        private static final float SCREEN_HEIGHT_REFERENCE = 3000f;
        private static final float SCREEN_WIDTH_REFERENCE = 1500f;
        private static final float SCROLL_FRICTION = .1f;

        private static final float DIM_BRIGHTNESS = .75f;

        private float scale = 1;
        private float centerX = 0;
        private float centerY = 0;
        private float backButtonScale = 1;
        private boolean mouseDown = true;
        private float scroll = 0;
        private float scrollMotion = 0;
        private static final int NUM_WORDS = 19;
        private long lastClick = 0;
        private int currentListSize = 0;
        private final List<String> foundWords = new ArrayList<>();
        private final List<String> missedWords = new ArrayList<>();
        private final List<String> notFoundWords = new ArrayList<>();
        private int clickX = 0;
        private int clickY = 0;
        private String title;

        public Display(){

            for(int i = 0; i < Board.getWordsInBoard().size(); i++){
                String word = Board.getWordsInBoard().get(i);
                if(Board.getListOfWordsFound().contains(word)){
                    foundWords.add(word);
                } else {
                    if(Solver.isCommon(word)){
                        missedWords.add(word);
                    } else {
                        notFoundWords.add(word);
                    }
                }
            }

            updateTitle();

            currentListSize = foundWords.size();

            addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
                @Override
                public void run(float delta) {
                    boolean click = false;
                    if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                        if(!mouseDown) {
                            click = true;
                            mouseDown = true;
                            lastClick = System.currentTimeMillis();
                            clickX = Source.getScreenMouseX();
                            clickY = Source.getScreenMouseY();
                        }
                    } else {
                        if(mouseDown && System.currentTimeMillis() - lastClick < 150 &&
                            Math.abs(clickX - Source.getScreenMouseX()) < 10 * scale &&
                            Math.abs(clickY - Source.getScreenMouseY()) < 10 * scale &&
                            Math.abs(centerX - Source.getScreenMouseX()) < 600 * scale &&
                            Math.abs((centerY - 225 * scale) - Source.getScreenMouseY()) < 1200 * scale){
                            if(Source.getScreenMouseX() > centerX){
                                mode++;
                            } else {
                                mode--;
                            }
                            if(mode >= 3){
                                mode = 0;
                            } else if(mode <= -1){
                                mode = 2;
                            }
                            switch(mode){
                                case 0 : currentListSize = foundWords.size(); break;
                                case 1 : currentListSize = missedWords.size(); break;
                                case 2 : currentListSize = notFoundWords.size();
                            }
                            updateTitle();
                        }
                        mouseDown = false;
                    }
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                        if(Math.abs(centerX - Source.getScreenMouseX()) < 600 * scale && Math.abs((centerY - 225 * scale) - Source.getScreenMouseY()) < 1200 * scale){
                            scrollMotion = -Gdx.input.getDeltaY() * scale;
                        }
                    }
                    if(Manager.isOnDesktop()) {
                        scroll += scrollMotion * delta * 15;
                    } else {
                        scroll += scrollMotion * delta;
                    }
                    if(scroll < 0){
                        scroll = 0;
                        scrollMotion = 0;
                    } else if(scroll > currentListSize - NUM_WORDS){
                        scroll = currentListSize - NUM_WORDS;
                        scrollMotion = 0;
                    }
                    applyScrollFriction(delta);
                    float yPos = centerY * 2 - 300 * scale;
                    if(Math.abs(Source.getScreenMouseX() - centerX) < 400 * scale && Math.abs(Source.getScreenMouseY() - yPos) < 150 * scale){
                        if(click){
                            backButtonScale = 1;
                            Source.previousPage();
                        }
                        if(!Manager.isOnDesktop()) return;
                        if(backButtonScale < 1.2) {
                            backButtonScale += delta * 2;
                        } else {
                            backButtonScale = 1.2f;
                        }
                    } else {
                        if(backButtonScale > 1) {
                            backButtonScale -= delta * 2;
                        } else {
                            backButtonScale = 1;
                        }
                    }
                }
            }));
        }

        private void updateTitle(){
            if(mode == 0){
                title = "Found (" + foundWords.size() + ")";
            } else if(mode == 1){
                title = "Missed (" + missedWords.size() + ")";
            } else {
                title = "Not Found (" + notFoundWords.size() + ")";
            }
        }

        /**
         * Applies friction to gradually slow down scrolling.
         */
        private void applyScrollFriction(float delta) {
            scrollMotion *= (float) Math.pow(SCROLL_FRICTION, delta);
        }

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {

            centerX = Source.getScreenWidth() / 2f;
            centerY = Source.getScreenHeight() / 2f;
            scale = calculateScale();

            sb.begin();
            drawBackground(sb);
            drawBackButton(sb);
            drawWordBackground(sb);
            drawFont(sb);
            drawTitle(sb);
            drawArrows(sb);
            sb.end();
        }

        private void drawArrows(SpriteBatch sb){
            leftArrow.setColor(.5f, .5f, .5f, 1);
            rightArrow.setColor(.5f, .5f, .5f, 1);
            leftArrow.setScale(.5f * scale, scale);
            rightArrow.setScale(.5f * scale, scale);
            leftArrow.setCenter(centerX - 550 * scale, centerY - 225 * scale);
            rightArrow.setCenter(centerX + 550 * scale, centerY - 225 * scale);
            leftArrow.draw(sb);
            rightArrow.draw(sb);
        }

        private void drawTitle(SpriteBatch sb){
            font.setColor(Color.GOLD);
            font.getData().setScale(1.5f * scale);
            layout.setText(font, title);
            font.getData().setScale(700 * scale / layout.width);
            layout.setText(font, title);
            font.draw(sb, title, centerX - layout.width / 2, centerY + 850 * scale);
        }

        private void drawFont(SpriteBatch sb){
            font.getData().setScale(scale * .75f);

            switch(mode){
                case 0 : drawFoundWords(sb); return;
                case 1 : drawMissedWords(sb); return;
                case 2 : drawNotFoundWords(sb);
            }
        }

        private void drawNotFoundWords(SpriteBatch sb){
            font.setColor(1, 0, 0, 1 - scroll % 1);
            int a = Math.max((int)scroll, 0);
            int max = Math.min(NUM_WORDS + (int)scroll, notFoundWords.size());
            int i = 0;
            if(a >= max) return;
            layout.setText(font, notFoundWords.get(a));
            font.draw(sb, notFoundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
            a++; i++;
            font.setColor(1, 0, 0, 1);
            while(a < max - 1){
                layout.setText(font, notFoundWords.get(a));
                font.draw(sb, notFoundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
                i++; a++;
            }
            if(a >= max) return;
            font.setColor(1, 0, 0, scroll % 1);
            layout.setText(font, notFoundWords.get(a));
            font.draw(sb, notFoundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
        }

        private void drawMissedWords(SpriteBatch sb){
            font.setColor(1, 1, 0, 1 - scroll % 1);
            int a = Math.max((int)scroll, 0);
            int max = Math.min(NUM_WORDS + (int)scroll, missedWords.size());
            int i = 0;
            if(a >= max) return;
            layout.setText(font, missedWords.get(a));
            font.draw(sb, missedWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
            a++; i++;
            font.setColor(1, 1, 0, 1);
            while(a < max - 1){
                layout.setText(font, missedWords.get(a));
                font.draw(sb, missedWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
                i++; a++;
            }
            if(a >= max) return;
            font.setColor(1, 1, 0, scroll % 1);
            layout.setText(font, missedWords.get(a));
            font.draw(sb, missedWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
        }

        private void drawFoundWords(SpriteBatch sb){
            font.setColor(0, 1, 0, 1 - scroll % 1);
            int a = Math.max((int)scroll, 0);
            int max = Math.min(NUM_WORDS + (int)scroll, foundWords.size());
            int i = 0;
            if(a >= max) return;
            layout.setText(font, foundWords.get(a));
            font.draw(sb, foundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
            a++; i++;
            font.setColor(0, 1, 0, 1);
            while(a < max - 1){
                layout.setText(font, foundWords.get(a));
                font.draw(sb, foundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
                i++; a++;
            }
            if(a >= max) return;
            font.setColor(0, 1, 0, scroll % 1);
            layout.setText(font, foundWords.get(a));
            font.draw(sb, foundWords.get(a), centerX - layout.width / 2, centerY - (i - scroll % 1) * 100 * scale + 550 * scale);
        }

        private void drawWordBackground(SpriteBatch sb){
            wordBackground.setCenter(centerX, centerY - 225 * scale);
            wordBackground.setScale(2500 * scale / wordBackgroundTexture.getHeight());
            wordBackground.draw(sb);
        }

        private void drawBackground(SpriteBatch sb) {
            // Draw tiled background
            sb.setColor(0.5f, 0.5f, 0.5f, 1);
            sb.draw(backgroundTexture,
                0, 0, 0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                1, 1, 0,
                0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                false, false);
        }

        private void drawBackButton(SpriteBatch sb){
            backButton.setCenter(centerX, centerY * 2 - 300 * scale);
            backButton.setScale(300 * scale / backTexture.getHeight() * backButtonScale);
            backButton.draw(sb);
        }

        private float calculateScale() {
            float yScale = Source.getScreenHeight() / SCREEN_HEIGHT_REFERENCE;
            float xScale = Source.getScreenWidth() / SCREEN_WIDTH_REFERENCE;
            return Math.min(xScale, yScale);
        }

        @Override
        public void dispose() {

        }
    }
}
