package com.grantkoupal.letterlink.backend;

import java.util.ArrayList;
import java.util.List;

public class StartGameResponse {
    public String gameSessionId;
    public String playerId;
    public String mode;
    public int boardWidth;
    public int boardHeight;
    public String boardLetters;
    public List<String> boardRows = new ArrayList<>();
    public boolean ranked;
    public long timeLimitSeconds;
    public String startedAt;
}
