package com.puzzle;

/**
 * Thrown when the player clicks a tile that cannot move into the blank space.
 */
public class InvalidMoveException extends Exception {
    private final int fromPos; // position of the clicked tile
    private final int toPos;   // position of the blank tile

    public InvalidMoveException(int from, int to) {
        super("Invalid move: tile at position " + from + " cannot move to " + to);
        this.fromPos = from;
        this.toPos = to;
    }

    public int getFromPos() {
        return fromPos;
    }

    public int getToPos() {
        return toPos;
    }
}
