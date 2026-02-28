package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.*;

/**
 * Jugador de Hex basado en el algoritmo Minimax con poda alfa-beta. Ahora
 * incluye Iterative Deepening para gestionar límites de tiempo.
 */
public class PlayerTimeOut implements IPlayer, IAuto {

    private final String playerName;
    private final PlayerType playerType;
    private final int maxSearchDepth;

    private long nodesEvaluated;

    private volatile boolean timeoutFlag;

    // Tabla de transposición
    private final Map<Long, TranspositionEntry> transpositionTable;
    private static final int MAX_TT_ENTRIES = 200000;

    // Constructor
    public PlayerTimeOut(String name, PlayerType type, int maxDepth) {
        this.playerName = name;
        this.playerType = type;
        this.maxSearchDepth = Math.min(maxDepth, 14);
        this.transpositionTable = new LinkedHashMap<Long, TranspositionEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, TranspositionEntry> eldest) {
                return size() > MAX_TT_ENTRIES;
            }
        };
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public PlayerMove move(HexGameStatus gameStatus) {
        this.nodesEvaluated = 0;
        this.transpositionTable.clear();
        this.timeoutFlag = false;

        if (gameStatus.isGameOver()) {
            return new PlayerMove(null, 0, 0, SearchType.MINIMAX);
        }

        boolean isMaximizingPlayer = (playerType == gameStatus.getCurrentPlayer());
        Point bestMove = null;
        double bestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (int depth = 1; depth <= maxSearchDepth; depth++) {
            Point currentBestMove = null;
            double currentBestScore = isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

            List<MoveNode> moves = gameStatus.getMoves();
            for (MoveNode moveNode : moves) {
                Point move = moveNode.getPoint();
                HexGameStatus nextState = new HexGameStatus(gameStatus);
                nextState.placeStone(move);

                double score = alphaBeta(nextState, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, !isMaximizingPlayer);

                if (isMaximizingPlayer && score > currentBestScore) {
                    currentBestScore = score;
                    currentBestMove = move;
                } else if (!isMaximizingPlayer && score < currentBestScore) {
                    currentBestScore = score;
                    currentBestMove = move;
                }

                if (timeoutFlag) {
                    break;
                }
            }

            if (!timeoutFlag) {
                bestMove = currentBestMove;
                bestScore = currentBestScore;
            } else {
                break;
            }
        }

        return new PlayerMove(
                bestMove,
                (int) bestScore,
                (int) Math.min(nodesEvaluated, Integer.MAX_VALUE),
                SearchType.MINIMAX
        );
    }

    private double alphaBeta(HexGameStatus state, int depth, double alpha, double beta, boolean isMaximizing) {
        if (timeoutFlag || depth == 0 || state.isGameOver()) {
            return evaluateState(state);
        }

        List<MoveNode> moves = state.getMoves();
        double bestValue = isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (MoveNode moveNode : moves) {
            Point move = moveNode.getPoint();
            HexGameStatus childState = new HexGameStatus(state);
            childState.placeStone(move);

            double value = alphaBeta(childState, depth - 1, alpha, beta, !isMaximizing);

            if (isMaximizing) {
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, bestValue);
            } else {
                bestValue = Math.min(bestValue, value);
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha) {
                break;
            }

            if (timeoutFlag) {
                break;
            }
        }

        return bestValue;
    }

    @Override
    public void timeout() {
        this.timeoutFlag = true;
    }

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

    private int computeDistanceToVictory(HexGameStatus state, int color) {
        int size = state.getSize();
        int INF = Integer.MAX_VALUE;
        int[][] distance = new int[size][size];
        for (int[] row : distance) {
            Arrays.fill(row, INF);
        }

        Deque<Node> deque = new ArrayDeque<>();

        if (color == 1) {
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
                    continue;
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

        return connections / 2.0;
    }

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

    private static class TranspositionEntry {

        double value;
        int depth;

        TranspositionEntry(double value, int depth) {
            this.value = value;
            this.depth = depth;
        }
    }

    private static class Node {

        int x, y, cost;

        Node(int x, int y, int cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
    }
}
