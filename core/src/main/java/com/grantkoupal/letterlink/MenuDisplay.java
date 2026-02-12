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

    // ===== Constants =====
    private static final int FONT_SIZE = 64;
    private static final int MENU_HEIGHT = 325;

    // UI scale + click radius
    private float scale = 1f;

    // ===== Assets / Graphics =====
    private final Texture hintTexture;
    private final Texture menuTexture;

    private final Graphic icon;
    private final Graphic hint;
    private final Graphic menu;

    // ===== Text =====
    private final BitmapFont font;
    private final GlyphLayout layout;

    // ===== Time / Input =====
    private final long startTime;
    private boolean mouseDown = false;

    // ===== Hint animation =====
    private final Animation animateHint;
    private float hintTimer = 0f;
    private float hintScale = 1f;

    public MenuDisplay() {
        // Textures
        hintTexture = new Texture(Source.getAsset("Misc/Light Bulb.png"));
        hintTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        menuTexture = new Texture(Source.getAsset("Misc/Menu.png"));
        menuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Graphics
        icon = new Graphic(com.grantkoupal.letterlink.DataManager.iconTexture);
        hint = new Graphic(hintTexture);
        menu = new Graphic(menuTexture);

        // Font
        font = Source.generateFont(com.grantkoupal.letterlink.DataManager.fontName, FONT_SIZE);
        updateScaleForFont();
        layout = new GlyphLayout(font, com.grantkoupal.letterlink.DataManager.userName);

        // Timer start
        startTime = System.currentTimeMillis();

        // Hint pulse animation
        animateHint = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                if (Board.getHintScore() < 10) {
                    hintScale = 1f;
                    hintTimer = 0f;
                    return;
                }
                hintTimer += delta * 3f;
                hintScale = 1f - ((float) -Math.cos(hintTimer) + 1f) / 10f;
            }
        });
    }

    @Override
    public void frame() {
        getPage().addAnimation(animateHint);
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        boolean activateClick = detectFreshClick();

        updateScale();

        drawMenuBackground(sr);

        // Positions
        float menuCenterY = Source.getScreenHeight() - MENU_HEIGHT * scale / 2f;

        float iconX = MENU_HEIGHT * scale / 2f;
        float iconY = menuCenterY;

        float hintX = Source.getScreenWidth() / 2f - 300f * scale;
        float hintY = menuCenterY;

        float menuX = Source.getScreenWidth() - MENU_HEIGHT * scale / 2f;
        float menuY = menuCenterY;

        // Scales + placement
        float baseIconScale = MENU_HEIGHT * scale / icon.getTexture().getWidth();

        icon.setScale(baseIconScale * 0.833f);
        icon.setCenter(iconX, iconY);

        hint.setScale(baseIconScale * 0.75f * hintScale);
        hint.setCenter(hintX, hintY);

        menu.setScale(baseIconScale * 0.75f);
        menu.setCenter(menuX, menuY);

        // Clock text
        font.getData().setScale(scale * (128f / FONT_SIZE));
        String clock = convertNumToTime(System.currentTimeMillis() - startTime);
        layout.setText(font, clock);

        // Input + draw
        sb.begin();

        if (activateClick) {
            float mouseX = Source.getScreenMouseX();
            float mouseY = Source.getScreenMouseY();

            if (distance(mouseX, mouseY, hintX, hintY) < 90f * scale) {
                if (!Board.menuOpen) {
                    String word = getRandomWordFromBoard(1);
                    System.out.println(word);
                    Board.activateHint(word);
                }
            }

            if (distance(mouseX, mouseY, menuX, menuY) < 90f * scale) {
                Board.menuOpen = true;
                mouseDown = false;
            }
        }

        hint.draw(sb);
        icon.draw(sb);
        menu.draw(sb);

        font.setColor(Color.BLACK);
        font.draw(
            sb,
            clock,
            Source.getScreenWidth() / 2f - layout.width / 2f,
            Source.getScreenHeight() - MENU_HEIGHT * scale * 0.833f + layout.height - 20f * scale
        );

        sb.end();
    }

    // ===== Helpers =====

    private boolean detectFreshClick() {
        boolean activateClick = false;

        boolean pressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (mouseDown != pressed) {
            mouseDown = !mouseDown;
            if (mouseDown) activateClick = true;
        }

        return activateClick;
    }

    private void updateScale() {
        float yScale = Source.getScreenHeight() / 3500f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);
    }

    private void updateScaleForFont() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);
        font.getData().setScale(scale);
    }

    private void drawMenuBackground(ShapeRenderer sr) {
        float topY = Source.getScreenHeight() - MENU_HEIGHT * scale;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(Color.WHITE);
        sr.rect(0, topY, Source.getScreenWidth(), MENU_HEIGHT * scale);

        sr.setColor(Color.BLACK);
        sr.rectLine(
            0,
            Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale,
            Source.getScreenWidth(),
            Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale,
            25 * scale
        );

        sr.end();
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private String getRandomWordFromBoard(int range) {
        float rank = Board.getCurrentRank();
        List<String> validWords = new ArrayList<>();

        for (int i = 0; i < Board.getWordsLeft().size(); i++) {
            String w = Board.getWordsLeft().get(i);
            if (WordDifficultyRanker.wordDifficulty(w) < Math.max(rank, 20) + range) {
                validWords.add(w);
            }
        }

        if (validWords.isEmpty()) {
            return getRandomWordFromBoard(range + 2);
        }

        return validWords.get((int) (Math.random() * validWords.size()));
    }

    private String convertNumToTime(long millis) {
        long minutes = millis / 60000;
        millis -= minutes * 60000;
        return add0("" + minutes) + ":" + add0("" + (millis / 1000));
    }

    private String add0(String s) {
        if (s.length() == 1) return "0" + s;
        return s;
    }

    // Kept as-is (unused in this class currently), just reorganized.
    private String constrain(String s, GlyphLayout gl) {
        String name = com.grantkoupal.letterlink.DataManager.userName;

        if (gl.width > Source.getScreenWidth() / 2f - 175 * scale) {
            int i = s.length();
            while (gl.width > Source.getScreenWidth() / 2f - 250 * scale) {
                i--;
                layout.setText(font, name.substring(0, i));
            }
            return s.substring(0, i) + "...";
        }

        return s;
    }

    @Override
    public void dispose() {
        hintTexture.dispose();
    }
}
