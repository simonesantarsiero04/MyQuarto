package com.example.myquarto;

/**
 * Utilizziamo questa classe solo nel caso in cui sia attivato il ChessClock
 * All'interno del gioco di Quarto! possiamo usare la stessa logica dell'orologio nel gioco degli scacchi
 */

/**
 * I metodi presenti in questa classe sono:
 * resetInternalStateAndNotify()
 * start()
 * stop()
 * reset()
 * switchTurn()
 * formatTime(int TotalSeconds)
 * isRunning()
 */

import android.os.CountDownTimer;

public class ChessClock {
    public interface ChessClockListener {
        void onTimeUpdated(String player1TimeFormatted, String player2TimeFormatted,
                           int player1TimeSeconds, int player2TimeSeconds, boolean isPlayer1Active);

        void onPlayerTimeout(int playerNumber);
    }

    private int player1TimeSeconds;
    private int player2TimeSeconds;
    private boolean isPlayer1Turn;
    private CountDownTimer timer;
    private final ChessClockListener listener;
    private final int initialTimeSecondsPerPlayer;
    private boolean isRunning = false;

    // Constructor
    public ChessClock(int initialMinutesPerPlayer, ChessClockListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("ChessClockListener non può essere null.");
        }
        this.initialTimeSecondsPerPlayer = initialMinutesPerPlayer * 60;
        this.listener = listener;
        this.isPlayer1Turn = true; // Giocatore 1 inizia per default
        resetInternalStateAndNotify();
    }

    private void resetInternalStateAndNotify() {
        this.player1TimeSeconds = this.initialTimeSecondsPerPlayer;
        this.player2TimeSeconds = this.initialTimeSecondsPerPlayer;
        // Notifica lo stato iniziale/resettato al listener
        listener.onTimeUpdated(
                formatTime(this.player1TimeSeconds),
                formatTime(this.player2TimeSeconds),
                this.player1TimeSeconds,
                this.player2TimeSeconds,
                this.isPlayer1Turn
        );
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;

        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) { // Tick ogni secondo
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isRunning) { // Controllo di sicurezza, se stop() è stato chiamato nel frattempo
                    cancel();
                    return;
                }

                if (isPlayer1Turn) {
                    player1TimeSeconds--;
                } else {
                    player2TimeSeconds--;
                }

                listener.onTimeUpdated(
                        formatTime(player1TimeSeconds),
                        formatTime(player2TimeSeconds),
                        player1TimeSeconds,
                        player2TimeSeconds,
                        isPlayer1Turn
                );

                // Controllo timeout
                if ((isPlayer1Turn && player1TimeSeconds <= 0) ||
                        (!isPlayer1Turn && player2TimeSeconds <= 0)) {
                    // Assicura che il tempo non vada sotto zero nella notifica finale
                    if (player1TimeSeconds < 0) player1TimeSeconds = 0;
                    if (player2TimeSeconds < 0) player2TimeSeconds = 0;

                    stop(); // Ferma il timer e setta isRunning = false
                    listener.onPlayerTimeout(isPlayer1Turn ? 1 : 2);
                }
            }

            @Override
            public void onFinish() {
                if (isRunning) {
                    stop();
                    listener.onPlayerTimeout(isPlayer1Turn ? 1 : 2);
                }
            }
        }.start();
    }

    public void stop() {
        if (!isRunning && timer == null) {
            return;
        }
        isRunning = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void reset() {
        stop();
        this.isPlayer1Turn = true; // Giocatore 1 inizia dopo un reset
        resetInternalStateAndNotify();
    }

    public void switchTurn() {
        isPlayer1Turn = !isPlayer1Turn;
        listener.onTimeUpdated(
                formatTime(player1TimeSeconds),
                formatTime(player2TimeSeconds),
                player1TimeSeconds,
                player2TimeSeconds,
                isPlayer1Turn
        );
    }
    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0; // Evita visualizzazione di tempo negativo
        }
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public boolean isRunning() {
        return isRunning;
    }

    // metodi getter anche se non vengono utilizzati

    public int getPlayer1TimeSeconds() {
        return player1TimeSeconds;
    }

    public int getPlayer2TimeSeconds() {
        return player2TimeSeconds;
    }
}