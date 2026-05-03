package com.puzzle;

/**
 * EXCEPTION HANDLING — thrown when a player attempts an illegal tile swap.
 */
public class InvalidMoveException extends Exception {
    private final int fromPos;
    private final int toPos;

    public InvalidMoveException(int from, int to) {
        super("Invalid move: tile at position " + from + " cannot move to " + to);
        this.fromPos = from;
        this.toPos   = to;
    }

    public int getFromPos() { return fromPos; }
    public int getToPos()   { return toPos;   }
}
