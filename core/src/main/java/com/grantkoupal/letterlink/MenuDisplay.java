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

public class MenuDisplay extends Agent {

    private final int fontSize = 64;
    private final int MENU_HEIGHT = 325;
    private float scale = 1;
    private final Texture hintTexture;
    private final Texture menuTexture;
    private final Graphic icon;
    private final Graphic hint;
    private final Graphic menu;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final long startTime;
    private final Board board;
    private boolean mouseDown = false;
    private Animation animateHint;
    private float hintTimer = 0;
    private float hintScale = 1;

    public MenuDisplay(Board board, Page p){

        this.board = board;

        hintTexture = new Texture(Source.getAsset("Misc/Light Bulb.png"));
        hintTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        menuTexture = new Texture(Source.getAsset("Misc/Menu.png"));
        menuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        icon = new Graphic(com.grantkoupal.letterlink.DataManager.iconTexture);
        hint = new Graphic(hintTexture);
        menu = new Graphic(menuTexture);

        font = Source.generateFont(com.grantkoupal.letterlink.DataManager.fontName, fontSize);

        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);

        font.getData().setScale(scale);

        layout = new GlyphLayout(font, com.grantkoupal.letterlink.DataManager.userName);

        startTime = System.currentTimeMillis();

        animateHint = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
            @Override
            public void run(float delta) {
                if(board.getHintScore() < 10) {
                    hintScale = 1;
                    hintTimer = 0;
                } else {
                    hintTimer += delta * 3;
                    hintScale = 1 - ((float) -Math.cos(hintTimer) + 1) / 10;
                }
            }
        });
        p.addAnimation(animateHint);
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {

        boolean activateClick = false;

        if(mouseDown != Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            mouseDown = !mouseDown;
            if(mouseDown) {
                activateClick = true;
            }
        }

        float yScale = Source.getScreenHeight() / 3500f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = (float) Math.min(xScale, yScale);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rect(0, Source.getScreenHeight() - MENU_HEIGHT * scale, Source.getScreenWidth(), MENU_HEIGHT * scale);
        sr.setColor(Color.BLACK);
        sr.rectLine(0, Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale, Source.getScreenWidth(), Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale, 25 * scale);
        sr.end();

        icon.setScale(MENU_HEIGHT * scale / icon.getTexture().getWidth() * .833f);
        icon.setCenter(MENU_HEIGHT * scale / 2, Source.getScreenHeight() - MENU_HEIGHT * scale / 2);

        float hintX = Source.getScreenWidth() / 2f - 300 * scale;
        float hintY = Source.getScreenHeight() - MENU_HEIGHT * scale / 2;

        hint.setScale(MENU_HEIGHT * scale / icon.getTexture().getWidth() * .75f * hintScale);
        hint.setCenter(hintX, hintY);

        float menuX = Source.getScreenWidth() - MENU_HEIGHT * scale / 2;
        float menuY = Source.getScreenHeight() - MENU_HEIGHT * scale / 2;

        menu.setScale(MENU_HEIGHT * scale / icon.getTexture().getWidth() * .75f);
        menu.setCenter(menuX, menuY);

        font.getData().setScale(scale * (128f / fontSize));
        String clock = convertNumToTime(System.currentTimeMillis() - startTime);
        layout.setText(font, clock);

        sb.begin();
        float hintDistance = (float)Math.sqrt(Math.pow(Source.getScreenMouseX() - hintX, 2) + Math.pow(Source.getScreenMouseY() - hintY, 2));
        if(hintDistance < 90 * scale && activateClick) {
            if(!Board.menuOpen) {
                String word = getRandomWordFromBoard(1);
                System.out.println(word);
                board.activateHint(word);
            }
        }
        float menuDistance = (float)Math.sqrt(Math.pow(Source.getScreenMouseX() - menuX, 2) + Math.pow(Source.getScreenMouseY() - menuY, 2));
        if(menuDistance < 90 * scale && activateClick) {
            Board.menuOpen = true;
            mouseDown = false;
        }
        hint.draw(sb);
        icon.draw(sb);
        menu.draw(sb);
        font.setColor(Color.BLACK);
        font.draw(sb, clock, Source.getScreenWidth() / 2f - layout.width / 2f, Source.getScreenHeight() - MENU_HEIGHT * scale * .833f + layout.height - 20 * scale);
        sb.end();
    }

    private String getRandomWordFromBoard(int range){
        float rank = board.getCurrentRank();
        List<String> validWords = new ArrayList<>();
        for(int i = 0; i < board.getWordsLeft().size(); i++){
            if(WordDifficultyRanker.wordDifficulty(board.getWordsLeft().get(i)) < Math.max(rank, 20) + range){
                validWords.add(board.getWordsLeft().get(i));
            }
        }
        if(validWords.isEmpty()){
            return getRandomWordFromBoard(range + 2);
        }
        return validWords.get((int)(Math.random() * validWords.size()));
    }

    private String convertNumToTime(long millis){
        long minutes = millis / 60000;
        millis -= minutes * 60000;
        return add0("" + minutes) + ":" + add0("" + (millis / 1000));
    }

    private String add0(String s){
        if(s.length() == 1){
            return "0" + s;
        }
        return s;
    }

    private String constrain(String s, GlyphLayout gl){
        String name = com.grantkoupal.letterlink.DataManager.userName;
        if(gl.width > Source.getScreenWidth() / 2f - 175 * scale){
            int i = s.length();
            while(gl.width > Source.getScreenWidth() / 2f - 250 * scale){
                i--;
                layout.setText(font, name.substring(0, i));
            }
            return s.substring(0, i) + "...";
        } else {
            return s;
        }
    }

    @Override
    public void dispose() {
        hintTexture.dispose();
    }
}
