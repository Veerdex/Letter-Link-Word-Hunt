package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.*;

/**
 * Displays the game results screen after completing a board.
 * Shows score, rating, statistics, and performance metrics.
 */
public class FinishPage extends Page {

    // ========================================
    // CONSTANTS - GRADING THRESHOLDS
    // ========================================

    private static final float GRADE_F_MINUS = 6.67f;
    private static final float GRADE_F = 13.33f;
    private static final float GRADE_F_PLUS = 20f;
    private static final float GRADE_D_MINUS = 26.67f;
    private static final float GRADE_D = 33.33f;
    private static final float GRADE_D_PLUS = 40f;
    private static final float GRADE_C_MINUS = 46.67f;
    private static final float GRADE_C = 53.33f;
    private static final float GRADE_C_PLUS = 60f;
    private static final float GRADE_B_MINUS = 66.67f;
    private static final float GRADE_B = 73.33f;
    private static final float GRADE_B_PLUS = 80f;
    private static final float GRADE_A_MINUS = 86.67f;
    private static final float GRADE_A = 93.33f;
    private static final float GRADE_A_PLUS = 100f;
    private static final float GRADE_S_MINUS = 106.67f;
    private static final float GRADE_S = 113.33f;

    // ========================================
    // CONSTANTS - TIME CALCULATION
    // ========================================

    private static final int SECONDS_PER_MINUTE = 60;
    private static final float MAX_TIME_BONUS_SECONDS = 120f;

    // ========================================
    // INSTANCE FIELDS
    // ========================================

    private final BoardResult results;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture statBackgroundTexture;
    private final Texture backgroundTexture;
    private final Texture moreTexture;
    private final Texture finishTexture;
    private final Graphic statBackground;
    private final Graphic moreButton;
    private final Graphic finishButton;

    // Formatted display strings
    private final String time;
    private final String score;
    private final String boardValue;
    private final String rating;

    private final DataDisplay dd;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public FinishPage(BoardResult results) {
        this.results = results;

        // Format display values
        this.time = formatTime(results.timeSeconds);
        this.score = formatWithCommas(results.score);
        this.boardValue = formatWithCommas((int) results.boardValue);
        this.rating = calculateLetterGrade(results);

        // Initialize textures
        this.backgroundTexture = initializeBackgroundTexture();
        this.statBackgroundTexture = initializeStatBackgroundTexture();
        this.moreTexture = DataManager.moreButtonTexture;
        this.finishTexture = DataManager.finishButtonTexture;
        this.statBackground = new Graphic(statBackgroundTexture);
        this.moreButton = new Graphic(moreTexture);
        this.finishButton = new Graphic(finishTexture);

        // Initialize font
        this.font = Source.generateFont("Cinzel", 128);
        this.layout = new GlyphLayout();

        // Add display agent
        this.dd = new DataDisplay();
        add(dd);
    }

    // ========================================
    // INITIALIZATION
    // ========================================

    private Texture initializeBackgroundTexture() {
        return DataManager.backgroundTexture;
    }

    private Texture initializeStatBackgroundTexture() {
        Texture texture = new Texture(Source.getAsset("Misc/Stat Background.png"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    // ========================================
    // PAGE LIFECYCLE
    // ========================================

    @Override
    public void initialize() {
        // No additional initialization needed
    }

    @Override
    public void restart() {
        add(dd);
    }

    @Override
    public void dispose() {
        statBackgroundTexture.dispose();
    }

    // ========================================
    // GRADING SYSTEM
    // ========================================

    /**
     * Calculates the letter grade based on performance.
     * Incorporates score, board value, and time bonus.
     */
    private String calculateLetterGrade(BoardResult results) {
        float timeBonusMultiplier = calculateTimeBonusMultiplier(results.timeSeconds);
        float adjustedTarget = results.SRankScore / timeBonusMultiplier;
        float percentage = (results.score / adjustedTarget) * 100f;

        return getLetterGradeFromPercentage(percentage);
    }

    /**
     * Calculates time bonus multiplier (faster completion = higher multiplier).
     * Maxes out at 120 seconds for full bonus.
     */
    private float calculateTimeBonusMultiplier(long timeSeconds) {
        return Math.max(MAX_TIME_BONUS_SECONDS / (float) timeSeconds, 1f);
    }

    /**
     * Converts percentage to letter grade with +/- modifiers.
     */
    private String getLetterGradeFromPercentage(float percent) {
        if (percent < GRADE_F_MINUS) {
            return "F-";
        } else if (percent < GRADE_F) {
            return "F";
        } else if (percent < GRADE_F_PLUS) {
            return "F+";
        } else if (percent < GRADE_D_MINUS) {
            return "D-";
        } else if (percent < GRADE_D) {
            return "D";
        } else if (percent < GRADE_D_PLUS) {
            return "D+";
        } else if (percent < GRADE_C_MINUS) {
            return "C-";
        } else if (percent < GRADE_C) {
            return "C";
        } else if (percent < GRADE_C_PLUS) {
            return "C+";
        } else if (percent < GRADE_B_MINUS) {
            return "B-";
        } else if (percent < GRADE_B) {
            return "B";
        } else if (percent < GRADE_B_PLUS) {
            return "B+";
        } else if (percent < GRADE_A_MINUS) {
            return "A-";
        } else if (percent < GRADE_A) {
            return "A";
        } else if (percent < GRADE_A_PLUS) {
            return "A+";
        } else if (percent < GRADE_S_MINUS) {
            return "S-";
        } else if (percent < GRADE_S) {
            return "S";
        } else {
            return "S+";
        }
    }

    // ========================================
    // FORMATTING UTILITIES
    // ========================================

    /**
     * Formats a number with comma separators (e.g., 1000 -> "1,000").
     */
    private String formatWithCommas(int number) {
        return String.format("%,d", number);
    }

    /**
     * Converts seconds to MM:SS format.
     */
    private String formatTime(long seconds) {
        long minutes = seconds / SECONDS_PER_MINUTE;
        long remainingSeconds = seconds % SECONDS_PER_MINUTE;
        return padZero(minutes) + ":" + padZero(remainingSeconds);
    }

    /**
     * Pads single digit numbers with leading zero.
     */
    private String padZero(long value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    // ========================================
    // INNER CLASS: DATA DISPLAY
    // ========================================

    /**
     * Agent responsible for rendering all statistics on the results screen.
     */
    class DataDisplay extends Agent {

        // ========== Layout Constants ==========

        private static final float SCREEN_HEIGHT_REFERENCE = 3000f;
        private static final float SCREEN_WIDTH_REFERENCE = 1500f;
        private static final float STAT_BACKGROUND_HEIGHT = 2000f;

        private static final float TITLE_FONT_SCALE = 1.25f;
        private static final float SCORE_FONT_SCALE = 2f;
        private static final float STAT_FONT_SCALE = 0.75f;

        private static final float TITLE_Y_OFFSET = 925f;
        private static final float SCORE_Y_OFFSET = 775f;

        private static final float LEFT_COLUMN_X = -650f;
        private static final float RIGHT_COLUMN_X = 650f;

        private static final float LONGEST_WORD_MAX_WIDTH = 500f;
        private static final float DECIMAL_PRECISION = 10f;

        private static final float GLOW_BRIGHTNESS = 1.25f;

        private float moreButtonScale = 1;
        private float finishButtonScale = 1;
        private float scale = 1;
        private float centerX = 0;
        private float centerY = 0;
        private boolean mouseDown = false;

        public DataDisplay(){
            addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
                @Override
                public void run(float delta) {
                    boolean click = false;
                    if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                        if(!mouseDown) {
                            click = true;
                            mouseDown = true;
                        }
                    } else {
                        mouseDown = false;
                    }
                    float yPos = centerY * 2 - 300 * scale;
                    if(Math.abs(Source.getScreenMouseX() - centerX) < 400 * scale && Math.abs(Source.getScreenMouseY() - yPos) < 150 * scale){
                        if(click){
                            moreButtonScale = 1;
                            WordOverview wo = new WordOverview();
                            Source.loadNewPage(wo);
                        }
                        if(!Manager.isOnDesktop()) return;
                        if(moreButtonScale < 1.2) {
                            moreButtonScale += delta * 2;
                        } else {
                            moreButtonScale = 1.2f;
                        }
                    } else {
                        if(moreButtonScale > 1) {
                            moreButtonScale -= delta * 2;
                        } else {
                            moreButtonScale = 1;
                        }
                    }
                    yPos = 300 * scale;
                    if(Math.abs(Source.getScreenMouseX() - centerX) < 400 * scale && Math.abs(Source.getScreenMouseY() - yPos) < 150 * scale){
                        if(click){
                            finishButtonScale = 1;
                            MainMenu mm = new MainMenu();
                            Source.loadNewPage(mm);
                        }
                        if(!Manager.isOnDesktop()) return;
                        if(finishButtonScale < 1.2) {
                            finishButtonScale += delta * 2;
                        } else {
                            finishButtonScale = 1.2f;
                        }
                    } else {
                        if(finishButtonScale > 1) {
                            finishButtonScale -= delta * 2;
                        } else {
                            finishButtonScale = 1;
                        }
                    }
                }
            }));
        }

        // ========== Stat Row Positions ==========

        private final StatRow[] STAT_ROWS = {
            new StatRow("Rating", 500f),
            new StatRow("Board Value", 345f),
            new StatRow("Total Words", 190f),
            new StatRow("Longest Word", 35f),
            new StatRow("Avg Word Length", -120f),
            new StatRow("Words/sec", -275f),
            new StatRow("Points/sec", -430f),
            new StatRow("Duration", -585f),
            new StatRow("Hints Used", -740f)
        };

        // ========================================
        // DRAWING
        // ========================================

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            scale = calculateScale();
            centerX = Source.getScreenWidth() / 2f;
            centerY = Source.getScreenHeight() / 2f;

            sb.begin();
            sb.setProjectionMatrix(Source.camera.combined);

            drawBackgrounds(sb);
            drawTitle(sb);
            drawScoreWithGlow(sb);
            drawStatistics(sb);
            drawMoreButton(sb);
            drawFinishButton(sb);

            sb.end();
        }

        // ========== Scale Calculation ==========

        private float calculateScale() {
            float yScale = Source.getScreenHeight() / SCREEN_HEIGHT_REFERENCE;
            float xScale = Source.getScreenWidth() / SCREEN_WIDTH_REFERENCE;
            return Math.min(xScale, yScale);
        }

        // ========== Background Drawing ==========

        private void drawMoreButton(SpriteBatch sb){
            moreButton.setCenter(centerX, centerY * 2 - 300 * scale);
            moreButton.setScale(300 * scale / moreTexture.getHeight() * moreButtonScale);
            moreButton.draw(sb);
        }

        private void drawFinishButton(SpriteBatch sb){
            finishButton.setCenter(centerX, 300 * scale);
            finishButton.setScale(300 * scale / finishButton.getHeight() * finishButtonScale);
            finishButton.draw(sb);
        }

        private void drawBackgrounds(SpriteBatch sb) {
            // Draw tiled background
            sb.setColor(0.5f, 0.5f, 0.5f, 1);
            sb.draw(backgroundTexture,
                0, 0, 0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                1, 1, 0,
                0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                false, false);

            // Draw stat panel background
            sb.setColor(1, 1, 1, 1);
            statBackground.setScale(STAT_BACKGROUND_HEIGHT * scale / statBackgroundTexture.getHeight());
            statBackground.setCenter(centerX, centerY);
            statBackground.draw(sb);
        }

        // ========== Title Drawing ==========

        private void drawTitle(SpriteBatch sb) {
            font.getData().setScale(scale * TITLE_FONT_SCALE);
            layout.setText(font, "Score");

            font.setColor(DataManager.menuColor.r, DataManager.menuColor.g,
                DataManager.menuColor.b, 1);
            font.draw(sb, "Score",
                centerX - layout.width / 2,
                centerY + TITLE_Y_OFFSET * scale);
        }

        // ========== Score Drawing ==========

        private void drawScoreWithGlow(SpriteBatch sb) {
            font.getData().setScale(scale * SCORE_FONT_SCALE);
            layout.setText(font, score);

            sb.setShader(Shader.glowShader);
            Shader.glowShader.setUniformf("u_brightness", GLOW_BRIGHTNESS);

            font.draw(sb, score,
                centerX - layout.width / 2,
                centerY + SCORE_Y_OFFSET * scale);
        }

        // ========== Statistics Drawing ==========

        private void drawStatistics(SpriteBatch sb) {
            font.getData().setScale(scale * STAT_FONT_SCALE);

            // Draw all stat values (right column)
            drawStatValue(sb, 0, rating);
            drawStatValue(sb, 1, boardValue);
            drawStatValue(sb, 2, String.valueOf(results.totalWords));
            drawLongestWordStat(sb);
            drawStatValue(sb, 4, formatDecimal(results.averageWordLength));
            drawStatValue(sb, 5, formatDecimal(results.wordsPerSecond));
            drawStatValue(sb, 6, formatDecimal(results.pointsPerSecond));
            drawStatValue(sb, 7, time);
            drawStatValue(sb, 8, String.valueOf(results.hintsUsed));

            // Reset shader and draw labels (left column)
            sb.setShader(null);
            drawStatLabels(sb);
        }

        private void drawStatValue(SpriteBatch sb, int rowIndex, String value) {
            layout.setText(font, value);
            float x = centerX + RIGHT_COLUMN_X * scale - layout.width;
            float y = centerY + STAT_ROWS[rowIndex].yOffset * scale;
            font.draw(sb, value, x, y);
        }

        private void drawLongestWordStat(SpriteBatch sb) {
            String longestWord = results.longestWord;
            font.getData().setScale(scale * STAT_FONT_SCALE);
            layout.setText(font, longestWord);

            // Scale down if too long
            if (layout.width > LONGEST_WORD_MAX_WIDTH * scale) {
                float adjustedScale = (LONGEST_WORD_MAX_WIDTH * scale / layout.width) *
                    scale * STAT_FONT_SCALE;
                font.getData().setScale(adjustedScale);
                layout.setText(font, longestWord);
            }

            float x = centerX + RIGHT_COLUMN_X * scale - layout.width;
            float y = centerY + STAT_ROWS[3].yOffset * scale;
            font.draw(sb, longestWord, x, y);

            // Reset scale
            font.getData().setScale(scale * STAT_FONT_SCALE);
        }

        private void drawStatLabels(SpriteBatch sb) {
            float x = centerX + LEFT_COLUMN_X * scale;

            for (StatRow row : STAT_ROWS) {
                float y = centerY + row.yOffset * scale;
                font.draw(sb, row.label, x, y);
            }
        }

        // ========== Formatting ==========

        private String formatDecimal(float value) {
            return String.valueOf(Math.round(value * DECIMAL_PRECISION) / DECIMAL_PRECISION);
        }

        // ========================================
        // CLEANUP
        // ========================================

        @Override
        public void dispose() {
            // No resources to dispose
        }

        // ========================================
        // HELPER CLASS: STAT ROW
        // ========================================

        /**
         * Represents a row in the statistics display.
         */
        private class StatRow {
            final String label;
            final float yOffset;

            StatRow(String label, float yOffset) {
                this.label = label;
                this.yOffset = yOffset;
            }
        }
    }
}
