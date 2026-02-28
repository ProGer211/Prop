package edu.epsevg.prop.lab.c4;

/**
 *
 * @author Gerard
 */
public class Jugador1 implements Jugador, IAuto {
    int profundidad;

    public Jugador1(int profundidad) {
        System.out.println("CREA JUGADOR1");
        this.profundidad = profundidad;
    }

    @Override
    public int moviment(Tauler t, int color) {
        System.out.println("El método moviment() ha sido llamado.");

        int mejorMovimiento = -1;
        int mejorValor = Integer.MIN_VALUE;

        // Iterar sobre todas las columnas posibles
        for (int col = 0; col < t.getMida(); col++) {
            if (t.movpossible(col)) {
                // Hacer una copia del tablero
                Tauler copiaTauler = new Tauler(t);

                // Realizar el movimiento en la copia
                copiaTauler.afegeix(col, color);

                // Verificar si el movimiento resulta en victoria
                if (copiaTauler.solucio(col, color)) {
                    // Si es una victoria, devolver este movimiento
                    System.out.println("Movimiento ganador encontrado en columna: " + col);
                    return col;
                }

                // Llamar a Minimax para el movimiento actual
                int valor = minimax(copiaTauler, profundidad - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, color);

                System.out.println("Columna " + col + ", Valor obtenido: " + valor);

                // Actualizar el mejor movimiento
                if (valor > mejorValor) {
                    mejorValor = valor;
                    mejorMovimiento = col;
                }
            }
        }

        // Si no hay un movimiento ganador inmediato, devolvemos el mejor encontrado
        if (mejorMovimiento != -1) {
            System.out.println("Mejor movimiento seleccionado: " + mejorMovimiento);
            return mejorMovimiento;
        } else {
            // Si no se encontró un mejor movimiento, elegir una columna válida
            for (int col = 0; col < t.getMida(); col++) {
                if (t.movpossible(col)) {
                    System.out.println("No se encontró un mejor movimiento, seleccionando columna: " + col);
                    return col;
                }
            }
            // Si ninguna columna es válida, retornar -1 (no hay movimientos posibles)
            System.out.println("No hay movimientos posibles.");
            return -1;
        }
    }

    private int minimax(Tauler t, int profundidad, int alpha, int beta, boolean maximizando, int color) {
        int colorOponente = -color;

        if (profundidad == 0 || !t.espotmoure()) {
            int evaluacion = evaluar(t, color);
            return evaluacion;
        }

        if (maximizando) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < t.getMida(); col++) {
                if (t.movpossible(col)) {
                    Tauler copiaTauler = new Tauler(t);
                    copiaTauler.afegeix(col, color);

                    if (copiaTauler.solucio(col, color)) {
                        return Integer.MAX_VALUE; // Victoria inmediata
                    }

                    int evaluacion = minimax(copiaTauler, profundidad - 1, alpha, beta, false, color);
                    maxEval = Math.max(maxEval, evaluacion);
                    alpha = Math.max(alpha, evaluacion);
                    if (beta <= alpha) {
                        break; // Poda beta
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < t.getMida(); col++) {
                if (t.movpossible(col)) {
                    Tauler copiaTauler = new Tauler(t);
                    copiaTauler.afegeix(col, colorOponente);

                    if (copiaTauler.solucio(col, colorOponente)) {
                        return Integer.MIN_VALUE; // Derrota inmediata
                    }

                    int evaluacion = minimax(copiaTauler, profundidad - 1, alpha, beta, true, color);
                    minEval = Math.min(minEval, evaluacion);
                    beta = Math.min(beta, evaluacion);
                    if (beta <= alpha) {
                        break; // Poda alfa
                    }
                }
            }
            return minEval;
        }
    }

    private int evaluar(Tauler t, int color) {
        int puntuacion = 0;

        // Evaluar el centro del tablero
        int columnasCentrales = t.getMida() / 2;
        int cuentaCentro = 0;
        for (int fila = 0; fila < t.getMida(); fila++) {
            int casilla = t.getColor(fila, columnasCentrales);
            if (casilla == color) {
                cuentaCentro++;
            }
        }
        puntuacion += cuentaCentro; // Bonificación por ocupar el centro

        // Evaluar líneas horizontales, verticales y diagonales
        puntuacion += evaluarLineas(t, color);

        return puntuacion;
    }

    private int evaluarLineas(Tauler t, int color) {
        int puntuacion = 0;

        // Obtener todas las posibles ventanas de 4 casillas
        for (int fila = 0; fila < t.getMida(); fila++) {
            for (int col = 0; col < t.getMida(); col++) {
                // Horizontal
                if (col + 3 < t.getMida()) {
                    int[] ventana = new int[4];
                    for (int i = 0; i < 4; i++) {
                        ventana[i] = t.getColor(fila, col + i);
                    }
                    puntuacion += evaluarVentana(ventana, color);
                }
                // Vertical
                if (fila + 3 < t.getMida()) {
                    int[] ventana = new int[4];
                    for (int i = 0; i < 4; i++) {
                        ventana[i] = t.getColor(fila + i, col);
                    }
                    puntuacion += evaluarVentana(ventana, color);
                }
                // Diagonal positiva
                if (fila + 3 < t.getMida() && col + 3 < t.getMida()) {
                    int[] ventana = new int[4];
                    for (int i = 0; i < 4; i++) {
                        ventana[i] = t.getColor(fila + i, col + i);
                    }
                    puntuacion += evaluarVentana(ventana, color);
                }
                // Diagonal negativa
                if (fila - 3 >= 0 && col + 3 < t.getMida()) {
                    int[] ventana = new int[4];
                    for (int i = 0; i < 4; i++) {
                        ventana[i] = t.getColor(fila - i, col + i);
                    }
                    puntuacion += evaluarVentana(ventana, color);
                }
            }
        }

        return puntuacion;
    }

    private int evaluarVentana(int[] ventana, int color) {
        int puntuacion = 0;
        int count = 0;
        int countOponente = 0;
        int vacios = 0;

        for (int casilla : ventana) {
            if (casilla == color) {
                count++;
            } else if (casilla == -color) {
                countOponente++;
            } else {
                vacios++;
            }
        }

        if (count == 4) {
            puntuacion += 10000;
        } else if (count == 3 && vacios == 1) {
            puntuacion += 80;
        } else if (count == 2 && vacios == 2) {
            puntuacion += 10;
        }

        if (countOponente == 3 && vacios == 1) {
            puntuacion -= 65; // Penalización por amenaza del oponente
        } else if (countOponente == 2 && vacios == 2) {
            puntuacion -= 5;
        }

        return puntuacion;
    }

    @Override
    public String nom() {
        return "Jugador1";
    }
}
