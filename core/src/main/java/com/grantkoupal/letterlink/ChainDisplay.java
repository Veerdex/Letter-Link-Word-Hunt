package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.Agent;
import com.grantkoupal.letterlink.quantum.Squircle;

public class ChainDisplay extends Agent {

    private Color RED = new Color(1, 94f / 255, 94f / 255, 1);
    private Color GREEN = new Color(94f / 255, 1, 94f / 255, 1);
    private Color YELLOW = new Color(1, 1, 94f / 255, 1);

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
        displayY = Source.getScreenHeight() / 2f + scale * 800;
        font.getData().setScale(Math.min(scale / (board.getStringChain().length() + 1) * 12, scale));
        layout.setText(font, board.getStringChain().toUpperCase());
        sr.begin(ShapeRenderer.ShapeType.Filled);
        Color fill = Color.WHITE;
        if(board.getStringChain().length() > 0) {
            switch (board.getCurrentState()) {
                case VALID:
                    fill = GREEN;
                    break;
                case COPY:
                    fill = YELLOW;
                    break;
                case INVALID:
                    fill = RED;
                    break;
                default:
                    fill = Color.WHITE;
            }
        }
        Squircle.drawSquircleWithOutline(sr, fill, Color.BLACK, 20 * scale, displayX, displayY, layout.width + 100 * scale, 150 * scale, 75 * scale);
        sr.end();
        sb.begin();
        font.draw(sb, board.getStringChain().toUpperCase(), displayX - layout.width / 2f, displayY + layout.height / 2f);
        sb.end();
    }
}
