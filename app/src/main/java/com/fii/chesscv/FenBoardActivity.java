package com.fii.chesscv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import no.bakkenbaeck.chessboardeditor.view.board.ChessBoardView;

public class FenBoardActivity extends AppCompatActivity {
    private ChessBoardView chessBoard;
    private String fen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fen_board);

        chessBoard = findViewById(R.id.chessBoard);

        Bundle b = getIntent().getExtras();
        fen = "";
        if(b != null)
            fen = b.getString("fen");
        chessBoard.setFen(fen);
    }
}