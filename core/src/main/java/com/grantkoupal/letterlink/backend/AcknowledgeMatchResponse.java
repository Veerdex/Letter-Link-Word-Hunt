package com.grantkoupal.letterlink.backend;

import java.util.ArrayList;
import java.util.List;

public class AcknowledgeMatchResponse {
    public String matchId;
    public String matchStatus;
    public boolean playerAcknowledged;
    public boolean bothAcknowledged;
    public boolean ready;
    public int power;
    public String boardLetters;
    public List<String> boardRows = new ArrayList<String>();
    public String updatedAt;
}
