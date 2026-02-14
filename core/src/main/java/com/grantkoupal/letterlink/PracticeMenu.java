package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.*;

import java.util.List;

public class PracticeMenu extends Agent {

    // ===== Constants =====
    private static final float HOVER_MAX_SCALE = 1.2f;

    private static final float BUTTON_HALF_WIDTH = 433f;
    private static final float BUTTON_HALF_HEIGHT = 150f;

    private static final float BUTTON_CENTER_OFFSET_Y = 400f;
    private static final float BUTTON_TARGET_HEIGHT = 300f;

    // ===== Assets =====
    private Texture resumeTexture;
    private Texture settingsTexture;
    private Texture finishTexture;

    // ===== Graphics =====
    private final Graphic RESUME;
    private final Graphic SETTINGS;
    private final Graphic FINISH;

    // ===== State =====
    private float scale;
    private boolean mouseDown = false;

    private float resumeScale = 1f;
    private float settingsScale = 1f;
    private float finishScale = 1f;

    public PracticeMenu() {
        instantiateTextures();

        RESUME = new Graphic(resumeTexture);
        SETTINGS = new Graphic(settingsTexture);
        FINISH = new Graphic(finishTexture);
    }

    @Override
    public void frame() {
        getPage().addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                if (!Board.menuOpen || Board.settingsOpen) return;

                float centerX = Source.getScreenWidth() / 2f;
                float centerY = Source.getScreenHeight() / 2f;

                float mouseX = Source.getScreenMouseX();
                float mouseY = Source.getScreenMouseY();

                boolean click = detectClick();

                // RESUME
                float resumeY = centerY + BUTTON_CENTER_OFFSET_Y * scale;
                if (isHovering(mouseX, mouseY, centerX, resumeY)) {
                    if (click) executeResume();
                    if(!Manager.isOnDesktop()) return;
                    resumeScale = approach(resumeScale, HOVER_MAX_SCALE, delta);
                } else {
                    resumeScale = approach(resumeScale, 1f, delta);
                }
                RESUME.setScale(BUTTON_TARGET_HEIGHT * scale / resumeTexture.getHeight() * resumeScale);

                // SETTINGS
                float settingsY = centerY;
                if (isHovering(mouseX, mouseY, centerX, settingsY)) {
                    if (click) executeSettings();
                    if(!Manager.isOnDesktop()) return;
                    settingsScale = approach(settingsScale, HOVER_MAX_SCALE, delta);
                } else {
                    settingsScale = approach(settingsScale, 1f, delta);
                }
                SETTINGS.setScale(BUTTON_TARGET_HEIGHT * scale / settingsTexture.getHeight() * settingsScale);

                // FINISH
                float finishY = centerY - BUTTON_CENTER_OFFSET_Y * scale;
                if (isHovering(mouseX, mouseY, centerX, finishY)) {
                    if (click) executeFinish();
                    if(!Manager.isOnDesktop()) return;
                    finishScale = approach(finishScale, HOVER_MAX_SCALE, delta);
                } else {
                    finishScale = approach(finishScale, 1f, delta);
                }
                FINISH.setScale(BUTTON_TARGET_HEIGHT * scale / finishTexture.getHeight() * finishScale);
            }
        }));
    }

    // ===== Button Actions =====

    private void executeResume() {
        Board.menuOpen = false;
        resetState();
    }

    private void executeSettings() {
        Board.settingsOpen = true;
        resetState();
    }

    private void executeFinish() {
        BoardResult results = new BoardResult();
        results.score = Board.getTotalPoints();
        results.hintsUsed = Board.getHintsUsed();
        results.boardValue = Board.getBoardValue();
        results.longestWord = Board.getLongestWord().toUpperCase();
        results.totalWords = Board.getListOfWordsFound().size();
        results.userRank = Board.getCurrentRank();
        results.timeSeconds = Board.getGameDuration() / 1000;
        results.SRankScore = Board.SRankScore;

        List<String> wordsFound = Board.getListOfWordsFound();
        int totalLetters = 0;
        for (String s : wordsFound) {
            totalLetters += s.length();
        }

        if (totalLetters == 0) {
            results.averageWordLength = 0;
        } else {
            results.averageWordLength = totalLetters / (float) results.totalWords;
        }

        results.wordsPerSecond = results.totalWords / (float) results.timeSeconds;
        results.pointsPerSecond = results.score / (float) results.timeSeconds;

        Source.loadNewPage(new FinishPage(results));
    }

    private void resetState() {
        resumeScale = 1f;
        settingsScale = 1f;
        finishScale = 1f;
        mouseDown = false;
    }

    // ===== Input / Hover helpers =====

    private boolean detectClick() {
        boolean click = false;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!mouseDown) {
                mouseDown = true;
                click = true;
            }
        } else {
            mouseDown = false;
        }

        return click;
    }

    private boolean isHovering(float mouseX, float mouseY, float centerX, float centerY) {
        return Math.abs(mouseX - centerX) < BUTTON_HALF_WIDTH * scale
            && Math.abs(mouseY - centerY) < BUTTON_HALF_HEIGHT * scale;
    }

    private float approach(float current, float target, float delta) {
        if (current < target) {
            current += delta;
            if (current > target) current = target;
            return current;
        }
        if (current > target) {
            current -= delta;
            if (current < target) current = target;
        }
        return current;
    }

    // ===== Assets =====

    private void instantiateTextures() {
        resumeTexture = DataManager.resumeButtonTexture;

        settingsTexture = DataManager.settingsButtonTexture;

        finishTexture = DataManager.finishButtonTexture;
    }

    // ===== Draw =====

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if (!Board.menuOpen) return;

        updateScale();

        // Background overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(DataManager.menuColor);
        sr.rect(0, 0, Source.getScreenWidth(), Source.getScreenHeight());
        sr.end();

        // Button centers
        float centerX = Source.getScreenWidth() / 2f;
        float centerY = Source.getScreenHeight() / 2f;

        RESUME.setCenter(centerX, centerY + BUTTON_CENTER_OFFSET_Y * scale);
        SETTINGS.setCenter(centerX, centerY);
        FINISH.setCenter(centerX, centerY - BUTTON_CENTER_OFFSET_Y * scale);

        sb.begin();
        RESUME.draw(sb);
        SETTINGS.draw(sb);
        FINISH.draw(sb);
        sb.end();
    }

    private void updateScale() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);
    }

    // ===== Cleanup =====

    @Override
    public void dispose() {
    }
}
