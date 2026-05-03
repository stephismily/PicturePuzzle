package com.puzzle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * IMAGE MANIPULATION — a concrete tile that renders a sub-image slice.
 * Extends the abstract PuzzlePiece.
 */
public class ImageTile extends PuzzlePiece {

    private byte[] pixelData; // raw ARGB pixel bytes — stored / loaded via RandomAccessFile

    public ImageTile(int originalIndex, int currentPosition, BufferedImage img) {
        super(originalIndex, currentPosition, img);
        this.pixelData = extractPixelBytes(img);
    }

    /** Flatten BufferedImage ARGB pixels into a byte array for RandomAccessFile storage. */
    private byte[] extractPixelBytes(BufferedImage img) {
        if (img == null) return new byte[0];
        int w = img.getWidth(), h = img.getHeight();
        byte[] data = new byte[w * h * 4];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                data[idx++] = (byte)((argb >> 24) & 0xFF); // A
                data[idx++] = (byte)((argb >> 16) & 0xFF); // R
                data[idx++] = (byte)((argb >> 8)  & 0xFF); // G
                data[idx++] = (byte)( argb         & 0xFF); // B
            }
        }
        return data;
    }

    /** Rebuild a BufferedImage from raw pixel bytes (used after RAF load). */
    public static BufferedImage bytesToImage(byte[] data, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (data[idx++] & 0xFF);
                int r = (data[idx++] & 0xFF);
                int g = (data[idx++] & 0xFF);
                int b = (data[idx++] & 0xFF);
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    @Override
    public void draw(Graphics g, int x, int y, int tileSize) {
        if (tileImage != null) {
            g.drawImage(tileImage, x, y, tileSize, tileSize, null);
        }
        if (highlighted) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(highlightColor);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
        }
        // Draw tile number (faint)
        g.setColor(new Color(255, 255, 255, 120));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(String.valueOf(originalIndex + 1), x + 6, y + 18);
    }

    @Override
    public int getPosition() { return currentPosition; }

    public byte[] getPixelData() { return pixelData; }
    public void setPixelData(byte[] d) { this.pixelData = d; }
}
