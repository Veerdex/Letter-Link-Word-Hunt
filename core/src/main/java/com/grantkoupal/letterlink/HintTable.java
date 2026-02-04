package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.Action;
import com.grantkoupal.letterlink.quantum.Agent;
import com.grantkoupal.letterlink.quantum.Animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HintTable extends Agent {

    private final float fontScale = .5f;

    private BitmapFont font;
    private GlyphLayout fontLayout;
    private String hiddenWord = "??????????????????????????????";

    private List<String> validWords;
    private List<Boolean> wordsFound = new ArrayList<Boolean>();
    private float scroll = 0;
    private float scrollMotion = 0;
    private float scale = 1;
    private float hintX = 0;
    private float hintY = 0;
    private FrameBuffer fb;

    public HintTable(List<String> validWords, List<Boolean> wordsFound){
        this.validWords = copyOf(validWords);
        sortByLengthDescThenAlphabetically(this.validWords);

        for(int i = 0; i < validWords.size(); i++){
            wordsFound.add(false);
        }

        this.wordsFound = wordsFound;

        fontLayout = new GlyphLayout();

        Source.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
            @Override
            public void run(float delta) {
                if(Source.getScreenMouseX() > hintX && Source.getScreenMouseX() < Source.getScreenWidth() / 2f &&
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

    private List<String> copyOf(List<String> list){
        List<String> copy = new ArrayList<String>();
        for(int i = 0; i < list.size(); i++){
            copy.add(list.get(i));
        }
        return copy;
    }

    public static void sortByLengthDescThenAlphabetically(List<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // First compare by length (longest first)
                int lengthCompare = Integer.compare(s2.length(), s1.length());

                // If lengths are equal, compare alphabetically
                if (lengthCompare == 0) {
                    return s1.compareToIgnoreCase(s2);
                }

                return lengthCompare;
            }
        });
    }

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 256);
    }

    public void setScroll(float f){
        if(f < 0){
            scroll = 0;
            scrollMotion = 0;
            return;
        } else if(f > validWords.size() - 10){
            scroll = validWords.size() - 10;
            scrollMotion = 0;
            return;
        }
        scroll = f;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        scale = (float)Math.min(xScale, yScale);
        hintX = Source.getScreenWidth() / 2f - scale * 650;
        hintY = Source.getScreenHeight() / 2f - scale * 1400;
        font.getData().setScale(scale * fontScale * .5f);
        sb.begin();
        for(int i = 0; i < 10; i++){
            print(sb, i);
        }
        sb.end();
    }

    private void print(SpriteBatch sb, int y){
        String word;
        if(!wordsFound.get(y + (int)scroll)){
            word = hiddenWord;
            font.setColor(Color.WHITE);
        } else {
            font.setColor(Color.GOLD);
            word = validWords.get(y + (int)scroll);
        }
        float yPos = (y - scroll % 1) * 150 * scale * fontScale;

        if(word.charAt(0) == '?'){
            for(int i = 0; i < validWords.get(y + (int)scroll).length(); i++){
                drawTile(yPos, i * 100 * scale * fontScale, "" + word.charAt(i), sb);
            }
        } else {
            for(int i = 0; i < word.length(); i++){
                drawTile(yPos, i * 100 * scale * fontScale, "" + word.charAt(i), sb);
            }
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
