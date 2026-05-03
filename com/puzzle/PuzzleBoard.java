package com.puzzle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Arrays;

/**
 * PUZZLE BOARD — manages the tile grid, shuffle, swap, and win detection.
 *
 * Key data structures:
 *  • ArrayList<PuzzlePiece>   — ordered list of tiles in current board layout
 *  • HashMap<Integer,Integer> — maps originalIndex → currentPosition for O(1) win-check
 */
public class PuzzleBoard {

    private List<PuzzlePiece>    tiles = new ArrayList<>();  // COLLECTIONS
    private Map<Integer,Integer> positionMap = new HashMap<>(); // originalIndex → currentPos
    private int gridSize;
    private int tileSize;
    private int moveCount = 0;
    private int blankIndex = -1; // current position of the blank tile

    public PuzzleBoard() {}

    /** Initialise the board from a freshly sliced tile array. */
    public void init(ImageTile[] imageTiles, int gridSize, int tileSize) {
        this.gridSize  = gridSize;
        this.tileSize  = tileSize;
        this.moveCount = 0;
        tiles.clear();
        positionMap.clear();

        int total = gridSize * gridSize;

        // Add all image tiles
        for (int i = 0; i < total - 1; i++) {
            tiles.add(imageTiles[i]);
            positionMap.put(i, i);
        }
        // Last slot is the blank tile
        BlankTile blank = new BlankTile(total - 1, total - 1);
        tiles.add(blank);
        positionMap.put(total - 1, total - 1);
        blankIndex = total - 1;
    }

    // ── SHUFFLE ───────────────────────────────────────────────────────────────

    /**
     * Fisher-Yates shuffle, repeated until the permutation is solvable.
     * A puzzle is solvable when the inversion count has the right parity.
     */
    public void shuffleTiles() {
        Random rng = new Random();
        int total  = tiles.size();
        do {
            Collections.shuffle(tiles, rng);
        } while (!isSolvable());

        // Rebuild positionMap and blankIndex after shuffle
        for (int pos = 0; pos < total; pos++) {
            PuzzlePiece p = tiles.get(pos);
            p.setCurrentPosition(pos);
            positionMap.put(p.getOriginalIndex(), pos);
            if (p instanceof BlankTile) blankIndex = pos;
        }
        moveCount = 0;
    }

    /** Count inversions to determine solvability. */
    private boolean isSolvable() {
        int n       = tiles.size();
        int[] order = new int[n];
        int blankPos = 0;
        for (int i = 0; i < n; i++) {
            order[i] = tiles.get(i).getOriginalIndex();
            if (tiles.get(i) instanceof BlankTile) blankPos = i;
        }
        int inversions = 0;
        for (int i = 0; i < n - 1; i++)
            for (int j = i + 1; j < n; j++)
                if (order[i] != n - 1 && order[j] != n - 1 && order[i] > order[j])
                    inversions++;

        if (gridSize % 2 == 1) return inversions % 2 == 0;
        int blankRowFromBottom = gridSize - (blankPos / gridSize);
        return (blankRowFromBottom % 2 == 0) == (inversions % 2 == 1);
    }

    // ── SWAP ──────────────────────────────────────────────────────────────────

    /**
     * Attempt to swap the tile at clickedPos with the blank tile.
     * Only adjacent (up/down/left/right) moves are valid.
     * Throws InvalidMoveException if the move is illegal.
     */
    public void swapWithBlank(int clickedPos) throws InvalidMoveException {
        if (!isAdjacentToBlank(clickedPos)) {
            throw new InvalidMoveException(clickedPos, blankIndex);
        }
        // Swap in the ArrayList
        PuzzlePiece clickedTile = tiles.get(clickedPos);
        PuzzlePiece blankTile   = tiles.get(blankIndex);

        tiles.set(clickedPos, blankTile);
        tiles.set(blankIndex, clickedTile);

        // Update currentPosition on each piece
        blankTile.setCurrentPosition(clickedPos);
        clickedTile.setCurrentPosition(blankIndex);

        // Update positionMap
        positionMap.put(blankTile.getOriginalIndex(),   clickedPos);
        positionMap.put(clickedTile.getOriginalIndex(), blankIndex);

        blankIndex = clickedPos;
        moveCount++;
    }

    private boolean isAdjacentToBlank(int pos) {
        int blankRow = blankIndex / gridSize, blankCol = blankIndex % gridSize;
        int posRow   = pos       / gridSize, posCol   = pos       % gridSize;
        return (Math.abs(blankRow - posRow) + Math.abs(blankCol - posCol)) == 1;
    }

    // ── WIN CHECK ─────────────────────────────────────────────────────────────

    /** O(n) win check: every tile must be in its original position. */
    public boolean isSolved() {
        for (PuzzlePiece p : tiles)
            if (!p.isInCorrectPosition()) return false;
        return true;
    }

    // ── RENDER ────────────────────────────────────────────────────────────────

    /** Draw the full board onto the given Graphics context. */
    public void render(Graphics g, int offsetX, int offsetY) {
        int total = gridSize * gridSize;
        for (int pos = 0; pos < total; pos++) {
            int col = pos % gridSize;
            int row = pos / gridSize;
            int x   = offsetX + col * tileSize;
            int y   = offsetY + row * tileSize;
            tiles.get(pos).draw(g, x, y, tileSize);
            // Thin grid line
            g.setColor(new Color(20, 20, 40));
            g.drawRect(x, y, tileSize, tileSize);
        }
    }

    /** Convert pixel (px, py) on the canvas to a board position index. */
    public int pixelToPosition(int px, int py, int offsetX, int offsetY) {
        int col = (px - offsetX) / tileSize;
        int row = (py - offsetY) / tileSize;
        if (col < 0 || col >= gridSize || row < 0 || row >= gridSize) return -1;
        return row * gridSize + col;
    }

    /** Highlight tiles that are already in the correct position. */
    public void showHints() {
        for (PuzzlePiece p : tiles) {
            if (p.isInCorrectPosition()) p.highlight(new Color(0, 220, 100, 180));
            else                         p.clearHighlight();
        }
    }

    public void clearHints() {
        for (PuzzlePiece p : tiles) p.clearHighlight();
    }

    // ── RESTORE STATE ─────────────────────────────────────────────────────────

    /**
     * Rebuild board from a loaded GameState (positions array).
     * tilePositions[originalIndex] = currentPosition
     */
    public void restoreState(GameState state, ImageTile[] imageTiles) {
        int[] pos = state.getTilePositions();
        int total = tiles.size();
        PuzzlePiece[] arranged = new PuzzlePiece[total];

        for (int orig = 0; orig < total; orig++) {
            int cur = pos[orig];
            if (orig == total - 1) {
                BlankTile bt = new BlankTile(orig, cur);
                arranged[cur] = bt;
                blankIndex    = cur;
            } else {
                imageTiles[orig].setCurrentPosition(cur);
                arranged[cur] = imageTiles[orig];
            }
            positionMap.put(orig, cur);
        }
        tiles.clear();
        tiles.addAll(Arrays.asList(arranged));
        moveCount = state.getMoveCount();
    }

    /** Build a GameState snapshot of the current board. */
    public GameState buildState(String imagePath) {
        int total = gridSize * gridSize;
        int[] pos = new int[total];
        for (int orig = 0; orig < total; orig++) pos[orig] = positionMap.get(orig);
        return new GameState(pos, moveCount, 0, imagePath, gridSize);
    }

    // Getters
    public int getMoveCount() { return moveCount; }
    public int getGridSize()  { return gridSize;  }
    public int getTileSize()  { return tileSize;  }
    public int getBoardPixelSize() { return gridSize * tileSize; }
}
