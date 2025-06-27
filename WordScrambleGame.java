import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WordScrambleGame extends JFrame {
    // Constants
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final String GAME_TITLE = "Word Scramble";
    public static final Color BG_COLOR_1 = new Color(68, 36, 102); // Dark Purple
    public static final Color BG_COLOR_2 = new Color(88, 46, 122); // Medium Purple
    public static final Color GRID_COLOR = new Color(110, 80, 150); // Light Purple for grid
    public static final Color TILE_COLOR = new Color(241, 196, 15); // Yellow for tiles
    public static final Color BUTTON_COLOR = new Color(46, 204, 113); // Green for buttons
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 48);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font GAME_FONT = new Font("Arial", Font.BOLD, 16);
    public static final int MAX_ROUNDS = 3;
    public static final int PADDING = 20; // Standard padding

    // Game state variables
    public int currentRound = 1;
    public int score = 0;
    public int timeRemaining = 180; // 5 minutes in seconds
    public boolean isPaused = false;
    public boolean isSoundOn = true;
    public boolean isMusicOn = true;
    public String[] currentWordSet = new String[6]; // 3 three-letter, 2 four-letter, 1 five-letter
    public boolean[] wordsFound = new boolean[6];
    public String scrambledLetters = "";
    public Timer gameTimer;
    public int healthPercentage = 100;
    public String currentInputWord = "";

    // UI Components
    public CardLayout cardLayout;
    public JPanel mainPanel;
    public JPanel gamePanel;
    public JPanel threeLetterGrid;
    public JPanel fourLetterGrid;
    public JPanel fiveLetterGrid;
    public JLabel[][] threeLetterLabels = new JLabel[3][3];
    public JLabel[][] fourLetterLabels = new JLabel[2][4];
    public JLabel[] fiveLetterLabels = new JLabel[5];
    public JLabel timeLabel;
    public JLabel scoreLabel;
    public JLabel roundLabel;
    public JPanel healthBarPanel;
    public JPanel letterTilesPanel;
    public JPanel wordInputPanel; // New word input box
    public JButton shuffleButton;
    public JButton clearButton;
    public JButton hintButton;
    public JPanel loadingPanel;
    public JProgressBar loadingBar;
    public JLabel loadingPercentLabel;
    public JLabel loadingLetterLabel;

    // Animation variables
    public int loadingProgress = 0;
    public Timer loadingTimer;
    public String[] loadingLetters = {"W", "O", "R", "D", "S"};
    public int currentLoadingLetter = 0;
    public Timer tileAnimationTimer;
    public Timer bubbleAnimationTimer;
    public List<Bubble> bubbles = new ArrayList<>();

    // Word sets data
    public List<WordSet> allWordSets = new ArrayList<>();

    // Files for word storage
    public static final String WORD_SETS_FILE = "word_sets.txt";

    // Word set class
    public class WordSet {
        public String letters;
        public String[] words = new String[6]; // 3 three-letter, 2 four-letter, 1 five-letter

        public WordSet(String letters, String[] words) {
            this.letters = letters;
            this.words = words;
        }
    }

    // Bubble class for background animation
    public class Bubble {
        public int x, y;
        public int size;
        public float speed;
        public Color color;

        public Bubble() {
            x = (int)(Math.random() * WINDOW_WIDTH);
            y = WINDOW_HEIGHT + (int)(Math.random() * 100);
            size = 10 + (int)(Math.random() * 30);
            speed = 0.5f + (float)(Math.random() * 1.5f);

            // Create a semi-transparent purple color
            int alpha = 50 + (int)(Math.random() * 100);
            color = new Color(150, 100, 200, alpha);
        }

        public void move() {
            y -= speed;
            if (y < -size) {
                y = WINDOW_HEIGHT + size;
                x = (int)(Math.random() * WINDOW_WIDTH);
            }
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval(x, y, size, size);
        }
    }

    public WordScrambleGame() {
        setTitle(GAME_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Initialize bubbles for background animation
        initializeBubbles();

        // Initialize UI
        initializeUI();

        // Load word sets
        loadWordSets();

        // Show start screen
        showStartScreen();
    }

    public void initializeBubbles() {
        // Create bubbles for background animation
        for (int i = 0; i < 30; i++) {
            bubbles.add(new Bubble());
        }

        // Start bubble animation
        bubbleAnimationTimer = new Timer();
        bubbleAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Bubble bubble : bubbles) {
                    bubble.move();
                }

                if (mainPanel != null) {
                    mainPanel.repaint();
                }
            }
        }, 0, 50);
    }

    public void initializeUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_COLOR_1,
                        0, getHeight(), BG_COLOR_2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw bubbles
                for (Bubble bubble : bubbles) {
                    bubble.draw(g2d);
                }
            }
        };
        mainPanel.setBackground(BG_COLOR_1);
        setContentPane(mainPanel);

        // Create all game screens
        createStartScreen();
        createLoadingScreen();
        createGameScreen();
        createPauseScreen();
        createMenuScreen();
        createRoundCompleteScreen();
        createGameOverScreen();
        createWordsFoundScreen();
    }

    public void createStartScreen() {
        JPanel startPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        startPanel.setOpaque(false);
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Game title with animated letter tiles
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("WORD");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // Create letter tiles for "SCRAMBLE"
        String[] letters = {"S", "C", "R", "A", "M", "B", "L", "E"};
        JPanel letterTilesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        letterTilesPanel.setOpaque(false);

        for (String letter : letters) {
            JLabel letterTile = createLetterTile(letter);
            letterTilesPanel.add(letterTile);
        }

        // Start tile animation
        startTileAnimation(letterTilesPanel);

        // Play button
        JButton playButton = createRoundedButton("Play");
        playButton.setPreferredSize(new Dimension(200, 60));
        playButton.addActionListener(e -> startLoading());

        // Add components to the start panel with proper spacing
        startPanel.add(Box.createVerticalGlue());
        startPanel.add(titlePanel);
        startPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        startPanel.add(letterTilesPanel);
        startPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playButton);
        startPanel.add(buttonPanel);
        startPanel.add(Box.createVerticalGlue());

        mainPanel.add(startPanel, "start");
    }

    public JLabel createLetterTile(String letter) {
        JLabel tile = new JLabel(letter);
        tile.setPreferredSize(new Dimension(50, 50));
        tile.setHorizontalAlignment(SwingConstants.CENTER);
        tile.setVerticalAlignment(SwingConstants.CENTER);
        tile.setFont(new Font("Arial", Font.BOLD, 24));
        tile.setForeground(new Color(50, 50, 50));
        tile.setOpaque(true);
        tile.setBackground(TILE_COLOR);

        // Create rounded border with shadow effect
        tile.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return tile;
    }

    public void startTileAnimation(JPanel tilesPanel) {
        if (tileAnimationTimer != null) {
            tileAnimationTimer.cancel();
        }

        tileAnimationTimer = new Timer();
        tileAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            public int direction = 1;
            public int count = 0;

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    Component[] components = tilesPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        if (components[i] instanceof JLabel) {
                            JLabel tile = (JLabel) components[i];

                            // Create a slight bouncing effect
                            int offset = (int)(Math.sin((count + i * 2) * 0.2) * 3);
                            tile.setBorder(BorderFactory.createCompoundBorder(
                                    new SoftBevelBorder(SoftBevelBorder.RAISED),
                                    BorderFactory.createEmptyBorder(5 + offset, 5, 5 - offset, 5)
                            ));
                        }
                    }
                    count++;
                });
            }
        }, 0, 50);
    }

    public void createLoadingScreen() {
        loadingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Loading text
        loadingPercentLabel = new JLabel("Loading 0%");
        loadingPercentLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loadingPercentLabel.setForeground(Color.WHITE);
        loadingPercentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Loading letter tile
        loadingLetterLabel = new JLabel(loadingLetters[0]);
        loadingLetterLabel.setPreferredSize(new Dimension(60, 60));
        loadingLetterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLetterLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadingLetterLabel.setFont(new Font("Arial", Font.BOLD, 30));
        loadingLetterLabel.setForeground(new Color(50, 50, 50));
        loadingLetterLabel.setOpaque(true);
        loadingLetterLabel.setBackground(TILE_COLOR);
        loadingLetterLabel.setBorder(BorderFactory.createCompoundBorder(
                new SoftBevelBorder(SoftBevelBorder.RAISED),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        loadingLetterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Loading progress bar
        loadingBar = new JProgressBar(0, 100);
        loadingBar.setValue(0);
        loadingBar.setStringPainted(false);
        loadingBar.setPreferredSize(new Dimension(400, 20));
        loadingBar.setBackground(new Color(100, 100, 150));
        loadingBar.setForeground(TILE_COLOR);
        loadingBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to the loading panel with proper spacing
        loadingPanel.add(Box.createVerticalGlue());
        loadingPanel.add(loadingPercentLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel tilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tilePanel.setOpaque(false);
        tilePanel.add(loadingLetterLabel);
        loadingPanel.add(tilePanel);

        loadingPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel barPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        barPanel.setOpaque(false);
        barPanel.add(loadingBar);
        loadingPanel.add(barPanel);

        loadingPanel.add(Box.createVerticalGlue());

        mainPanel.add(loadingPanel, "loading");
    }

    public void createGameScreen() {
        gamePanel = new JPanel(new BorderLayout(PADDING, PADDING)) {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        gamePanel.setOpaque(false);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Reduced padding

        // Top panel with game info
        JPanel topPanel = new JPanel(new BorderLayout(PADDING, 0));
        topPanel.setOpaque(false);

        // Menu button
        JButton menuButton = createRoundedButton("Menu");
        menuButton.setPreferredSize(new Dimension(120, 40));
        menuButton.addActionListener(e -> showMenu());

        // Game info panel
        JPanel gameInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        gameInfoPanel.setOpaque(false);

        roundLabel = new JLabel("Round: 1");
        roundLabel.setFont(GAME_FONT);
        roundLabel.setForeground(Color.WHITE);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(GAME_FONT);
        scoreLabel.setForeground(Color.WHITE);

        timeLabel = new JLabel("Time: 03:00");
        timeLabel.setFont(GAME_FONT);
        timeLabel.setForeground(Color.WHITE);

        gameInfoPanel.add(roundLabel);
        gameInfoPanel.add(scoreLabel);
        gameInfoPanel.add(timeLabel);

        // Pause button - FIXED: Using simple text instead of Unicode
        JButton pauseButton = createIconButton("||");
        pauseButton.setFont(new Font("Arial", Font.BOLD, 16));
        pauseButton.setPreferredSize(new Dimension(40, 40));
        pauseButton.addActionListener(e -> pauseGame());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(menuButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(pauseButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(gameInfoPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Center panel with grids
        JPanel centerPanel = new JPanel(new BorderLayout(0, 5)); // Further reduced spacing
        centerPanel.setOpaque(false);

        // Create the main grid panel with proper layout
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // 3-letter words grid (3x3) - Left side
        threeLetterGrid = new JPanel(new GridLayout(3, 3, 2, 2)); // Reduced gap
        threeLetterGrid.setOpaque(false);
        threeLetterGrid.setPreferredSize(new Dimension(156, 156)); // Further reduced

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                threeLetterLabels[i][j] = createGridCell();
                threeLetterGrid.add(threeLetterLabels[i][j]);
            }
        }

        // 4-letter words grid (2x4) - Right side
        fourLetterGrid = new JPanel(new GridLayout(2, 4, 2, 2)); // Reduced gap
        fourLetterGrid.setOpaque(false);
        fourLetterGrid.setPreferredSize(new Dimension(208, 104)); // Further reduced

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                fourLetterLabels[i][j] = createGridCell();
                fourLetterGrid.add(fourLetterLabels[i][j]);
            }
        }

        // 5-letter word grid (1x5) - Bottom
        fiveLetterGrid = new JPanel(new GridLayout(1, 5, 2, 2)); // Reduced gap
        fiveLetterGrid.setOpaque(false);
        fiveLetterGrid.setPreferredSize(new Dimension(260, 52)); // Further reduced

        for (int i = 0; i < 5; i++) {
            fiveLetterLabels[i] = createGridCell();
            fiveLetterGrid.add(fiveLetterLabels[i]);
        }

        // Add grids with proper positioning and spacing
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8); // Further reduced spacing
        gbc.anchor = GridBagConstraints.CENTER;
        gridPanel.add(threeLetterGrid, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gridPanel.add(fourLetterGrid, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 8, 5, 8); // Minimal margin to save space
        gbc.anchor = GridBagConstraints.CENTER;
        gridPanel.add(fiveLetterGrid, gbc);

        // Health/hint bar panel (MADE SLIMMER)
        healthBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the purple bar background
                g2d.setColor(new Color(80, 50, 120));
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                        0, 0, getWidth() - 50, getHeight(), 15, 15);
                g2d.fill(roundedRect);

                // Draw the progress sections
                int sections = 4;
                int sectionWidth = (getWidth() - 50) / sections;

                for (int i = 0; i < sections; i++) {
                    if (i < (healthPercentage * sections / 100)) {
                        g2d.setColor(new Color(120, 80, 160));
                    } else {
                        g2d.setColor(new Color(80, 50, 120));
                    }

                    if (i < sections - 1) {
                        g2d.fillRect(i * sectionWidth, 0, sectionWidth, getHeight());
                        g2d.setColor(new Color(100, 65, 140));
                        g2d.fillRect(i * sectionWidth + sectionWidth - 1, 0, 2, getHeight());
                    } else {
                        RoundRectangle2D rightSection = new RoundRectangle2D.Float(
                                i * sectionWidth, 0, sectionWidth, getHeight(), 15, 15);
                        g2d.fill(rightSection);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 5); // MADE EVEN SLIMMER
            }
        };
        healthBarPanel.setOpaque(false);

        // Hint button
        hintButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(80, 50, 120));
                g2d.fillOval(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(241, 196, 15));
                int iconSize = getWidth() / 2;
                int x = (getWidth() - iconSize) / 2;
                int y = (getHeight() - iconSize) / 2;

                g2d.fillOval(x, y, iconSize, iconSize);
                g2d.fillRect(x + iconSize/3, y + iconSize, iconSize/3, iconSize/4);

                g2d.dispose();
            }
        };
        hintButton.setPreferredSize(new Dimension(40, 40));
        hintButton.setBorderPainted(false);
        hintButton.setContentAreaFilled(false);
        hintButton.setFocusPainted(false);
        hintButton.addActionListener(e -> showHint());

        JPanel healthPanel = new JPanel(new BorderLayout(PADDING, 0));
        healthPanel.setOpaque(false);
        healthPanel.add(healthBarPanel, BorderLayout.CENTER);
        healthPanel.add(hintButton, BorderLayout.EAST);

        // Create word input panel (NEW)
        wordInputPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded rectangle background
                g2d.setColor(new Color(80, 50, 120));
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 15, 15);
                g2d.fill(roundedRect);
            }
        };
        wordInputPanel.setPreferredSize(new Dimension(300, 35)); // Made slightly smaller
        wordInputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3)); // Reduced vertical padding
        wordInputPanel.setOpaque(false);

        // Add word input panel between health bar and grids
        JPanel healthAndInputPanel = new JPanel(new BorderLayout(0, 3)); // Reduced spacing
        healthAndInputPanel.setOpaque(false);
        healthAndInputPanel.add(healthPanel, BorderLayout.NORTH);
        healthAndInputPanel.add(wordInputPanel, BorderLayout.SOUTH);

        centerPanel.add(gridPanel, BorderLayout.CENTER);
        centerPanel.add(healthAndInputPanel, BorderLayout.SOUTH);

        // Bottom panel with letter tiles and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(0, PADDING));
        bottomPanel.setOpaque(false);

        // Action buttons (REMOVED ENTER BUTTON)
        clearButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(BUTTON_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                int padding = getWidth() / 4;
                g2d.drawLine(padding, padding, getWidth() - padding, getHeight() - padding);
                g2d.drawLine(getWidth() - padding, padding, padding, getHeight() - padding);

                g2d.dispose();
            }
        };
        clearButton.setPreferredSize(new Dimension(50, 40));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearCurrentWord());

        shuffleButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(BUTTON_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(getWidth(), getHeight()) / 3;

                g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 45, 270);

                int arrowSize = radius / 2;
                g2d.drawLine(centerX, centerY - radius, centerX - arrowSize, centerY - radius - arrowSize/2);
                g2d.drawLine(centerX, centerY - radius, centerX - arrowSize, centerY - radius + arrowSize/2);

                g2d.dispose();
            }
        };
        shuffleButton.setPreferredSize(new Dimension(50, 40));
        shuffleButton.setBorderPainted(false);
        shuffleButton.setContentAreaFilled(false);
        shuffleButton.setFocusPainted(false);
        shuffleButton.addActionListener(e -> shuffleLetters());

        // Letter tiles panel
        letterTilesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        letterTilesPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(clearButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(shuffleButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(letterTilesPanel, BorderLayout.CENTER);

        // Add all panels to the game panel
        gamePanel.add(topPanel, BorderLayout.NORTH);
        gamePanel.add(centerPanel, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, "game");
    }

    public JLabel createGridCell() {
        JLabel cell = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded rectangle for the cell
                g2d.setColor(GRID_COLOR);
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 15, 15);
                g2d.fill(roundedRect);

                // If the cell has a letter, draw it with a different background
                if (isOpaque() && getBackground() == TILE_COLOR) {
                    g2d.setColor(TILE_COLOR);
                    g2d.fill(roundedRect);
                }

                super.paintComponent(g);
            }
        };
        cell.setPreferredSize(new Dimension(50, 50)); // Further reduced for consistency
        cell.setMinimumSize(new Dimension(50, 50));
        cell.setMaximumSize(new Dimension(50, 50));
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.setVerticalAlignment(SwingConstants.CENTER);
        cell.setFont(new Font("Arial", Font.BOLD, 24)); // Slightly smaller font
        cell.setForeground(new Color(50, 50, 50));
        cell.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return cell;
    }

    public void createPauseScreen() {
        JPanel pausePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        pausePanel.setOpaque(false);
        pausePanel.setLayout(new BoxLayout(pausePanel, BoxLayout.Y_AXIS));
        pausePanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Pause title
        JLabel pauseLabel = new JLabel("GAME PAUSED");
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 36));
        pauseLabel.setForeground(Color.WHITE);
        pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Resume button - FIXED: Using simple text instead of Unicode
        JButton resumeButton = createRoundedButton("Resume");
        resumeButton.setPreferredSize(new Dimension(200, 60));
        resumeButton.setMaximumSize(new Dimension(200, 60));
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeButton.addActionListener(e -> resumeGame());

        // Add components to the pause panel with proper spacing
        pausePanel.add(Box.createVerticalGlue());
        pausePanel.add(pauseLabel);
        pausePanel.add(Box.createRigidArea(new Dimension(0, 50)));
        pausePanel.add(resumeButton);
        pausePanel.add(Box.createVerticalGlue());

        mainPanel.add(pausePanel, "pause");
    }

    public void createMenuScreen() {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Menu title
        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 36));
        menuLabel.setForeground(Color.WHITE);
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Close button - FIXED: Using simple text instead of Unicode
        JButton closeButton = createIconButton("X");
        closeButton.setPreferredSize(new Dimension(40, 40));
        closeButton.addActionListener(e -> resumeGame());

        // Menu options - FIXED: Using simple text instead of Unicode
        JButton soundButton = createRoundedButton("Sound ON");
        soundButton.setPreferredSize(new Dimension(200, 50));
        soundButton.setMaximumSize(new Dimension(200, 50));
        soundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        soundButton.addActionListener(e -> toggleSound(soundButton));

        JButton musicButton = createRoundedButton("Music ON");
        musicButton.setPreferredSize(new Dimension(200, 50));
        musicButton.setMaximumSize(new Dimension(200, 50));
        musicButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicButton.addActionListener(e -> toggleMusic(musicButton));

        JButton wordsFoundButton = createRoundedButton("Words found");
        wordsFoundButton.setPreferredSize(new Dimension(200, 50));
        wordsFoundButton.setMaximumSize(new Dimension(200, 50));
        wordsFoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsFoundButton.addActionListener(e -> showWordsFound());

        JButton helpButton = createRoundedButton("Help");
        helpButton.setPreferredSize(new Dimension(200, 50));
        helpButton.setMaximumSize(new Dimension(200, 50));
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.addActionListener(e -> showHelp());

        // NEW: Play Again button
        JButton playAgainButton = createRoundedButton("Play Again");
        playAgainButton.setPreferredSize(new Dimension(200, 50));
        playAgainButton.setMaximumSize(new Dimension(200, 50));
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> restartGame());

        JButton quitButton = createRoundedButton("Quit");
        quitButton.setPreferredSize(new Dimension(200, 50));
        quitButton.setMaximumSize(new Dimension(200, 50));
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> quitGame());

        // Add components to the menu panel with proper spacing
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(menuLabel, BorderLayout.CENTER);
        titlePanel.add(closeButton, BorderLayout.EAST);

        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(titlePanel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        menuPanel.add(soundButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(musicButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(wordsFoundButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(helpButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(playAgainButton); // NEW
        menuPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        menuPanel.add(quitButton);
        menuPanel.add(Box.createVerticalGlue());

        mainPanel.add(menuPanel, "menu");
    }

    public void createRoundCompleteScreen() {
        // Remove the old screen if it exists
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals("roundComplete")) {
                mainPanel.remove(comp);
                break;
            }
        }

        JPanel roundCompletePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        roundCompletePanel.setName("roundComplete");
        roundCompletePanel.setOpaque(false);
        roundCompletePanel.setLayout(new BoxLayout(roundCompletePanel, BoxLayout.Y_AXIS));
        roundCompletePanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Round complete title
        JLabel titleLabel = new JLabel("GREAT JOB!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(TILE_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, MAX_ROUNDS);
        progressBar.setValue(currentRound);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setBackground(GRID_COLOR);
        progressBar.setForeground(TILE_COLOR);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Round stats - FIXED: Calculate current values dynamically
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roundLabel = new JLabel("Round " + currentRound + " complete:");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(TILE_COLOR);
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual words found count
        int wordsFoundCount = 0;
        for (boolean found : wordsFound) {
            if (found) wordsFoundCount++;
        }

        JLabel targetWordsLabel = new JLabel("Words found .................... " + wordsFoundCount + "/6");
        targetWordsLabel.setFont(GAME_FONT);
        targetWordsLabel.setForeground(Color.WHITE);
        targetWordsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual time spent
        int timeSpent = 180 - timeRemaining;
        JLabel timeSpentLabel = new JLabel("Time spent ............................. " + formatTime(timeSpent));
        timeSpentLabel.setFont(GAME_FONT);
        timeSpentLabel.setForeground(Color.WHITE);
        timeSpentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Show actual current score
        JLabel roundScoreLabel = new JLabel("Round score ............................ " + score);
        roundScoreLabel.setFont(GAME_FONT);
        roundScoreLabel.setForeground(Color.WHITE);
        roundScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(roundLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        statsPanel.add(targetWordsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(timeSpentLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(roundScoreLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton nextRoundButton = createRoundedButton("Next round");
        nextRoundButton.setPreferredSize(new Dimension(200, 50));
        nextRoundButton.addActionListener(e -> startNextRound());

        JButton wordsButton = createRoundedButton("Words");
        wordsButton.setPreferredSize(new Dimension(200, 50));
        wordsButton.addActionListener(e -> showWordsFound());

        buttonPanel.add(nextRoundButton);
        buttonPanel.add(wordsButton);

        // Add components to the round complete panel with proper spacing
        roundCompletePanel.add(Box.createVerticalGlue());
        roundCompletePanel.add(titleLabel);
        roundCompletePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        roundCompletePanel.add(progressBar);
        roundCompletePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        roundCompletePanel.add(statsPanel);
        roundCompletePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        roundCompletePanel.add(buttonPanel);
        roundCompletePanel.add(Box.createVerticalGlue());

        mainPanel.add(roundCompletePanel, "roundComplete");
    }

    public void createGameOverScreen() {
        // Remove the old screen if it exists
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals("gameOver")) {
                mainPanel.remove(comp);
                break;
            }
        }

        JPanel gameOverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        gameOverPanel.setName("gameOver");
        gameOverPanel.setOpaque(false);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Game over title
        JLabel titleLabel = new JLabel("TIME'S UP!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Show actual final score
        JLabel finalScoreLabel = new JLabel("Final score .................................... " + score);
        finalScoreLabel.setFont(GAME_FONT);
        finalScoreLabel.setForeground(Color.WHITE);
        finalScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, MAX_ROUNDS);
        progressBar.setValue(currentRound);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setBackground(GRID_COLOR);
        progressBar.setForeground(new Color(231, 76, 60)); // Red
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Round stats - FIXED: Calculate current values dynamically
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roundLabel = new JLabel("Round " + currentRound + " failed:");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(TILE_COLOR);
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual words found count
        int wordsFoundCount = 0;
        for (boolean found : wordsFound) {
            if (found) wordsFoundCount++;
        }

        JLabel targetWordsLabel = new JLabel("Words found .................... " + wordsFoundCount + "/6");
        targetWordsLabel.setFont(GAME_FONT);
        targetWordsLabel.setForeground(Color.WHITE);
        targetWordsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual time spent
        int timeSpent = 180 - timeRemaining;
        JLabel timeSpentLabel = new JLabel("Time spent ............................. " + formatTime(timeSpent));
        timeSpentLabel.setFont(GAME_FONT);
        timeSpentLabel.setForeground(Color.WHITE);
        timeSpentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Show actual current score
        JLabel roundScoreLabel = new JLabel("Round score ............................ " + score);
        roundScoreLabel.setFont(GAME_FONT);
        roundScoreLabel.setForeground(Color.WHITE);
        roundScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(roundLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        statsPanel.add(targetWordsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(timeSpentLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(roundScoreLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton playAgainButton = createRoundedButton("Play again");
        playAgainButton.setPreferredSize(new Dimension(200, 50));
        playAgainButton.addActionListener(e -> restartGame());

        JButton wordsButton = createRoundedButton("Words");
        wordsButton.setPreferredSize(new Dimension(200, 50));
        wordsButton.addActionListener(e -> showWordsFound());

        buttonPanel.add(playAgainButton);
        buttonPanel.add(wordsButton);

        // Add components to the game over panel with proper spacing
        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(titleLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameOverPanel.add(finalScoreLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gameOverPanel.add(progressBar);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        gameOverPanel.add(statsPanel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        gameOverPanel.add(buttonPanel);
        gameOverPanel.add(Box.createVerticalGlue());

        mainPanel.add(gameOverPanel, "gameOver");
    }

    public void createWordsFoundScreen() {
        JPanel wordsFoundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
                // The bubbles will be visible through this transparent panel
            }
        };
        wordsFoundPanel.setOpaque(false);
        wordsFoundPanel.setLayout(new BoxLayout(wordsFoundPanel, BoxLayout.Y_AXIS));
        wordsFoundPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Words title
        JLabel wordsTitle = new JLabel("Current Round Words");
        wordsTitle.setFont(new Font("Arial", Font.BOLD, 24));
        wordsTitle.setForeground(TILE_COLOR);
        wordsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Words display
        JPanel wordsPanel = new JPanel();
        wordsPanel.setLayout(new BoxLayout(wordsPanel, BoxLayout.Y_AXIS));
        wordsPanel.setOpaque(false);
        wordsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3-letter words
        JLabel threeLetterTitle = new JLabel("3-Letter Words:");
        threeLetterTitle.setFont(GAME_FONT);
        threeLetterTitle.setForeground(Color.WHITE);
        threeLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(threeLetterTitle);

        for (int i = 0; i < 3; i++) {
            String word = (currentWordSet != null && i < currentWordSet.length && currentWordSet[i] != null) ? currentWordSet[i] : "???";
            String status = (i < wordsFound.length && wordsFound[i]) ? " - FOUND" : " - MISSING"; // FIXED: Using text instead of symbols
            JLabel wordLabel = new JLabel(word + status);
            wordLabel.setFont(GAME_FONT);
            wordLabel.setForeground((i < wordsFound.length && wordsFound[i]) ? Color.GREEN : Color.RED);
            wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            wordsPanel.add(wordLabel);
        }

        wordsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 4-letter words
        JLabel fourLetterTitle = new JLabel("4-Letter Words:");
        fourLetterTitle.setFont(GAME_FONT);
        fourLetterTitle.setForeground(Color.WHITE);
        fourLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(fourLetterTitle);

        for (int i = 3; i < 5; i++) {
            String word = (currentWordSet != null && i < currentWordSet.length && currentWordSet[i] != null) ? currentWordSet[i] : "???";
            String status = (i < wordsFound.length && wordsFound[i]) ? " - FOUND" : " - MISSING"; // FIXED: Using text instead of symbols
            JLabel wordLabel = new JLabel(word + status);
            wordLabel.setFont(GAME_FONT);
            wordLabel.setForeground((i < wordsFound.length && wordsFound[i]) ? Color.GREEN : Color.RED);
            wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            wordsPanel.add(wordLabel);
        }

        wordsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 5-letter word
        JLabel fiveLetterTitle = new JLabel("5-Letter Word:");
        fiveLetterTitle.setFont(GAME_FONT);
        fiveLetterTitle.setForeground(Color.WHITE);
        fiveLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(fiveLetterTitle);

        String word = (currentWordSet != null && currentWordSet.length > 5 && currentWordSet[5] != null) ? currentWordSet[5] : "???";
        String status = (wordsFound.length > 5 && wordsFound[5]) ? " - FOUND" : " - MISSING"; // FIXED: Using text instead of symbols
        JLabel wordLabel = new JLabel(word + status);
        wordLabel.setFont(GAME_FONT);
        wordLabel.setForeground((wordsFound.length > 5 && wordsFound[5]) ? Color.GREEN : Color.RED);
        wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(wordLabel);

        // OK button
        JButton okButton = createRoundedButton("Ok");
        okButton.setPreferredSize(new Dimension(100, 50));
        okButton.setMaximumSize(new Dimension(100, 50));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> resumeGame());

        // Add components to the words found panel with proper spacing
        wordsFoundPanel.add(Box.createVerticalGlue());
        wordsFoundPanel.add(wordsTitle);
        wordsFoundPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        wordsFoundPanel.add(wordsPanel);
        wordsFoundPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        wordsFoundPanel.add(okButton);
        wordsFoundPanel.add(Box.createVerticalGlue());

        mainPanel.add(wordsFoundPanel, "wordsFound");
    }

    public JButton createRoundedButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(BUTTON_COLOR.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_COLOR.brighter());
                } else {
                    g2.setColor(BUTTON_COLOR);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border painting needed
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    public JButton createIconButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(BUTTON_COLOR.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_COLOR.brighter());
                } else {
                    g2.setColor(BUTTON_COLOR);
                }

                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border painting needed
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    public void showStartScreen() {
        cardLayout.show(mainPanel, "start");
    }

    public void startLoading() {
        cardLayout.show(mainPanel, "loading");

        // Reset loading progress
        loadingProgress = 0;
        currentLoadingLetter = 0;
        loadingBar.setValue(0);
        loadingPercentLabel.setText("Loading 0%");
        loadingLetterLabel.setText(loadingLetters[0]);

        // Start loading animation
        loadingTimer = new Timer();
        loadingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                loadingProgress += 2;

                if (loadingProgress <= 100) {
                    loadingBar.setValue(loadingProgress);
                    loadingPercentLabel.setText("Loading " + loadingProgress + "%");

                    // Change letter tile every 20%
                    if (loadingProgress % 20 == 0 && currentLoadingLetter < loadingLetters.length - 1) {
                        currentLoadingLetter++;
                        loadingLetterLabel.setText(loadingLetters[currentLoadingLetter]);
                    }
                } else {
                    loadingTimer.cancel();
                    SwingUtilities.invokeLater(() -> startGame());
                }
            }
        }, 50, 50);
    }

    public void startGame() {
        // Initialize game state
        currentRound = 1;
        score = 0;
        timeRemaining = 180; // 3 minutes in seconds
        healthPercentage = 100;

        // Update UI
        roundLabel.setText("Round: " + currentRound);
        scoreLabel.setText("Score: " + score);
        updateTimeLabel();

        // Generate word set for the current round
        generateWordSet();

        // Start game timer
        startGameTimer();

        // Show game screen
        cardLayout.show(mainPanel, "game");
    }

    public void startGameTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    timeRemaining--;

                    // Update health bar based on 3 minutes (180 seconds)
                    healthPercentage = Math.max(0, (int)(timeRemaining / 180.0 * 100));

                    SwingUtilities.invokeLater(() -> {
                        updateTimeLabel();

                        // Check if time is up
                        if (timeRemaining <= 0) {
                            gameTimer.cancel();
                            gameOver();
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    public void updateTimeLabel() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    public void pauseGame() {
        isPaused = true;
        cardLayout.show(mainPanel, "pause");
    }

    public void resumeGame() {
        isPaused = false;
        cardLayout.show(mainPanel, "game");
    }

    public void showMenu() {
        isPaused = true;
        cardLayout.show(mainPanel, "menu");
    }

    public void toggleSound(JButton soundButton) {
        isSoundOn = !isSoundOn;
        soundButton.setText("Sound " + (isSoundOn ? "ON" : "OFF")); // FIXED: Using text instead of symbols
    }

    public void toggleMusic(JButton musicButton) {
        isMusicOn = !isMusicOn;
        musicButton.setText("Music " + (isMusicOn ? "ON" : "OFF")); // FIXED: Using text instead of symbols
    }

    public void showWordsFound() {
        // FIXED: Recreate the words found screen with current data
        mainPanel.remove(mainPanel.getComponent(mainPanel.getComponentCount() - 1)); // Remove old screen
        createWordsFoundScreen(); // Create new screen with updated data
        cardLayout.show(mainPanel, "wordsFound");
    }

    public void showHelp() {
        JOptionPane.showMessageDialog(this,
                "Word Scramble Game Help:\n\n" +
                        "1. Use the 5 scrambled letters to form words.\n" +
                        "2. Click on letter tiles to select them.\n" +
                        "3. Selected letters appear in the word input box.\n" +
                        "4. Words are automatically detected and placed when valid.\n" +
                        "5. Find 3 three-letter words, 2 four-letter words, and 1 five-letter word.\n" +
                        "6. Use the shuffle button to rearrange the same letters.\n" +
                        "7. Use the hint button to reveal letters one by one.\n" +
                        "8. Complete all three rounds to win!",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    public void quitGame() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to quit the game?",
                "Quit Game", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else {
            resumeGame();
        }
    }

    public void generateWordSet() {
        // Select a random word set from the loaded sets
        if (!allWordSets.isEmpty()) {
            WordSet selectedSet = allWordSets.get((int)(Math.random() * allWordSets.size()));
            currentWordSet = selectedSet.words.clone();
            scrambledLetters = shuffleString(selectedSet.letters);
        } else {
            // Fallback to default word set
            currentWordSet = new String[]{"HAM", "HAS", "ASH", "MASH", "MASS", "SMASH"};
            scrambledLetters = "SHSMA";
        }

        // Reset found words
        wordsFound = new boolean[6];

        // Update UI
        updateLetterTiles();
        clearAllGrids();
        clearCurrentWord();
    }

    public String shuffleString(String input) {
        char[] characters = input.toCharArray();

        // Shuffle the array
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int)(Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        return new String(characters);
    }

    public void updateLetterTiles() {
        letterTilesPanel.removeAll();

        for (char c : scrambledLetters.toCharArray()) {
            JLabel letterTile = createLetterTile(String.valueOf(c));
            letterTile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Add letter to current input word (allow multiple selections)
                    if (currentInputWord.length() < 5) {
                        currentInputWord += c;
                        updateWordInputDisplay();

                        // Check if the current word is valid after each letter click
                        checkWordAutomatically();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    letterTile.setBorder(BorderFactory.createCompoundBorder(
                            new SoftBevelBorder(SoftBevelBorder.RAISED),
                            BorderFactory.createEmptyBorder(8, 5, 2, 5)
                    ));
                    letterTile.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    letterTile.setBorder(BorderFactory.createCompoundBorder(
                            new SoftBevelBorder(SoftBevelBorder.RAISED),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                    letterTile.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
            letterTilesPanel.add(letterTile);
        }

        letterTilesPanel.revalidate();
        letterTilesPanel.repaint();
    }

    // NEW: Update word input display
    public void updateWordInputDisplay() {
        wordInputPanel.removeAll();

        for (char c : currentInputWord.toCharArray()) {
            JLabel letterLabel = new JLabel(String.valueOf(c));
            letterLabel.setPreferredSize(new Dimension(25, 25)); // Made smaller
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            letterLabel.setVerticalAlignment(SwingConstants.CENTER);
            letterLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Smaller font
            letterLabel.setForeground(Color.WHITE);
            letterLabel.setOpaque(true);
            letterLabel.setBackground(TILE_COLOR);
            letterLabel.setBorder(BorderFactory.createCompoundBorder(
                    new SoftBevelBorder(SoftBevelBorder.RAISED),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));
            wordInputPanel.add(letterLabel);
        }

        wordInputPanel.revalidate();
        wordInputPanel.repaint();
    }

    public void clearAllGrids() {
        // Clear 3-letter grids
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                threeLetterLabels[i][j].setText("");
                threeLetterLabels[i][j].setOpaque(false);
                threeLetterLabels[i][j].setBackground(null);
            }
        }

        // Clear 4-letter grids
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                fourLetterLabels[i][j].setText("");
                fourLetterLabels[i][j].setOpaque(false);
                fourLetterLabels[i][j].setBackground(null);
            }
        }

        // Clear 5-letter grid
        for (int i = 0; i < 5; i++) {
            fiveLetterLabels[i].setText("");
            fiveLetterLabels[i].setOpaque(false);
            fiveLetterLabels[i].setBackground(null);
        }
    }

    public void clearCurrentWord() {
        currentInputWord = "";
        updateWordInputDisplay();
    }

    public void shuffleLetters() {
        // Generate a new word set instead of just shuffling letters
        generateWordSet();

        // Add a shuffle animation effect
        Timer shuffleTimer = new Timer();
        shuffleTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if (count < 5) {
                    SwingUtilities.invokeLater(() -> {
                        Component[] components = letterTilesPanel.getComponents();
                        for (Component component : components) {
                            if (component instanceof JLabel) {
                                JLabel tile = (JLabel) component;
                                int offset = (count % 2 == 0) ? 2 : -2;
                                tile.setLocation(tile.getX() + offset, tile.getY());
                            }
                        }
                    });
                    count++;
                } else {
                    shuffleTimer.cancel();
                }
            }
        }, 0, 50);
    }

    public void checkWordAutomatically() {
        if (currentInputWord.length() < 3) {
            return; // Need at least 3 letters
        }

        // Check if the word matches any of the target words
        boolean wordFound = false;
        int wordIndex = -1;

        for (int i = 0; i < currentWordSet.length; i++) {
            if (currentWordSet[i].equalsIgnoreCase(currentInputWord) && !wordsFound[i]) {
                wordFound = true;
                wordIndex = i;
                break;
            }
        }

        if (wordFound) {
            wordsFound[wordIndex] = true;
            score += 30; // Fixed 30 points per word
            scoreLabel.setText("Score: " + score);

            // Place the word in the appropriate grid
            placeWordInGrid(currentInputWord, wordIndex);

            // Animate the found word
            animateFoundWord(currentInputWord);

            // Clear the current word
            clearCurrentWord();

            // Check how many words have been found
            int wordsFoundCount = 0;
            for (boolean found : wordsFound) {
                if (found) wordsFoundCount++;
            }

            // Auto-reveal a letter after 2 words found
            if (wordsFoundCount == 2) {
                autoRevealLetter();
            }

            // Check if all words are found
            boolean allWordsFound = true;
            for (boolean found : wordsFound) {
                if (!found) {
                    allWordsFound = false;
                    break;
                }
            }

            if (allWordsFound) {
                roundComplete();
            }
        }
    }

    public void autoRevealLetter() {
        // Find words that haven't been found yet
        List<Integer> remainingWordIndices = new ArrayList<>();
        for (int i = 0; i < wordsFound.length; i++) {
            if (!wordsFound[i]) {
                remainingWordIndices.add(i);
            }
        }

        if (!remainingWordIndices.isEmpty()) {
            // Get a random word from the remaining words
            int randomIndex = remainingWordIndices.get((int)(Math.random() * remainingWordIndices.size()));
            String word = currentWordSet[randomIndex];

            // Reveal one letter in the appropriate grid
            revealLetterInGrid(word, randomIndex);

            // Show notification
            JOptionPane.showMessageDialog(this,
                    "Bonus! A letter has been revealed for finding 2 words!",
                    "Auto Reveal", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void placeWordInGrid(String word, int wordIndex) {
        if (wordIndex < 3) {
            // 3-letter word
            int row = wordIndex;
            for (int j = 0; j < 3; j++) {
                if (j < word.length()) {
                    threeLetterLabels[row][j].setText(String.valueOf(word.charAt(j)));
                    threeLetterLabels[row][j].setOpaque(true);
                    threeLetterLabels[row][j].setBackground(TILE_COLOR);
                    animateGridCell(threeLetterLabels[row][j]);
                }
            }
        } else if (wordIndex < 5) {
            // 4-letter word
            int row = wordIndex - 3;
            for (int j = 0; j < 4; j++) {
                if (j < word.length()) {
                    fourLetterLabels[row][j].setText(String.valueOf(word.charAt(j)));
                    fourLetterLabels[row][j].setOpaque(true);
                    fourLetterLabels[row][j].setBackground(TILE_COLOR);
                    animateGridCell(fourLetterLabels[row][j]);
                }
            }
        } else {
            // 5-letter word
            for (int j = 0; j < 5; j++) {
                if (j < word.length()) {
                    fiveLetterLabels[j].setText(String.valueOf(word.charAt(j)));
                    fiveLetterLabels[j].setOpaque(true);
                    fiveLetterLabels[j].setBackground(TILE_COLOR);
                    animateGridCell(fiveLetterLabels[j]);
                }
            }
        }
    }

    public void animateGridCell(JLabel cell) {
        Timer animTimer = new Timer();
        animTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                if (count < 5) {
                    SwingUtilities.invokeLater(() -> {
                        float[] hsb = Color.RGBtoHSB(TILE_COLOR.getRed(), TILE_COLOR.getGreen(), TILE_COLOR.getBlue(), null);
                        float brightness = hsb[2] + (count % 2 == 0 ? 0.1f : -0.1f);
                        brightness = Math.max(0.7f, Math.min(1.0f, brightness));
                        cell.setBackground(Color.getHSBColor(hsb[0], hsb[1], brightness));
                    });
                    count++;
                } else {
                    SwingUtilities.invokeLater(() -> {
                        cell.setBackground(TILE_COLOR);
                    });
                    animTimer.cancel();
                }
            }
        }, 0, 50);
    }

    public void animateFoundWord(String word) {
        // Create a more elaborate animation for found word
        JPanel animPanel = new JPanel(new BorderLayout());
        animPanel.setBackground(new Color(46, 204, 113, 200)); // Semi-transparent green

        JLabel animLabel = new JLabel("Word Found!");
        animLabel.setFont(new Font("Arial", Font.BOLD, 24));
        animLabel.setForeground(Color.WHITE);
        animLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel wordLabel = new JLabel(word.toUpperCase());
        wordLabel.setFont(new Font("Arial", Font.BOLD, 20));
        wordLabel.setForeground(Color.WHITE);
        wordLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel pointsLabel = new JLabel("+30 points");
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        pointsLabel.setForeground(Color.WHITE);
        pointsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        animPanel.add(animLabel, BorderLayout.NORTH);
        animPanel.add(wordLabel, BorderLayout.CENTER);
        animPanel.add(pointsLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this,
                animPanel,
                "Success", JOptionPane.PLAIN_MESSAGE);
    }

    public void roundComplete() {
        // Stop the timer
        gameTimer.cancel();

        // FIXED: Recreate the round complete screen with current data
        createRoundCompleteScreen();

        // Show round complete screen
        cardLayout.show(mainPanel, "roundComplete");
    }

    public void startNextRound() {
        // Increment round
        currentRound++;

        if (currentRound <= MAX_ROUNDS) {
            // Reset time to 3 minutes
            timeRemaining = 180;
            healthPercentage = 100;

            // Update UI
            roundLabel.setText("Round: " + currentRound);
            updateTimeLabel();

            // Generate new word set
            generateWordSet();

            // Start game timer
            startGameTimer();

            // Show game screen
            cardLayout.show(mainPanel, "game");
        } else {
            // Game completed
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed all rounds!\nFinal Score: " + score,
                    "Game Complete", JOptionPane.INFORMATION_MESSAGE);

            // Restart the game
            restartGame();
        }
    }

    public void gameOver() {
        // Stop the timer
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // FIXED: Recreate the game over screen with current data
        createGameOverScreen();

        // Show game over screen
        cardLayout.show(mainPanel, "gameOver");
    }

    public void restartGame() {
        // FIXED: Stop any existing timer first
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // Reset game state
        currentRound = 1;
        score = 0;
        timeRemaining = 180; // FIXED: Reset timer properly
        healthPercentage = 100;
        isPaused = false;

        // Start the game
        startGame();
    }

    public void showHint() {
        // Find a word that hasn't been found yet
        List<Integer> remainingWordIndices = new ArrayList<>();
        for (int i = 0; i < wordsFound.length; i++) {
            if (!wordsFound[i]) {
                remainingWordIndices.add(i);
            }
        }

        if (!remainingWordIndices.isEmpty()) {
            // Get a random word from the remaining words
            int randomIndex = remainingWordIndices.get((int)(Math.random() * remainingWordIndices.size()));
            String hintWord = currentWordSet[randomIndex];

            // Reveal one letter in the appropriate grid (not necessarily the first)
            revealLetterInGrid(hintWord, randomIndex);

            // Deduct 10 points for using hint (FIXED: Allow negative scores)
            score -= 10;
            scoreLabel.setText("Score: " + score);

            // Create a hint dialog
            JPanel hintPanel = new JPanel(new BorderLayout(0, 10));
            hintPanel.setBackground(new Color(52, 152, 219, 200)); // Semi-transparent blue
            hintPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel hintLabel = new JLabel("Hint Used!");
            hintLabel.setFont(new Font("Arial", Font.BOLD, 20));
            hintLabel.setForeground(Color.WHITE);

            JLabel letterLabel = new JLabel("Revealed a letter for: " + hintWord.length() + "-letter word (-10 points)");
            letterLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            letterLabel.setForeground(Color.WHITE);

            hintPanel.add(hintLabel, BorderLayout.NORTH);
            hintPanel.add(letterLabel, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(this,
                    hintPanel,
                    "Hint", JOptionPane.PLAIN_MESSAGE);

            // Note: Do NOT mark the word as found or check for completion
            // since we only revealed one letter
        } else {
            JOptionPane.showMessageDialog(this,
                    "No more hints available. You've found all words!",
                    "Hint", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void revealLetterInGrid(String word, int wordIndex) {
        if (wordIndex < 3) {
            // 3-letter word
            int row = wordIndex;
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (threeLetterLabels[row][j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                threeLetterLabels[row][randomPos].setText(String.valueOf(word.charAt(randomPos)));
                threeLetterLabels[row][randomPos].setOpaque(true);
                threeLetterLabels[row][randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animateGridCell(threeLetterLabels[row][randomPos]);
            }
        } else if (wordIndex < 5) {
            // 4-letter word
            int row = wordIndex - 3;
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (fourLetterLabels[row][j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                fourLetterLabels[row][randomPos].setText(String.valueOf(word.charAt(randomPos)));
                fourLetterLabels[row][randomPos].setOpaque(true);
                fourLetterLabels[row][randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animateGridCell(fourLetterLabels[row][randomPos]);
            }
        } else {
            // 5-letter word
            // Find all empty positions
            List<Integer> emptyPositions = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                if (fiveLetterLabels[j].getText().isEmpty()) {
                    emptyPositions.add(j);
                }
            }
            // Reveal a random empty position
            if (!emptyPositions.isEmpty()) {
                int randomPos = emptyPositions.get((int)(Math.random() * emptyPositions.size()));
                fiveLetterLabels[randomPos].setText(String.valueOf(word.charAt(randomPos)));
                fiveLetterLabels[randomPos].setOpaque(true);
                fiveLetterLabels[randomPos].setBackground(new Color(52, 152, 219)); // Blue for hint
                animateGridCell(fiveLetterLabels[randomPos]);
            }
        }
    }

    public void loadWordSets() {
        // Load word sets from file
        allWordSets = loadWordSetsFromFile(WORD_SETS_FILE);

        // If file doesn't exist or is empty, create default word sets
        if (allWordSets.isEmpty()) {
            createDefaultWordSets();
            saveWordSetsToFile(WORD_SETS_FILE, allWordSets);
        }
    }

    public void createDefaultWordSets() {
        // Create 50 word sets as requested
        String[][] wordSetsData = {
                {"SHSMA", "HAM", "HAS", "ASH", "MASH", "MASS", "SMASH"},
                {"RPSEA", "SPA", "RAP", "SEA", "PARE", "RASP", "SPARE"},
                {"ATRES", "TEA", "RAT", "EAR", "RATE", "STAR", "STARE"},
                {"ETARC", "CAT", "RAT", "EAR", "CART", "TEAR", "CARET"},
                {"BTALS", "BAT", "SAT", "LAB", "SLAB", "LAST", "BLAST"},
                {"RDMEA", "RED", "ARM", "EAR", "DAME", "READ", "DREAM"},
                {"LNPAT", "LAP", "NAP", "TAP", "PLAN", "PANT", "PLANT"},
                {"GRNIO", "GIN", "RIG", "ION", "RING", "GRIN", "GROIN"},
                {"FLMEA", "ELF", "LAM", "FEA", "FAME", "MEAL", "FLAME"},
                {"CRTEA", "CAR", "RAT", "EAR", "CART", "TEAR", "CRATE"},
                {"BRLUE", "RUB", "BEL", "RUE", "BLUE", "RUBE", "BLUER"},
                {"STNEA", "NET", "SAT", "TEA", "NEAT", "SENT", "ANTES"},
                {"PRLAE", "LAP", "RAP", "EAR", "PALE", "REAL", "PEARL"},
                {"MTNEA", "MET", "NET", "TEA", "MEAT", "NEAT", "MEANT"},
                {"WRTEA", "WAR", "RAT", "EAR", "WEAR", "TEAR", "WATER"},
                {"GRLAE", "GAL", "RAG", "EAR", "GEAR", "REAL", "LARGE"},
                {"BRTEA", "BAR", "RAT", "EAR", "BEAR", "TEAR", "BARTER"},
                {"SRLAE", "SAL", "RAG", "EAR", "SEAL", "REAL", "LASER"},
                {"PRTEA", "PAR", "RAT", "EAR", "PEAR", "TEAR", "TAPER"},
                {"FRLAE", "FAR", "RAG", "EAR", "FEAR", "REAL", "FLARE"},
                {"DRLAE", "DAL", "RAG", "EAR", "DEAL", "REAL", "ALDER"},
                {"HRLAE", "HAL", "RAG", "EAR", "HEAL", "REAL", "HALER"},
                {"TRLAE", "TAL", "RAG", "EAR", "TALE", "REAL", "ALTER"},
                {"VRLAE", "VAL", "RAG", "EAR", "VEAL", "REAL", "RAVEL"},
                {"KRLAE", "KAL", "RAG", "EAR", "KALE", "REAL", "LAKER"},
                {"NRLAE", "NAL", "RAG", "EAR", "LEAN", "REAL", "LEARN"},
                {"MRLAE", "MAL", "RAG", "EAR", "MALE", "REAL", "REALM"},
                {"CRLAE", "CAL", "RAG", "EAR", "LACE", "REAL", "CLEAR"},
                {"BRLAE", "BAL", "RAG", "EAR", "BALE", "REAL", "BLARE"},
                {"WRLAE", "WAL", "RAG", "EAR", "WALE", "REAL", "WALER"},
                {"YRLAE", "YAL", "RAG", "EAR", "YALE", "REAL", "EARLY"},
                {"ORLAE", "OAL", "RAG", "EAR", "ORAL", "REAL", "OALER"},
                {"IRLAE", "IAL", "RAG", "EAR", "RAIL", "REAL", "AILER"},
                {"URLAE", "UAL", "RAG", "EAR", "RULE", "REAL", "UALER"},
                {"ERLAE", "EAL", "RAG", "EAR", "EARL", "REAL", "EALER"},
                {"QRLAE", "QAL", "RAG", "EAR", "QALE", "REAL", "QALER"},
                {"XRLAE", "XAL", "RAG", "EAR", "AXLE", "REAL", "XALER"},
                {"ZRLAE", "ZAL", "RAG", "EAR", "ZEAL", "REAL", "ZALER"},
                {"JRLAE", "JAL", "RAG", "EAR", "JALE", "REAL", "JALER"},
                {"LRLAE", "LAL", "RAG", "EAR", "LEAL", "REAL", "LALER"},
                {"RRLAE", "RAL", "RAG", "EAR", "RARE", "REAL", "RALER"},
                {"SRLAE", "SAL", "RAG", "EAR", "SALE", "REAL", "LASER"},
                {"TRLAE", "TAL", "RAG", "EAR", "TALE", "REAL", "ALTER"},
                {"URLAE", "UAL", "RAG", "EAR", "RULE", "REAL", "UALER"},
                {"VRLAE", "VAL", "RAG", "EAR", "VALE", "REAL", "RAVEL"},
                {"WRLAE", "WAL", "RAG", "EAR", "WALE", "REAL", "WALER"},
                {"XRLAE", "XAL", "RAG", "EAR", "AXLE", "REAL", "XALER"},
                {"YRLAE", "YAL", "RAG", "EAR", "YALE", "REAL", "EARLY"},
                {"ZRLAE", "ZAL", "RAG", "EAR", "ZEAL", "REAL", "ZALER"},
                {"ARLAE", "AAL", "RAG", "EAR", "AREA", "REAL", "AALER"}
        };

        for (String[] data : wordSetsData) {
            String letters = data[0];
            String[] words = new String[6];
            System.arraycopy(data, 1, words, 0, 6);
            allWordSets.add(new WordSet(letters, words));
        }
    }

    public List<WordSet> loadWordSetsFromFile(String filename) {
        List<WordSet> wordSets = new ArrayList<>();
        try {
            File file = new File(filename);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split(",");
                        if (parts.length == 7) {
                            String letters = parts[0];
                            String[] words = new String[6];
                            System.arraycopy(parts, 1, words, 0, 6);
                            wordSets.add(new WordSet(letters, words));
                        }
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordSets;
    }

    public void saveWordSetsToFile(String filename, List<WordSet> wordSets) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            for (WordSet wordSet : wordSets) {
                writer.print(wordSet.letters);
                for (String word : wordSet.words) {
                    writer.print("," + word);
                }
                writer.println();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WordScrambleGame game = new WordScrambleGame();
            game.setVisible(true);
        });
    }
}
