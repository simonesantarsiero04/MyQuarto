package com.example.myquarto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gestisce la logica di gioco, lo stato della partita e orchestra le interazioni
 * tra la logica (Board, VictoryCheck) e la UI (GameUIHandler).
 */

/**
 * I metodi presenti in questa classe sono:
 * startGame()
 * handleSelectPieceForOpponent(Piece piece, ImageView pieceImageViewFromList)
 * handlePlacePiece(int row, int col)
 * handleQuartoButtonClick(int callingPlayer)
 * endGame(String message, VictoryCheck.VictoryResult result)
 * resetGame()
 * handleAbortGame()
 * isBoardFull()
 * onTimeUpdated(String p1Time, String p2Time, int p1Sec, int p2Sec, boolean isP1Active)
 * onPlayerTimeout(int playerNumber)
 * onPlayAgain()
 * onQuit()
 * onReturnToSettings()
 */
public class MainActivity extends AppCompatActivity implements ChessClock.ChessClockListener, GameUIHandler.GameDialogListener {

    // Componenti di Logica e Stato
    private Board board;
    private VictoryCheck victoryCheck;
    private ChessClock chessClock;
    private List<Piece> currentAvailablePiecesList;

    private int currentPlayer = 1;
    private boolean isSelectingPiecePhase = true;
    private boolean isWaitingForQuartoCallAfter16th = false;

    // Gestori
    private GameUIHandler uiHandler;
    private final Handler drawCheckHandler = new Handler(Looper.getMainLooper());
    private Runnable drawCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new GameUIHandler(this);

        boolean timerEnabledSetting;
        int timerMinutesSetting;
        boolean[] winPreferencesSetting;

        Intent intent = getIntent();
        timerEnabledSetting = intent.getBooleanExtra(StartupActivity.EXTRA_TIMER_ENABLED, false);
        timerMinutesSetting = intent.getIntExtra(StartupActivity.EXTRA_TIMER_MINUTES, 3);
        winPreferencesSetting = intent.getBooleanArrayExtra(StartupActivity.EXTRA_WIN_PREFERENCES);
        if (winPreferencesSetting == null) {
            winPreferencesSetting = new boolean[]{true, true, true, true, false, true};
        }

        board = new Board();
        currentAvailablePiecesList = new ArrayList<>(board.getAvailablePieces());
        victoryCheck = new VictoryCheck(board,
                winPreferencesSetting[0], winPreferencesSetting[1], winPreferencesSetting[2],
                winPreferencesSetting[3], winPreferencesSetting[4], winPreferencesSetting[5]);

        if (timerEnabledSetting) {
            chessClock = new ChessClock(timerMinutesSetting, this);
        }

        uiHandler.setupListeners(v -> handleAbortGame(), this::handleQuartoButtonClick);
        uiHandler.setupBoardImageViews(this::handlePlacePiece);
        uiHandler.setupAvailablePieceImageViews(currentAvailablePiecesList, this::handleSelectPieceForOpponent);

        startGame();
    }

    private void startGame() {
        currentPlayer = 1;
        isSelectingPiecePhase = true;
        uiHandler.updateGameStateDisplay(currentPlayer, isSelectingPiecePhase);
        if (chessClock != null) {
            chessClock.start();
        }
    }

    private void handleSelectPieceForOpponent(Piece piece, ImageView pieceImageViewFromList) {
        if (!isSelectingPiecePhase) {
            Toast.makeText(this, R.string.toast_select_spot_first, Toast.LENGTH_SHORT).show();
            return;
        }

        int opponent = (currentPlayer == 1) ? 2 : 1;
        if (board.assignPieceToPlayer(opponent, piece)) {
            // Aggiorna UI
            pieceImageViewFromList.setEnabled(false);
            pieceImageViewFromList.setAlpha(0.2f);
            uiHandler.setPlayerPieceSlot(opponent, piece);
            uiHandler.clearPlayerPieceSlot(currentPlayer);

            // Aggiorna logica e stato
            currentAvailablePiecesList.remove(piece);
            isSelectingPiecePhase = false;
            currentPlayer = opponent;

            if (chessClock != null) {
                chessClock.switchTurn();
            }
            uiHandler.updateGameStateDisplay(currentPlayer, isSelectingPiecePhase);
        }
    }

    private void handlePlacePiece(int row, int col) {
        if (isSelectingPiecePhase) {
            Toast.makeText(this, R.string.toast_select_piece_first, Toast.LENGTH_SHORT).show();
            return;
        }

        Piece pieceToPlace = board.getPlayerPiece(currentPlayer);
        if (pieceToPlace == null) {
            Log.e("MainActivity", "handlePlacePiece: pieceToPlace era null per il giocatore " + currentPlayer);
            return;
        }

        if (board.placePlayerPiece(currentPlayer, row, col)) {
            // Aggiorna UI
            uiHandler.setPieceOnBoard(row, col, pieceToPlace);
            uiHandler.clearPlayerPieceSlot(currentPlayer);

            // Controlla fine partita per scacchiera piena, i giocatori hanno 7 secondi per chiamare quarto! diversamente è patta
            if (isBoardFull()) {
                Log.d("GAME_FLOW", "16° pezzo piazzato. In attesa di chiamata QUARTO!");
                isWaitingForQuartoCallAfter16th = true;

                drawCheckRunnable = () -> {
                    if (isWaitingForQuartoCallAfter16th) {
                        isWaitingForQuartoCallAfter16th = false;
                        Log.d("GAME_FLOW", "Timer scaduto. Dichiarata PATTA.");
                        endGame(getString(R.string.dialog_draw_message), null);
                    }
                };
                drawCheckHandler.postDelayed(drawCheckRunnable, 7000); // 7 secondi

                uiHandler.updateGameStateDisplay(currentPlayer, isSelectingPiecePhase);
                return;
            }

            // Prosegui al turno successivo
            isSelectingPiecePhase = true;
            uiHandler.updateGameStateDisplay(currentPlayer, isSelectingPiecePhase);
        } else {
            Toast.makeText(this, "Mossa non valida!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleQuartoButtonClick(int callingPlayer) {
        if (callingPlayer != currentPlayer) {
            Toast.makeText(this, getString(R.string.dialog_not_your_turn_quarto_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isWaitingForQuartoCallAfter16th) {
            if (drawCheckRunnable != null) drawCheckHandler.removeCallbacks(drawCheckRunnable);
            isWaitingForQuartoCallAfter16th = false;
        }

        VictoryCheck.VictoryResult result = victoryCheck.checkWin();
        if (result.type != VictoryCheck.VictoryType.NONE) {
            // VITTORIA!
            String winnerMessage = String.format(Locale.getDefault(), getString(R.string.dialog_player_wins_message),
                    callingPlayer, result.type.toString(), uiHandler.formatWinPosition(result));
            endGame(winnerMessage, result);
        } else {
            // NESSUN QUARTO
            if (isBoardFull()) {
                endGame(getString(R.string.dialog_draw_message), null);
            } else {
                uiHandler.showNoQuartoDialog();
                if (!isBoardFull()) {
                    isSelectingPiecePhase = true;
                    uiHandler.updateGameStateDisplay(currentPlayer, isSelectingPiecePhase);
                }
            }
        }
    }

    private void endGame(String message, VictoryCheck.VictoryResult result) {
        if (chessClock != null) chessClock.stop();
        if (result != null) {
            uiHandler.highlightWinningCells(result);
        }
        uiHandler.showGameEndDialog(message, this);
    }

    private void resetGame() {
        // Resetta stato e logica
        board.reset();
        currentAvailablePiecesList = new ArrayList<>(board.getAvailablePieces());
        if (drawCheckHandler != null && drawCheckRunnable != null) {
            drawCheckHandler.removeCallbacks(drawCheckRunnable);
        }
        isWaitingForQuartoCallAfter16th = false;
        if (chessClock != null) {
            chessClock.reset();
        }

        // Resetta UI tramite handler
        uiHandler.resetUI(currentAvailablePiecesList, this::handleSelectPieceForOpponent);

        // Riavvia la partita
        startGame();
    }

    private void handleAbortGame() {
        if (drawCheckHandler != null && drawCheckRunnable != null) drawCheckHandler.removeCallbacks(drawCheckRunnable);
        if (chessClock != null && chessClock.isRunning()) chessClock.stop();

        Intent intent = new Intent(MainActivity.this, StartupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isBoardFull() {
        return currentAvailablePiecesList.isEmpty() && board.getPlayerPiece(1) == null && board.getPlayerPiece(2) == null;
    }

    //region Implementazione Listener
    @Override
    public void onTimeUpdated(String p1Time, String p2Time, int p1Sec, int p2Sec, boolean isP1Active) {
        uiHandler.updateClock(p1Time, p2Time, isP1Active);
    }

    @Override
    public void onPlayerTimeout(int playerNumber) {
        String message = (playerNumber == 1) ?
                getString(R.string.dialog_player_1_wins_timeout) :
                getString(R.string.dialog_player_2_wins_timeout);
        endGame(message, null);
    }

    @Override
    public void onPlayAgain() {
        resetGame();
    }

    @Override
    public void onQuit() {
        finishAffinity();
    }

    @Override
    public void onReturnToSettings() {
        handleAbortGame();
    }
}