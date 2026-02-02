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
import com.badlogic.gdx.math.Vector2;
import com.grantkoupal.letterlink.quantum.*;
import com.grantkoupal.letterlink.quantum.Manager;
import com.grantkoupal.letterlink.quantum.Page;

import java.util.ArrayList;
import java.util.List;

import static com.grantkoupal.letterlink.HintTable.sortByLengthDescThenAlphabetically;

public class Board extends Agent {

    private static final Color RED_TINT = new Color(1, .8f, .8f, 1f);
    private static final Color YELLOW_TINT = new Color(1, 1, .7f, 1f);
    private static final Color GREEN_TINT = new Color(.7f, 1, .7f, 1f);

    // Constants
    private static final int MIN_BOARD_SIZE = 4;
    private static final float TRACE_WIDTH = 20;

    // Textures and Graphics
    private Texture tileTexture;
    private Texture boardTexture;
    private Texture backgroundTexture;
    private Graphic boardBackground;
    private BitmapFont font;

    // Board Structure
    private int width;
    private int height;
    private float boardBackgroundScale = 1;
    private int boardValue = 0;
    private int totalPoints = 0;
    private List<List<Character>> board = new ArrayList<>();
    private List<Tile> tiles = new ArrayList<>();
    private List<String> wordsInBoard = new ArrayList<>();
    private List<Boolean> wordsFound = new ArrayList<>();
    private List<String> listOfWordsFound = new ArrayList<>();

    // Board State
    private float scale = 1;
    private float boardX = 0;
    private float boardY = 0;
    public enum LetterState {UNSELECTED, INVALID, VALID, COPY};

    // Tile Selection
    public Tile currentTile = null;
    public Tile previousTile = null;
    private List<Tile> tileChain = new ArrayList<>();
    private String stringChain = "";

    // Drawing
    private Vector2 mouseVector = new Vector2();
    private Color traceColor = new Color(1, 0, 0, .5f);
    private FrameBuffer fb;
    private List<Animation> tileAnimations = new ArrayList<>();

    // ========== Constructor ==========

    public Board(int width, int height, int power) {
        initializeFont();
        loadTextures();
        setDimensions(width, height);
        generateBoard(power);
        initializeBoard();
        generatePieces();
    }

    // ========== Initialization Methods ==========

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 256);
    }

    private void loadTextures() {
        tileTexture = DataManager.tileTexture;
        boardTexture = DataManager.boardTexture;
        backgroundTexture = DataManager.backgroundTexture;
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        boardBackground = new Graphic(boardTexture);
    }

    private void setDimensions(int width, int height) {

        this.width = Math.max(width, MIN_BOARD_SIZE);
        this.height = Math.max(height, MIN_BOARD_SIZE);

        fb = new FrameBuffer(Pixmap.Format.RGBA8888, Source.getScreenWidth(), Source.getScreenHeight(), false);
    }

    private void initializeBoard() {
        board = Solver.getBoard();
    }

    private void generateBoard(int power) {
        Solver.setBoard(width, height, generateBasedOffPower(power));
        Solver.resetWords();
        listOfWordsFound.clear();
        wordsFound.clear();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Solver.checkWords(x, y, "", new int[width][height], new ArrayList<>());
            }
        }

        int points = Solver.calculatePoints();

        if(points < 100000){
            generateBoard(power);
            return;
        }

        boardValue = Solver.calculatePoints();

        wordsInBoard = Solver.getTreasureWords();

        sortByLengthDescThenAlphabetically(wordsInBoard);

        for(int i = 0; i < wordsInBoard.size(); i++){
            wordsFound.add(false);
        }
    }

    private String generateBasedOffPower(int power){
        switch(power){
            case 0 : return ImprovedBoardGenerator.generateFastLevel3(width, height);
            case 1 : return ImprovedBoardGenerator.generateFastLevel2_5(width, height);
            case 2 : return ImprovedBoardGenerator.generateFastLevel2(width, height);
            case 3 : return ImprovedBoardGenerator.generateFastLevel1_5(width, height);
            case 4 : return ImprovedBoardGenerator.generateFastLevel1(width, height);
            case 5 : return ImprovedBoardGenerator.generateOptimizedBoard(width, height);
            case 6 : return ImprovedBoardGenerator.generateClusteredBoard(width, height);
            case 7 : return ImprovedBoardGenerator.generateOptimalBoard(width, height);
            case 8 : return ImprovedBoardGenerator.generateBestBoard(width, height);
            default : return ImprovedBoardGenerator.generateHybridBoard(width, height);
        }
    }

    private void generatePieces() {
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                tiles.add(new Tile(x, y));
            }
        }
    }

    // ========== Public Methods ==========

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

    public void addAnimations(Page p) {
        for (Animation a : tileAnimations) {
            p.addAnimation(a);
        }
        p.addAnimation(createWordCheckAnimation());
    }

    public List<List<Character>> getBoard() {
        return board;
    }

    // ========== Setters ==========

    public void setScale(float f) {
        scale = 12f / Math.max(width, height) * f;
        boardBackgroundScale = f;
    }

    public void setBoardX(float x) {
        boardX = x;
    }

    public void setBoardY(float y) {
        boardY = y;
    }

    public void setMouseVector(float x, float y) {
        mouseVector.x = x;
        mouseVector.y = y;
    }

    public void updateFrameBuffer(FrameBuffer fb) {
        if (this.fb != null) {
            this.fb.dispose();
        }
        this.fb = fb;
    }

    public void setChainState(LetterState newState){
        for(int i = 0; i < tileChain.size(); i++){
            tileChain.get(i).state = newState;
        }
    }

    public void updateChainState(){
        switch(getState()){
            case 0 : setChainState(LetterState.COPY); break;
            case 1 : setChainState(LetterState.VALID); break;
            case 2 : setChainState(LetterState.INVALID); break;
        }
    }

    private int getState(){
        int index = wordsInBoard.indexOf(buildWordFromChain());
        if (index != -1) {
            if(wordsFound.get(index)){
                return 0; // Word already found
            }
            return 1; // New Word
        }
        return 2; // Invalid Word
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

    public List<String> getWordsInBoard(){return wordsInBoard;}

    public List<Boolean> getWordsFound(){return wordsFound;}

    public int getBoardValue(){
        return boardValue;
    }

    public List<String> getListOfWordsFound(){return listOfWordsFound;}

    public String getStringChain(){
        return stringChain;
    }

    public int getTotalPoints(){return totalPoints;}

    // ========== Animation Creation ==========

    private Animation createWordCheckAnimation() {
        return new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                handleWordSubmission();
                updateBackgroundPosition();
            }
        });
    }

    private void handleWordSubmission() {
        if (tileChain.size() > 0 && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {

            check(buildWordFromChain());

            for(Tile t : tileChain){
                t.tile.setColor(Color.WHITE);
                t.state = LetterState.UNSELECTED;
            }
            if(!Manager.isOnDesktop() && currentTile != null){
                currentTile.hover = false;
                currentTile = null;
            }

            resetTileChain();
        }
    }

    private String buildWordFromChain() {
        StringBuilder sb = new StringBuilder();
        for (int i = tileChain.size() - 1; i >= 0; i--) {
            sb.append(tileChain.get(i).letter);
        }
        return sb.toString();
    }

    private void resetTileChain() {
        tileChain.clear();
        for (Tile t : tiles) {
            t.added = false;
            t.state = LetterState.UNSELECTED;
        }
        stringChain = "";
        previousTile = null;
    }

    private void updateBackgroundPosition() {
        boardBackground.setCenter(Source.getScreenWidth() / 2, Source.getScreenHeight() / 2);
    }

    // ========== Drawing Methods ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        setBoardX(Source.getScreenWidth() / 2);
        setBoardY(Source.getScreenHeight() / 2);
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        setScale((float)Math.min(xScale, yScale));
        setMouseVector(Source.getScreenMouseX(), Source.getScreenHeight() - Source.getScreenMouseY());

        drawBackground(sb);
        drawBoardBackground(sb);
        drawTiles(sb);
        drawTraceLines(sr, sb);
    }

    private void drawBackground(SpriteBatch sb){
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

    private void drawBoardBackground(SpriteBatch sb) {
        sb.setProjectionMatrix(Source.camera.combined);
        sb.begin();
        boardBackground.setScale(2.25f * boardBackground.getWidth() / 1720 * boardBackgroundScale);
        boardBackground.draw(sb);
        sb.end();
    }

    private void updateColor(Tile t){
        switch(t.state){
            case COPY : t.tile.setColor(YELLOW_TINT); break;
            case INVALID : t.tile.setColor(RED_TINT); break;
            case VALID : t.tile.setColor(GREEN_TINT); break;
            case UNSELECTED : t.tile.setColor(Color.WHITE); break;
        }
    }

    private void drawTiles(SpriteBatch sb) {
        sb.begin();

        // Draw all tiles except current
        for (Tile t : tiles) {
            if (t != currentTile) {
                if(tileChain.size() != 0){
                    updateColor(t);
                }
                t.tile.draw(sb);
                drawLetter(sb, t);
            }
        }

        // Draw current tile on top
        if (currentTile != null) {
            if(tileChain.size() == 0 && Manager.isOnDesktop()){
                currentTile.tile.setColor(RED_TINT);
            } else {
                updateColor(currentTile);
            }
            currentTile.tile.draw(sb);
            drawLetter(sb, currentTile);
        }

        sb.end();
    }

    private void drawLetter(SpriteBatch sb, Tile t) {
        font.getData().setScale(t.letterScale * scale / 5f);
        t.layout.setText(font, t.letter);

        float x = t.x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale - t.layout.width / 2;
        float y = t.y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale + t.layout.height / 2;

        font.draw(sb, t.letter, x, y);
    }

    private void drawTraceLines(ShapeRenderer sr, SpriteBatch sb) {
        if (tileChain.size() < 1) {
            return;
        }

        renderTraceToFrameBuffer(sr);
        renderFrameBufferToScreen(sb);
    }

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

    private void drawTraceFromMouseToFirstTile(ShapeRenderer sr) {
        float mouseX = Source.getScreenMouseX();
        float mouseY = Source.getScreenHeight() - Source.getScreenMouseY();
        float firstTileX = convertToX(tileChain.get(0).x);
        float firstTileY = convertToY(tileChain.get(0).y);

        sr.rectLine(mouseX, mouseY, firstTileX, firstTileY, TRACE_WIDTH * scale);
        sr.circle(mouseX, mouseY, TRACE_WIDTH / 2 * scale);
    }

    private void drawTracesBetweenTiles(ShapeRenderer sr) {
        for (int i = 0; i < tileChain.size() - 1; i++) {
            float x1 = convertToX(tileChain.get(i).x);
            float y1 = convertToY(tileChain.get(i).y);
            float x2 = convertToX(tileChain.get(i + 1).x);
            float y2 = convertToY(tileChain.get(i + 1).y);

            sr.rectLine(x1, y1, x2, y2, TRACE_WIDTH * scale);
            sr.circle(x1, y1, TRACE_WIDTH / 2 * scale);
        }

        // Draw circle at last tile
        float lastX = convertToX(tileChain.get(tileChain.size() - 1).x);
        float lastY = convertToY(tileChain.get(tileChain.size() - 1).y);
        sr.circle(lastX, lastY, TRACE_WIDTH / 2 * scale);
    }

    private void renderFrameBufferToScreen(SpriteBatch sb) {
        sb.begin();
        sb.setColor(1, 1, 1, 0.5f);
        sb.draw(fb.getColorBufferTexture(), 0, 0, Source.getScreenWidth(), Source.getScreenHeight());
        sb.setColor(1, 1, 1, 1);
        sb.end();
    }

    // ========== Coordinate Conversion ==========

    private float convertToX(float x) {
        return x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale;
    }

    private float convertToY(float y) {
        return (height - y - 1) * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
        if (fb != null) {
            fb.dispose();
        }
    }

    private Board me() {
        return this;
    }

    // ========== Inner Class: Tile ==========

    class Tile {
        // Tile Properties
        public final int x, y;
        public Sprite tile;
        public String letter;
        public GlyphLayout layout;
        public float letterScale = 1;
        public LetterState state = LetterState.UNSELECTED;

        // Animation State
        public Animation t;
        private boolean hover = false;
        private boolean added = false;

        // Constructor
        public Tile(int x, int y) {
            this.x = x;
            this.y = y;

            initializeTileSprite();
            initializeLetter();
            createAnimation();
        }

        private void initializeTileSprite() {
            tile = new Sprite(tileTexture);
            tile.setCenterX(x * 100 + 200 - width * (50 * scale));
        }

        private void initializeLetter() {
            letter = "" + board.get(x).get(y);
            layout = new GlyphLayout();
            layout.setText(font, letter);
        }

        private void createAnimation() {
            t = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
                @Override
                public void run(float delta) {
                    updateHoverState(delta);
                    handleTileSelection();
                    handleTileDeselection();
                    updateTilePosition();
                }
            });

            tileAnimations.add(t);
        }

        // Animation Update Methods
        private void updateHoverState(float delta) {
            if (isHover()) {
                if(Manager.isOnDesktop()) {
                    increaseScale(delta);
                }
                setHoverActive();
            } else {
                if(Manager.isOnDesktop()) {
                    decreaseScale(delta);
                }
                setHoverInactive();
            }
        }

        private void increaseScale(float delta) {
            if (letterScale < .2 + 1) {
                letterScale += delta * 2;
            } else {
                letterScale = .2f + 1;
            }
        }

        private void decreaseScale(float delta) {
            if (letterScale > 1) {
                letterScale -= delta * 2;
            } else {
                letterScale = 1;
            }
        }

        private void setHoverActive() {
            if (!hover) {
                hover = true;
                currentTile = me();
            }
        }

        private void setHoverInactive() {
            if (hover) {
                hover = false;
                currentTile = null;
                if(tileChain.size() == 0 && Manager.isOnDesktop()) {
                    state = LetterState.UNSELECTED;
                    tile.setColor(Color.WHITE);
                }
            }
        }

        private void handleTileSelection() {
            if (hover && !added && isSelected(45)) {
                added = true;
                tileChain.add(0, me());
                stringChain = stringChain.concat(letter);
                previousTile = me();
                updateChainState();
            }
        }

        private void handleTileDeselection() {
            if (tileChain.size() > 1 && hover && added && isSelected(35) && tileChain.get(1) == me()) {
                tileChain.get(0).added = false;
                previousTile = me();
                tileChain.get(0).state = LetterState.UNSELECTED;
                stringChain = stringChain.substring(0, stringChain.length() - 1);
                tileChain.remove(0);
                updateChainState();
            }
        }

        private void updateTilePosition() {
            float centerY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;
            float centerX = x * (100 * scale) + boardX - width * (50 * scale) + 50 * scale;
            float tileScale = scale / (tile.getHeight() / 100);

            tile.setCenterY(centerY);
            tile.setCenterX(centerX);
            tile.setScale(letterScale * tileScale);
        }

        // Hit Detection Methods
        public boolean isHover() {
            return checkHitCircle(45);
        }

        public boolean isSelected(int radius) {
            return ((previousTile == null || (Math.abs(previousTile.x - x) <= 1 && Math.abs(previousTile.y - y) <= 1)) &&
                Gdx.input.isButtonPressed(Input.Buttons.LEFT) &&
                checkHitCircle(radius));
        }

        private boolean checkHitCircle(float radius) {
            float tileX = x * (100 * scale) + boardX - Board.this.width * (50 * scale) + 50 * scale;
            float tileY = y * (100 * scale) + boardY - height * (50 * scale) + 50 * scale;

            float difX = Source.getScreenMouseX() - tileX;
            float difY = Source.getScreenMouseY() - tileY;

            return Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2)) < radius * scale;
        }

        private Tile me() {
            return this;
        }
    }
}
