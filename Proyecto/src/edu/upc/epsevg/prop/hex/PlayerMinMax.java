package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.*;

/**
 * Jugador de Hex basado en el algoritmo Minimax con poda alfa-beta.
 * Optimizado con tablas de transposición y heurísticas avanzadas.
 */
public class PlayerMinMax implements IPlayer, IAuto {

    private final String playerName;
    private final PlayerType playerType;
    private final int searchDepth;

    private long nodesEvaluated;
    private int deepestLevelReached;

    // Tabla de transposición para almacenar estados previamente evaluados
    private final Map<Long, TranspositionEntry> transpositionTable;
    private static final int MAX_TT_ENTRIES = 200000;

    // Hash de Zobrist para una rápida identificación de estados
    private final long[][] zobristPlayer1;
    private final long[][] zobristPlayer2;
    private final long zobristPlayer1Turn;
    private final long zobristPlayer2Turn;

    // Heurística de historia para ordenar movimientos
    private final Map<Point, Integer> moveHistory;

    /**
     * Constructor del jugador MinMax.
     *
     * @param name       Nombre del jugador
     * @param type       Tipo de jugador (PLAYER1 o PLAYER2)
     * @param maxDepth   Profundidad máxima de búsqueda
     */
    public PlayerMinMax(String name, PlayerType type, int maxDepth) {
        this.playerName = name;
        this.playerType = type;
        this.searchDepth = Math.min(maxDepth, 14);

        this.transpositionTable = new LinkedHashMap<Long, TranspositionEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, TranspositionEntry> eldest) {
                return size() > MAX_TT_ENTRIES;
            }
        };

        this.moveHistory = new HashMap<>();

        // Inicialización de Zobrist
        Random random = new Random();
        int boardSize = 20; // Suponiendo un tamaño máximo del tablero
        zobristPlayer1 = new long[boardSize][boardSize];
        zobristPlayer2 = new long[boardSize][boardSize];
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                zobristPlayer1[x][y] = random.nextLong();
                zobristPlayer2[x][y] = random.nextLong();
            }
        }
        zobristPlayer1Turn = random.nextLong();
        zobristPlayer2Turn = random.nextLong();
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public PlayerMove move(HexGameStatus gameStatus) {
        this.nodesEvaluated = 0;
        this.deepestLevelReached = 0;
        transpositionTable.clear();
        moveHistory.clear(); // Reiniciar la heurística de historia para cada movimiento

        if (gameStatus.isGameOver()) {
            return new PlayerMove(null, 0, 0, SearchType.MINIMAX);
        }

        boolean isMaximizingPlayer = (playerType == gameStatus.getCurrentPlayer());
        double bestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        Point optimalMove = null;

        List<MoveNode> possibleMoves = gameStatus.getMoves();
        if (possibleMoves.isEmpty()) {
            return new PlayerMove(null, 0, 0, SearchType.MINIMAX);
        }

        // Ordenar movimientos inicialmente usando una evaluación rápida
        possibleMoves.sort((m1, m2) -> {
            double eval1 = quickEvaluation(gameStatus, m1, isMaximizingPlayer);
            double eval2 = quickEvaluation(gameStatus, m2, isMaximizingPlayer);
            int history1 = moveHistory.getOrDefault(m1.getPoint(), 0);
            int history2 = moveHistory.getOrDefault(m2.getPoint(), 0);
            double combined1 = eval1 + 0.001 * history1;
            double combined2 = eval2 + 0.001 * history2;
            return isMaximizingPlayer ? Double.compare(combined2, combined1) : Double.compare(combined1, combined2);
        });

        double currentBest = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MoveNode moveNode : possibleMoves) {
            Point move = moveNode.getPoint();
            HexGameStatus nextState = new HexGameStatus(gameStatus);
            nextState.placeStone(move);

            double moveValue = alphaBeta(nextState, searchDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, !isMaximizingPlayer);

            if (isMaximizingPlayer) {
                if (moveValue > currentBest) {
                    currentBest = moveValue;
                    optimalMove = move;
                }
                bestScore = Math.max(bestScore, currentBest);
            } else {
                if (moveValue < currentBest) {
                    currentBest = moveValue;
                    optimalMove = move;
                }
                bestScore = Math.min(bestScore, currentBest);
            }
        }

        // Actualizar la profundidad máxima alcanzada
        this.deepestLevelReached = searchDepth;

        return new PlayerMove(
            optimalMove,
            (int) bestScore,
            (int) Math.min(nodesEvaluated, Integer.MAX_VALUE),
            SearchType.MINIMAX
        );
    }

    /**
     * Algoritmo Minimax con poda alfa-beta.
     *
     * @param state       Estado actual del juego
     * @param depth       Profundidad restante
     * @param alpha       Valor alfa para poda
     * @param beta        Valor beta para poda
     * @param isMaximizing Indica si es el turno del jugador que maximiza
     * @return Valor evaluado del estado
     */
    private double alphaBeta(HexGameStatus state, int depth, double alpha, double beta, boolean isMaximizing) {
        nodesEvaluated++;

        int currentLevel = searchDepth - depth;
        if (currentLevel > deepestLevelReached) {
            deepestLevelReached = currentLevel;
        }

        long stateHash = generateZobristHash(state);
        TranspositionEntry entry = transpositionTable.get(stateHash);
        if (entry != null && entry.depth >= depth) {
            return entry.value;
        }

        if (depth == 0 || state.isGameOver()) {
            double evaluation = evaluateState(state);
            transpositionTable.put(stateHash, new TranspositionEntry(evaluation, depth));
            return evaluation;
        }

        List<MoveNode> moves = state.getMoves();
        if (moves.isEmpty()) {
            double evaluation = evaluateState(state);
            transpositionTable.put(stateHash, new TranspositionEntry(evaluation, depth));
            return evaluation;
        }

        // Ordenar movimientos usando evaluación rápida y heurística de historia
        moves.sort((m1, m2) -> {
            double eval1 = quickEvaluation(state, m1, isMaximizing);
            double eval2 = quickEvaluation(state, m2, isMaximizing);
            int history1 = moveHistory.getOrDefault(m1.getPoint(), 0);
            int history2 = moveHistory.getOrDefault(m2.getPoint(), 0);
            double combined1 = eval1 + 0.001 * history1;
            double combined2 = eval2 + 0.001 * history2;
            return isMaximizing ? Double.compare(combined2, combined1) : Double.compare(combined1, combined2);
        });

        double bestValue = isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MoveNode moveNode : moves) {
            Point move = moveNode.getPoint();
            HexGameStatus childState = new HexGameStatus(state);
            childState.placeStone(move);

            double childValue = alphaBeta(childState, depth - 1, alpha, beta, !isMaximizing);

            if (isMaximizing) {
                if (childValue > bestValue) {
                    bestValue = childValue;
                    // Actualizar la heurística de historia si se mejora el valor
                    moveHistory.put(move, moveHistory.getOrDefault(move, 0) + 1);
                }
                alpha = Math.max(alpha, bestValue);
                if (alpha >= beta) {
                    // Registrar movimiento que causa la poda
                    moveHistory.put(move, moveHistory.getOrDefault(move, 0) + 1);
                    break;
                }
            } else {
                if (childValue < bestValue) {
                    bestValue = childValue;
                    // Actualizar la heurística de historia si se mejora el valor
                    moveHistory.put(move, moveHistory.getOrDefault(move, 0) + 1);
                }
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) {
                    // Registrar movimiento que causa la poda
                    moveHistory.put(move, moveHistory.getOrDefault(move, 0) + 1);
                    break;
                }
            }
        }

        transpositionTable.put(stateHash, new TranspositionEntry(bestValue, depth));
        return bestValue;
    }

    /**
     * Evaluación estática del estado del juego usando múltiples heurísticas.
     *
     * @param state Estado actual del juego
     * @return Valor evaluado del estado
     */
    private double evaluateState(HexGameStatus state) {
        if (state.isGameOver()) {
            return (state.GetWinner() == playerType) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        int myColor = (playerType == PlayerType.PLAYER1) ? 1 : -1;
        int opponentColor = -myColor;

        int myDistance = computeDistanceToVictory(state, myColor);
        int opponentDistance = computeDistanceToVictory(state, opponentColor);

        double myConnectivity = computeConnectivity(state, myColor);
        double opponentConnectivity = computeConnectivity(state, opponentColor);
        double myCentralControl = computeCentralControl(state, myColor);
        double opponentCentralControl = computeCentralControl(state, opponentColor);
        double myLiberties = computeLiberties(state, myColor);
        double opponentLiberties = computeLiberties(state, opponentColor);

        double distanceFactor = 3.0;
        double connectivityWeight = 0.4;
        double centralControlWeight = 0.2;
        double libertiesWeight = 0.1;

        // Penalización si el oponente está cerca
        double nearOpponentPenalty = 0.0;
        if (opponentDistance <= 3) {
            nearOpponentPenalty = -6000 * (4 - opponentDistance);
        }

        // Evaluación combinada
        return distanceFactor * (opponentDistance - myDistance)
               + connectivityWeight * (opponentConnectivity - myConnectivity)
               + centralControlWeight * (myCentralControl - opponentCentralControl)
               + libertiesWeight * (myLiberties - opponentLiberties)
               + nearOpponentPenalty;
    }

    /**
     * Evaluación rápida para ordenar movimientos sin realizar búsquedas completas.
     *
     * @param state       Estado actual del juego
     * @param moveNode    Movimiento a evaluar
     * @param isMaximizing Indica si es el turno del jugador que maximiza
     * @return Valor evaluado del movimiento
     */
    private double quickEvaluation(HexGameStatus state, MoveNode moveNode, boolean isMaximizing) {
        HexGameStatus tempState = new HexGameStatus(state);
        tempState.placeStone(moveNode.getPoint());

        int myColor = (playerType == PlayerType.PLAYER1) ? 1 : -1;

        double connectivity = computeConnectivity(tempState, myColor);
        double centralControl = computeCentralControl(tempState, myColor);

        // Peso arbitrario para combinar las heurísticas
        return connectivity * 2 + centralControl;
    }

    // ---------------------- Heurísticas ----------------------

    /**
     * Calcula la distancia mínima para que un jugador gane.
     *
     * @param state Estado actual del juego
     * @param color Color del jugador (1 o -1)
     * @return Distancia mínima a la victoria
     */
    private int computeDistanceToVictory(HexGameStatus state, int color) {
        int size = state.getSize();
        int INF = Integer.MAX_VALUE;
        int[][] distance = new int[size][size];
        for (int[] row : distance) {
            Arrays.fill(row, INF);
        }

        Deque<Node> deque = new ArrayDeque<>();

        if (color == 1) {
            // PLAYER1: conectar x=0 con x=n-1
            for (int y = 0; y < size; y++) {
                int cell = state.getPos(0, y);
                if (cell == color) {
                    distance[y][0] = 0;
                    deque.addFirst(new Node(0, y, 0));
                } else if (cell == 0) {
                    distance[y][0] = 1;
                    deque.addLast(new Node(0, y, 1));
                }
            }
        } else {
            // PLAYER2: conectar y=0 con y=n-1
            for (int x = 0; x < size; x++) {
                int cell = state.getPos(x, 0);
                if (cell == color) {
                    distance[0][x] = 0;
                    deque.addFirst(new Node(x, 0, 0));
                } else if (cell == 0) {
                    distance[0][x] = 1;
                    deque.addLast(new Node(x, 0, 1));
                }
            }
        }

        while (!deque.isEmpty()) {
            Node current = deque.pollFirst();
            int x = current.x;
            int y = current.y;
            int cost = current.cost;

            if ((color == 1 && x == size - 1) || (color == -1 && y == size - 1)) {
                return cost;
            }

            for (Point neighbor : state.getNeigh(new Point(x, y))) {
                int nx = neighbor.x;
                int ny = neighbor.y;
                int cell = state.getPos(nx, ny);
                int additionalCost;

                if (cell == color) {
                    additionalCost = 0;
                } else if (cell == 0) {
                    additionalCost = 1;
                } else {
                    continue; // Casilla ocupada por el oponente
                }

                int newCost = cost + additionalCost;
                if (newCost < distance[ny][nx]) {
                    distance[ny][nx] = newCost;
                    if (additionalCost == 0) {
                        deque.addFirst(new Node(nx, ny, newCost));
                    } else {
                        deque.addLast(new Node(nx, ny, newCost));
                    }
                }
            }
        }

        return INF;
    }

    /**
     * Calcula la conectividad de las piezas de un jugador.
     *
     * @param state Estado actual del juego
     * @param color Color del jugador (1 o -1)
     * @return Número de conexiones
     */
    private double computeConnectivity(HexGameStatus state, int color) {
        int size = state.getSize();
        int connections = 0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (state.getPos(x, y) == color) {
                    for (Point neighbor : state.getNeigh(new Point(x, y))) {
                        if (state.getPos(neighbor.x, neighbor.y) == color) {
                            connections++;
                        }
                    }
                }
            }
        }

        return connections / 2.0; // Cada conexión se cuenta dos veces
    }

    /**
     * Calcula el control central de un jugador.
     *
     * @param state Estado actual del juego
     * @param color Color del jugador (1 o -1)
     * @return Valor de control central
     */
    private double computeCentralControl(HexGameStatus state, int color) {
        int size = state.getSize();
        double control = 0.0;
        double center = (size - 1) / 2.0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (state.getPos(x, y) == color) {
                    double distance = Math.sqrt(Math.pow(x - center, 2) + Math.pow(y - center, 2));
                    control += (size / 2.0 - distance);
                }
            }
        }

        return control;
    }

    /**
     * Calcula las libertades (espacios vacíos adyacentes) de un jugador.
     *
     * @param state Estado actual del juego
     * @param color Color del jugador (1 o -1)
     * @return Número de libertades
     */
    private double computeLiberties(HexGameStatus state, int color) {
        int size = state.getSize();
        double liberties = 0.0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (state.getPos(x, y) == color) {
                    for (Point neighbor : state.getNeigh(new Point(x, y))) {
                        if (state.getPos(neighbor.x, neighbor.y) == 0) {
                            liberties++;
                        }
                    }
                }
            }
        }

        return liberties;
    }

    // ---------------------- Zobrist Hashing ----------------------

    /**
     * Genera un hash único para el estado actual del juego usando Zobrist hashing.
     *
     * @param state Estado actual del juego
     * @return Valor hash del estado
     */
    private long generateZobristHash(HexGameStatus state) {
        long hash = 0L;
        int size = state.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int cell = state.getPos(x, y);
                if (cell == 1) {
                    hash ^= zobristPlayer1[x][y];
                } else if (cell == -1) {
                    hash ^= zobristPlayer2[x][y];
                }
            }
        }

        if (state.getCurrentPlayer() == PlayerType.PLAYER1) {
            hash ^= zobristPlayer1Turn;
        } else {
            hash ^= zobristPlayer2Turn;
        }

        return hash;
    }

    @Override
    public void timeout() {
        // Implementación vacía ya que no se utiliza Iterative Deepening en esta versión
    }

    // ---------------------- Clases Internas ----------------------

    /**
     * Entrada en la tabla de transposición.
     */
    private static class TranspositionEntry {
        double value;
        int depth;

        TranspositionEntry(double value, int depth) {
            this.value = value;
            this.depth = depth;
        }
    }

    /**
     * Nodo utilizado en el 0-1 BFS para calcular la distancia a la victoria.
     */
    private static class Node {
        int x, y, cost;

        Node(int x, int y, int cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
    }
}