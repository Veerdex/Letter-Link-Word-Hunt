package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.Action;
import com.grantkoupal.letterlink.quantum.Agent;
import com.grantkoupal.letterlink.quantum.Animation;

import java.util.List;

public class GuessTable extends Agent {

    private final float fontScale = .5f;

    private BitmapFont font;
    private GlyphLayout fontLayout;
    private List<String> listOfWordsFound;

    private float scroll = 0;
    private float scrollMotion = 0;
    private float scale = 1;
    private float hintX = 0;
    private float hintY = 0;
    private int listSize;

    public GuessTable(List<String> listOfWordsFound){
        this.listOfWordsFound = listOfWordsFound;
        listSize = listOfWordsFound.size();

        fontLayout = new GlyphLayout();

        Source.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
            @Override
            public void run(float delta) {
                if(Source.getScreenMouseX() > hintX && Source.getScreenMouseX() > Source.getScreenWidth() / 2f &&
                    Source.getScreenMouseY() < Source.getScreenHeight() / 2f - scale * 700 &&
                    Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    scrollMotion = Gdx.input.getDeltaY();
                }
                setScroll(scroll + scrollMotion * delta);
                scrollMotion *= (float)Math.pow(.1f, delta);
            }
        }));

        initializeFont();
    }

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 256);
    }

    public void setScroll(float f){
        if(f < 0){
            scroll = 0;
            scrollMotion = 0;
            return;
        } else if(f > Math.max(0, listOfWordsFound.size() - 10)){
            scroll = Math.max(0, listOfWordsFound.size() - 10);
            scrollMotion = 0;
            return;
        }
        scroll = f;
    }

    @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        scale = (float)Math.min(xScale, yScale);
        hintX = Source.getScreenWidth() / 2f;// - scale * 650;
        hintY = Source.getScreenHeight() / 2f - scale * 1400;
        font.getData().setScale(scale * fontScale * .5f);
        if (listSize != listOfWordsFound.size()) {
            setScroll(scroll + 1);
            listSize = listOfWordsFound.size();
        }
        sb.begin();
        for(int i = 0; i < 10; i++){
            print(sb, i);
        }
        sb.end();
    }

    private void print(SpriteBatch sb, int y){

        if(y + (int)scroll < 0 || y + (int)scroll >= listOfWordsFound.size()){
            return;
        }

        String word;

        word = listOfWordsFound.get(y + (int)scroll);

        float yPos = (y - scroll % 1) * 150 * scale * fontScale;

        for(int i = 0; i < listOfWordsFound.get(y + (int)scroll).length(); i++){
            drawTile(yPos, i * 125 * scale * fontScale, "" + word.charAt(i), sb);
        }

        font.setColor(Color.WHITE);
    }

    private void drawTile(float y, float x, String letter, SpriteBatch sb){
        fontLayout.setText(font, letter);

        x -= fontLayout.width / 2 - hintX;
        y += fontLayout.height / 2 + hintY;

        font.draw(sb, letter, x, y);
    }
}
