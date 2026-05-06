package com.puzzle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Loads the source image, slices it into tiles, and caches tile bytes.
 */
public class ImageLoader {

    private BufferedImage sourceImage;   // original image loaded from disk
    private RandomAccessFile tileCacheFile; // file used to store tile bytes
    private int tileSize;                // size of each square tile in pixels
    private int gridSize;                // number of tiles per row or column
    private static final String CACHE_PATH = "tile_cache.dat";

    public ImageLoader() {}

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

    public ImageTile[] sliceIntoTiles(int n) throws IOException {
        if (sourceImage == null) throw new IllegalStateException("No image loaded.");
        this.gridSize = n;
        int total = n * n;
        int imgW = sourceImage.getWidth();
        int imgH = sourceImage.getHeight();
        tileSize = Math.min(imgW, imgH) / n; // base tile size from image dimensions

        int maxTileSize = 480 / n; // keep tiles small enough for the board area
        if (tileSize > maxTileSize) {
            System.out.println("[ImageLoader] Constraining tileSize from " + tileSize +
                    " to " + maxTileSize + " to fit display area");
            tileSize = maxTileSize;
        }

        BufferedImage scaled = new BufferedImage(n * tileSize, n * tileSize,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(sourceImage, 0, 0, n * tileSize, n * tileSize, null);
        g2.dispose();

        tileCacheFile = new RandomAccessFile(CACHE_PATH, "rw");
        tileCacheFile.setLength(0);

        ImageTile[] tiles = new ImageTile[total];
        int bytesPerTile = tileSize * tileSize * 4; // 4 bytes per pixel for ARGB

        for (int i = 0; i < total; i++) {
            int col = i % n;
            int row = i / n;
            BufferedImage sub = scaled.getSubimage(col * tileSize, row * tileSize,
                    tileSize, tileSize);
            ImageTile tile = new ImageTile(i, i, sub);

            long offset = (long) i * bytesPerTile;
            tileCacheFile.seek(offset);
            tileCacheFile.write(tile.getPixelData());

            tiles[i] = tile;
        }
        System.out.printf("[ImageLoader] Cached %d tiles (%d bytes each) → %s%n",
                total, bytesPerTile, CACHE_PATH);
        return tiles;
    }

    public byte[] readTileAt(int index) throws IOException {
        if (tileCacheFile == null) throw new IllegalStateException("Cache not initialised.");
        int bytesPerTile = tileSize * tileSize * 4;
        long offset = (long) index * bytesPerTile;
        byte[] data = new byte[bytesPerTile];
        tileCacheFile.seek(offset);
        tileCacheFile.readFully(data);
        return data;
    }

    public void writeTileAt(int index, byte[] data) throws IOException {
        if (tileCacheFile == null) throw new IllegalStateException("Cache not initialised.");
        int bytesPerTile = tileSize * tileSize * 4;
        long offset = (long) index * bytesPerTile;
        tileCacheFile.seek(offset);
        tileCacheFile.write(data);
    }

    public void closeCache() {
        try {
            if (tileCacheFile != null) tileCacheFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage generateDemoImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 80, 200),
                w, h, new Color(200, 50, 120));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 60));
        for (int x = 0; x < w; x += 60) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 60) {
            g2.drawLine(0, y, w, y);
        }

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
