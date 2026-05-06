package com.puzzle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * IMAGE MANIPULATION + RANDOM ACCESS FILE
 *
 * Responsibilities:
 *  1. Load a source image from disk using ImageIO.
 *  2. Slice it into N×N tile sub-images (BufferedImage.getSubimage).
 *  3. Write every tile's raw pixel bytes into a RandomAccessFile cache.
 *  4. Allow O(1) retrieval of any tile by seeking to (index × tileByteSize).
 */
public class ImageLoader {

    private BufferedImage sourceImage;
    private RandomAccessFile tileCacheFile;   // RANDOM ACCESS FILE — core concept
    private int tileSize;
    private int gridSize;
    private static final String CACHE_PATH = "tile_cache.dat";

    public ImageLoader() {}

    /**
     * Load image from the given path.
     * Falls back to a procedurally generated gradient image if the file is not found.
     */
    public BufferedImage loadImage(String path) throws IOException {
        File f = new File(path);
        if (f.exists()) {
            sourceImage = ImageIO.read(f);
        } else {
            System.out.println("[ImageLoader] File not found — generating demo image.");
            sourceImage = generateDemoImage(480, 480);
        }
        return sourceImage;
    }

    /** Slice the loaded image into an N×N grid of ImageTile objects.
     *  Pixel data is written to the RandomAccessFile cache immediately. */
    public ImageTile[] sliceIntoTiles(int n) throws IOException {
        if (sourceImage == null) throw new IllegalStateException("No image loaded.");
        this.gridSize = n;
        int total     = n * n;
        int imgW      = sourceImage.getWidth();
        int imgH      = sourceImage.getHeight();
        tileSize      = Math.min(imgW, imgH) / n;

        // CONSTRAINT: Ensure tiles fit within the display area (480×480)
        // Tiles must fit: offsetX + n * tileSize ≤ canvasWidth
        // With offsetX=20, canvasWidth=520: tileSize ≤ 500/n
        // Using 480 (BOARD_DISPLAY) for safety: tileSize ≤ 480/n
        int maxTileSize = 480 / n;
        if (tileSize > maxTileSize) {
            System.out.println("[ImageLoader] Constraining tileSize from " + tileSize + 
                             " to " + maxTileSize + " to fit display area");
            tileSize = maxTileSize;
        }

        // Scale source to exact n*tileSize square for clean slicing
        BufferedImage scaled = new BufferedImage(n * tileSize, n * tileSize,
                                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(sourceImage, 0, 0, n * tileSize, n * tileSize, null);
        g2.dispose();

        // Open RandomAccessFile for tile cache
        tileCacheFile = new RandomAccessFile(CACHE_PATH, "rw");
        tileCacheFile.setLength(0); // clear previous cache

        ImageTile[] tiles = new ImageTile[total];
        int bytesPerTile  = tileSize * tileSize * 4; // ARGB = 4 bytes/pixel

        for (int i = 0; i < total; i++) {
            int col = i % n;
            int row = i / n;
            // IMAGE MANIPULATION — getSubimage carves out a tile region
            BufferedImage sub = scaled.getSubimage(col * tileSize, row * tileSize,
                                                    tileSize, tileSize);
            ImageTile tile = new ImageTile(i, i, sub);

            // RANDOM ACCESS FILE — write tile bytes at exact offset
            long offset = (long) i * bytesPerTile;
            tileCacheFile.seek(offset);
            tileCacheFile.write(tile.getPixelData());

            tiles[i] = tile;
        }
        System.out.printf("[ImageLoader] Cached %d tiles (%d bytes each) → %s%n",
                total, bytesPerTile, CACHE_PATH);
        return tiles;
    }

    /**
     * RANDOM ACCESS FILE — read tile pixel bytes at computed offset.
     * No sequential scan; direct seek to position.
     */
    public byte[] readTileAt(int index) throws IOException {
        if (tileCacheFile == null) throw new IllegalStateException("Cache not initialised.");
        int bytesPerTile = tileSize * tileSize * 4;
        long offset      = (long) index * bytesPerTile;
        byte[] data      = new byte[bytesPerTile];
        tileCacheFile.seek(offset);
        tileCacheFile.readFully(data);
        return data;
    }

    /** RANDOM ACCESS FILE — overwrite tile bytes at the tile's offset. */
    public void writeTileAt(int index, byte[] data) throws IOException {
        if (tileCacheFile == null) throw new IllegalStateException("Cache not initialised.");
        int bytesPerTile = tileSize * tileSize * 4;
        long offset      = (long) index * bytesPerTile;
        tileCacheFile.seek(offset);
        tileCacheFile.write(data);
    }

    public void closeCache() {
        try { if (tileCacheFile != null) tileCacheFile.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // ── Procedural demo image ──────────────────────────────────────────────────
    private BufferedImage generateDemoImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 80, 200),
                                              w, h, new Color(200, 50, 120));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        // Grid pattern
        g2.setColor(new Color(255, 255, 255, 60));
        for (int x = 0; x < w; x += 60) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 60) g2.drawLine(0, y, w, y);
        // Central text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "PUZZLE";
        g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, h / 2);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        fm = g2.getFontMetrics();
        String sub = "Shuffle & Solve";
        g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 + 40);
        g2.dispose();
        return img;
    }

    public int getTileSize() { return tileSize; }
    public BufferedImage getSourceImage() { return sourceImage; }
}
