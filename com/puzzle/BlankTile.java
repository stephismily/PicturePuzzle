package com.puzzle;

import java.awt.*;

/**
 * Represents the blank space on the puzzle board.
 */
public class BlankTile extends PuzzlePiece {

    public BlankTile(int originalIndex, int currentPosition) {
        super(originalIndex, currentPosition, null);
    }

    @Override
    public void draw(Graphics g, int x, int y, int tileSize) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(x, y, tileSize, tileSize);
        g.setColor(new Color(60, 60, 90));
        g.drawRect(x, y, tileSize, tileSize);
    }

    @Override
    public int getPosition() {
        return currentPosition;
    }
}
