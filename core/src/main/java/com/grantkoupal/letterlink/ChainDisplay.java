package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.Agent;

public class ChainDisplay extends Agent {

    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();

    private Board board;

    private float displayX = 0;
    private float displayY = 0;
    private float scale = 1;

    public ChainDisplay(Board b){
        font = Source.generateFont(DataManager.fontName, 128);
        font.setColor(Color.BLACK);

        board = b;
    }

    @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        scale = (float)Math.min(xScale, yScale);
        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 838;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rect(displayX - 500 * scale, displayY - 150 * scale, 1000 * scale, 300 * scale);
        sr.end();
        font.getData().setScale(Math.min(scale / (board.getStringChain().length() + 1) * 10, 2f * scale));
        layout.setText(font, board.getStringChain().toUpperCase());
        sb.begin();
        font.draw(sb, board.getStringChain().toUpperCase(), displayX - layout.width / 2f, displayY + layout.height / 2f);
        sb.end();
    }
}
