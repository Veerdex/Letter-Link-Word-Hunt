package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The main game board where players trace letters to form words.
 * Manages the letter grid, tile interactions, word validation, and visual feedback.
 * Supports mouse/touch input for selecting letter chains with real-time color-coded validation.
 */
public class Board extends Agent {

    // ========================================
    // CONSTANTS
    // ========================================

    // Tile color tints based on state
    private static final Color RED_TINT = new Color(1, 0.8f, 0.8f, 1f);
    private static final Color YELLOW_TINT = new Color(1, 1, 0.7f, 1f);
    private static final Color GREEN_TINT = new Color(0.7f, 1, 0.7f, 1f);
    private static final Color BLUE_TINT = new Color(0.7f, 0.7f, 1f, 1f);
    private static final Color PINK_TINT = new Color(0.5f, 1f, 1f, 1f);

    // Visual constants
    private static final float TRACE_WIDTH = 15f;

    // Timing constants
    private static final int RANK_CALCULATION_CUTOFF = 121000;
    private static final int RANK_CALCULATION_FREQUENCY = 5000;

    // ========================================
    // ENUMS
    // ========================================

    /**
     * Represents the validation state of a letter chain.
     */
    public enum LetterState {
        UNSELECTED,  // Not part of current chain
        INVALID,     // Invalid word
        VALID,       // Valid new word
        COPY,        // Word already found
        HINT,        // Hint tile
        HINT_START   // Starting tile of hint
    }

    // ========================================
    // STATIC GAME STATE
    // ========================================

    // Board dimensions
    private static int width;
    private static int height;
    private static List<List<Character>> board;

    // Game data
    public static final List<Boolean> wordsFound = new ArrayList<Boolean>();
    public static final List<String> listOfWordsFound = new ArrayList<String>();
    public static final List<String> wordsLeft = new ArrayList<String>();
    public static int boardValue = 0;
    public static int totalPoints = 0;
    public static float currentRank = 0;
    public static int SRankScore = 0;

    // Timing
    private static long startTime = 0;
    private static long lastGuess = 0;
    private static long nextLog = 10000;

    // Hints
    private static int hintsUsed = 0;
    private static int finalScore = 0;

    // UI state
    public static boolean menuOpen = false;
    public static boolean settingsOpen = false;

    // ========================================
    // STATIC RENDERING RESOURCES
    // ========================================

    // Textures and graphics
    private static Texture tileTexture;
    private static Texture backgroundTexture;
    private static Graphic boardBackground;
    private static Graphic textBackground;
    private static BitmapFont font;
    private static FrameBuffer fb;

    // Layout and scaling
    private static float scale = 1;
    private static float boardBackgroundScale = 1;
    private static float boardX = 0;
    private static float boardY = 0;

    // Unused legacy fields (kept for compatibility, could be removed)
    private static float mouseDirection = 0;
    private static float deltaX = 0;
    private static float deltaY = 0;

    // ========================================
    // STATIC TILE MANAGEMENT
    // ========================================

    private static final List<Tile> tiles = new ArrayList<Tile>();
    private static final List<Animation> tileAnimations = new ArrayList<Animation>();

    // Tile selection state
    private static Tile currentTile = null;
    private static Tile previousTile = null;
    private static final List<Tile> tileChain = new ArrayList<Tile>();
    private static String stringChain = "";
    public static LetterState currentChainState = LetterState.UNSELECTED;

    // Trace color
    private static final Color traceColor = DataManager.chainColor;

    // ========================================
    // INSTANCE FIELDS (for animation tracking)
    // ========================================

    private final LinkedList<Integer> prevDeltas = new LinkedList<Integer>();
    private int prevMouseX = 0;
    private int prevMouseY = 0;

    // ========================================
    // INITIALIZATION - PUBLIC API
    // ========================================

    /**
     * Generates textures and graphics resources.
     * Call this when visual settings change or on first load.
     */
    public static void generateTextures() {
        initializeFont();
        loadTextures();
    }

    /**
     * Generates tile objects for the current board.
     * Call this after board dimensions are set.
     */
    public static void generateObjects() {
        generateTiles();
    }

    /**
     * Creates a new game board with the specified dimensions.
     * Loads board data from Solver and initializes game state.
     */
    public static void loadNewBoard() {
        boolean remakeTextures = width != Solver.getBoardWidth() ||
            height != Solver.getBoardHeight();

        resetGameState();
        initializeTimers();
        loadBoardData();
        setDimensions();
        initializeBoard();

        if (remakeTextures) {
            generateTextures();
            generateObjects();
        }
    }

    // ========================================
    // INITIALIZATION - PRIVATE HELPERS
    // ========================================

    private static void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 256);
    }

    private static void loadTextures() {
        if (tileTexture == DataManager.tileTexture) {
            return;
        }

        tileTexture = DataManager.tileTexture;
        backgroundTexture = DataManager.backgroundTexture;
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        boardBackground = new Graphic(DataManager.boardTexture);
        textBackground = new Graphic(DataManager.bottomTextTexture);
    }

    private static void setDimensions() {
        width = Solver.getBoardWidth();
        height = Solver.getBoardHeight();

        if (fb != null) {
            fb.dispose();
        }

        fb = new FrameBuffer(Pixmap.Format.RGBA8888,
            Source.getScreenWidth(),
            Source.getScreenHeight(),
            false);
    }

    private static void initializeBoard() {
        board = Solver.getBoard();
    }

    private static void generateTiles() {
        tiles.clear();
        tileAnimations.clear();  // Important: prevent memory leak

        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                tiles.add(new Tile(x, y));
            }
        }
    }

    private static void resetGameState() {
        // Clear collections
        listOfWordsFound.clear();
        wordsLeft.clear();
        wordsFound.clear();
        tileChain.clear();

        // Reset references
        previousTile = null;
        currentTile = null;
        stringChain = "";

        // Reset counters
        totalPoints = 0;
        currentRank = 0;
        finalScore = 0;
        hintsUsed = 0;
        nextLog = 10000;
    }

    private static void initializeTimers() {
        startTime = System.currentTimeMillis();
        lastGuess = System.currentTimeMillis();
    }

    private static void loadBoardData() {
        boardValue = Solver.getBoardValue();
        wordsLeft.addAll(Solver.getTreasureWords());

        for (int i = 0; i < wordsLeft.size(); i++) {
            wordsFound.add(false);
        }
    }

    // ========================================
    // ANIMATION MANAGEMENT
    // ========================================

    /**
     * Registers all tile animations and word checking logic with the page.
     */
    public void frame() {
        registerTileAnimations();
        registerWordCheckAnimation();
        registerMouseTracker();
    }

    private void registerTileAnimations() {
        for (Animation animation : tileAnimations) {
            getPage().addAnimation(animation);
        }
    }

    private void registerWordCheckAnimation() {
        getPage().addAnimation(createWordCheckAnimation());
    }

    private void registerMouseTracker() {
        getPage().addTimer(new Timer(0.01f, Timer.INDEFINITE, new TimeFrame() {
            @Override
            public void run(long iteration) {
                updateMouseDelta();
            }
        }));
    }

    private void updateMouseDelta() {
        int dx = Source.getScreenMouseX() - prevMouseX;
        int dy = Source.getScreenMouseY() - prevMouseY;
        prevMouseX = Source.getScreenMouseX();
        prevMouseY = Source.getScreenMouseY();

        deltaX += dx;
        deltaY += dy;
        prevDeltas.addFirst(dy);
        prevDeltas.addFirst(dx);

        if (prevDeltas.size() > 10) {
            deltaY -= prevDeltas.removeLast();
            deltaX -= prevDeltas.removeLast();
        }
    }

    private static Animation createWordCheckAnimation() {
        return new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                handleWordSubmission();
            }
        });
    }

    // ========================================
    // WORD VALIDATION
    // ========================================

    /**
     * Checks if a word is valid and not already found, then records it.
     * @param word Word to validate
     * @return true if word was valid and newly found
     */
    private static boolean check(String word) {
        int index = Solver.getTreasureWords().indexOf(word);
        if (index != -1 && !wordsFound.get(index)) {
            wordsFound.set(index, true);
            listOfWordsFound.add(word);
            totalPoints += Solver.getWordValue(word);
            wordsLeft.remove(word);
            lastGuess = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Handles word submission when the player releases the mouse.
     */
    private static void handleWordSubmission() {
        if (tileChain.isEmpty() || Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            return;
        }

        check(buildWordFromChain());
        resetTileStates();
        resetMobileHoverState();
        resetTileChain();
    }

    private static void resetTileStates() {
        for (Tile tile : tileChain) {
            tile.tile.setColor(Color.WHITE);
            tile.state = LetterState.UNSELECTED;
        }
    }

    private static void resetMobileHoverState() {
        if (!Manager.isOnDesktop() && currentTile != null) {
            currentTile.hover = false;
            currentTile = null;
        }
    }

    /**
     * Builds a word string from the current tile chain (reversed order).
     * @return Word formed by the chain
     */
    private static String buildWordFromChain() {
        StringBuilder sb = new StringBuilder();
        for (int i = tileChain.size() - 1; i >= 0; i--) {
            sb.append(tileChain.get(i).letter);
        }
        return sb.toString();
    }

    /**
     * Clears the current tile chain and resets all tile states.
     */
    private static void resetTileChain() {
        tileChain.clear();

        for (Tile tile : tiles) {
            tile.added = false;
            tile.state = LetterState.UNSELECTED;
        }

        stringChain = "";
        previousTile = null;
    }

    // ========================================
    // CHAIN STATE MANAGEMENT
    // ========================================

    /**
     * Updates the state of all tiles in the current chain.
     * @param newState New state to apply
     */
    public static void setChainState(LetterState newState) {
        currentChainState = newState;
        for (Tile tile : tileChain) {
            tile.state = newState;
        }
    }

    /**
     * Updates the chain state based on word validation.
     */
    private static void updateChainState() {
        int wordState = getWordState();

        switch (wordState) {
            case 0:
                setChainState(LetterState.COPY);
                break;
            case 1:
                setChainState(LetterState.VALID);
                break;
            case 2:
                setChainState(LetterState.INVALID);
                break;
        }
    }

    /**
     * Determines the validation state of the current word chain.
     * @return 0 = already found, 1 = valid new word, 2 = invalid
     */
    private static int getWordState() {
        int index = Solver.getTreasureWords().indexOf(buildWordFromChain());
        if (index != -1) {
            return wordsFound.get(index) ? 0 : 1;
        }
        return 2;
    }

    // ========================================
    // HINT SYSTEM
    // ========================================

    /**
     * Activates a hint by highlighting the path for a specific word.
     * @param word The word to show a hint for
     */
    public static void activateHint(String word) {
        hintsUsed++;
        clearAllHints();

        List<Integer> path = Solver.getWordPath(Solver.getTreasureWords().indexOf(word));
        if (path == null) {
            return;
        }

        highlightHintPath(path);
    }

    private static void clearAllHints() {
        for (Tile tile : tiles) {
            tile.state = LetterState.UNSELECTED;
        }
    }

    private static void highlightHintPath(List<Integer> path) {
        for (int i = 0; i < path.size(); i += 2) {
            Tile tile = getTile(path.get(i), path.get(i + 1));
            if (tile == null) {
                continue;
            }

            if (i == 0) {
                tile.state = LetterState.HINT_START;
            } else {
                tile.state = LetterState.HINT;
            }
        }
    }

    // ========================================
    // GETTERS
    // ========================================

    public static float getBoardWidth() {
        return width;
    }

    public static float getBoardHeight() {
        return height;
    }

    public static float getBoardX() {
        return boardX;
    }

    public static float getBoardY() {
        return boardY;
    }

    public static List<List<Character>> getBoard() {
        return board;
    }

    public static List<String> getWordsInBoard() {
        return Solver.getTreasureWords();
    }

    public static List<Boolean> getWordsFound() {
        return wordsFound;
    }

    public static int getBoardValue() {
        return boardValue;
    }

    public static List<String> getListOfWordsFound() {
        return listOfWordsFound;
    }

    public static String getStringChain() {
        return stringChain;
    }

    public static int getTotalPoints() {
        return totalPoints;
    }

    public static LetterState getCurrentState() {
        return currentChainState;
    }

    public static List<String> getWordsLeft() {
        return wordsLeft;
    }

    public static float getCurrentRank() {
        return currentRank;
    }

    public static int getHintScore() {
        return (int)(System.currentTimeMillis() - lastGuess) / 1000;
    }

    public static int getHintsUsed() {
        return hintsUsed;
    }

    public static String getLongestWord() {
        int length = 0;
        String longestWord = "---";

        for (int i = 0; i < listOfWordsFound.size(); i++) {
            int newLength = listOfWordsFound.get(i).length();
            if (newLength > length) {
                longestWord = listOfWordsFound.get(i);
                length = newLength;
            }
        }

        return longestWord;
    }

    public static long getGameDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public static int getSRankScore() {
        return SRankScore;
    }

    // ========================================
    // SETTERS
    // ========================================

    public static void setBoardScale(float scale) {
        Board.scale = 12f / Math.max(width, height) * scale;
        Board.boardBackgroundScale = scale;
    }

    public static void setBoardX(float x) {
        Board.boardX = x;
    }

    public static void setBoardY(float y) {
        Board.boardY = y;
    }

    public static void setSRankScore(int score) {
        SRankScore = score;
    }

    /**
     * Updates the framebuffer, disposing the old one.
     * @param fb New framebuffer
     */
    public static void updateFrameBuffer(FrameBuffer fb) {
        if (Board.fb != null) {
            Board.fb.dispose();
        }
        Board.fb = fb;
    }

    // ========================================
    // RENDERING - MAIN DRAW METHOD
    // ========================================

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        updateRankCalculation();

        calculateLayout();
        drawBackground(sb);
        drawTextBackground(sb);
        drawBoardBackground(sb);
        drawTiles(sb);

        if (!menuOpen) {
            drawTraceLines(sr, sb);
        }
    }

    /**
     * Periodically calculates rank in background thread
     */
    private void updateRankCalculation() {
        long time = System.currentTimeMillis() - startTime;

        if (time > nextLog && time < RANK_CALCULATION_CUTOFF) {
            nextLog += RANK_CALCULATION_FREQUENCY;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    currentRank = (float)Math.pow(Solver.calculateRank(listOfWordsFound), 1.25f);
                    if (time > RANK_CALCULATION_CUTOFF - RANK_CALCULATION_FREQUENCY) {
                        finalScore = totalPoints;
                    }
                }
            }).start();
        }
    }

    // ========================================
    // RENDERING - LAYOUT
    // ========================================

    private static void calculateLayout() {
        setBoardX(Source.getScreenWidth() / 2f);
        setBoardY(Source.getScreenHeight() / 2f);

        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        setBoardScale(Math.min(xScale, yScale));
    }

    // ========================================
    // RENDERING - BACKGROUNDS
    // ========================================

    private static void drawBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();
        sb.draw(backgroundTexture,
            0, 0, 0, 0,
            Source.getScreenWidth(), Source.getScreenHeight(),
            1, 1, 0,
            0, 0,
            Source.getScreenWidth(), Source.getScreenHeight(),
            false, false);
        sb.end();
    }

    private static void drawTextBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();

        // Draw shadow
        textBackground.setColor(0, 0, 0, 0.5f);
        textBackground.setCenter(
            Source.getScreenWidth() / 2f + boardBackgroundScale * 50,
            Source.getScreenHeight() / 2f - boardBackgroundScale * 1175
        );
        textBackground.draw(sb);

        // Draw main background
        textBackground.setColor(Color.WHITE);
        textBackground.setCenter(
            Source.getScreenWidth() / 2f,
            Source.getScreenHeight() / 2f - boardBackgroundScale * 1125
        );
        textBackground.setScale(2.64598f * boardBackgroundScale * DataManager.bottomTextScale);
        textBackground.draw(sb);

        sb.end();
    }

    private static void drawBoardBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();

        drawBoardShadow(sb);

        boardBackground.setColor(1, 1, 1, 1);
        boardBackground.setScale(2.734375f * boardBackgroundScale * DataManager.boardScale);
        boardBackground.draw(sb);

        sb.end();
    }

    private static void drawBoardShadow(SpriteBatch sb) {
        boardBackground.setColor(0, 0, 0, 0.5f);
        boardBackground.setCenter(
            Source.getScreenWidth() / 2f + boardBackgroundScale * 50,
            Source.getScreenHeight() / 2f - boardBackgroundScale * 50
        );
        boardBackground.draw(sb);
        boardBackground.setCenter(Source.getScreenWidth() / 2f, Source.getScreenHeight() / 2f);
    }

    // ========================================
    // RENDERING - TILES
    // ========================================

    private static void drawTiles(SpriteBatch sb) {
        sb.begin();
        font.setColor(DataManager.tileTextColor);

        // Draw shadows for all tiles
        for (Tile tile : tiles) {
            tile.drawShadow(sb);
        }

        // Draw all non-hovered tiles
        for (Tile tile : tiles) {
            if (tile != currentTile) {
                updateTileColorForDrawing(tile);
                tile.drawTile(sb);
                drawLetter(sb, tile);
            }
        }

        // Draw hovered tile on top
        if (currentTile != null) {
            updateTileColorForDrawing(currentTile);
            currentTile.drawTile(sb);
            drawLetter(sb, currentTile);
        }

        sb.end();
    }

    /**
     * Updates tile color based on current state and context.
     */
    private static void updateTileColorForDrawing(Tile tile) {
        if (!tileChain.isEmpty()) {
            updateTileColor(tile);
        } else if (tile == currentTile && Manager.isOnDesktop() &&
            tile.state != LetterState.HINT && tile.state != LetterState.HINT_START) {
            tile.tile.setColor(RED_TINT);
        } else {
            applyHintOrDefaultColor(tile);
        }
    }

    private static void applyHintOrDefaultColor(Tile tile) {
        if (tile.state == LetterState.HINT) {
            tile.tile.setColor(BLUE_TINT);
        } else if (tile.state == LetterState.HINT_START) {
            tile.tile.setColor(PINK_TINT);
        } else {
            tile.tile.setColor(Color.WHITE);
        }
    }

    /**
     * Updates a tile's color based on its current state (for chain validation).
     */
    private static void updateTileColor(Tile tile) {
        switch (tile.state) {
            case COPY:
                tile.tile.setColor(YELLOW_TINT);
                break;
            case INVALID:
                tile.tile.setColor(RED_TINT);
                break;
            case VALID:
                tile.tile.setColor(GREEN_TINT);
                break;
            case HINT:
                tile.tile.setColor(BLUE_TINT);
                break;
            case HINT_START:
                tile.tile.setColor(PINK_TINT);
                break;
            case UNSELECTED:
            default:
                tile.tile.setColor(Color.WHITE);
                break;
        }
    }

    /**
     * Draws a letter on a tile, centered with optional outline.
     */
    private static void drawLetter(SpriteBatch sb, Tile tile) {
        font.getData().setScale(tile.letterScale * scale / 5f);
        tile.layout.setText(font, tile.letter.toUpperCase());

        float x = tile.x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale - tile.layout.width / 2;
        float y = tile.y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale + tile.layout.height / 2;

        if (DataManager.tileTextOutline) {
            drawLetterOutline(sb, tile, x, y);
        }

        font.draw(sb, tile.letter.toUpperCase(), x, y);
    }

    private static void drawLetterOutline(SpriteBatch sb, Tile tile, float x, float y) {
        Color originalColor = font.getColor().cpy();
        font.setColor(Color.BLACK);

        int outlineThickness = 2;
        for (int dx = -outlineThickness; dx <= outlineThickness; dx++) {
            for (int dy = -outlineThickness; dy <= outlineThickness; dy++) {
                if (dx != 0 || dy != 0) {
                    font.draw(sb, tile.letter.toUpperCase(), x + dx, y + dy);
                }
            }
        }

        font.setColor(originalColor);
    }

    // ========================================
    // RENDERING - TRACE LINES
    // ========================================

    private static void drawTraceLines(ShapeRenderer sr, SpriteBatch sb) {
        if (tileChain.isEmpty()) {
            return;
        }

        renderTraceToFrameBuffer(sr);
        renderFrameBufferToScreen(sb);
    }

    private static void renderTraceToFrameBuffer(ShapeRenderer sr) {
        fb.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setProjectionMatrix(Source.camera.combined);
        sr.setColor(traceColor);

        drawTraceFromMouseToFirstTile(sr);
        drawTracesBetweenTiles(sr);

        sr.end();
        fb.end();
    }

    private static void drawTraceFromMouseToFirstTile(ShapeRenderer sr) {
        float mouseX = Source.getScreenMouseX();
        float mouseY = Source.getScreenHeight() - Source.getScreenMouseY();

        float firstTileX = convertToScreenX(tileChain.get(0).x);
        float firstTileY = convertToScreenY(tileChain.get(0).y);

        sr.rectLine(mouseX, mouseY, firstTileX, firstTileY, TRACE_WIDTH * scale);
        sr.circle(mouseX, mouseY, TRACE_WIDTH / 2 * scale);
    }

    private static void drawTracesBetweenTiles(ShapeRenderer sr) {
        for (int i = 0; i < tileChain.size() - 1; i++) {
            float x1 = convertToScreenX(tileChain.get(i).x);
            float y1 = convertToScreenY(tileChain.get(i).y);
            float x2 = convertToScreenX(tileChain.get(i + 1).x);
            float y2 = convertToScreenY(tileChain.get(i + 1).y);

            sr.rectLine(x1, y1, x2, y2, TRACE_WIDTH * scale);
            sr.circle(x1, y1, TRACE_WIDTH / 2 * scale);
        }

        // Draw circle at the last tile
        int lastIndex = tileChain.size() - 1;
        float lastX = convertToScreenX(tileChain.get(lastIndex).x);
        float lastY = convertToScreenY(tileChain.get(lastIndex).y);
        sr.circle(lastX, lastY, TRACE_WIDTH / 2 * scale);
    }

    private static void renderFrameBufferToScreen(SpriteBatch sb) {
        sb.begin();
        sb.setColor(1, 1, 1, 0.5f);
        sb.draw(fb.getColorBufferTexture(), 0, 0, Source.getScreenWidth(), Source.getScreenHeight());
        sb.setColor(1, 1, 1, 1);
        sb.end();
    }

    // ========================================
    // COORDINATE CONVERSION
    // ========================================

    /**
     * Converts grid X coordinate to screen X coordinate.
     */
    private static float convertToScreenX(float gridX) {
        return gridX * (100 * scale) + boardX - width * (50 * scale) + 50 * scale;
    }

    /**
     * Converts grid Y coordinate to screen Y coordinate (inverted).
     */
    private static float convertToScreenY(float gridY) {
        return (height - gridY - 1) * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;
    }

    // ========================================
    // TILE LOOKUP
    // ========================================

    /**
     * Gets a tile at the specified grid coordinates.
     * @return Tile at position, or null if out of bounds
     */
    private static Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }

        int index = x * height + (height - 1 - y);
        return tiles.get(index);
    }

    // ========================================
    // CLEANUP
    // ========================================

    @Override
    public void dispose() {
        if (fb != null) {
            fb.dispose();
        }
    }

    // ========================================
    // INNER CLASS: TILE
    // ========================================

    /**
     * Represents a single letter tile on the board.
     * Handles rendering, animation, and interaction logic.
     */
    static class Tile {

        // ========== Constants ==========

        private static final float HOVER_SCALE_BONUS = 0.2f;
        private static final float SCALE_SPEED = 2f;
        private static final int HOVER_RADIUS = 45;
        private static final int SELECTION_RADIUS = 45;
        private static final int DESELECTION_RADIUS = 35;

        // ========== State ==========

        public final int x, y;
        public Sprite tile;
        public String letter;
        public GlyphLayout layout;
        public float letterScale = 1;
        public LetterState state = LetterState.UNSELECTED;
        public Animation animation;

        private boolean hover = false;
        private boolean added = false;

        // ========== Constructor ==========

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;

            initializeTileSprite();
            initializeLetter();
            createAnimation();
        }

        // ========== Initialization ==========

        private void initializeTileSprite() {
            tile = new Sprite(tileTexture);
            tile.setCenterX(x * 100 + 200 - width * (50 * scale));
        }

        private void initializeLetter() {
            letter = "" + board.get(x).get(y);
            layout = new GlyphLayout();
            layout.setText(font, letter.toUpperCase());
        }

        private void createAnimation() {
            animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
                @Override
                public void run(float delta) {
                    if (menuOpen) {
                        return;
                    }

                    updateHoverState(delta);
                    handleTileSelection();
                    handleTileDeselection();
                }
            });

            tileAnimations.add(animation);
        }

        // ========== Drawing ==========

        public void drawTile(SpriteBatch sb) {
            updateTilePosition(0, 0);
            tile.draw(sb);
        }

        public void drawShadow(SpriteBatch sb) {
            updateTilePosition(20 + (letterScale - 1) * 75, -20 - (letterScale - 1) * 75);
            sb.setShader(Shader.blurShader);
            Shader.blurShader.setUniformf("u_tint", 0f, 0f, 0f, 0.5f);
            Shader.blurShader.setUniformf("blurSize", 0.01f);
            tile.setScale(scale * 0.3f * DataManager.tileScale);
            tile.draw(sb);
            sb.setShader(null);
        }

        private void updateTilePosition(float offsetX, float offsetY) {
            float centerX = x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale + offsetX * scale;
            float centerY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale + offsetY * scale;
            float tileScale = scale * 0.185546f;

            tile.setCenterX(Math.round(centerX));
            tile.setCenterY(Math.round(centerY));
            tile.setScale(letterScale * tileScale * 2 * DataManager.tileScale);
        }

        // ========== Hover Animation ==========

        private void updateHoverState(float delta) {
            if (isHovering()) {
                if (Manager.isOnDesktop()) {
                    increaseScale(delta);
                }
                activateHover();
            } else {
                if (Manager.isOnDesktop()) {
                    decreaseScale(delta);
                }
                deactivateHover();
            }
        }

        private void increaseScale(float delta) {
            float targetScale = 1f + HOVER_SCALE_BONUS;
            if (letterScale < targetScale) {
                letterScale += delta * SCALE_SPEED;
                letterScale = Math.min(letterScale, targetScale);
            }
        }

        private void decreaseScale(float delta) {
            if (letterScale > 1f) {
                letterScale -= delta * SCALE_SPEED;
                letterScale = Math.max(letterScale, 1f);
            }
        }

        private void activateHover() {
            if (!hover) {
                hover = true;
                currentTile = this;
            }
        }

        private void deactivateHover() {
            if (hover) {
                hover = false;
                currentTile = null;

                if (shouldResetToUnselected()) {
                    state = LetterState.UNSELECTED;
                    tile.setColor(Color.WHITE);
                }
            }
        }

        private boolean shouldResetToUnselected() {
            return tileChain.isEmpty() &&
                Manager.isOnDesktop() &&
                state != LetterState.HINT &&
                state != LetterState.HINT_START;
        }

        // ========== Selection Logic ==========

        private void handleTileSelection() {
            if (hover && !added && isSelected(SELECTION_RADIUS)) {
                addToChain();
            }
        }

        private void handleTileDeselection() {
            if (shouldDeselect()) {
                removeFromChain();
            }
        }

        private boolean shouldDeselect() {
            return tileChain.size() > 1 &&
                hover &&
                added &&
                isSelected(DESELECTION_RADIUS) &&
                tileChain.get(1) == this;
        }

        private void addToChain() {
            added = true;
            tileChain.add(0, this);
            stringChain = stringChain.concat(letter);
            previousTile = this;
            updateChainState();
        }

        private void removeFromChain() {
            tileChain.get(0).added = false;
            tileChain.get(0).state = LetterState.UNSELECTED;

            previousTile = this;
            stringChain = stringChain.substring(0, stringChain.length() - 1);
            tileChain.remove(0);

            updateChainState();
        }

        /**
         * Programmatically adds tile to chain without hover/click (for snappy selection).
         */
        private void quickSelect() {
            if (!added) {
                addToChain();
            }
        }

        // ========== Hit Detection ==========

        public boolean isHovering() {
            return checkHitCircle(HOVER_RADIUS);
        }

        public boolean isSelected(int radius) {
            return isAdjacentToPrevious() &&
                Gdx.input.isButtonPressed(Input.Buttons.LEFT) &&
                checkHitCircle(radius);
        }

        private boolean isAdjacentToPrevious() {
            return previousTile == null ||
                (Math.abs(previousTile.x - x) <= 1 && Math.abs(previousTile.y - y) <= 1);
        }

        private boolean checkHitCircle(float radius) {
            float tileX = x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale;
            float tileY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;

            float deltaX = Source.getScreenMouseX() - tileX;
            float deltaY = Source.getScreenMouseY() - tileY;

            return Math.sqrt(deltaX * deltaX + deltaY * deltaY) < radius * scale;
        }
    }
}
