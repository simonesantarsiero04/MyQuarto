package com.example.myquarto;

/**
 * Questa classe rappresenta quali possono essere le diverse vittorie del gioco Quarto!
 * Le vittorie possono essere in:
 * verticale
 * orizzontale
 * diagonale
 * sui vertici dei quadrati 2x2
 * sui vertici dei quadrati 3x3
 * sui vertici dei quadrati 4x4
 *
 * le vittorie possono essere di uno qualsiasi degli attributi in comune
 * (es tutti chiari, tutti pieni etc)
 * l'ordine di controllo delle posizioni vincenti è stato assegnato in maniera casuale: righe, colonne, diagonali e vertici
 * l'ordine di controllo degli attributi è stato assegnato in maniera casuale: larghezza, forma, colore, tipo
 */

/**
 * I metodi di questa classe sono:
 * checkWin()
 * checkLine(Piece p1, Piece p2, Piece p3, Piece p4)
 * checkAttribute(T a1, T a2, T a3, T a4)
 */
public class VictoryCheck {
    // Tipi di vittoria basati sugli attributi dei pezzi
    public enum VictoryType {
        NONE,
        LARGHEZZA, FORMA, COLORE, TIPO
    }

    // Quali posizioni possono essere coperte
    public enum WinPosition {
        ROW, COLUMN, DIAGONAL, SQUARE_2x2, SQUARE_3x3, SQUARE_4x4
    }

    // Risultato del controllo di vittoria
    public static class VictoryResult {
        public final VictoryType type;
        public final WinPosition position;
        public final int index; // Riga/colonna (0-3), diagonale principale o secondaria e coordinate dei vertici dei quadrati
        public VictoryResult(VictoryType type, WinPosition position, int index) {
            this.type = type;
            this.position = position;
            this.index = index;
        }
    }

    private final Board board;
    // Flag per abilitare/disabilitare specifici controlli di vittoria
    // Anche questi sono final perchè una volta decisi all'inizio della partita non possono cambiare
    // Il default è tutto on tranne 3x3 squares
    private final boolean checkRows;
    private final boolean checkColumns;
    private final boolean checkDiagonal;
    private final boolean check2x2Squares;
    private final boolean check3x3Squares;
    private final boolean check4x4Square;

    // constructor
    public VictoryCheck(Board board,
                        boolean checkRows,
                        boolean checkColumns,
                        boolean checkDiagonal,
                        boolean check2x2Squares,
                        boolean check3x3Squares,
                        boolean check4x4Square) {
        this.board = board;
        this.checkRows = checkRows;
        this.checkColumns = checkColumns;
        this.checkDiagonal = checkDiagonal;
        this.check2x2Squares = check2x2Squares;
        this.check3x3Squares = check3x3Squares;
        this.check4x4Square = check4x4Square;
    }

    // Metodo per verificare la vittoria
    public VictoryResult checkWin() {
        VictoryType resultType;

        // controllo righe (qualsiasi riga, colonne esatte)
        if (checkRows) {
            for (int row = 0; row < 4; row++) {
                resultType = checkLine(
                        board.getPiece(row, 0),
                        board.getPiece(row, 1),
                        board.getPiece(row, 2),
                        board.getPiece(row, 3)
                );
                if (resultType != VictoryType.NONE) {
                    return new VictoryResult(resultType, WinPosition.ROW, row);
                }
            }
        }

        // controllo colonne (qualsiasi colonna, righe esatte)
        if (checkColumns) {
            for (int col = 0; col < 4; col++) {
                resultType = checkLine(
                        board.getPiece(0, col),
                        board.getPiece(1, col),
                        board.getPiece(2, col),
                        board.getPiece(3, col)
                );
                if (resultType != VictoryType.NONE) {
                    return new VictoryResult(resultType, WinPosition.COLUMN, col);
                }
            }
        }

        // controllo diagonali (caselle esatte)
        if (checkDiagonal) {
            resultType = checkLine(
                    board.getPiece(0, 0),
                    board.getPiece(1, 1),
                    board.getPiece(2, 2),
                    board.getPiece(3, 3)
            );
            if (resultType != VictoryType.NONE) {
                return new VictoryResult(resultType, WinPosition.DIAGONAL, 0); // Diagonale principale
            }

            resultType = checkLine(
                    board.getPiece(0, 3),
                    board.getPiece(1, 2),
                    board.getPiece(2, 1),
                    board.getPiece(3, 0)
            );
            if (resultType != VictoryType.NONE) {
                return new VictoryResult(resultType, WinPosition.DIAGONAL, 1); // Diagonale secondaria
            }
        }

        // Controllo vertici di quadrati 2x2
        if (check2x2Squares) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    resultType = checkLine(
                            board.getPiece(row, col),                   // alto sx
                            board.getPiece(row, col + 1),           // alto dx
                            board.getPiece(row + 1, col),           // basso sx
                            board.getPiece(row + 1, col + 1)    // basso dx
                    );
                    if (resultType != VictoryType.NONE) {
                        return new VictoryResult(resultType, WinPosition.SQUARE_2x2, row * 10 + col);
                    }
                }
            }
        }

        // Controllo vertici di quadrati 3x3
        if (check3x3Squares) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    resultType = checkLine(
                            board.getPiece(row, col),                   //alto sx
                            board.getPiece(row, col + 2),           // alto dx
                            board.getPiece(row + 2, col),           // basso sx
                            board.getPiece(row + 2, col + 2)    // basso dx
                    );
                    if (resultType != VictoryType.NONE) {
                        return new VictoryResult(resultType, WinPosition.SQUARE_3x3, row * 10 + col);
                    }
                }
            }
        }

        // Controllo per i 4 vertici della scacchiera
        if (check4x4Square) {
            resultType = checkLine(
                    board.getPiece(0, 0),
                    board.getPiece(0, 3),
                    board.getPiece(3, 0),
                    board.getPiece(3, 3)
            );
            if (resultType != VictoryType.NONE) {
                return new VictoryResult(resultType, WinPosition.SQUARE_4x4, 0);
            }
        }

        // Nessuna vittoria trovata
        return new VictoryResult(VictoryType.NONE, null, -1);
    }

    // Controlla se 4 pezzi formano una linea vincente
    private VictoryType checkLine(Piece p1, Piece p2, Piece p3, Piece p4) {
        // Se anche solo un pezzo manca, non c'è linea vincente
        if (p1 == null || p2 == null || p3 == null || p4 == null) {
            return VictoryType.NONE;
        }

        // Verifica ogni attributo
        // Verifica larghezza
        if (checkAttribute(p1.getLarghezza(), p2.getLarghezza(), p3.getLarghezza(), p4.getLarghezza())) {
            return VictoryType.LARGHEZZA;
        }
        // Verifica forma
        if (checkAttribute(p1.getForma(), p2.getForma(), p3.getForma(), p4.getForma())) {
            return VictoryType.FORMA;
        }
        // Verifica colore
        if (checkAttribute(p1.getColore(), p2.getColore(), p3.getColore(), p4.getColore())) {
            return VictoryType.COLORE;
        }
        // Verifica tipo
        if (checkAttribute(p1.getTipo(), p2.getTipo(), p3.getTipo(), p4.getTipo())) {
            return VictoryType.TIPO;
        }

        return VictoryType.NONE; // Nessuna caratteristica comune
    }

    // Metodo per confrontare 4 attributi della stessa categoria
    private <T> boolean checkAttribute(T a1, T a2, T a3, T a4) {
        return a1 == a2 && a2 == a3 && a3 == a4;
    }
}
