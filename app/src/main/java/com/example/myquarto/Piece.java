package com.example.myquarto;
/**
 * Questa classe rappresenta una pedina del gioco Quarto.
 * Ogni pedina ha 4 attributi, gli attributi sono binari:
 * - larghezza (largo, stretto)
 * - forma (quadrato, rotondo)
 * - colore (chiaro, scuro) (in verità non importa: basta che siano due colori diversi)
 * - tipo (pieno, vuoto)
 */

/**
 * I metodi di questa classe sono:
 * Standard get
 * ToShortString()
 */

public class Piece {
    // le variabili sono di tipo final perchè all'interno del gioco gli attributi di una pedina non possono cambiare.
    private final Larghezza larghezza;
    private final Forma forma;
    private final Colore colore;
    private final Tipo tipo;

    // Attributi
    public enum Larghezza { LARGO, STRETTO }
    public enum Forma { QUADRATO, ROTONDO }
    public enum Colore { CHIARO, SCURO }
    public enum Tipo { PIENO, VUOTO }
    // Costruttore
    public Piece(Larghezza larghezza, Forma forma, Colore colore, Tipo tipo) {
        this.larghezza = larghezza;
        this.forma = forma;
        this.colore = colore;
        this.tipo = tipo;
    }

    // Non servono i setter perchè i pezzi sono statici
    public Larghezza getLarghezza() {
        return larghezza;
    }

    public Forma getForma() {
        return forma;
    }

    public Colore getColore() {
        return colore;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String toShortString() {
        return larghezza.name().charAt(0) + "" +   // "L" o "S"
                forma.name().charAt(0) + "" +       // "Q" o "R"
                colore.name().charAt(0) + "" +      // "C" o "S"
                tipo.name().charAt(0);              // "P" o "V"
    }
}