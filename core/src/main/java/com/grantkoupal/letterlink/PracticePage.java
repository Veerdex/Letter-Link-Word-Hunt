package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.grantkoupal.letterlink.quantum.Page;
import com.grantkoupal.letterlink.quantum.Process;

public class PracticePage extends Page {

    private Board board;
    private HintTable hintTable;
    private final int boardWidth = 4;
    private final int boardHeight = 4;
    private TextureSet bts;

    @Override
    public void initialize() {

        bts = new TextureSet("Wood Piece.png", "Boise.png", "Blob.jpg");

        board = new Board(boardWidth,
            boardHeight,
            Solver.generateBoard(boardWidth, boardHeight),
            bts);

        System.out.println(board.getWordsInBoard());

        add(board);

        hintTable = new HintTable(board.getWordsInBoard(), board.getWordsFound(), bts);

        add(hintTable);

        board.addAnimations(this);

        Source.addRenderer(renderer);

        run();
    }

    private void run(){
        addResize(new Process() {
            @Override
            public boolean run() {
                board.updateFrameBuffer(new FrameBuffer(Pixmap.Format.RGBA8888, Source.getScreenWidth(), Source.getScreenHeight(), false));
                return true;
            }
        });
    }

    @Override
    public void restart() {

    }

    @Override
    public void dispose() {
        board.dispose();
        bts.dispose();
    }
}
