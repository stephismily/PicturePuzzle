package com.puzzle;

import java.io.Serializable;

/**
 * Holds the saved puzzle state for load/save operations.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private int[] tilePositions;  // original tile index → current board position
    private int moveCount;        // how many moves were made so far
    private long elapsedSeconds;  // elapsed time when the state was saved
    private String imagePath;     // path to the source image file
    private int gridSize;        // size of the board grid

    public GameState(int[] tilePositions, int moveCount, long elapsedSeconds,
                     String imagePath, int gridSize) {
        this.tilePositions = tilePositions;
        this.moveCount = moveCount;
        this.elapsedSeconds = elapsedSeconds;
        this.imagePath = imagePath;
        this.gridSize = gridSize;
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("IMAGE=").append(imagePath).append("\n");
        sb.append("GRID=").append(gridSize).append("\n");
        sb.append("MOVES=").append(moveCount).append("\n");
        sb.append("TIME=").append(elapsedSeconds).append("\n");
        sb.append("POSITIONS=");
        for (int i = 0; i < tilePositions.length; i++) {
            sb.append(tilePositions[i]);
            if (i < tilePositions.length - 1) sb.append(",");
        }
        sb.append("\n");
        return sb.toString();
    }

    public static GameState fromCSV(String csv) {
        String[] lines = csv.split("\n");
        String imagePath = "";
        int gridSize = 3;
        int moveCount = 0;
        long elapsed = 0;
        int[] positions = new int[0];

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("IMAGE=")) {
                imagePath = line.substring(6);
            } else if (line.startsWith("GRID=")) {
                gridSize = Integer.parseInt(line.substring(5));
            } else if (line.startsWith("MOVES=")) {
                moveCount = Integer.parseInt(line.substring(6));
            } else if (line.startsWith("TIME=")) {
                elapsed = Long.parseLong(line.substring(5));
            } else if (line.startsWith("POSITIONS=")) {
                String[] parts = line.substring(10).split(",");
                positions = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    positions[i] = Integer.parseInt(parts[i].trim());
                }
            }
        }
        return new GameState(positions, moveCount, elapsed, imagePath, gridSize);
    }

    public String getDetails() {
        return String.format("Image: %s | Grid: %dx%d | Moves: %d | Time: %ds",
                imagePath, gridSize, gridSize, moveCount, elapsedSeconds);
    }

    public int[] getTilePositions() { return tilePositions; }
    public int getMoveCount() { return moveCount; }
    public long getElapsedSeconds() { return elapsedSeconds; }
    public String getImagePath() { return imagePath; }
    public int getGridSize() { return gridSize; }
}
