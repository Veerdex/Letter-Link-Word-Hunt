package com.grantkoupal.letterlink.backend;

import java.util.ArrayList;
import java.util.List;

public class MatchStatusResponse {
    public String ticketId;
    public String ticketStatus;
    public String matchId;
    public String matchStatus;
    public boolean playerAcknowledged;
    public boolean bothAcknowledged;
    public boolean ready;
    public String opponentId;
    public String opponentUsername;
    public String mode;
    public String currentGamemode;
    public int boardWidth;
    public int boardHeight;
    public int power;
    public String boardLetters;
    public List<String> boardRows = new ArrayList<String>();
    public String updatedAt;
}
