package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.grantkoupal.letterlink.backend.data.SessionData;
import com.grantkoupal.letterlink.quantum.core.Action;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Animation;

import java.util.ArrayList;
import java.util.List;

public class GamemodeSelection extends Agent {

    private static final String MENU_PATH = "MainMenu/";
    private static final String GAMEMODE_INDEX_PATH = "MainMenu/Gamemodes/Index.txt";

    private static final float BASE_SCREEN_WIDTH = 1500f;
    private static final float BASE_SCREEN_HEIGHT = 3000f;
    private static final float PANEL_HEIGHT = 2250f;

    private static final float MODE_START_NUM = 1.5f;
    private static final float GAMEMODE_START_NUM = 6f;

    private static final float OPTION_Y_SPACING = 125f;
    private static final float OPTION_HOVER_HALF_WIDTH = 500f;
    private static final float OPTION_HOVER_HALF_HEIGHT = 140f;
    private static final float MAX_BUTTON_SCALE = 1.1f;

    private static final float TITLE_MODE_Y = 100f;
    private static final float TITLE_GAMEMODE_Y = 1525f;

    private static final float DIVIDER_ONE_Y = 575f;
    private static final float DIVIDER_TWO_Y = 1400f;
    private static final float DIVIDER_HALF_WIDTH = 600f;

    private static final float PIPE_TILE_SIZE = 500f;
    private static final int OUTLINE_THICKNESS = 2;

    private static final float OPTION_FONT_SCALE = 0.6f;
    private static final float MAX_FONT_WIDTH = 1250f;

    private final Texture background;
    private final Texture pipeH;
    private final Texture pipeVR;
    private final Texture pipeVL;
    private final Texture pipeL;
    private final Texture pipeR;
    private final Texture outline;
    private final Texture outlineShadow;
    private final Texture dropDown;

    private FrameBuffer fb;

    private float scale = 1f;
    private float width = 0f;
    private float height = 0f;
    private float x = 0f;
    private float yPos = 0f;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private List<Option> options;
    private boolean buttonsEnabled = false;

    private float gamemodeNum = GAMEMODE_START_NUM;
    private float modeNum = MODE_START_NUM;
    private boolean clickCooldown = false;

    public GamemodeSelection() {
        background = getTexture("Themes/" + ThemeManager.currentTheme + "/GamemodeBackground");
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        pipeH = getTexture("Pipe(Horizontal)");
        pipeH.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        pipeVR = getTexture("Pipe(Vertical-Right)");
        pipeVR.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        pipeVL = getTexture("Pipe(Vertical-Left)");
        pipeVL.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        pipeL = getTexture("Pipe(Corner-Left)");
        pipeR = getTexture("Pipe(Corner-Right)");
        outline = getTexture("Gamemodes/Outline");
        outlineShadow = getTexture("Gamemodes/OutlineShadow");

        dropDown = getTexture("Gamemodes/DropDown");

        font = Source.generateFont(DataManager.fontName, 256);

        // setDimensions();
    }

    @Override
    public void frame() {
        options = getEnabledOptionsFromIndex(Source.getAsset(GAMEMODE_INDEX_PATH));

        /*
        parent.addResize(new Process() {
            @Override
            public boolean run() {
                setDimensions();
                return true;
            }
        });
        */
    }

    private List<Option> getEnabledOptionsFromIndex(FileHandle indexFile) {
        List<Option> loadedOptions = new ArrayList<>();

        if (indexFile == null || !indexFile.exists() || indexFile.isDirectory()) {
            Gdx.app.log("loadOptions", "Invalid Index.txt handle");
            return loadedOptions;
        }

        FileHandle root = indexFile.parent();
        if (root == null) {
            Gdx.app.log("loadOptions", "Index.txt has no parent directory");
            return loadedOptions;
        }

        String[] folderNames = indexFile.readString("UTF-8").split("\\r?\\n");

        for (String rawFolderName : folderNames) {
            String folderName = rawFolderName.trim();
            if (folderName.length() == 0) {
                continue;
            }

            FileHandle subdirectory = root.child(folderName);
            if (!subdirectory.exists()) {
                Gdx.app.log("loadOptions", "Missing folder: " + subdirectory.path());
                continue;
            }

            FileHandle dataFile = subdirectory.child("Data.txt");
            if (!dataFile.exists()) {
                Gdx.app.log("loadOptions", "Missing Data.txt in " + subdirectory.path());
                continue;
            }

            String title = getEnabledTitle(dataFile);
            if (title == null) {
                continue;
            }

            FileHandle backgroundFile = subdirectory.child("Background.png");
            if (!backgroundFile.exists()) {
                Gdx.app.log("loadOptions", "Missing Background.png in " + subdirectory.path());
                continue;
            }

            Texture backgroundTexture = new Texture(backgroundFile);
            loadedOptions.add(new Option(title, backgroundTexture));

            Gdx.app.log("loadOptions", "Added option: " + title + " -> " + backgroundFile.path());
        }

        return loadedOptions;
    }

    private String getEnabledTitle(FileHandle dataFile) {
        String text = dataFile.readString("UTF-8");
        if (text == null) {
            return null;
        }

        text = text.trim();
        if (text.length() == 0) {
            return null;
        }

        String[] parts = text.split("\\s+");
        if (parts.length < 2) {
            Gdx.app.log("loadOptions", "Invalid Data.txt format: " + dataFile.path());
            return null;
        }

        String enabledValue = parts[parts.length - 1];
        if (!enabledValue.equals("1")) {
            return null;
        }

        StringBuilder title = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                title.append(" ");
            }
            title.append(parts[i]);
        }

        return title.toString();
    }

    public void popUp() {
        x = 0;

        Animation animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                x += delta * 2;

                if (x > Math.PI / 2) {
                    x = MathUtils.PI / 4;
                    yPos = PANEL_HEIGHT;
                    stop();
                    buttonsEnabled = true;
                    return;
                }

                yPos = MathUtils.sin(x) * PANEL_HEIGHT;
            }
        });

        parent.addAnimation(animation);
    }

    public void dropDown() {
        x = MathUtils.PI / 2;
        buttonsEnabled = false;

        Animation animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                x += delta * 2;

                if (x > Math.PI) {
                    x = 0;
                    yPos = 0;
                    stop();
                    ((MainMenu) parent).actionsEnabled = true;
                    return;
                }

                yPos = MathUtils.sin(x) * PANEL_HEIGHT;
            }
        });

        parent.addAnimation(animation);
    }

    private Texture getTexture(String name) {
        Texture texture = new Texture(Source.getAsset(MENU_PATH + name + ".png"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = Source.getScreenHeight() / BASE_SCREEN_HEIGHT;
        float xScale = Source.getScreenWidth() / BASE_SCREEN_WIDTH;

        scale = Math.min(xScale, yScale);
        width = Source.getScreenWidth();
        height = Source.getScreenHeight();

        float tempYPos = yPos * scale;

        sb.begin();
        sb.setColor(Color.WHITE);

        float dropDownScale = .4f * scale;
        sb.draw(dropDown, width / 2 - dropDown.getWidth() / 2f * dropDownScale, (yPos + (yPos / (960 / scale) * 164) - 98) * scale, dropDown.getWidth() * dropDownScale, dropDown.getHeight() * dropDownScale);

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && buttonsEnabled && Math.sqrt(Math.pow(Source.getScreenMouseX() - width / 2, 2) + Math.pow(Source.getScreenMouseY() - ((yPos + 98 / 2f) * scale), 2)) < 200 * scale){
            dropDown();
        }

        drawRepeated(background, sb, 0, 0, width, tempYPos, PIPE_TILE_SIZE, PIPE_TILE_SIZE);

        drawRepeated(
            pipeH,
            sb,
            pipeL.getWidth() * scale / 2f,
            tempYPos,
            width - pipeR.getWidth() * scale,
            pipeH.getHeight() * scale / 2f,
            pipeH.getWidth() * scale / 2f,
            pipeH.getHeight() * scale / 2f
        );

        drawRepeated(
            pipeVR,
            sb,
            width - pipeVR.getWidth() * scale / 2f,
            0,
            pipeVR.getWidth() * scale / 2f,
            tempYPos,
            pipeVR.getWidth() * scale / 2f,
            pipeVR.getHeight() * scale / 2f
        );

        drawRepeated(
            pipeVL,
            sb,
            0,
            0,
            pipeVR.getWidth() * scale / 2f,
            tempYPos,
            pipeVR.getWidth() * scale / 2f,
            pipeVR.getHeight() * scale / 2f
        );

        sb.draw(pipeL, 0, tempYPos, pipeL.getHeight() * scale / 2f, pipeL.getHeight() * scale / 2f);
        sb.draw(pipeR, width - pipeR.getWidth() * scale / 2f, tempYPos, pipeR.getHeight() * scale / 2f, pipeR.getHeight() * scale / 2f);

        drawTitles(sb, tempYPos);

        for (Option option : options) {
            option.draw(sb);
        }

        sb.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rectLine(
            width / 2f - DIVIDER_HALF_WIDTH * scale,
            tempYPos - DIVIDER_ONE_Y * scale,
            width / 2f + DIVIDER_HALF_WIDTH * scale,
            tempYPos - DIVIDER_ONE_Y * scale,
            3 * scale
        );
        sr.rectLine(
            width / 2f - DIVIDER_HALF_WIDTH * scale,
            tempYPos - DIVIDER_TWO_Y * scale,
            width / 2f + DIVIDER_HALF_WIDTH * scale,
            tempYPos - DIVIDER_TWO_Y * scale,
            10 * scale
        );
        sr.end();
    }

    private void drawTitles(SpriteBatch sb, float tempYPos) {
        font.getData().setScale(scale * 0.4f);
        font.setColor(Color.YELLOW);

        layout.setText(font, "Mode");
        font.draw(sb, "Mode", width / 2f - layout.width / 2f, tempYPos - TITLE_MODE_Y * scale + layout.height / 2f);

        layout.setText(font, "Game Mode");
        font.draw(sb, "Game Mode", width / 2f - layout.width / 2f, tempYPos - TITLE_GAMEMODE_Y * scale + layout.height / 2f);
    }

    private void drawLetterOutline(SpriteBatch sb, String text, float x, float y) {
        Color originalColor = font.getColor().cpy();
        font.setColor(Color.BLACK);

        for (int dx = -OUTLINE_THICKNESS; dx <= OUTLINE_THICKNESS; dx++) {
            for (int dy = -OUTLINE_THICKNESS; dy <= OUTLINE_THICKNESS; dy++) {
                if (dx != 0 || dy != 0) {
                    font.draw(sb, text, x + dx, y + dy);
                }
            }
        }

        font.setColor(originalColor);
    }

    private static void drawRepeated(
        Texture texture,
        SpriteBatch sb,
        float x,
        float y,
        float areaWidth,
        float areaHeight,
        float tileWidth,
        float tileHeight
    ) {
        int srcW = Math.round(areaWidth * texture.getWidth() / tileWidth);
        int srcH = Math.round(areaHeight * texture.getHeight() / tileHeight);

        sb.draw(
            texture,
            x, y,
            0, 0,
            areaWidth, areaHeight,
            1, 1,
            0,
            0, 0,
            srcW, srcH,
            false, false
        );
    }

    @Override
    public void dispose() {
        background.dispose();
        pipeH.dispose();
        pipeL.dispose();
        pipeR.dispose();
        pipeVR.dispose();
        pipeVL.dispose();
        outline.dispose();
        dropDown.dispose();

        if (fb != null) {
            fb.dispose();
        }
    }

    private void setDimensions() {
        int fbWidth = Math.max(1, background.getWidth());
        int fbHeight = Math.max(1, background.getHeight());

        if (fb != null) {
            fb.dispose();
        }

        fb = new FrameBuffer(Pixmap.Format.RGBA8888, fbWidth, fbHeight, false);
    }

    private void reorder() {
        gamemodeNum = GAMEMODE_START_NUM;
        modeNum = MODE_START_NUM;

        for (Option option : options) {
            option.update();
        }
    }

    class Option {
        private final Texture optionBackground;
        public final String name;
        private final String label;
        private final boolean isMode;
        private final float fontHeight;

        public float num;
        private float fontWidth;
        private float buttonScale = 1f;
        private float fontScale = OPTION_FONT_SCALE;

        public Option(String name, Texture background) {
            this.name = name;
            this.optionBackground = background;
            this.isMode = isLowercaseName(name);

            if (isMode) {
                label = ("" + name.charAt(0)).toUpperCase() + name.substring(1);
            } else {
                label = name;
            }

            update();

            font.getData().setScale(1);
            layout.setText(font, name);
            fontWidth = layout.width;

            if (fontWidth > MAX_FONT_WIDTH) {
                float extra = MAX_FONT_WIDTH / layout.width;
                fontScale *= extra;
                font.getData().setScale(extra);
                layout.setText(font, name);
                fontWidth = layout.width / extra;
            }

            fontHeight = layout.height;

            addAnimation();
        }

        public void update() {
            if (isLowercaseName(name)) {
                if (name.equals(SessionData.mode)) {
                    num = 0;
                } else {
                    num = modeNum;
                    modeNum++;
                }
            } else {
                if (name.equals(SessionData.currentGamemode)) {
                    num = 4.5f;
                } else {
                    num = gamemodeNum;
                    gamemodeNum++;
                }
            }
        }

        private void addAnimation() {
            Animation animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
                @Override
                public void run(float delta) {
                    if (!buttonsEnabled) {
                        buttonScale = 1f;
                        return;
                    }

                    float tempYPos = yPos * scale;
                    float mouseX = Source.getScreenMouseX();
                    float mouseY = Source.getScreenMouseY();

                    boolean hovering =
                        Math.abs(mouseX - (width / 2f)) < OPTION_HOVER_HALF_WIDTH * scale &&
                            Math.abs(mouseY - (tempYPos - OPTION_Y_SPACING * (num + 1))) < OPTION_HOVER_HALF_HEIGHT * scale;

                    if (hovering) {
                        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !clickCooldown) {
                            if (isMode) {
                                SessionData.mode = label.toLowerCase();
                            } else {
                                SessionData.currentGamemode = label;
                            }

                            clickCooldown = true;
                            reorder();
                        }

                        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                            clickCooldown = false;
                        }

                        if (!Source.isOnDesktop()) {
                            return;
                        }

                        if (buttonScale < MAX_BUTTON_SCALE) {
                            buttonScale += delta;
                        } else {
                            buttonScale = MAX_BUTTON_SCALE;
                        }
                    } else {
                        if (buttonScale > 1f) {
                            buttonScale -= delta;
                        } else {
                            buttonScale = 1f;
                        }
                    }
                }
            });

            parent.addAnimation(animation);
        }

        public void draw(SpriteBatch sb) {
            float tempScale = scale * buttonScale;
            float tempYPos = yPos * scale;
            float centerY = tempYPos - OPTION_Y_SPACING * (num + 1) * scale * 2.6f;

            drawShadow(sb, (buttonScale - 1f) * 1000f + 50f, tempYPos);

            sb.draw(
                optionBackground,
                width / 2f - (outline.getWidth() - 20) / 2f * tempScale,
                centerY - (outline.getHeight() - 20) / 2f * tempScale,
                (outline.getWidth() - 20) * tempScale,
                (outline.getHeight() - 20) * tempScale
            );

            if (num == 0 || num == 3) {
                sb.setShader(Shader.glowShader);
                Shader.glowShader.setUniformf("u_brightness", 2f);
            }

            sb.draw(
                outline,
                width / 2f - outline.getWidth() / 2f * tempScale,
                centerY - outline.getHeight() / 2f * tempScale,
                outline.getWidth() * tempScale,
                outline.getHeight() * tempScale
            );

            if (num == 0 || num == 3) {
                sb.setShader(null);
            }

            float tempFontScale = tempScale * fontScale;
            float textX = width / 2f - fontWidth / 2f * tempFontScale;
            float textY = centerY + fontHeight / 2f * tempFontScale;

            font.getData().setScale(tempFontScale);
            font.setColor(Color.WHITE);

            drawLetterOutline(sb, label, textX, textY);
            font.draw(sb, label, textX, textY);
        }

        private void drawShadow(SpriteBatch sb, float offset, float tempYPos) {
            sb.setColor(0, 0, 0, 0.5f);
            sb.draw(
                outlineShadow,
                width / 2f - (outline.getWidth() - offset) / 2f * scale,
                tempYPos - OPTION_Y_SPACING * (num + 1) * scale * 2.6f - (outline.getHeight() + offset) / 2f * scale,
                outline.getWidth() * scale,
                outline.getHeight() * scale
            );
            sb.setColor(Color.WHITE);
        }

        private boolean isLowercaseName(String value) {
            return value.toLowerCase().equals(value);
        }
    }
}
