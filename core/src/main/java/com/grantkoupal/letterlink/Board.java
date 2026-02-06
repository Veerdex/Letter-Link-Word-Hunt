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

    // ========== Constants ==========

    // Tile color tints based on state
    private static final Color RED_TINT = new Color(1, 0.8f, 0.8f, 1f);
    private static final Color YELLOW_TINT = new Color(1, 1, 0.7f, 1f);
    private static final Color GREEN_TINT = new Color(0.7f, 1, 0.7f, 1f);

    // Board constraints
    private static final int MIN_BOARD_SIZE = 4;

    // Visual constants
    private static final float TRACE_WIDTH = 20f;
    private static final float TRACE_ALPHA = 0.5f;

    // ========== Letter State Enum ==========

    /**
     * Represents the validation state of a letter chain.
     */
    public enum LetterState {
        UNSELECTED,  // Not part of current chain
        INVALID,     // Invalid word
        VALID,       // Valid new word
        COPY         // Word already found
    }

    // ========== Textures and Graphics ==========

    private Texture tileTexture;
    private Texture backgroundTexture;
    private Graphic boardBackground;
    private Graphic textBackground;
    private BitmapFont font;
    private FrameBuffer fb;

    // ========== Board Structure ==========

    private int width;
    private int height;
    private List<List<Character>> board;
    private final List<Tile> tiles = new ArrayList<>();

    // ========== Word Data ==========

    private final List<String> wordsInBoard;
    private final List<Boolean> wordsFound = new ArrayList<>();
    private final List<String> listOfWordsFound = new ArrayList<>();
    private final int boardValue;
    private int totalPoints = 0;

    // ========== Layout and Scaling ==========

    private float scale = 1;
    private float boardBackgroundScale = 1;
    private float boardX = 0;
    private float boardY = 0;
    private float mouseDirection = 0;
    private float deltaX = 0;
    private float deltaY = 0;

    // ========== Tile Selection State ==========

    private Tile currentTile = null;
    private Tile previousTile = null;
    private final List<Tile> tileChain = new ArrayList<>();
    private String stringChain = "";
    public LetterState currentChainState = LetterState.UNSELECTED;

    // ========== Animation ==========

    private final List<Animation> tileAnimations = new ArrayList<>();
    private final Color traceColor = DataManager.chainColor;
    private final long startTime;
    private long nextLog = 10000;

    // ========== Constructor ==========

    /**
     * Creates a new game board with the specified dimensions and difficulty.
     * @param width Number of columns (minimum 4)
     * @param height Number of rows (minimum 4)
     */
    public Board(int width, int height, List<List<Character>> board) {
        startTime = System.currentTimeMillis();
        this.board = board;
        boardValue = Solver.getBoardValue();
        wordsInBoard = Solver.getTreasureWords();
        for(int i = 0; i < wordsInBoard.size(); i++){
            wordsFound.add(false);
        }
        initializeFont();
        loadTextures();
        setDimensions(width, height);
        initializeBoard();
        generateTiles();
    }

    // ========== Initialization ==========

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 256);
    }

    private void loadTextures() {
        tileTexture = DataManager.tileTexture;
        backgroundTexture = DataManager.backgroundTexture;

        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        boardBackground = new Graphic(DataManager.boardTexture);
        textBackground = new Graphic(DataManager.bottomTextTexture);
    }

    private void setDimensions(int width, int height) {
        this.width = Math.max(width, MIN_BOARD_SIZE);
        this.height = Math.max(height, MIN_BOARD_SIZE);

        fb = new FrameBuffer(Pixmap.Format.RGBA8888, Source.getScreenWidth(), Source.getScreenHeight(), false);
    }

    private void initializeBoard() {
        board = Solver.getBoard();
    }

    /**
     * Creates Tile objects for each position on the board.
     */
    private void generateTiles() {
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                tiles.add(new Tile(x, y));
            }
        }
    }

    // ========== Animation Management ==========

    private final LinkedList<Integer> prevDeltas = new LinkedList<>();

    /**
     * Registers all tile animations and word checking logic with the page.
     * @param page Parent page to add animations to
     */
    public void addAnimations(Page page) {
        for (Animation animation : tileAnimations) {
            page.addAnimation(animation);
        }
        page.addAnimation(createWordCheckAnimation());

        page.addTimer(new Timer(.01f, Timer.INDEFINITE, new TimeFrame(){
            @Override
            public void run(long iteration){
                int dx = Source.getScreenMouseX() - prevMouseX;
                int dy = Source.getScreenMouseY() - prevMouseY;
                prevMouseX = Source.getScreenMouseX();
                prevMouseY = Source.getScreenMouseY();

                deltaX += dx;
                deltaY += dy;
                prevDeltas.addFirst(dy);
                prevDeltas.addFirst(dx);
                if(prevDeltas.size() > 10){
                    deltaY -= prevDeltas.removeLast();
                    deltaX -= prevDeltas.removeLast();
                }
                //System.out.println(deltaX + ", " + deltaY);
            }
        }));
    }

    /**
     * Creates an animation that handles word submission and background updates.
     */
    private Animation createWordCheckAnimation() {
        return new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                handleWordSubmission();
            }
        });
    }

    // ========== Word Validation ==========

    /**
     * Checks if a word is valid and not already found, then records it.
     * @param word Word to validate
     * @return true if word was valid and newly found
     */
    public boolean check(String word) {
        int index = wordsInBoard.indexOf(word);
        if (index != -1 && !wordsFound.get(index)) {
            wordsFound.set(index, true);
            listOfWordsFound.add(word);
            totalPoints += Solver.getWordValue(word);
            return true;
        }
        return false;
    }

    /**
     * Handles word submission when the player releases the mouse.
     */
    private void handleWordSubmission() {
        if (tileChain.size() > 0 && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            check(buildWordFromChain());

            // Reset all tiles in the chain
            for (Tile tile : tileChain) {
                tile.tile.setColor(Color.WHITE);
                tile.state = LetterState.UNSELECTED;
            }

            // Reset hover state on mobile
            if (!Manager.isOnDesktop() && currentTile != null) {
                currentTile.hover = false;
                currentTile = null;
            }

            resetTileChain();
        }
    }

    /**
     * Builds a word string from the current tile chain (reversed order).
     * @return Word formed by the chain
     */
    private String buildWordFromChain() {
        StringBuilder sb = new StringBuilder();
        for (int i = tileChain.size() - 1; i >= 0; i--) {
            sb.append(tileChain.get(i).letter);
        }
        return sb.toString();
    }

    /**
     * Clears the current tile chain and resets all tile states.
     */
    private void resetTileChain() {
        tileChain.clear();
        for (Tile tile : tiles) {
            tile.added = false;
            tile.state = LetterState.UNSELECTED;
        }
        stringChain = "";
        previousTile = null;
    }

    // ========== Chain State Management ==========

    /**
     * Updates the state of all tiles in the current chain.
     * @param newState New state to apply
     */
    public void setChainState(LetterState newState) {
        currentChainState = newState;
        for (Tile tile : tileChain) {
            tile.state = newState;
        }
    }

    /**
     * Updates the chain state based on word validation.
     */
    public void updateChainState() {
        switch (getWordState()) {
            case 0: setChainState(LetterState.COPY); break;     // Already found
            case 1: setChainState(LetterState.VALID); break;    // Valid new word
            case 2: setChainState(LetterState.INVALID); break;  // Invalid word
        }
    }

    /**
     * Determines the validation state of the current word chain.
     * @return 0 = already found, 1 = valid new word, 2 = invalid
     */
    private int getWordState() {
        int index = wordsInBoard.indexOf(buildWordFromChain());
        if (index != -1) {
            return wordsFound.get(index) ? 0 : 1;
        }
        return 2;
    }

    // ========== Setters ==========

    public void setScale(float scale) {
        this.scale = 12f / Math.max(width, height) * scale;
        this.boardBackgroundScale = scale;
    }

    public void setBoardX(float x) {
        this.boardX = x;
    }

    public void setBoardY(float y) {
        this.boardY = y;
    }

    /**
     * Updates the framebuffer, disposing the old one.
     * @param fb New framebuffer
     */
    public void updateFrameBuffer(FrameBuffer fb) {
        if (this.fb != null) {
            this.fb.dispose();
        }
        this.fb = fb;
    }

    // ========== Getters ==========

    public float getBoardWidth() {
        return width;
    }

    public float getBoardHeight() {
        return height;
    }

    public float getBoardX() {
        return boardX;
    }

    public float getBoardY() {
        return boardY;
    }

    public List<List<Character>> getBoard() {
        return board;
    }

    public List<String> getWordsInBoard() {
        return wordsInBoard;
    }

    public List<Boolean> getWordsFound() {
        return wordsFound;
    }

    public int getBoardValue() {
        return boardValue;
    }

    public List<String> getListOfWordsFound() {
        return listOfWordsFound;
    }

    public String getStringChain() {
        return stringChain;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public LetterState getCurrentState() {
        return currentChainState;
    }

    // ========== Drawing ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {

        if(System.currentTimeMillis() - startTime > nextLog){
            nextLog += 10000;
            new Thread(() -> {
                System.out.println(Math.pow(Solver.calculateRank(listOfWordsFound), 1.25f));
            }).start();
        }

        calculateLayout();
        drawBackground(sb);
        drawTextBackground(sb);
        drawBoardBackground(sb);
        drawTiles(sb);
        drawTraceLines(sr, sb);
    }

    /**
     * Calculates layout positions and scale based on screen size.
     */
    private void calculateLayout() {
        setBoardX(Source.getScreenWidth() / 2f);
        setBoardY(Source.getScreenHeight() / 2f);

        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        setScale(Math.min(xScale, yScale));
    }

    /**
     * Draws the tiled background texture.
     */
    private void drawBackground(SpriteBatch sb) {
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

    /**
     * Draws the background behind the bottom text area.
     */
    private void drawTextBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();
        textBackground.setColor(0, 0, 0, .5f);
        textBackground.setCenter(Source.getScreenWidth() / 2f + boardBackgroundScale * 50, Source.getScreenHeight() / 2f - boardBackgroundScale * 1175);
        textBackground.draw(sb);
        textBackground.setColor(Color.WHITE);
        textBackground.setCenter(Source.getScreenWidth() / 2f, Source.getScreenHeight() / 2f - boardBackgroundScale * 1125);
        textBackground.setScale(2.64598f * boardBackgroundScale * DataManager.bottomTextScale);
        textBackground.draw(sb);
        sb.end();
    }

    /**
     * Draws the board background graphic.
     */
    private void drawBoardBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();
        drawBoardShadow(sb);
        boardBackground.setColor(1, 1, 1, 1);
        boardBackground.setScale(2.734375f * boardBackgroundScale * DataManager.boardScale);
        boardBackground.draw(sb);
        sb.end();
    }

    private void drawBoardShadow(SpriteBatch sb){
        boardBackground.setColor(0, 0, 0, .5f);
        boardBackground.setCenter(Source.getScreenWidth() / 2f + boardBackgroundScale * 50, Source.getScreenHeight() / 2f - boardBackgroundScale * 50);
        boardBackground.draw(sb);
        boardBackground.setCenter(Source.getScreenWidth() / 2f, Source.getScreenHeight() / 2f);
    }

    /**
     * Updates a tile's color based on its current state.
     */
    private void updateTileColor(Tile tile) {
        switch (tile.state) {
            case COPY:
                tile.tile.setColor(YELLOW_TINT);
                return;
            case INVALID:
                tile.tile.setColor(RED_TINT);
                return;
            case VALID:
                tile.tile.setColor(GREEN_TINT);
                return;
            case UNSELECTED:
                tile.tile.setColor(Color.WHITE);
        }
    }

    /**
     * Draws all tiles and their letters, with the current tile on top.
     */
    private void drawTiles(SpriteBatch sb) {
        sb.begin();

        font.setColor(DataManager.tileTextColor);

        // Draw all tiles except the hovered one
        for (Tile tile : tiles) {
            tile.drawShadow(sb);
            drawLetter(sb, tile);
        }

        // Draw all tiles except the hovered one
        for (Tile tile : tiles) {
            if (tile != currentTile) {
                if (tileChain.size() != 0) {
                    updateTileColor(tile);
                } else {
                    tile.tile.setColor(Color.WHITE);
                }
                tile.drawTile(sb);
                drawLetter(sb, tile);
            }
        }

        // Draw hovered tile on top
        if (currentTile != null) {
            if (tileChain.size() == 0 && Manager.isOnDesktop()) {
                currentTile.tile.setColor(RED_TINT);
            } else {
                updateTileColor(currentTile);
            }
            currentTile.drawTile(sb);
            drawLetter(sb, currentTile);
        }

        sb.end();
    }

    /**
     * Draws a letter on a tile, centered.
     * @param sb SpriteBatch to draw with
     * @param tile Tile to draw letter for
     */
    private void drawLetter(SpriteBatch sb, Tile tile) {
        font.getData().setScale(tile.letterScale * scale / 5f);
        tile.layout.setText(font, tile.letter.toUpperCase());

        float x = tile.x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale - tile.layout.width / 2;
        float y = tile.y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale + tile.layout.height / 2;

        Color originalColor = font.getColor().cpy(); // Save original color

        // Draw black outline
        if(DataManager.tileTextOutline) {
            font.setColor(Color.BLACK);
            int outlineThickness = 2;
            for (int dx = -outlineThickness; dx <= outlineThickness; dx++) {
                for (int dy = -outlineThickness; dy <= outlineThickness; dy++) {
                    if (dx != 0 || dy != 0) {
                        font.draw(sb, tile.letter.toUpperCase(), x + dx, y + dy);
                    }
                }
            }
        }

        // Draw main letter
        font.setColor(originalColor);
        font.draw(sb, tile.letter.toUpperCase(), x, y);
    }

    /**
     * Draws the trace lines connecting selected tiles.
     */
    private void drawTraceLines(ShapeRenderer sr, SpriteBatch sb) {
        if (tileChain.size() < 1) {
            return;
        }

        renderTraceToFrameBuffer(sr);
        renderFrameBufferToScreen(sb);
    }

    /**
     * Renders trace lines to the framebuffer for translucent overlay.
     */
    private void renderTraceToFrameBuffer(ShapeRenderer sr) {
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

    private int prevMouseX = 0;
    private int prevMouseY = 0;

    /**
     * Draws a line from the mouse cursor to the first tile in the chain.
     */
    private void drawTraceFromMouseToFirstTile(ShapeRenderer sr) {
        float mouseX = Source.getScreenMouseX();
        float mouseY = Source.getScreenHeight() - Source.getScreenMouseY();
        int tileX = tileChain.get(0).x;
        int tileY = tileChain.get(0).y;
        float firstTileX = convertToScreenX(tileX);
        float firstTileY = convertToScreenY(tileY);
        //Snappy
        /*mouseDirection = (float)((Math.atan2(mouseY - firstTileY, mouseX - firstTileX) + Math.PI / 2) * 180 / Math.PI);
        float distance = (float)Math.sqrt(Math.pow(mouseX - firstTileX, 2) + Math.pow(mouseY - firstTileY, 2));
        int mouseDeltaDirection = directionToSector45((float)((Math.atan2(-deltaY, deltaX) + Math.PI / 2) * (180 / Math.PI)));
        if(distance > 60 * scale && directionToSector45(mouseDirection) == mouseDeltaDirection){
            switch(mouseDeltaDirection){
                case 0 : chain(tileX, tileY + 1); break;
                case 1 : chain(tileX + 1, tileY + 1); break;
                case 2 : chain(tileX + 1, tileY); break;
                case 3 : chain(tileX + 1, tileY - 1); break;
                case 4 : chain(tileX, tileY - 1); break;
                case 5 : chain(tileX - 1, tileY - 1); break;
                case 6 : chain(tileX - 1, tileY); break;
                case 7 : chain(tileX - 1, tileY + 1); break;
            }
        }*/

        sr.rectLine(mouseX, mouseY, firstTileX, firstTileY, TRACE_WIDTH * scale);
        sr.circle(mouseX, mouseY, TRACE_WIDTH / 2 * scale);
    }

    private void chain(int x, int y){
        Tile t;
        if((t = getTile(x, y)) != null){
            t.quickSelect();
        }
    }

    private Tile getTile(int x, int y){
        // Bounds check
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }

        // Formula: column * height + (reversed y index)
        int index = x * height + (height - 1 - y);
        return tiles.get(index);
    }

    /**
     * Converts a direction angle to a discrete value from 0-7.
     * Each value represents a 45-degree sector.
     */
    private int directionToSector45(float direction) {
        // Normalize angle to [0, 360) range
        float normalized = direction;
        if (normalized < 0) {
            normalized += 360;
        }

        // Add 22.5 degrees offset so that 0 is centered at [-22.5, 22.5]
        normalized += 22.5f;

        // Handle wraparound (e.g., 360 becomes 0)
        if (normalized >= 360) {
            normalized -= 360;
        }

        // Divide by 45 to get sector (0-7)
        int sector = (int)(normalized / 45f);

        // Clamp to valid range just in case of floating point edge cases
        return Math.min(sector, 7);
    }

    public static float averageAngles(float angle1, float angle2) {
        // Normalize both angles to [0, 360)
        angle1 = normalizeAngle(angle1);
        angle2 = normalizeAngle(angle2);

        // Check if we need to wrap around 0°
        float diff = Math.abs(angle1 - angle2);

        if (diff > 180) {
            // Wrapping case: e.g., average of 350° and 10° should be 0°, not 180°
            float sum = angle1 + angle2 + 360;
            return (sum / 2) % 360;
        } else {
            // Normal case
            return (angle1 + angle2) / 2;
        }
    }

    private static float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    /**
     * Converts a direction angle to a discrete value from 0-7.
     * Cardinal directions (0,2,4,6) have 60-degree sectors.
     * Diagonal directions (1,3,5,7) have 30-degree sectors.
     *
     * 0 = [-30, 30]      (North) - 60°
     * 1 = [30, 60]       (North-East) - 30°
     * 2 = [60, 120]      (East) - 60°
     * 3 = [120, 150]     (South-East) - 30°
     * 4 = [150, 210]     (South) - 60°
     * 5 = [210, 240]     (South-West) - 30°
     * 6 = [240, 300]     (West) - 60°
     * 7 = [300, 330]     (North-West) - 30°
     */
    private int directionToSector60(float direction) {
        // Normalize angle to [0, 360) range
        float normalized = direction;
        if (normalized < 0) {
            normalized += 360;
        }

        // Define boundaries for each sector
        // Cardinals get 60°, diagonals get 30°
        if (normalized >= 330 || normalized < 30) return 0;  // North (60°)
        if (normalized >= 30 && normalized < 60) return 1;   // NE (30°)
        if (normalized >= 60 && normalized < 120) return 2;  // East (60°)
        if (normalized >= 120 && normalized < 150) return 3; // SE (30°)
        if (normalized >= 150 && normalized < 210) return 4; // South (60°)
        if (normalized >= 210 && normalized < 240) return 5; // SW (30°)
        if (normalized >= 240 && normalized < 300) return 6; // West (60°)
        if (normalized >= 300 && normalized < 330) return 7; // NW (30°)

        return 0; // Fallback (shouldn't reach here)
    }

    /**
     * Draws lines between all tiles in the chain.
     */
    private void drawTracesBetweenTiles(ShapeRenderer sr) {
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

    /**
     * Draws the framebuffer to the screen with transparency.
     */
    private void renderFrameBufferToScreen(SpriteBatch sb) {
        sb.begin();
        sb.setColor(1, 1, 1, 0.5f);
        sb.draw(fb.getColorBufferTexture(), 0, 0, Source.getScreenWidth(), Source.getScreenHeight());
        sb.setColor(1, 1, 1, 1);
        sb.end();
    }

    // ========== Coordinate Conversion ==========

    /**
     * Converts grid X coordinate to screen X coordinate.
     */
    private float convertToScreenX(float gridX) {
        return gridX * (100 * scale) + boardX - width * (50 * scale) + 50 * scale;
    }

    /**
     * Converts grid Y coordinate to screen Y coordinate (inverted).
     */
    private float convertToScreenY(float gridY) {
        return (height - gridY - 1) * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
        if (fb != null) {
            fb.dispose();
        }
    }

    // ========== Inner Class: Tile ==========

    /**
     * Represents a single letter tile on the board.
     * Handles rendering, animation, and interaction logic.
     */
    class Tile {

        // ========== Constants ==========
        private static final float HOVER_SCALE_BONUS = 0.2f;
        private static final float SCALE_SPEED = 2f;
        private static final int HOVER_RADIUS = 45;
        private static final int SELECTION_RADIUS = 45;
        private static final int DESELECTION_RADIUS = 35;

        // ========== Position ==========
        public final int x, y;

        // ========== Visual ==========
        public Sprite tile;
        public String letter;
        public GlyphLayout layout;
        public float letterScale = 1;
        public LetterState state = LetterState.UNSELECTED;

        // ========== State ==========
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

        public void drawTile(SpriteBatch sb){
            updateTilePosition(0, 0);
            tile.draw(sb);
        }

        public void drawShadow(SpriteBatch sb){
            updateTilePosition(20 + (letterScale - 1) * 75, - 20 - (letterScale - 1) * 75);
            sb.setShader(Shader.blurShader);
            Shader.blurShader.setUniformf("u_tint", 0f, 0f, 0f, 0.5f);
            Shader.blurShader.setUniformf("blurSize", .01f);
            tile.setScale(scale * .3f * DataManager.tileScale);
            tile.draw(sb);
            sb.setShader(null);
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

        /**
         * Creates the animation that handles hover, selection, and positioning.
         */
        private void createAnimation() {
            animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
                @Override
                public void run(float delta) {
                    updateHoverState(delta);
                    handleTileSelection();
                    handleTileDeselection();
                }
            });

            tileAnimations.add(animation);
        }

        // ========== Hover Animation ==========

        /**
         * Updates hover state and scale animation.
         */
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

                if (tileChain.size() == 0 && Manager.isOnDesktop()) {
                    state = LetterState.UNSELECTED;
                    tile.setColor(Color.WHITE);
                }
            }
        }

        // ========== Selection Logic ==========

        /**
         * Adds this tile to the chain if hovered and clicked.
         */
        private void handleTileSelection() {
            if (hover && !added && isSelected(SELECTION_RADIUS)) {
                added = true;
                tileChain.add(0, this);
                stringChain = stringChain.concat(letter);
                previousTile = this;
                updateChainState();
            }
        }

        private void quickSelect(){
            if(!added) {
                added = true;
                tileChain.add(0, this);
                stringChain = stringChain.concat(letter);
                previousTile = this;
                updateChainState();
            }
        }

        /**
         * Removes the last tile from the chain if hovering over the second-to-last tile.
         */
        private void handleTileDeselection() {
            if (tileChain.size() > 1 && hover && added &&
                isSelected(DESELECTION_RADIUS) && tileChain.get(1) == this) {

                tileChain.get(0).added = false;
                tileChain.get(0).state = LetterState.UNSELECTED;

                previousTile = this;
                stringChain = stringChain.substring(0, stringChain.length() - 1);
                tileChain.remove(0);

                updateChainState();
            }
        }

        /**
         * Updates the tile's sprite position and scale.
         */
        private void updateTilePosition(float offsetX, float offsetY) {
            float centerX = x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale + offsetX * scale;
            float centerY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale + offsetY * scale;
            float tileScale = scale * 0.185546f;

            tile.setCenterX(Math.round(centerX));
            tile.setCenterY(Math.round(centerY));
            tile.setScale(letterScale * tileScale * 2 * DataManager.tileScale);
        }

        // ========== Hit Detection ==========

        /**
         * Checks if the mouse is hovering over this tile.
         */
        public boolean isHovering() {
            return checkHitCircle(HOVER_RADIUS);
        }

        /**
         * Checks if this tile is selected (clicked and adjacent to previous).
         * @param radius Hit detection radius
         */
        public boolean isSelected(int radius) {
            boolean isAdjacent = previousTile == null ||
                (Math.abs(previousTile.x - x) <= 1 && Math.abs(previousTile.y - y) <= 1);

            return isAdjacent &&
                Gdx.input.isButtonPressed(Input.Buttons.LEFT) &&
                checkHitCircle(radius);
        }

        /**
         * Checks if the mouse is within a circular radius of the tile center.
         * @param radius Radius in unscaled units
         */
        private boolean checkHitCircle(float radius) {
            float tileX = x * (100 * scale) + boardX - Board.this.width * (50 * scale) + 50 * scale;
            float tileY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;

            float deltaX = Source.getScreenMouseX() - tileX;
            float deltaY = Source.getScreenMouseY() - tileY;

            return Math.sqrt(deltaX * deltaX + deltaY * deltaY) < radius * scale;
        }
    }
}
