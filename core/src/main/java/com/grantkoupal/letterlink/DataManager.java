package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;

/**
 * Manages and caches game textures and font assets.
 * Provides centralized access to commonly used visual resources.
 */
public class DataManager {

    // ========== Asset Paths ==========
    private static final String TILE_PATH = "Boggle Board/Pieces/";
    private static final String BOARD_PATH = "Boggle Board/Boards/";
    private static final String BACKGROUND_PATH = "Backgrounds/";
    private static final String BOTTOM_TEXT_PATH = "Bottom Text Backgrounds/";

    // ========== Textures ==========
    protected static Texture tileTexture;
    protected static Texture boardTexture;
    protected static Texture backgroundTexture;
    protected static Texture bottomTextTexture;

    // ========== Font ==========
    protected static String fontName;

    // ========== Asset Loading ==========

    /**
     * Loads and initializes all game assets from the specified file names.
     * This should be called once at the start of each game session.
     *
     * @param tileName Name of the tile texture file (e.g., "Wood Piece.png")
     * @param boardName Name of the board texture file (e.g., "Bronco.png")
     * @param backgroundName Name of the background texture file (e.g., "Waves.png")
     * @param fontName Name of the font to use (e.g., "Coiny")
     * @param bottomTextBackgroundName Name of the bottom text background texture (e.g., "Grey.png")
     */
    public static void setAssets(String tileName, String boardName, String backgroundName,
                                 String fontName, String bottomTextBackgroundName) {
        tileTexture = new Texture(TILE_PATH + tileName);
        boardTexture = new Texture(BOARD_PATH + boardName);
        backgroundTexture = new Texture(BACKGROUND_PATH + backgroundName);
        bottomTextTexture = new Texture(BOTTOM_TEXT_PATH + bottomTextBackgroundName);
        DataManager.fontName = fontName;
    }

    // ========== Resource Cleanup ==========

    /**
     * Disposes of all loaded textures to free memory.
     * This should be called when the game session ends or when switching themes.
     */
    public static void dispose() {
        if (tileTexture != null) {
            tileTexture.dispose();
        }
        if (boardTexture != null) {
            boardTexture.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (bottomTextTexture != null) {
            bottomTextTexture.dispose();
        }
    }
}
