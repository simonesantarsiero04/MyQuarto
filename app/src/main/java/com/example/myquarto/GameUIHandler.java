package com.example.myquarto;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gestisce tutte le interazioni con l'interfaccia utente (UI) per la partita.
 * Trova le View, le aggiorna e risponde agli eventi della logica di gioco.
 */

/**
 * I metodi presenti in questa classe sono:
 * GameUIHandler(@NonNull MainActivity activity)
 * findViews()
 * setupListeners(View.OnClickListener abortListener, QuartoButtonClickListener quartoListener)
 * setupBoardImageViews(BoardCellClickListener listener)
 * setupAvailablePieceImageViews(List<Piece> availablePieces, AvailablePieceClickListener listener)
 * formatTime(int TotalSeconds)
 * updateClock(String p1Time, String p2Time, boolean isP1Active)
 * updateGameStateDisplay(int currentPlayer, boolean isSelectingPiecePhase)
 * setPieceOnBoard(int row, int col, Piece piece)
 * clearPlayerPieceSlot(int player)
 * highlightWinningCells(VictoryCheck.VictoryResult result)
 * showGameEndDialog(String message, GameDialogListener listener)
 * showNoQuartoDialog()
 * resetUI(List<Piece> initialPieces, AvailablePieceClickListener listener)
 * getDrawableIdForPiece(Piece piece)
 * String formatWinPosition(VictoryCheck.VictoryResult result)
 */
public class GameUIHandler {

    @FunctionalInterface
    public interface BoardCellClickListener {
        void onCellClick(int row, int col);
    }

    @FunctionalInterface
    public interface AvailablePieceClickListener {
        void onPieceClick(Piece piece, ImageView pieceView);
    }

    @FunctionalInterface
    public interface QuartoButtonClickListener {
        void onQuartoClick(int player);
    }

    public interface GameDialogListener {
        void onPlayAgain();
        void onQuit();
        void onReturnToSettings();
    }
    //endregion

    private final MainActivity activity;

    // Riferimenti ai componenti UI
    private TextView textViewPlayer1Clock, textViewPlayer2Clock;
    private TextView textViewPlayer1Status, textViewPlayer2Status;
    private ImageView imageViewPlayer1PieceSlot, imageViewPlayer2PieceSlot;
    private Button buttonPlayer1Quarto, buttonPlayer2Quarto;
    private GridLayout gridLayoutBoard;
    private GridLayout gridLayoutAvailablePieces;
    private final ImageView[][] boardImageViews = new ImageView[4][4];

    // Costanti e dimensioni UI
    private final int EMPTY_CELL_DRAWABLE_ID = R.drawable.cell_empty;
    private final int EMPTY_PIECE_SLOT_DRAWABLE_ID = R.drawable.piece_slot_empty;
    private final int WIN_CELL_DRAWABLE_ID = R.drawable.win_cell;
    private final int largoPiecePxSize;
    private final int strettoPiecePxSize;
    private final int pieceMarginPx;
    private final int boardPxSize;

    public GameUIHandler(@NonNull MainActivity activity) {
        this.activity = activity;

        // Carica le dimensioni dalle risorse
        largoPiecePxSize = activity.getResources().getDimensionPixelSize(R.dimen.piece_largo_size);
        strettoPiecePxSize = activity.getResources().getDimensionPixelSize(R.dimen.piece_stretto_size);
        pieceMarginPx = activity.getResources().getDimensionPixelSize(R.dimen.piece_image_margin);
        boardPxSize = activity.getResources().getDimensionPixelSize(R.dimen.piece_board_size);

        findViews();
    }

    private void findViews() {
        textViewPlayer1Clock = activity.findViewById(R.id.textview_player1_clock);
        textViewPlayer2Clock = activity.findViewById(R.id.textview_player2_clock);
        textViewPlayer1Status = activity.findViewById(R.id.textview_player1_status);
        textViewPlayer2Status = activity.findViewById(R.id.textview_player2_status);
        imageViewPlayer1PieceSlot = activity.findViewById(R.id.imageview_player1_piece_slot);
        imageViewPlayer2PieceSlot = activity.findViewById(R.id.imageview_player2_piece_slot);
        buttonPlayer1Quarto = activity.findViewById(R.id.button_player1_quarto);
        buttonPlayer2Quarto = activity.findViewById(R.id.button_player2_quarto);
        gridLayoutBoard = activity.findViewById(R.id.gridlayout_board);
        gridLayoutAvailablePieces = activity.findViewById(R.id.gridlayout_available_pieces);
    }

    public void setupListeners(View.OnClickListener abortListener, QuartoButtonClickListener quartoListener) {
        activity.findViewById(R.id.button_abort_game).setOnClickListener(abortListener);
        buttonPlayer1Quarto.setOnClickListener(v -> quartoListener.onQuartoClick(1));
        buttonPlayer2Quarto.setOnClickListener(v -> quartoListener.onQuartoClick(2));
    }

    public void setupBoardImageViews(BoardCellClickListener listener) {
        if (gridLayoutBoard == null) return;
        gridLayoutBoard.removeAllViews();
        int cellImageSize = boardPxSize;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                ImageView imageView = new ImageView(activity);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellImageSize;
                params.height = cellImageSize;

                params.setMargins(pieceMarginPx, pieceMarginPx, pieceMarginPx, pieceMarginPx);
                params.rowSpec = GridLayout.spec(r);
                params.columnSpec = GridLayout.spec(c);
                params.setGravity(Gravity.CENTER);
                imageView.setLayoutParams(params);
                imageView.setImageResource(EMPTY_CELL_DRAWABLE_ID);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                final int row = r;
                final int col = c;
                imageView.setOnClickListener(v -> listener.onCellClick(row, col));
                boardImageViews[r][c] = imageView;
                gridLayoutBoard.addView(imageView);
            }
        }
    }

    public void setupAvailablePieceImageViews(List<Piece> availablePieces, AvailablePieceClickListener listener) {
        if (gridLayoutAvailablePieces == null) return;
        gridLayoutAvailablePieces.removeAllViews();

        for (Piece piece : availablePieces) {
            ImageView imageView = new ImageView(activity);
            int pieceSize = (piece.getLarghezza() == Piece.Larghezza.STRETTO) ? strettoPiecePxSize : largoPiecePxSize;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = pieceSize;
            params.height = pieceSize;

            params.setMargins(pieceMarginPx, pieceMarginPx, pieceMarginPx, pieceMarginPx);
            params.setGravity(Gravity.CENTER);
            imageView.setLayoutParams(params);

            imageView.setImageResource(getDrawableIdForPiece(piece));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setOnClickListener(v -> listener.onPieceClick(piece, (ImageView) v));
            gridLayoutAvailablePieces.addView(imageView);
        }
    }

    public void updateClock(String p1Time, String p2Time, boolean isP1Active) {
        textViewPlayer1Clock.setText(p1Time);
        textViewPlayer2Clock.setText(p2Time);
        textViewPlayer1Clock.setTextColor(ContextCompat.getColor(activity, isP1Active ? R.color.active_clock_color : R.color.inactive_clock_color));
        textViewPlayer2Clock.setTextColor(ContextCompat.getColor(activity, !isP1Active ? R.color.active_clock_color : R.color.inactive_clock_color));
    }

    public void updateGameStateDisplay(int currentPlayer, boolean isSelectingPiecePhase) {
        String statusP1, statusP2;
        if (isSelectingPiecePhase) {
            statusP1 = (currentPlayer == 1) ? activity.getString(R.string.status_select_piece_for_opponent) : activity.getString(R.string.status_waiting_opponent);
            statusP2 = (currentPlayer == 2) ? activity.getString(R.string.status_select_piece_for_opponent) : activity.getString(R.string.status_waiting_opponent);
        } else {
            statusP1 = (currentPlayer == 1) ? activity.getString(R.string.status_place_your_piece) : activity.getString(R.string.status_waiting_opponent);
            statusP2 = (currentPlayer == 2) ? activity.getString(R.string.status_place_your_piece) : activity.getString(R.string.status_waiting_opponent);
        }
        textViewPlayer1Status.setText(statusP1);
        textViewPlayer2Status.setText(statusP2);
        buttonPlayer1Quarto.setEnabled(currentPlayer == 1);
        buttonPlayer2Quarto.setEnabled(currentPlayer == 2);
    }

    public void setPieceOnBoard(int row, int col, Piece piece) {
        ImageView cellImageView = boardImageViews[row][col];
        cellImageView.setBackgroundResource(EMPTY_CELL_DRAWABLE_ID);
        cellImageView.setImageResource(getDrawableIdForPiece(piece));
        cellImageView.setEnabled(false);

        int padding = (piece.getLarghezza() == Piece.Larghezza.STRETTO) ? (largoPiecePxSize - strettoPiecePxSize) / 2 : 0;
        cellImageView.setPadding(padding, padding, padding, padding);
    }

    public void setPlayerPieceSlot(int player, Piece piece) {
        ImageView targetSlot = (player == 1) ? imageViewPlayer1PieceSlot : imageViewPlayer2PieceSlot;
        targetSlot.setImageResource(getDrawableIdForPiece(piece));

        if (piece != null) {
            int padding = (piece.getLarghezza() == Piece.Larghezza.STRETTO) ? (largoPiecePxSize - strettoPiecePxSize) / 2 : 0;
            targetSlot.setPadding(padding, padding, padding, padding);
        }
    }

    public void clearPlayerPieceSlot(int player) {
        ImageView targetSlot = (player == 1) ? imageViewPlayer1PieceSlot : imageViewPlayer2PieceSlot;
        targetSlot.setImageResource(EMPTY_PIECE_SLOT_DRAWABLE_ID);
    }

    public void highlightWinningCells(VictoryCheck.VictoryResult result) {
        List<ImageView> cellsToHighlight = new ArrayList<>();
        int r, c;
        switch (result.position) {
            case ROW:
                for (int col = 0; col < 4; col++) cellsToHighlight.add(boardImageViews[result.index][col]);
                break;
            case COLUMN:
                for (int row = 0; row < 4; row++) cellsToHighlight.add(boardImageViews[row][result.index]);
                break;
            case DIAGONAL:
                if (result.index == 0) { // Principale
                    for (int i = 0; i < 4; i++) cellsToHighlight.add(boardImageViews[i][i]);
                } else { // Secondaria
                    for (int i = 0; i < 4; i++) cellsToHighlight.add(boardImageViews[i][3 - i]);
                }
                break;
            case SQUARE_2x2:
                r = result.index / 10; c = result.index % 10;
                cellsToHighlight.add(boardImageViews[r][c]);
                cellsToHighlight.add(boardImageViews[r][c + 1]);
                cellsToHighlight.add(boardImageViews[r + 1][c]);
                cellsToHighlight.add(boardImageViews[r + 1][c + 1]);
                break;
            case SQUARE_3x3:
                r = result.index / 10; c = result.index % 10;
                cellsToHighlight.add(boardImageViews[r][c]);
                cellsToHighlight.add(boardImageViews[r][c + 2]);
                cellsToHighlight.add(boardImageViews[r + 2][c]);
                cellsToHighlight.add(boardImageViews[r + 2][c + 2]);
                break;
            case SQUARE_4x4:
                cellsToHighlight.add(boardImageViews[0][0]);
                cellsToHighlight.add(boardImageViews[0][3]);
                cellsToHighlight.add(boardImageViews[3][0]);
                cellsToHighlight.add(boardImageViews[3][3]);
                break;
        }
        for (ImageView cellView : cellsToHighlight) {
            if (cellView != null) cellView.setBackgroundResource(WIN_CELL_DRAWABLE_ID);
        }
    }

    public void showGameEndDialog(String message, GameDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_quarto_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.button_play_again, (dialog, which) -> listener.onPlayAgain())
                .setNegativeButton(R.string.button_quit, (dialog, which) -> listener.onQuit())
                .setNeutralButton(R.string.button_return_to_settings, (dialog, which) -> listener.onReturnToSettings());

        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.alpha = 0.8f;
            window.setAttributes(layoutParams);
        }
    }

    public void showNoQuartoDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_no_quarto_title)
                .setMessage(R.string.dialog_no_quarto_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void resetUI(List<Piece> initialPieces, AvailablePieceClickListener listener) {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                boardImageViews[r][c].setImageResource(EMPTY_CELL_DRAWABLE_ID);
                boardImageViews[r][c].setEnabled(true);
                boardImageViews[r][c].setPadding(0, 0, 0, 0);
                boardImageViews[r][c].setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent));
            }
        }
        clearPlayerPieceSlot(1);
        clearPlayerPieceSlot(2);
        setupAvailablePieceImageViews(initialPieces, listener);
    }

    private int getDrawableIdForPiece(Piece piece) {
        if (piece == null) return EMPTY_CELL_DRAWABLE_ID;
        String drawableName = piece.toShortString().toLowerCase();
        int resId = activity.getResources().getIdentifier(drawableName, "drawable", activity.getPackageName());
        return resId == 0 ? EMPTY_CELL_DRAWABLE_ID : resId;
    }

    public String formatWinPosition(VictoryCheck.VictoryResult result) {
        if (result == null || result.position == null) return "posizione non specificata";
        switch (result.position) {
            case ROW: return "riga " + (result.index + 1);
            case COLUMN: return "colonna " + (result.index + 1);
            case DIAGONAL: return "diagonale " + (result.index == 0 ? "principale" : "secondaria");
            case SQUARE_2x2: return String.format(Locale.getDefault(),"vertici del 2x2 di coordinate (%d,%d)", result.index / 10 + 1, result.index % 10 + 1);
            case SQUARE_3x3: return String.format(Locale.getDefault(),"vertici del 3x3 di coordinate (%d,%d)", result.index / 10 + 1, result.index % 10 + 1);
            case SQUARE_4x4: return "vertici scacchiera";
            default: return "sconosciuta";
        }
    }
}