package com.puzzle;

import java.io.Serializable;

/**
 * SERIALIZATION — snapshot of the full puzzle state.
 * Used for binary quick-save (.ser) and CSV text-save (.txt).
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private int[]  tilePositions;  // tilePositions[originalIndex] = currentPosition
    private int    moveCount;
    private long   elapsedSeconds;
    private String imagePath;
    private int    gridSize;

    public GameState(int[] tilePositions, int moveCount, long elapsedSeconds,
                     String imagePath, int gridSize) {
        this.tilePositions  = tilePositions;
        this.moveCount      = moveCount;
        this.elapsedSeconds = elapsedSeconds;
        this.imagePath      = imagePath;
        this.gridSize       = gridSize;
    }

    /** Serialize to a single CSV line for CHARACTER STREAM text save. */
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

    /** Deserialize from the CSV text format. */
    public static GameState fromCSV(String csv) {
        String[] lines = csv.split("\n");
        String imagePath = "";
        int gridSize = 3, moveCount = 0;
        long elapsed = 0;
        int[] positions = new int[0];

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("IMAGE="))     imagePath = line.substring(6);
            else if (line.startsWith("GRID=")) gridSize  = Integer.parseInt(line.substring(5));
            else if (line.startsWith("MOVES="))moveCount = Integer.parseInt(line.substring(6));
            else if (line.startsWith("TIME=")) elapsed   = Long.parseLong(line.substring(5));
            else if (line.startsWith("POSITIONS=")) {
                String[] parts = line.substring(10).split(",");
                positions = new int[parts.length];
                for (int i = 0; i < parts.length; i++)
                    positions[i] = Integer.parseInt(parts[i].trim());
            }
        }
        return new GameState(positions, moveCount, elapsed, imagePath, gridSize);
    }

    public String getDetails() {
        return String.format("Image: %s | Grid: %dx%d | Moves: %d | Time: %ds",
                imagePath, gridSize, gridSize, moveCount, elapsedSeconds);
    }

    // Getters
    public int[]  getTilePositions()  { return tilePositions;  }
    public int    getMoveCount()      { return moveCount;       }
    public long   getElapsedSeconds() { return elapsedSeconds; }
    public String getImagePath()      { return imagePath;       }
    public int    getGridSize()       { return gridSize;        }
}
