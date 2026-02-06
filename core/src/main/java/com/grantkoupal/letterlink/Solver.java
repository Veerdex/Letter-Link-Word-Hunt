package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles word searching, validation, and scoring for a Boggle-style letter board.
 * Uses a 4D dictionary structure for fast prefix/word lookup and recursive path finding.
 */
public class Solver {

    // ========== Constants ==========

    private static int boardValue = 0;
    private static final int ALPHABET_SIZE = 26;
    private static final int ASCII_OFFSET = 97; // 'a' in ASCII
    private static final String DICTIONARY_FILE = "Words.txt";

    /**
     * Letter frequency weights for random board generation.
     * Based on English letter frequency, higher = more common.
     * Index 0 = 'a', 1 = 'b', etc.
     */
    private static final int[] LETTER_WEIGHTS = {
        82,  // a
        15,  // b
        28,  // c
        43,  // d
        127, // e
        22,  // f
        20,  // g
        61,  // h
        70,  // i
        15,  // j
        8,   // k
        40,  // l
        24,  // m
        67,  // n
        75,  // o
        19,  // p
        9,   // q
        60,  // r
        63,  // s
        91,  // t
        28,  // u
        10,  // v
        24,  // w
        15,  // x
        20,  // y
        7    // z
    };

    // ========== Dictionary Structure ==========

    /**
     * 4D word storage indexed by first three letters [a-z][a-z][a-z].
     * Provides O(1) prefix lookup for fast word validation during search.
     */
    private static List<List<List<List<String>>>> dictionary = new ArrayList<>();

    // ========== Board State ==========

    private static List<List<Character>> board = new ArrayList<>();
    private static int boardWidth = 0;
    private static int boardHeight = 0;

    // ========== Search Results ==========

    private static List<String> treasureWords = new ArrayList<>();
    private static List<List<Integer>> wordPaths = new ArrayList<>();
    private static int[] wordLengthCounts = new int[26];
    private static int totalPoints = 0;

    // ========== Board Configuration ==========

    /**
     * Sets the board layout from a string of letters.
     * Validates that the board is rectangular.
     *
     * @param width Board width
     * @param height Board height
     * @param letters String of letters (length must equal width * height)
     * @throws IllegalArgumentException if board is not rectangular
     */
    public static void setBoard(int width, int height, String letters) {
        // Parse letters into 2D structure
        List<List<Character>> newBoard = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            newBoard.add(new ArrayList<>());
            for (int y = 0; y < height; y++) {
                newBoard.get(x).add(letters.charAt(x * width + y));
            }
        }

        // Validate rectangular shape
        validateBoardShape(newBoard);

        // Copy into internal board representation
        board.clear();
        for (int x = 0; x < width; x++) {
            board.add(new ArrayList<>());
            for (int y = 0; y < height; y++) {
                board.get(x).add(newBoard.get(x).get(y));
            }
        }

        boardWidth = width;
        boardHeight = height;
    }

    /**
     * Validates that all rows have the same width.
     */
    private static void validateBoardShape(List<List<Character>> boardToValidate) {
        int expectedWidth = boardToValidate.get(0).size();
        for (List<Character> row : boardToValidate) {
            if (row.size() != expectedWidth) {
                throw new IllegalArgumentException("Board must be rectangular");
            }
        }
    }

    // ========== Dictionary Management ==========

    /**
     * Initializes or resets the dictionary and clears previous search results.
     * Loads words from the dictionary file and organizes them for fast lookup.
     */
    public static void resetWords() {
        initializeDictionary();
        loadDictionaryFromFile();
        clearSearchResults();
    }

    /**
     * Initializes the 4D dictionary structure if not already created.
     */
    private static void initializeDictionary() {
        if (dictionary.isEmpty()) {
            for (int x = 0; x < ALPHABET_SIZE; x++) {
                dictionary.add(new ArrayList<>());
                for (int y = 0; y < ALPHABET_SIZE; y++) {
                    dictionary.get(x).add(new ArrayList<>());
                    for (int z = 0; z < ALPHABET_SIZE; z++) {
                        dictionary.get(x).get(y).add(new ArrayList<>());
                    }
                }
            }
        } else {
            clearDictionary();
        }
    }

    /**
     * Clears all existing dictionary entries.
     */
    private static void clearDictionary() {
        for (int x = 0; x < ALPHABET_SIZE; x++) {
            for (int y = 0; y < ALPHABET_SIZE; y++) {
                for (int z = 0; z < ALPHABET_SIZE; z++) {
                    dictionary.get(x).get(y).get(z).clear();
                }
            }
        }
    }

    /**
     * Loads dictionary words from file and indexes them by first 3 letters.
     * Only words with 3+ letters are stored.
     */
    private static void loadDictionaryFromFile() {
        String fileContent = Gdx.files.internal(DICTIONARY_FILE).readString();
        int start = 0;
        int end;

        // Process each line
        while ((end = fileContent.indexOf('\n', start)) != -1) {
            String word = fileContent.substring(start, end).trim();
            addWordToDictionary(word);
            start = end + 1;
        }

        // Handle last line if no newline at end of file
        if (start < fileContent.length()) {
            String word = fileContent.substring(start).trim();
            addWordToDictionary(word);
        }
    }

    /**
     * Adds a word to the dictionary if it's 3+ letters.
     */
    private static void addWordToDictionary(String word) {
        if (word.length() >= 3) {
            try {
                int firstLetter = word.charAt(0) - ASCII_OFFSET;
                int secondLetter = word.charAt(1) - ASCII_OFFSET;
                int thirdLetter = word.charAt(2) - ASCII_OFFSET;

                dictionary.get(firstLetter)
                    .get(secondLetter)
                    .get(thirdLetter)
                    .add(word);
            } catch (Exception e) {
                // Ignore words with invalid characters
            }
        }
    }

    /**
     * Clears all previous search results.
     */
    private static void clearSearchResults() {
        totalPoints = 0;
        treasureWords.clear();
        wordPaths.clear();

        for (int i = 0; i < wordLengthCounts.length; i++) {
            wordLengthCounts[i] = 0;
        }
    }

    // ========== Word Search ==========

    /**
     * Recursively searches the board for valid words starting from a position.
     * Uses backtracking to explore all possible paths without revisiting cells.
     *
     * @param x Starting x coordinate
     * @param y Starting y coordinate
     * @param currentWord Word built so far
     * @param visited Tracks which cells have been used in current path
     * @param path Current coordinate path (x,y pairs)
     * @return List of valid words found from this position
     */
    public static List<String> checkWords(int x, int y, String currentWord, int[][] visited, List<Integer> path) {
        List<String> foundWords = new ArrayList<>();

        // Try to add current cell to the path
        try {
            currentWord += board.get(x).get(y);
            path = new ArrayList<>(path);
            path.add(x);
            path.add(y);
        } catch (Exception e) {
            return foundWords; // Out of bounds
        }

        // Early termination: if 3+ chars and not a valid prefix, stop searching
        if (currentWord.length() > 2 && !isValidPrefix(currentWord)) {
            return foundWords;
        }

        // Check if current word is in dictionary
        if (currentWord.length() > 2) {
            addIfValidWord(currentWord, path, foundWords);
        }

        // Mark current cell as visited
        int[][] newVisited = copyVisitedGrid(visited);
        newVisited[x][y] = 1;

        // Explore all 8 neighboring cells
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip current cell

                int newX = x + dx;
                int newY = y + dy;

                try {
                    if (newVisited[newX][newY] == 0) {
                        List<String> subWords = checkWords(newX, newY, currentWord, newVisited, path);
                        foundWords.addAll(subWords);
                    }
                } catch (Exception e) {
                    // Out of bounds, skip
                }
            }
        }

        return foundWords;
    }

    /**
     * Copies the visited grid for backtracking.
     */
    private static int[][] copyVisitedGrid(int[][] original) {
        int[][] copy = new int[boardWidth][boardHeight];
        for (int i = 0; i < boardWidth * boardHeight; i++) {
            copy[i % boardWidth][i / boardWidth] = original[i % boardWidth][i / boardWidth];
        }
        return copy;
    }

    /**
     * Checks if the current word is a valid prefix in the dictionary.
     * A prefix is valid if any dictionary word starts with it.
     *
     * @param prefix Word prefix to check
     * @return true if at least one dictionary word starts with this prefix
     */
    private static boolean isValidPrefix(String prefix) {
        List<String> candidates = getDictionaryEntry(prefix);

        for (String word : candidates) {
            if (word.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the word to results if it exists in the dictionary.
     * Removes the word from dictionary to prevent duplicates.
     *
     * @param word Word to check
     * @param path Coordinate path used to form the word
     * @param foundWords List to add word to if valid
     */
    private static void addIfValidWord(String word, List<Integer> path, List<String> foundWords) {
        List<String> candidates = getDictionaryEntry(word);

        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i).equals(word)) {
                foundWords.add(word);
                treasureWords.add(word);
                wordPaths.add(new ArrayList<>(path));
                candidates.remove(i);
                break;
            }
        }
    }

    /**
     * Gets the dictionary entry for words with this prefix.
     */
    private static List<String> getDictionaryEntry(String word) {
        int firstLetter = word.charAt(0) - ASCII_OFFSET;
        int secondLetter = word.charAt(1) - ASCII_OFFSET;
        int thirdLetter = word.charAt(2) - ASCII_OFFSET;

        return dictionary.get(firstLetter)
            .get(secondLetter)
            .get(thirdLetter);
    }

    // ========== Scoring ==========

    /**
     * Calculates the point value for a word based on its length.
     *
     * @param word Word to score
     * @return Point value (100 for 3 letters, scaling up to 10,600 for 25 letters)
     */
    public static int getWordValue(String word) {
        int length = word.length();

        if (length < 3 || length > 25) {
            return 0;
        }

        // Base value: 100 points for 3-letter words
        // Increases by 400 for length 4, then varying amounts
        switch (length) {
            case 3:  return 100;
            case 4:  return 400;
            case 5:  return 800;
            case 6:  return 1400;
            case 7:  return 1800;
            case 8:  return 2200;
            case 9:  return 2600;
            case 10: return 3000;
            case 11: return 3600;
            case 12: return 4000;
            case 13: return 4600;
            case 14: return 5000;
            case 15: return 5600;
            case 16: return 6000;
            case 17: return 6600;
            case 18: return 7000;
            case 19: return 7600;
            case 20: return 8000;
            case 21: return 8600;
            case 22: return 9000;
            case 23: return 9600;
            case 24: return 10000;
            case 25: return 10600;
            default: return 0;
        }
    }

    /**
     * Calculates total score from all found words and updates length statistics.
     *
     * @return Total points from all found words
     */
    public static int calculatePoints() {
        totalPoints = 0;

        for (String word : treasureWords) {
            int length = word.length();
            int value = getWordValue(word);

            totalPoints += value;

            if (length < wordLengthCounts.length) {
                wordLengthCounts[length]++;
            }
        }

        return totalPoints;
    }

    // ========== Board Generation ==========

    /**
     * Generates a random board using weighted letter frequencies.
     * More common letters (like 'e', 'a', 't') appear more frequently.
     *
     * @param width Board width
     * @param height Board height
     * @return String of random letters
     */
    public static String generateBoard(int width, int height) {
        int totalWeight = calculateTotalWeight();
        StringBuilder board = new StringBuilder();

        for (int i = 0; i < width * height; i++) {
            board.append(selectRandomLetter(totalWeight));
        }

        return board.toString();
    }

    /**
     * Calculates the sum of all letter weights.
     */
    private static int calculateTotalWeight() {
        int total = 0;
        for (int weight : LETTER_WEIGHTS) {
            total += weight;
        }
        return total;
    }

    /**
     * Selects a random letter based on weighted probabilities.
     *
     * @param totalWeight Sum of all letter weights
     * @return Random letter ('a'-'z')
     */
    private static char selectRandomLetter(int totalWeight) {
        int random = (int) (Math.random() * totalWeight);
        int cumulative = 0;

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            cumulative += LETTER_WEIGHTS[i];
            if (random < cumulative) {
                return (char) (i + ASCII_OFFSET);
            }
        }

        return '!'; // Should never happen
    }

    // ========== Getters ==========

    public static List<List<Character>> getBoard() {
        return board;
    }

    public static List<String> getTreasureWords() {
        return treasureWords;
    }

    /**
     * Gets the coordinate path for a specific found word.
     *
     * @param index Index of the word in treasureWords
     * @return List of coordinates (x,y pairs) forming the path
     */
    public static List<Integer> getWordPath(int index) {
        return wordPaths.get(index);
    }

    /**
     * Gets the total number of words found.
     */
    public static int getNumWords() {
        return wordPaths.size();
    }

    public static int getBoardValue(){
        return boardValue;
    }

    public static void setBoardValue(int value){
        boardValue = value;
    }

    public static int getBoardWidth(){
        return boardWidth;
    }

    public static int getBoardHeight(){
        return boardHeight;
    }

    private static int points;

    public static float calculateRank(List<String> foundWords){
        points = 0;
        for(int i = 0; i < foundWords.size(); i++){
            points += getWordValue(foundWords.get(i));
        }

        return adjust(50, 25, 0);
    }

    private static float adjust(float rank, float change, int iteration){
        int tempPoints = 0;
        for(int i = 0; i < treasureWords.size(); i++){
            if(WordDifficultyRanker.wordDifficulty(treasureWords.get(i)) < rank){
                tempPoints += getWordValue(treasureWords.get(i));
            }
        }

        if(iteration == 100){
            return rank;
        } else if(points > tempPoints){
            return adjust(rank + change, change / 2, iteration + 1);
        } else {
            return adjust(rank - change, change / 2, iteration + 1);
        }
    }
}
