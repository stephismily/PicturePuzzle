package com.puzzle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Base tile class shared by image tiles and the blank tile.
 */
public abstract class PuzzlePiece {

    protected int originalIndex;    // where this tile belongs when the puzzle is solved
    protected int currentPosition;  // where this tile is right now on the board
    protected BufferedImage tileImage; // image shown for this tile
    protected boolean highlighted = false; // true when the tile should show a hint border
    protected Color highlightColor = Color.YELLOW; // color used to draw the hint border

    public PuzzlePiece(int originalIndex, int currentPosition, BufferedImage img) {
        this.originalIndex = originalIndex;
        this.currentPosition = currentPosition;
        this.tileImage = img;
    }

    public abstract void draw(Graphics g, int x, int y, int tileSize);

    public abstract int getPosition();

    public boolean isInCorrectPosition() {
        return originalIndex == currentPosition;
    }

    public void highlight(Color c) {
        this.highlighted = true;
        this.highlightColor = c;
    }

    public void clearHighlight() {
        this.highlighted = false;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public void setCurrentPosition(int p) {
        this.currentPosition = p;
    }

    public BufferedImage getTileImage() {
        return tileImage;
    }
}
