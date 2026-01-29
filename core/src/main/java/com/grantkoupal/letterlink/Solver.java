package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles word searching, scoring, and path tracking for a Boggle-style board.
 */
public class Solver {

    /** Total score accumulated from found words */
    private static int points = 0;

    private static int[] letterPower = new int[]{
        82, // a
        15, // b
        28, // c
        43, // d
        127, // e
        22, // f
        20, // g
        61, // h
        70, // i
        15, // j
        8, // k
        40, // l
        24, // m
        67, // n
        75, // o
        19, // p
        9, // q
        60, // r
        63, // s
        91, // t
        28, // u
        10, // v
        24, // w
        15, // x
        20, // y
        7, // z
    };

    /**
     * 4D word storage indexed by first three letters [a-z][a-z][a-z]
     * Used for fast prefix and word lookup.
     */
    private static List<List<List<List<String>>>> words = new ArrayList<List<List<List<String>>>>();

    /** Stores all valid words found on the board */
    private static List<String> treasureWords = new ArrayList<String>();

    /** Tracks how many words were found of each length */
    private static int[] wordLengthAmount = new int[26];

    /**
     * Stores coordinate paths for each discovered word.
     * Each list contains x,y pairs in traversal order.
     */
    private static List<List<Integer>> wordPaths = new ArrayList<List<Integer>>();

    /** Represents the game board as a 2D list of letter indices */
    private static List<List<Character>> board = new ArrayList<List<Character>>();

    /** Width of the board */
    private static int boardWidth = 0;

    /** Height of the board */
    private static int boardHeight = 0;

    /**
     * Sets the board layout and validates rectangular shape.
     *
     * @param newBoard incoming board layout
     */
    public static void setBoard(List<List<Character>> newBoard){
        int width = newBoard.get(0).size();
        int height = newBoard.size();

        /** Validates that the board is rectangular */
        for(int i = 0; i < height; i++){
            if(newBoard.get(i).size() != width){
                throw new IllegalArgumentException();
            }
        }

        /** Clears the existing board */
        board.clear();

        /** Copies values into internal board representation */
        for(int x = 0; x < width; x++){
            board.add(new ArrayList<Character>());
            for(int y = 0; y < height; y++){
                board.get(x).add(newBoard.get(x).get(y));
            }
        }

        boardWidth = width;
        boardHeight = height;
    }

    /**
     * Initializes or resets the dictionary and clears previous results.
     */
    public static void resetWords() {

        /** Initializes the 4D dictionary structure if empty */
        if (words.size() == 0) {
            for (int x = 0; x < 26; x++) {
                words.add(new ArrayList<List<List<String>>>());
                for (int y = 0; y < 26; y++) {
                    words.get(x).add(new ArrayList<List<String>>());
                    for (int z = 0; z < 26; z++) {
                        words.get(x).get(y).add(new ArrayList<String>());
                    }
                }
            }
        } else {

            /** Clears all existing dictionary entries */
            for (int x = 0; x < 26; x++) {
                for (int y = 0; y < 26; y++) {
                    for (int z = 0; z < 26; z++) {
                        words.get(x).get(y).get(z).clear();
                    }
                }
            }
        }

        /** Loads dictionary words from file */
        String fileContent = Gdx.files.internal("Words.txt").readString();
        int start = 0;
        int end;

        while ((end = fileContent.indexOf('\n', start)) != -1) {
            String line = fileContent.substring(start, end).trim();

            if (line.length() >= 3) {
                try {
                    words.get((int) line.charAt(0) - 97)
                        .get((int) line.charAt(1) - 97)
                        .get((int) line.charAt(2) - 97)
                        .add(line);
                } catch (Exception ie) {}
            }

            start = end + 1;
        }

        // Handle last line if no newline at end of file
        if (start < fileContent.length()) {
            String line = fileContent.substring(start).trim();
            if (line.length() >= 3) {
                try {
                    words.get((int) line.charAt(0) - 97)
                        .get((int) line.charAt(1) - 97)
                        .get((int) line.charAt(2) - 97)
                        .add(line);
                } catch (Exception ie) {
                }
            }
        }

        points = 0;

        /* Clears previously found words */
        treasureWords.clear();
        wordPaths.clear();

        for(int i = 0; i < wordLengthAmount.length; i++){
            wordLengthAmount[i] = 0;
        }
    }

    /**
     * Calculates total score based on word lengths.
     **/
    private static void calculatePoints(){
        for(String a : treasureWords){
            switch(a.length()){
                case 3 : points += 100; wordLengthAmount[3]++; break;
                case 4 : points += 400; wordLengthAmount[4]++; break;
                case 5 : points += 800; wordLengthAmount[5]++; break;
                case 6 : points += 1400; wordLengthAmount[6]++; break;
                case 7 : points += 1800; wordLengthAmount[7]++; break;
                case 8 : points += 2200; wordLengthAmount[8]++; break;
                case 9 : points += 2600; wordLengthAmount[9]++; break;
                case 10 : points += 3000; wordLengthAmount[10]++; break;
                case 11 : points += 3600; wordLengthAmount[11]++; break;
                case 12 : points += 4000; wordLengthAmount[12]++; break;
                case 13 : points += 4600; wordLengthAmount[13]++; break;
                case 14 : points += 5000; wordLengthAmount[14]++; break;
                case 15 : points += 5600; wordLengthAmount[15]++; break;
                case 16 : points += 6000; wordLengthAmount[16]++; break;
                case 17 : points += 6600; wordLengthAmount[17]++; break;
                case 18 : points += 7000; wordLengthAmount[18]++; break;
                case 19 : points += 7600; wordLengthAmount[19]++; break;
                case 20 : points += 8000; wordLengthAmount[20]++; break;
                case 21 : points += 8600; wordLengthAmount[21]++; break;
                case 22 : points += 9000; wordLengthAmount[22]++; break;
                case 23 : points += 9600; wordLengthAmount[23]++; break;
                case 24 : points += 10000; wordLengthAmount[24]++; break;
                case 25 : points += 10600; wordLengthAmount[25]++;
            }
        }
    }

    /**
     * Recursively searches the board for valid words.
     *
     * @param x starting x position
     * @param y starting y position
     * @param currentWord word built so far
     * @param s visited space tracking
     * @param c current coordinate path
     * @return list of valid words found
     */
    public static List<String> checkWords(int x, int y, String currentWord, int[][] s, List<Integer> c){
        List<String> foundWords = new ArrayList<String>();

        List<Integer> currentID = new ArrayList<Integer>();
        for(Integer a : c) currentID.add(a);

        try{
            currentWord += board.get(x).get(y);
            currentID.add(x);
            currentID.add(y);
        }catch(Exception ie){return new ArrayList<String>();}

        if(currentWord.length() > 2 && !isSubWord(currentWord)) return new ArrayList<String>();

        int[][] spaces = new int[boardWidth][boardHeight];

        if(currentWord.length() > 2) addWords(currentWord, currentID, foundWords);

        /** Copies visited state */
        for(int i = 0; i < boardWidth * boardHeight; i++)
            spaces[i % boardWidth][i / boardWidth] = s[i % boardWidth][i / boardWidth];

        spaces[x][y] = 1;

        /** Explores neighboring cells */
        for(int i = 0; i < 9; i++){
            if(i == 4) i++;
            try{
                if(spaces[x + i % 3 - 1][y + i / 3 - 1] == 0){
                    List<String> subWords =
                        checkWords(x + i % 3 - 1, y + i / 3 - 1, currentWord, spaces, currentID);
                    for(String a : subWords) foundWords.add(a);
                }
            } catch(Exception ie){}
        }

        return foundWords;
    }

    /**
     * Checks if the current word is a valid prefix.
     *
     * @param currentWord word prefix
     * @return true if prefix exists
     */
    private static boolean isSubWord(String currentWord){
        CharSequence cs = currentWord;
        for(int i = 0; i <
        words.get((int)currentWord.charAt(0) - 97)
            .get((int)currentWord.charAt(1) - 97)
            .get((int)currentWord.charAt(2) - 97).size(); i++){
            if(words.get((int)currentWord.charAt(0) - 97)
                .get((int)currentWord.charAt(1) - 97)
                .get((int)currentWord.charAt(2) - 97)
                .get(i).contains(cs)) return true;
        }
        return false;
    }

    /**
     * Adds completed words to the result set and records their paths.
     *
     * @param currentWord completed word
     * @param currentID path used to create the word
     * @param foundWords list of found words
     */
    private static void addWords(String currentWord, List<Integer> currentID, List<String> foundWords){
        for(int i = 0; i <
        words.get((int)currentWord.charAt(0) - 97)
            .get((int)currentWord.charAt(1) - 97)
            .get((int)currentWord.charAt(2) - 97).size(); i++){
            if(words.get((int)currentWord.charAt(0) - 97)
                .get((int)currentWord.charAt(1) - 97)
                .get((int)currentWord.charAt(2) - 97)
                .get(i).equals(currentWord)){
                foundWords.add(
                    words.get((int)currentWord.charAt(0) - 97)
                        .get((int)currentWord.charAt(1) - 97)
                        .get((int)currentWord.charAt(2) - 97)
                        .get(i));
                treasureWords.add(currentWord);
                wordPaths.add(currentID);
                words.get((int)currentWord.charAt(0) - 97)
                    .get((int)currentWord.charAt(1) - 97)
                    .get((int)currentWord.charAt(2) - 97)
                    .remove(i);
                i--;
                break;
            }
        }
    }

    public static List<Integer> getWordPath(int i){
        return wordPaths.get(i);
    }

    public static int getNumWords(){
        return wordPaths.size();
    }

    public static String generateBoard(int width, int height){
        int total = 0;
        for(int i = 0; i < 26; i++){
            total += letterPower[i];
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < width * height; i++){
            sb.append(randomLetter(total));
        }
        return sb.toString();
    }

    private static char randomLetter(int total){
        int r = (int)(Math.random() * total);

        int val = 0;

        for(int i = 0; i < 26; i++){
            val += letterPower[i];
            if(r < val){
                return (char)(i + 97);
            }
        }
        return '!';
    }
}
