package com.puzzle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CHARACTER STREAMS — saves and loads the full game state as human-readable text.
 *
 * Uses:
 *  • FileWriter + BufferedWriter  → write game state to puzzle_save.txt
 *  • FileReader  + BufferedReader → read  game state from puzzle_save.txt
 *  • ObjectOutputStream           → binary quick-save (Serialization)
 *  • ObjectInputStream            → binary quick-load (Serialization)
 */
public class GameStateManager {

    private static final String TEXT_SAVE  = "puzzle_save.txt";
    private static final String BIN_SAVE   = "puzzle_save.ser";
    private static final String SCORE_FILE = "best_score.txt";

    // ── TEXT SAVE (Character Streams) ─────────────────────────────────────────

    /**
     * Write game state to a plain-text file using BufferedWriter.
     * CHARACTER STREAM: FileWriter wraps the file; BufferedWriter buffers writes.
     */
    public void saveGame(GameState state) {
        try (FileWriter fw = new FileWriter(TEXT_SAVE);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("# Image Puzzle Shuffle Game — Save File");
            bw.newLine();
            bw.write(state.toCSV());
            bw.flush();
            System.out.println("[GameStateManager] Game saved to " + TEXT_SAVE);

        } catch (IOException e) {
            System.err.println("[GameStateManager] Save failed: " + e.getMessage());
        }
    }

    /**
     * Read game state from the plain-text file using BufferedReader.
     * CHARACTER STREAM: FileReader wraps the file; BufferedReader.readLine() reads line-by-line.
     */
    public GameState loadGame() {
        File f = new File(TEXT_SAVE);
        if (!f.exists()) {
            System.out.println("[GameStateManager] No save file found.");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(TEXT_SAVE);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {   // skip comments
                    sb.append(line).append("\n");
                }
            }
            System.out.println("[GameStateManager] Game loaded from " + TEXT_SAVE);
            return GameState.fromCSV(sb.toString());

        } catch (IOException e) {
            System.err.println("[GameStateManager] Load failed: " + e.getMessage());
            return null;
        }
    }

    // ── BINARY QUICK-SAVE (Serialization) ─────────────────────────────────────

    public void quickSave(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(BIN_SAVE))) {
            oos.writeObject(state);
            System.out.println("[GameStateManager] Quick-save written to " + BIN_SAVE);
        } catch (IOException e) {
            System.err.println("[GameStateManager] Quick-save failed: " + e.getMessage());
        }
    }

    public GameState quickLoad() {
        File f = new File(BIN_SAVE);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(BIN_SAVE))) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[GameStateManager] Quick-load failed: " + e.getMessage());
            return null;
        }
    }

    // ── BEST SCORE (Character Streams) ────────────────────────────────────────

    /** Save personal best move count using character-stream FileWriter. */
    public void saveBestScore(int moves) {
        try (FileWriter fw = new FileWriter(SCORE_FILE);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.valueOf(moves));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[GameStateManager] Score save failed: " + e.getMessage());
        }
    }

    /** Read personal best move count using character-stream FileReader. */
    public int readBestScore() {
        File f = new File(SCORE_FILE);
        if (!f.exists()) return Integer.MAX_VALUE;
        try (FileReader fr = new FileReader(SCORE_FILE);
             BufferedReader br = new BufferedReader(fr)) {
            String line = br.readLine();
            if (line != null && !line.isBlank())
                return Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            System.err.println("[GameStateManager] Score read failed: " + e.getMessage());
        }
        return Integer.MAX_VALUE;
    }

    public void deleteSave() {
        new File(TEXT_SAVE).delete();
        new File(BIN_SAVE).delete();
    }
}
