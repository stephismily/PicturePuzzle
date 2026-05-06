package com.puzzle;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.Timer;

/**
 * Main puzzle app window and game controller.
 */
public class PuzzleApp extends Frame implements ActionListener, MouseListener {

    private Canvas boardCanvas; // area where the puzzle is drawn
    private Button btnLoad, btnShuffle, btnSave, btnLoad2, btnHint, btnNew;
    private Label lblMoves, lblTime, lblBest, lblStatus;
    private Choice choiceGrid;
    private Panel topPanel, bottomPanel, sidePanel;

    private ImageLoader imageLoader = new ImageLoader();
    private PuzzleBoard puzzleBoard = new PuzzleBoard();
    private GameStateManager stateManager = new GameStateManager();
    private ImageTile[] currentTiles;
    private String currentImagePath = ""; // currently loaded image path

    private boolean gameStarted = false; // true while a game is active
    private boolean showingHint = false; // true while hint mode is on
    private long startTime = 0;          // timer start in milliseconds
    private int elapsedSec = 0;          // seconds passed since start
    private Timer timer;

    private static final int BOARD_OFFSET_X = 20; // left margin for board
    private static final int BOARD_OFFSET_Y = 20; // top margin for board
    private static final int BOARD_DISPLAY  = 480; // board width and height

    public PuzzleApp() {
        super("Image Puzzle Shuffle Game");
        buildGUI();
        setVisible(true);
        lblStatus.setText("Click 'Load Image' to start.");
    }

    private void buildGUI() {
        setSize(780, 600);
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(18, 18, 35));

        boardCanvas = new Canvas() {
            @Override public void paint(Graphics g) { paintBoard(g); }
        };
        boardCanvas.setSize(BOARD_DISPLAY + 40, BOARD_DISPLAY + 40);
        boardCanvas.setBackground(new Color(25, 25, 45));
        boardCanvas.addMouseListener(this);
        add(boardCanvas, BorderLayout.CENTER);

        sidePanel = new Panel(new GridLayout(0, 1, 6, 6));
        sidePanel.setBackground(new Color(28, 28, 55));

        Label title = new Label("IMAGE PUZZLE", Label.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(140, 170, 255));
        sidePanel.add(title);

        sidePanel.add(makeLabel("Grid Size:"));
        choiceGrid = new Choice();
        choiceGrid.add("3 x 3");
        choiceGrid.add("4 x 4");
        choiceGrid.add("5 x 5");
        sidePanel.add(choiceGrid);

        btnLoad    = makeButton("Load Image",  new Color(60, 100, 200));
        btnShuffle = makeButton("Shuffle",     new Color(60, 140, 80));
        btnNew     = makeButton("New Game",    new Color(120, 80, 180));
        btnHint    = makeButton("Show Hints",  new Color(180, 140, 30));
        btnSave    = makeButton("Save Game",   new Color(160, 80, 60));
        btnLoad2   = makeButton("Load Game",   new Color(60, 130, 150));

        sidePanel.add(btnLoad);
        sidePanel.add(btnShuffle);
        sidePanel.add(btnNew);
        sidePanel.add(btnHint);
        sidePanel.add(btnSave);
        sidePanel.add(btnLoad2);

        sidePanel.add(makeLabel("─────────────"));
        lblMoves  = makeLabel("Moves: 0");
        lblTime   = makeLabel("Time:  0s");
        lblBest   = makeLabel("Best:  —");
        lblStatus = makeLabel("Ready");
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 11));
        lblStatus.setForeground(new Color(180, 220, 180));
        sidePanel.add(lblMoves);
        sidePanel.add(lblTime);
        sidePanel.add(lblBest);
        sidePanel.add(lblStatus);

        add(sidePanel, BorderLayout.EAST);

        timer = new Timer(1000, e -> {
            if (gameStarted) {
                elapsedSec = (int) ((System.currentTimeMillis() - startTime) / 1000);
                lblTime.setText("Time: " + elapsedSec + "s");
            }
        });
        timer.start();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });

        setLocationRelativeTo(null);
    }

    private void paintBoard(Graphics g) {
        g.setColor(new Color(25, 25, 45));
        g.fillRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        if (!gameStarted) {
            g.setColor(new Color(100, 120, 200));
            g.setFont(new Font("Arial", Font.BOLD, 22));
            String msg = "Load an image to begin!";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, (boardCanvas.getWidth() - fm.stringWidth(msg)) / 2,
                          boardCanvas.getHeight() / 2);
            return;
        }

        puzzleBoard.render(g, BOARD_OFFSET_X, BOARD_OFFSET_Y);

        g.setColor(new Color(200, 200, 255, 180));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Moves: " + puzzleBoard.getMoveCount(), BOARD_OFFSET_X + 4,
                      BOARD_OFFSET_X + puzzleBoard.getBoardPixelSize() + 16);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if      (src == btnLoad)    onLoadImage();
        else if (src == btnShuffle) onShuffle();
        else if (src == btnNew)     onNewGame();
        else if (src == btnHint)    onHint();
        else if (src == btnSave)    onSave();
        else if (src == btnLoad2)   onLoadSave();
    }

    private void onLoadImage() {
        FileDialog fd = new FileDialog(this, "Select an Image", FileDialog.LOAD);
        fd.setFile("*.jpg;*.jpeg;*.png;*.bmp");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (dir == null || file == null) return;
        currentImagePath = dir + file;
        startGame(currentImagePath);
    }

    private void startGame(String path) {
        int n = choiceGrid.getSelectedIndex() + 3; // grid size 3, 4, or 5
        try {
            imageLoader.closeCache();
            imageLoader.loadImage(path);
            currentTiles = imageLoader.sliceIntoTiles(n);
            puzzleBoard.init(currentTiles, n, imageLoader.getTileSize());
            puzzleBoard.shuffleTiles();
            gameStarted = true;
            startTime   = System.currentTimeMillis();
            elapsedSec  = 0;
            updateBestLabel();
            setStatus("Game started! " + n + "×" + n + " grid.");
            boardCanvas.repaint();
        } catch (IOException ex) {
            setStatus("Error: " + ex.getMessage());
        }
    }

    private void onShuffle() {
        if (!gameStarted) { setStatus("Load an image first!"); return; }
        puzzleBoard.shuffleTiles();
        puzzleBoard.clearHints();
        showingHint = false;
        startTime = System.currentTimeMillis();
        elapsedSec = 0;
        btnHint.setLabel("Show Hints");
        setStatus("Shuffled! Solve it.");
        boardCanvas.repaint();
    }

    private void onNewGame() {
        if (currentImagePath.isEmpty()) { onLoadImage(); return; }
        startGame(currentImagePath);
    }

    private void onHint() {
        if (!gameStarted) return;
        showingHint = !showingHint;
        if (showingHint) {
            puzzleBoard.showHints();
            btnHint.setLabel("Hide Hints");
        } else {
            puzzleBoard.clearHints();
            btnHint.setLabel("Show Hints");
        }
        boardCanvas.repaint();
    }

    private void onSave() {
        if (!gameStarted) { setStatus("Nothing to save!"); return; }
        GameState gs = puzzleBoard.buildState(currentImagePath);
        stateManager.saveGame(gs);
        stateManager.quickSave(gs);
        setStatus("Game saved!");
    }

    private void onLoadSave() {
        GameState gs = stateManager.loadGame();
        if (gs == null) { setStatus("No save file found."); return; }
        currentImagePath = gs.getImagePath();
        int n = gs.getGridSize();
        try {
            imageLoader.closeCache();
            imageLoader.loadImage(currentImagePath);
            currentTiles = imageLoader.sliceIntoTiles(n);
            puzzleBoard.init(currentTiles, n, imageLoader.getTileSize());
            puzzleBoard.restoreState(gs, currentTiles);
            gameStarted = true;
            elapsedSec  = (int) gs.getElapsedSeconds();
            startTime   = System.currentTimeMillis() - elapsedSec * 1000L;
            setStatus("Saved game restored!");
            boardCanvas.repaint();
        } catch (IOException ex) {
            setStatus("Load error: " + ex.getMessage());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameStarted) return;
        int pos = puzzleBoard.pixelToPosition(e.getX(), e.getY(), BOARD_OFFSET_X, BOARD_OFFSET_Y);
        if (pos < 0) return;
        try {
            puzzleBoard.swapWithBlank(pos);
            lblMoves.setText("Moves: " + puzzleBoard.getMoveCount());
            if (showingHint) puzzleBoard.showHints();
            boardCanvas.repaint();
            if (puzzleBoard.isSolved()) onWin();
        } catch (InvalidMoveException ex) {
            setStatus("Invalid move — click a tile adjacent to the blank.");
        }
    }

    private void onWin() {
        gameStarted = false;
        int moves = puzzleBoard.getMoveCount();
        int best  = stateManager.readBestScore();
        String msg = "Congratulations!\nSolved in " + moves + " moves and " + elapsedSec + "s!";
        if (moves < best) {
            stateManager.saveBestScore(moves);
            msg += "\nNew personal best!";
        }
        updateBestLabel();
        stateManager.deleteSave();
        Dialog d = new Dialog(this, "Puzzle Solved!", true);
        d.setLayout(new FlowLayout());
        d.setSize(320, 140);
        d.setLocationRelativeTo(this);
        Label lbl = new Label(msg.replace("\n", " | "), Label.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        Button ok = new Button("   OK   ");
        ok.addActionListener(ev -> d.dispose());
        d.add(lbl);
        d.add(ok);
        d.setVisible(true);
        boardCanvas.repaint();
    }

    private void onClose() {
        timer.stop();
        imageLoader.closeCache();
        dispose();
        System.exit(0);
    }

    private void updateBestLabel() {
        int b = stateManager.readBestScore();
        lblBest.setText("Best: " + (b == Integer.MAX_VALUE ? "—" : b + " moves"));
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
        System.out.println("[Status] " + msg);
    }

    private Button makeButton(String label, Color bg) {
        Button b = new Button(label);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.addActionListener(this);
        return b;
    }

    private Label makeLabel(String text) {
        Label l = new Label(text, Label.CENTER);
        l.setForeground(new Color(200, 210, 255));
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        new PuzzleApp();
    }
}
