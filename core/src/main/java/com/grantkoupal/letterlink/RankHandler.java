package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;

public class RankHandler {
    private static Texture[] rankTextures = new Texture[19];
    public enum ID {
        B1, B2, B3,
        S1, S2, S3,
        G1, G2, G3,
        P1, P2, P3,
        D1, D2, D3,
        L1, L2, L3,
        WL
    };

    public static void loadTextures(){
        for(int i = 0; i < 19; i++){
            rankTextures[i] = loadTexture(intToTexture(i));
        }
    }

    private static Texture loadTexture(String name){
        Texture temp = new Texture(Source.getAsset("Ranks/" + name + ".png"), true);
        temp.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap);
        return temp;
    }

    private static String intToTexture(int i){
        switch(i){
            case 0 : return "Bronze 1";
            case 1 : return "Bronze 2";
            case 2 : return "Bronze 3";
            case 3 : return "Silver 1";
            case 4 : return "Silver 2";
            case 5 : return "Silver 3";
            case 6 : return "Gold 1";
            case 7 : return "Gold 2";
            case 8 : return "Gold 3";
            case 9 : return "Platinum 1";
            case 10 : return "Platinum 2";
            case 11 : return "Platinum 3";
            case 12 : return "Diamond 1";
            case 13 : return "Diamond 2";
            case 14 : return "Diamond 3";
            case 15 : return "Legend 1";
            case 16 : return "Legend 2";
            case 17 : return "Legend 3";
            case 18 : return "WordLord";
        }

        return "Bronze 1";
    }

    public static Texture getTexture(int i){
        return rankTextures[i];
    }

    public static Texture getTexture(ID id){
        return rankTextures[stringToInt(id)];
    }

    private static int stringToInt(ID id){
        switch(id){
            case B1 : return 0;
            case B2 : return 1;
            case B3 : return 2;
            case S1 : return 3;
            case S2 : return 4;
            case S3 : return 5;
            case G1 : return 6;
            case G2 : return 7;
            case G3 : return 8;
            case P1 : return 9;
            case P2 : return 10;
            case P3 : return 11;
            case D1 : return 12;
            case D2 : return 13;
            case D3 : return 14;
            case L1 : return 15;
            case L2 : return 16;
            case L3 : return 17;
            case WL : return 18;
        }

        return 0;
    }

    public static Texture getTextureBasedOffRank(){
        float rank = DataManager.rank;
        int temp = 100;
        for(int i = 0; i < 18; i++){
            if(rank < temp){
                return getTexture(i);
            }
            temp += 100;
        }
        return getTexture(0);
    }
}
