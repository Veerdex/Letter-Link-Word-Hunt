package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Action;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Animation;

import java.util.List;

/**
 * Displays a scrollable table of words that the player has found.
 * Renders on the right side of the screen and supports mouse scrolling.
 */
public class GuessTable extends Agent {

    private final int VISIBLE_ROWS = 8;

    // ========== Graphics ==========
    private BitmapFont font;
    private GlyphLayout fontLayout;
    private final int fontSize = 32;

    // ========== Data ==========
    private List<String> listOfWordsFound;
    private int listSize;

    // ========== Scroll State ==========
    private float scroll = 0;
    private float scrollMotion = 0;

    // ========== Layout ==========
    private float scale = 1;
    private float hintX = 0;
    private float hintY = 0;

    // ========== Constructor ==========

    /**
     * Creates a new GuessTable to display found words.
     * @param listOfWordsFound List of words the player has discovered
     */
    public GuessTable(List<String> listOfWordsFound) {
        this.listOfWordsFound = listOfWordsFound;
        this.listSize = listOfWordsFound.size();

        fontLayout = new GlyphLayout();
        initializeFont();
        setupScrollAnimation();
    }

    // ========== Initialization ==========

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, fontSize);
    }

    /**
     * Sets up the scroll animation that responds to mouse input on the right side.
     */
    private void setupScrollAnimation() {
        Source.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                if(Board.menuOpen) return;
                handleScrollInput(delta);
            }
        }));
    }

    // ========== Scroll Handling ==========

    /**
     * Handles mouse scrolling when hovering over the table area.
     */
    private void handleScrollInput(float delta) {
        boolean isOverTable = Source.getScreenMouseX() > hintX &&
            Source.getScreenMouseX() > Source.getScreenWidth() / 2f &&
            Source.getScreenMouseY() < Source.getScreenHeight() / 2f - scale * 700;

        if (isOverTable && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            scrollMotion = Gdx.input.getDeltaY();
        }

        setScroll(scroll + scrollMotion * delta);
        scrollMotion *= (float) Math.pow(0.1f, delta);
    }

    /**
     * Sets the scroll position with bounds checking.
     * @param f New scroll position
     */
    public void setScroll(float f) {
        int maxScroll = Math.max(0, listOfWordsFound.size() - 7);

        if (f < 0) {
            scroll = 0;
            scrollMotion = 0;
        } else if (f > maxScroll) {
            scroll = maxScroll;
            scrollMotion = 0;
        } else {
            scroll = f;
        }
    }

    // ========== Drawing ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
        handleNewWordAdded();
        render(sb);
    }

    /**
     * Calculates screen-relative layout positions and scales.
     */
    private void calculateLayout() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);
        hintX = Source.getScreenWidth() / 2f;
        hintY = Source.getScreenHeight() / 2f - scale * 1350;
        font.getData().setScale(scale * (64f / fontSize));
    }

    /**
     * Auto-scrolls down when a new word is added to the list.
     */
    private void handleNewWordAdded() {
        if (listSize != listOfWordsFound.size()) {
            setScroll(scroll + 1);
            listSize = listOfWordsFound.size();
        }
    }

    /**
     * Renders the word list to the framebuffer.
     */
    private void render(SpriteBatch sb) {

        font.setColor(Color.WHITE);

        sb.begin();
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            printWord(sb, i);
        }
        sb.end();
    }

    /**
     * Renders a single word at the given row index.
     * @param sb SpriteBatch to draw with
     * @param y Row index (0-9)
     */
    private void printWord(SpriteBatch sb, int y) {
        int wordIndex = y + (int) scroll;

        // Check bounds
        if (wordIndex < 0 || wordIndex >= listOfWordsFound.size()) {
            return;
        }

        String word = listOfWordsFound.get(wordIndex);
        float yPos = (y - scroll % 1) * 75 * scale;

        if(y == 0){
            font.setColor(1, 1, 1, 1 - scroll % 1);
        } else if(y == VISIBLE_ROWS - 1){
            font.setColor(1, 1, 1, scroll % 1);
        } else {
            font.setColor(Color.WHITE);
        }

        // Draw each letter of the word
        for (int i = 0; i < word.length(); i++) {
            drawLetter(yPos, i * 50 * scale, "" + word.charAt(i), sb);
        }
    }

    /**
     * Draws a single letter at the specified position.
     * @param y Vertical position
     * @param x Horizontal position
     * @param letter Letter to draw
     * @param sb SpriteBatch to draw with
     */
    private void drawLetter(float y, float x, String letter, SpriteBatch sb) {
        fontLayout.setText(font, letter.toUpperCase());

        x += -fontLayout.width / 2 + hintX;
        y += fontLayout.height / 2 + hintY;

        font.draw(sb, letter.toUpperCase(), x, y);
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
    }
}
