package com.grantkoupal.letterlink.backend;

import java.util.ArrayList;
import java.util.List;

public class FinishGameResponse {
    public String gameSessionId;
    public String status;
    public String mode;
    public int boardWidth;
    public int boardHeight;
    public String boardLetters;
    public List<String> boardRows = new ArrayList<>();
    public boolean ranked;
    public boolean timedOut;
    public boolean countedAsWin;
    public int targetScore;
    public int validatedScore;
    public int acceptedWordCount;
    public int rejectedWordCount;
    public List<String> acceptedWords = new ArrayList<>();
    public List<RejectedWordResponse> rejectedWords = new ArrayList<>();
    public int wins;
    public int losses;
    public Integer mmrBefore;
    public Integer mmrAfter;
    public String finishedAt;
}
