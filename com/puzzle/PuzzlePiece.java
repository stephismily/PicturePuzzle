package com.puzzle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ABSTRACT CLASS — defines the blueprint for every tile on the board.
 * Concrete subclasses: ImageTile, BlankTile
 */
public abstract class PuzzlePiece {

    protected int originalIndex;    // correct position (0-based)
    protected int currentPosition;  // where it currently sits on the board
    protected BufferedImage tileImage;
    protected boolean highlighted = false;
    protected Color highlightColor = Color.YELLOW;

    public PuzzlePiece(int originalIndex, int currentPosition, BufferedImage img) {
        this.originalIndex  = originalIndex;
        this.currentPosition = currentPosition;
        this.tileImage       = img;
    }

    /** Draw this tile at pixel co-ordinates (x, y) on the given Graphics context. */
    public abstract void draw(Graphics g, int x, int y, int tileSize);

    /** Return the tile's current board position. */
    public abstract int getPosition();

    public boolean isInCorrectPosition() {
        return originalIndex == currentPosition;
    }

    public void highlight(Color c) {
        this.highlighted   = true;
        this.highlightColor = c;
    }

    public void clearHighlight() {
        this.highlighted = false;
    }

    public int getOriginalIndex()  { return originalIndex; }
    public void setCurrentPosition(int p) { this.currentPosition = p; }
    public BufferedImage getTileImage()   { return tileImage; }
}
