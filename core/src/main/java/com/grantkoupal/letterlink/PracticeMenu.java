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

    private Texture resumeTexture;
    private Texture settingsTexture;
    private Texture finishTexture;
    private final Graphic RESUME;
    private final Graphic SETTINGS;
    private final Graphic FINISH;
    private float scale;
    private boolean mouseDown = false;
    private float resumeScale = 1;
    private float settingsScale = 1;
    private float finishScale = 1;
    private final Board board;

    public PracticeMenu(Page p, Board board){
        instantiateTextures(DataManager.menuButtonColor);

        this.board = board;

        RESUME = new Graphic(resumeTexture);
        SETTINGS = new Graphic(settingsTexture);
        FINISH = new Graphic(finishTexture);

        addAnimation(p);
    }

    private void addAnimation(Page p){
        p.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
            @Override
            public void run(float delta) {

                if(!Board.menuOpen || Board.settingsOpen) return;

                float centerX = Source.getScreenWidth() / 2f;
                float centerY = Source.getScreenHeight() / 2f;
                float mouseX = Source.getScreenMouseX();
                float mouseY = Source.getScreenMouseY();

                boolean click = false;
                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    if(!mouseDown) {
                        mouseDown = true;
                        click = true;
                    }
                } else {
                    mouseDown = false;
                }

                // RESUME

                if(Math.abs(mouseX - centerX) < 433 * scale && Math.abs(mouseY - (centerY + 400 * scale)) < 150 * scale){
                    if(click){
                        executeResume();
                    }
                    if(resumeScale < 1.2) {
                        resumeScale += delta;
                    } else {
                        resumeScale = 1.2f;
                    }
                } else {
                    if(resumeScale > 1) {
                        resumeScale -= delta;
                    } else {
                        resumeScale = 1;
                    }
                }
                RESUME.setScale(300 * scale / resumeTexture.getHeight() * resumeScale);

                // SETTINGS

                if(Math.abs(mouseX - centerX) < 433 * scale && Math.abs(mouseY - centerY) < 150 * scale){
                    if(click){
                        executeSettings();
                    }
                    if(settingsScale < 1.2) {
                        settingsScale += delta;
                    } else {
                        settingsScale = 1.2f;
                    }
                } else {
                    if(settingsScale > 1) {
                        settingsScale -= delta;
                    } else {
                        settingsScale = 1;
                    }
                }
                SETTINGS.setScale(300 * scale / settingsTexture.getHeight() * settingsScale);

                // FINISH

                if(Math.abs(mouseX - centerX) < 433 * scale && Math.abs(mouseY - (centerY - 400 * scale)) < 150 * scale){
                    if(click){
                        executeFinish(p);
                    }
                    if(finishScale < 1.2) {
                        finishScale += delta;
                    } else {
                        finishScale = 1.2f;
                    }
                } else {
                    if(finishScale > 1) {
                        finishScale -= delta;
                    } else {
                        finishScale = 1;
                    }
                }
                FINISH.setScale(300 * scale / finishTexture.getHeight() * finishScale);


            }
        }));
    }

    private void executeResume(){
        Board.menuOpen = false;
        resumeScale = 1;
        settingsScale = 1;
        finishScale = 1;
        mouseDown = false;
    }

    private void executeSettings(){
        Board.settingsOpen = true;
        resumeScale = 1;
        settingsScale = 1;
        finishScale = 1;
        mouseDown = false;
    }

    private void executeFinish(Page p){
        BoardResult results = new BoardResult();
        results.score = board.getTotalPoints();
        results.hintsUsed = board.getHintsUsed();
        results.boardValue = board.getBoardValue();
        results.longestWord = board.getLongestWord().toUpperCase();
        results.totalWords = board.getListOfWordsFound().size();
        results.userRank = board.getCurrentRank();
        results.timeSeconds = board.getGameDuration() / 1000;
        List<String> wordsFound = board.getListOfWordsFound();
        int totalLetters = 0;
        for(int i = 0; i < wordsFound.size(); i++){
            totalLetters += wordsFound.get(i).length();
        }
        if(totalLetters == 0){
            results.averageWordLength = 0;
        } else {
            results.averageWordLength = totalLetters / (float) results.totalWords;
        }
        results.wordsPerSecond = results.totalWords / (float)results.timeSeconds;
        results.pointsPerSecond = results.score / (float)results.timeSeconds;
        FinishPage fp = new FinishPage(results);
        Source.loadNewPage(fp);
    }

    private void instantiateTextures(String color){
        resumeTexture = new Texture(Source.getAsset("Menu/" + color + " Resume.png"));
        resumeTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        settingsTexture = new Texture(Source.getAsset("Menu/" + color + " Settings.png"));
        settingsTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        finishTexture = new Texture(Source.getAsset("Menu/" + color + " Finish.png"));
        finishTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if(!Board.menuOpen) return;

        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(DataManager.menuColor);
        sr.rect(0, 0, Source.getScreenWidth(), Source.getScreenHeight());
        sr.end();

        float centerX = Source.getScreenWidth() / 2f;
        float centerY = Source.getScreenHeight() / 2f;

        RESUME.setCenter(centerX, centerY + 400 * scale);
        SETTINGS.setCenter(centerX, centerY);
        FINISH.setCenter(centerX, centerY - 400 * scale);

        sb.begin();
        RESUME.draw(sb);
        SETTINGS.draw(sb);
        FINISH.draw(sb);
        sb.end();
    }

    @Override
    public void dispose() {
        resumeTexture.dispose();
        settingsTexture.dispose();
        finishTexture.dispose();
    }
}
