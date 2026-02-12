package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Graphic;

public class Settings extends Agent {

    // ===== Constants =====
    private static final int FONT_SIZE = 64;

    private static final float LABEL_SCALE = 1.5f;

    private static final float LABEL_X_OFFSET = -450f;
    private static final float SWITCH_X_OFFSET = 300f;

    private static final float ROW_SPACING = 200f;
    private static final float MUSIC_Y_OFFSET = 200f;
    private static final float SOUND_Y_OFFSET = 0f;
    private static final float VIBRATION_Y_OFFSET = -200f;

    private static final float BG_TARGET_HEIGHT = 2500f;
    private static final float SWITCH_TARGET_HEIGHT = 100f;
    private static final float X_TARGET_HEIGHT = 150f;

    private static final float SWITCH_CLICK_HALF_WIDTH = 110f;
    private static final float SWITCH_CLICK_HALF_HEIGHT = 50f;

    private static final float X_X_OFFSET = 400f;
    private static final float X_Y_OFFSET = 975f;
    private static final float X_CLICK_RADIUS = 100f;

    // ===== Assets / Graphics =====
    private final Texture onSwitchTexture;
    private final Texture offSwitchTexture;
    private final Texture backgroundTexture;
    private final Texture xTexture;

    private final Graphic onSwitch;
    private final Graphic offSwitch;
    private final Graphic background;
    private final Graphic X;

    // ===== Text =====
    private final BitmapFont font;
    private final GlyphLayout layout;

    // ===== Input =====
    private boolean mouseDown = false;

    public Settings() {
        onSwitchTexture = new Texture(Source.getAsset("Misc/On Switch.png"));
        onSwitchTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        offSwitchTexture = new Texture(Source.getAsset("Misc/Off Switch.png"));
        offSwitchTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        onSwitch = new Graphic(onSwitchTexture);
        offSwitch = new Graphic(offSwitchTexture);

        backgroundTexture = new Texture(Source.getAsset("Misc/Settings Background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new Graphic(backgroundTexture);

        xTexture = new Texture(Source.getAsset("Misc/Red X.png"));
        xTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        X = new Graphic(xTexture);

        font = Source.generateFont(DataManager.fontName, FONT_SIZE);
        layout = new GlyphLayout();
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if (!Board.settingsOpen) return;

        float scale = computeScale();

        boolean click = detectClick();

        float centerX = Source.getScreenWidth() / 2f;
        float centerY = Source.getScreenHeight() / 2f;

        float alignX = centerX + SWITCH_X_OFFSET * scale;

        float musicY = centerY + MUSIC_Y_OFFSET * scale;
        float soundY = centerY + SOUND_Y_OFFSET * scale;
        float vibrationY = centerY + VIBRATION_Y_OFFSET * scale;

        float xCenterX = centerX + X_X_OFFSET * scale;
        float xCenterY = centerY + X_Y_OFFSET * scale;

        sb.begin();

        drawBackground(sb, centerX, centerY, scale);
        drawLabels(sb, centerX, scale, musicY, soundY, vibrationY);
        drawCloseX(sb, scale, xCenterX, xCenterY);

        drawToggle(sb, DataManager.music, alignX, musicY, scale);
        drawToggle(sb, DataManager.sound, alignX, soundY, scale);
        drawToggle(sb, DataManager.vibration, alignX, vibrationY, scale);

        if (click) {
            handleClick(
                Source.getScreenMouseX(),
                Source.getScreenMouseY(),
                alignX, musicY, soundY, vibrationY,
                xCenterX, xCenterY,
                scale
            );
        }

        sb.end();
    }

    // ===== Draw helpers =====

    private void drawBackground(SpriteBatch sb, float centerX, float centerY, float scale) {
        background.setCenter(centerX, centerY);
        background.setScale(BG_TARGET_HEIGHT * scale / backgroundTexture.getHeight());
        background.draw(sb);
    }

    private void drawLabels(SpriteBatch sb, float centerX, float scale, float musicY, float soundY, float vibrationY) {
        font.setColor(Color.WHITE);
        font.getData().setScale(scale * LABEL_SCALE);

        drawLabel(sb, "Music", centerX + LABEL_X_OFFSET * scale, musicY);
        drawLabel(sb, "Sound", centerX + LABEL_X_OFFSET * scale, soundY);
        drawLabel(sb, "Vibration", centerX + LABEL_X_OFFSET * scale, vibrationY);
    }

    private void drawLabel(SpriteBatch sb, String text, float x, float y) {
        layout.setText(font, text);
        font.draw(sb, text, x, y + layout.height / 2f);
    }

    private void drawCloseX(SpriteBatch sb, float scale, float xCenterX, float xCenterY) {
        X.setScale(scale * X_TARGET_HEIGHT / xTexture.getHeight());
        X.setCenter(xCenterX, xCenterY);
        X.draw(sb);
    }

    private void drawToggle(SpriteBatch sb, boolean enabled, float x, float y, float scale) {
        Graphic g = enabled ? onSwitch : offSwitch;
        Texture t = enabled ? onSwitchTexture : offSwitchTexture;

        g.setCenter(x, y);
        g.setScale(SWITCH_TARGET_HEIGHT * scale / t.getHeight());
        g.draw(sb);
    }

    // ===== Input helpers =====

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

    private void handleClick(
        float mouseX,
        float mouseY,
        float alignX,
        float musicY,
        float soundY,
        float vibrationY,
        float xCenterX,
        float xCenterY,
        float scale
    ) {
        // Toggle switches
        if (Math.abs(mouseX - alignX) < SWITCH_CLICK_HALF_WIDTH * scale) {
            if (Math.abs(mouseY - musicY) < SWITCH_CLICK_HALF_HEIGHT * scale) {
                DataManager.music = !DataManager.music;
            } else if (Math.abs(mouseY - soundY) < SWITCH_CLICK_HALF_HEIGHT * scale) {
                DataManager.sound = !DataManager.sound;
            } else if (Math.abs(mouseY - vibrationY) < SWITCH_CLICK_HALF_HEIGHT * scale) {
                DataManager.vibration = !DataManager.vibration;
            }
        }

        // Close button
        if (distance(mouseX, mouseY, xCenterX, xCenterY) < X_CLICK_RADIUS * scale) {
            Board.settingsOpen = false;
        }
    }

    private float computeScale() {
        float yScale = Source.getScreenHeight() / 3500f;
        float xScale = Source.getScreenWidth() / 1500f;
        return Math.min(xScale, yScale);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public void dispose() {
        onSwitchTexture.dispose();
        offSwitchTexture.dispose();
        backgroundTexture.dispose();
        xTexture.dispose();
    }
}
