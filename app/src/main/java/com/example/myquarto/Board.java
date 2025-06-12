package com.example.myquarto;

/**
 * Questa classe rappresenta una BOARD del gioco Quarto!
 * Una board è una matrice 4*4 di pedine
 */

/**
 * I metodi presenti in questa classe sono:
 * generateAllPieces()
 * assignPieceToPlayer(int player, Piece piece)
 * placePlayerPiece(int player, int row, int col)
 * getPlayerPiece(int player)
 * getAvailablePieces()
 * isValidSpot(int row, int col)
 * getPiece(int row, int col)
 * reset()
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class Board {
    private final Piece[][] grid;
    private final List<Piece> availablePieces;
    private Piece player1Piece; // Pezzo che il giocatore 1 deve piazzare
    private Piece player2Piece; // Pezzo che il giocatore 2 deve piazzare

    public Board() {
        this.grid = new Piece[4][4];
        this.availablePieces = generateAllPieces();
        Collections.shuffle(availablePieces); // Mescola i pezzi disponibili all'inizio
    }

    // Genera tutti i 16 pezzi unici (2)^4
    private List<Piece> generateAllPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (Piece.Larghezza l : Piece.Larghezza.values()) {
            for (Piece.Forma f : Piece.Forma.values()) {
                for (Piece.Colore c : Piece.Colore.values()) {
                    for (Piece.Tipo t : Piece.Tipo.values()) {
                        pieces.add(new Piece(l, f, c, t));
                    }
                }
            }
        }
        return pieces;
    }

    public boolean assignPieceToPlayer(int player, Piece piece) {
        if (!availablePieces.contains(piece)) return false; // Pezzo non più disponibile

        if (player == 1) {
            player1Piece = piece;
        } else if (player == 2) {
            player2Piece = piece;
        } else {
            return false; // Giocatore non valido
        }

        availablePieces.remove(piece); // Rimuove il pezzo da quelli globalmente disponibili
        return true;
    }

    public boolean placePlayerPiece(int player, int row, int col) {
        Piece pieceToPlace = (player == 1) ? player1Piece : player2Piece;

        if (pieceToPlace == null || !isValidSpot(row, col)) {
            return false;
        }

        grid[row][col] = pieceToPlace;

        if (player == 1) {
            player1Piece = null;
        } else {
            player2Piece = null;
        }

        return true;
    }
    public Piece getPlayerPiece(int player) {
        return (player == 1) ? player1Piece : player2Piece;
    }
    public List<Piece> getAvailablePieces() {
        return new ArrayList<>(availablePieces);
    }

    // Controlla se una casella è valida (entro i limiti e vuota)
    public boolean isValidSpot(int row, int col) {
        return row >= 0 && row < 4 && col >= 0 && col < 4 && grid[row][col] == null;
    }
    public Piece getPiece(int row, int col) {
        return isValidSpot(row, col) ? null : grid[row][col];
    }

    public void reset() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = null;
            }
        }

        availablePieces.clear();
        availablePieces.addAll(generateAllPieces());
        Collections.shuffle(availablePieces);

        player1Piece = null;
        player2Piece = null;
    }

    @Override
    public String toString() {
        return "Board{" +
                "grid=" + Arrays.toString(grid) +
                ", availablePieces=" + availablePieces +
                ", player1Piece=" + player1Piece +
                ", player2Piece=" + player2Piece +
                '}';
    }

    public Piece[][] getGrid() {
        return grid;
    }
}
