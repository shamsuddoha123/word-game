import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class UIManager {
    // Constants - Updated with brighter cartoonish colors
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final Color BG_COLOR_1 = new Color(68, 36, 102); // Dark Purple (original start screen color)
    public static final Color BG_COLOR_2 = new Color(88, 46, 122); // Medium Purple (original start screen color)
    public static final Color GRID_COLOR = new Color(120, 100, 160); // Brighter purple for grid
    public static final Color TILE_COLOR = new Color(241, 196, 15); // Bright yellow for tiles
    public static final Color BUTTON_COLOR = new Color(46, 204, 113); // Bright green for buttons
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 48);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font GAME_FONT = new Font("Arial", Font.BOLD, 16);
    public static final int PADDING = 20; // Standard padding

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
    public JPanel wordInputPanel;
    public JButton shuffleButton;
    public JButton clearButton;
    public JButton hintButton;

    // Add these new UI component fields at the top of UIManager class
    public JLabel categoryLabel;
    public JLabel chancesLabel;
    public JLabel categoryTimerLabel;
    public JPanel scrambledLettersPanel;
    public JPanel userInputPanel;
    public JProgressBar categoryProgressBar;
    public JLabel categoryScoreLabel;
    public JLabel categoryTitleLabel; // Add this new field

    public AnimationManager animationManager;
    private WordScrambleGame game;

    public CategoryAnimationManager categoryAnimationManager;

    public UIManager(AnimationManager animationManager, WordScrambleGame game) {
        this.animationManager = animationManager;
        this.game = game;
        this.categoryAnimationManager = new CategoryAnimationManager();
    }

    public void initializeUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Create original gradient background for start screen
                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_COLOR_1,
                        0, getHeight(), BG_COLOR_2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw bubbles
                animationManager.drawBubbles(g2d);
            }
        };
        mainPanel.setBackground(BG_COLOR_1);

        // Initialize bubbles for background animation
        animationManager.initializeBubbles(mainPanel);

        // Create all game screens
        createStartScreen();
        createGameScreen(); // This will now have the cartoon background
        createPauseScreen();
        createMenuScreen();
        createRoundCompleteScreen();
        createGameOverScreen();
        createWordsFoundScreen();
        createCategoryGameScreen();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void createStartScreen() {
        JPanel startPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
            }
        };
        startPanel.setOpaque(false);
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Game title with animated letter tiles
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("WORD SCRAMBLE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Two Level Challenge");
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        subtitleLabel.setForeground(TILE_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Level buttons
        JButton level1Button = createRoundedButton("Level 1: Categorized Word Game");
        level1Button.setPreferredSize(new Dimension(300, 60));
        level1Button.addActionListener(e -> {
            game.uiManager.createCategoryGameScreen();
            game.startCategoryGame();
        });

        JButton level2Button = createRoundedButton("Level 2: Word Scramble");
        level2Button.setPreferredSize(new Dimension(300, 60));
        level2Button.addActionListener(e -> game.startMainGame());

        // Add components to the start panel with proper spacing
        startPanel.add(Box.createVerticalGlue());
        startPanel.add(titlePanel);
        startPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        startPanel.add(subtitleLabel);
        startPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        level1Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        level2Button.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(level1Button);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(level2Button);

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

    public void createGameScreen() {
        gamePanel = new JPanel(new BorderLayout(PADDING, PADDING)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // More cartoonish gradient background - brighter colors (same as level 1)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(135, 206, 250), // Sky blue
                        0, getHeight(), new Color(255, 182, 193)); // Light pink
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw bubbles
                // animationManager.drawBubbles(g2d);

                // Draw birds (NEW - added to level 2)
                categoryAnimationManager.drawAnimations(g2d);

                // Remove this line:
                // drawClouds(g2d);
            }
        };

        // Initialize animations for level 2 (NEW)
        categoryAnimationManager.initializeAnimations(gamePanel);

        gamePanel.setOpaque(false);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Reduced padding

        // Top panel with game info
        JPanel topPanel = new JPanel(new BorderLayout(PADDING, 0));
        topPanel.setOpaque(false);

        // Menu button
        JButton menuButton = createRoundedButton("Menu");
        menuButton.setPreferredSize(new Dimension(120, 40));
        menuButton.addActionListener(e -> game.showMenu());

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
        pauseButton.addActionListener(e -> game.pauseGame());

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

                // Draw the cartoon-style bar background with brighter colors
                g2d.setColor(new Color(180, 160, 200, 150)); // Brighter background
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                        0, 0, getWidth() - 50, getHeight(), 15, 15);
                g2d.fill(roundedRect);

                // Draw the progress sections with bright cartoon colors
                int sections = 4;
                int sectionWidth = (getWidth() - 50) / sections;
                int filledSections = (int) Math.ceil(game.gameState.timeRemaining / 45.0);

                for (int i = 0; i < sections; i++) {
                    if (i < filledSections) {
                        // Use bright rainbow colors for filled sections
                        Color[] rainbowColors = {
                                new Color(255, 100, 150),  // Bright Pink
                                new Color(255, 150, 100),  // Bright Orange
                                new Color(255, 200, 100),  // Bright Yellow
                                new Color(100, 200, 200)   // Bright Teal
                        };
                        g2d.setColor(rainbowColors[i % rainbowColors.length]);
                    } else {
                        g2d.setColor(new Color(150, 150, 150, 100)); // Light gray
                    }

                    if (i < sections - 1) {
                        g2d.fillRect(i * sectionWidth, 0, sectionWidth, getHeight());
                    } else {
                        RoundRectangle2D rightSection = new RoundRectangle2D.Float(
                                i * sectionWidth, 0, sectionWidth, getHeight(), 15, 15);
                        g2d.fill(rightSection);
                    }
                }

                // Draw border
                g2d.setColor(new Color(120, 120, 120));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 50, getHeight()-1, 15, 15);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 8); // Slightly thicker
            }
        };
        healthBarPanel.setOpaque(false);

        // Hint button
        hintButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(120, 100, 160)); // Brighter background
                g2d.fillOval(0, 0, getWidth(), getHeight());

                g2d.setColor(TILE_COLOR);
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
        hintButton.addActionListener(e -> game.showHint());

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

                // Draw rounded rectangle background with brighter color
                g2d.setColor(new Color(120, 100, 160)); // Brighter background
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
        clearButton.addActionListener(e -> game.clearCurrentWord());

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
        shuffleButton.addActionListener(e -> game.shuffleLetters());

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
        resumeButton.addActionListener(e -> game.resumeGame());

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
        closeButton.addActionListener(e -> game.resumeGame());

        // Menu options - FIXED: Using simple text instead of Unicode
        JButton soundButton = createRoundedButton("Sound ON");
        soundButton.setPreferredSize(new Dimension(200, 50));
        soundButton.setMaximumSize(new Dimension(200, 50));
        soundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        soundButton.addActionListener(e -> game.toggleSound(soundButton));

        JButton musicButton = createRoundedButton("Music ON");
        musicButton.setPreferredSize(new Dimension(200, 50));
        musicButton.setMaximumSize(new Dimension(200, 50));
        musicButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicButton.addActionListener(e -> game.toggleMusic(musicButton));

        JButton wordsFoundButton = createRoundedButton("Words found");
        wordsFoundButton.setPreferredSize(new Dimension(200, 50));
        wordsFoundButton.setMaximumSize(new Dimension(200, 50));
        wordsFoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsFoundButton.addActionListener(e -> game.showWordsFound());

        JButton helpButton = createRoundedButton("Help");
        helpButton.setPreferredSize(new Dimension(200, 50));
        helpButton.setMaximumSize(new Dimension(200, 50));
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.addActionListener(e -> game.showHelp());

        // NEW: Play Again button
        JButton playAgainButton = createRoundedButton("Play Again");
        playAgainButton.setPreferredSize(new Dimension(200, 50));
        playAgainButton.setMaximumSize(new Dimension(200, 50));
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> game.restartGame());

        JButton quitButton = createRoundedButton("Quit");
        quitButton.setPreferredSize(new Dimension(200, 50));
        quitButton.setMaximumSize(new Dimension(200, 50));
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> game.quitGame());

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
        JProgressBar progressBar = new JProgressBar(0, GameState.MAX_ROUNDS);
        progressBar.setValue(game.gameState.currentRound);
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

        JLabel roundLabel = new JLabel("Round " + game.gameState.currentRound + " complete:");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(TILE_COLOR);
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual words found count
        int wordsFoundCount = 0;
        for (boolean found : game.gameState.wordsFound) {
            if (found) wordsFoundCount++;
        }

        JLabel targetWordsLabel = new JLabel("Words found .................... " + wordsFoundCount + "/6");
        targetWordsLabel.setFont(GAME_FONT);
        targetWordsLabel.setForeground(Color.WHITE);
        targetWordsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual time spent
        int timeSpent = 180 - game.gameState.timeRemaining;
        JLabel timeSpentLabel = new JLabel("Time spent ............................. " + game.gameState.formatTime(timeSpent));
        timeSpentLabel.setFont(GAME_FONT);
        timeSpentLabel.setForeground(Color.WHITE);
        timeSpentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Show actual current score
        JLabel roundScoreLabel = new JLabel("Round score ............................ " + game.gameState.score);
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
        nextRoundButton.addActionListener(e -> game.startNextRound());

        JButton wordsButton = createRoundedButton("Words");
        wordsButton.setPreferredSize(new Dimension(200, 50));
        wordsButton.addActionListener(e -> game.showWordsFound());

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
        JLabel finalScoreLabel = new JLabel("Final score .................................... " + game.gameState.score);
        finalScoreLabel.setFont(GAME_FONT);
        finalScoreLabel.setForeground(Color.WHITE);
        finalScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, GameState.MAX_ROUNDS);
        progressBar.setValue(game.gameState.currentRound);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setBackground(GRID_COLOR);
        progressBar.setForeground(new Color(180, 60, 50)); // Darker Red
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Round stats - FIXED: Calculate current values dynamically
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roundLabel = new JLabel("Round " + game.gameState.currentRound + " failed:");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(TILE_COLOR);
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual words found count
        int wordsFoundCount = 0;
        for (boolean found : game.gameState.wordsFound) {
            if (found) wordsFoundCount++;
        }

        JLabel targetWordsLabel = new JLabel("Words found .................... " + wordsFoundCount + "/6");
        targetWordsLabel.setFont(GAME_FONT);
        targetWordsLabel.setForeground(Color.WHITE);
        targetWordsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Calculate actual time spent
        int timeSpent = 180 - game.gameState.timeRemaining;
        JLabel timeSpentLabel = new JLabel("Time spent ............................. " + game.gameState.formatTime(timeSpent));
        timeSpentLabel.setFont(GAME_FONT);
        timeSpentLabel.setForeground(Color.WHITE);
        timeSpentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Show actual current score
        JLabel roundScoreLabel = new JLabel("Round score ............................ " + game.gameState.score);
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
        playAgainButton.addActionListener(e -> game.restartGame());

        JButton wordsButton = createRoundedButton("Words");
        wordsButton.setPreferredSize(new Dimension(200, 50));
        wordsButton.addActionListener(e -> game.showWordsFound());

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
        // Remove old screen if exists
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals("wordsFound")) {
                mainPanel.remove(comp);
                break;
            }
        }

        JPanel wordsFoundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Don't call super.paintComponent to allow the mainPanel's background to show through
            }
        };
        wordsFoundPanel.setName("wordsFound");
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

        // Check if game is still active (time remaining > 0)
        boolean gameActive = game.gameState.timeRemaining > 0;

        // 3-letter words
        JLabel threeLetterTitle = new JLabel("3-Letter Words:");
        threeLetterTitle.setFont(GAME_FONT);
        threeLetterTitle.setForeground(Color.WHITE);
        threeLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(threeLetterTitle);

        for (int i = 0; i < 3; i++) {
            if (gameActive) {
                // During gameplay: only show found words
                if (i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) {
                    String word = (game.gameState.currentWordSet != null && i < game.gameState.currentWordSet.length && game.gameState.currentWordSet[i] != null) ? game.gameState.currentWordSet[i] : "???";
                    JLabel wordLabel = new JLabel(word + " - FOUND");
                    wordLabel.setFont(GAME_FONT);
                    wordLabel.setForeground(Color.GREEN);
                    wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    wordsPanel.add(wordLabel);
                }
            } else {
                // After game ends: show all words
                String word = (game.gameState.currentWordSet != null && i < game.gameState.currentWordSet.length && game.gameState.currentWordSet[i] != null) ? game.gameState.currentWordSet[i] : "???";
                String status = (i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) ? " - FOUND" : " - MISSING";
                JLabel wordLabel = new JLabel(word + status);
                wordLabel.setFont(GAME_FONT);
                wordLabel.setForeground((i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) ? Color.GREEN : Color.RED);
                wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                wordsPanel.add(wordLabel);
            }
        }

        wordsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 4-letter words
        JLabel fourLetterTitle = new JLabel("4-Letter Words:");
        fourLetterTitle.setFont(GAME_FONT);
        fourLetterTitle.setForeground(Color.WHITE);
        fourLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(fourLetterTitle);

        for (int i = 3; i < 5; i++) {
            if (gameActive) {
                // During gameplay: only show found words
                if (i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) {
                    String word = (game.gameState.currentWordSet != null && i < game.gameState.currentWordSet.length && game.gameState.currentWordSet[i] != null) ? game.gameState.currentWordSet[i] : "???";
                    JLabel wordLabel = new JLabel(word + " - FOUND");
                    wordLabel.setFont(GAME_FONT);
                    wordLabel.setForeground(Color.GREEN);
                    wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    wordsPanel.add(wordLabel);
                }
            } else {
                // After game ends: show all words
                String word = (game.gameState.currentWordSet != null && i < game.gameState.currentWordSet.length && game.gameState.currentWordSet[i] != null) ? game.gameState.currentWordSet[i] : "???";
                String status = (i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) ? " - FOUND" : " - MISSING";
                JLabel wordLabel = new JLabel(word + status);
                wordLabel.setFont(GAME_FONT);
                wordLabel.setForeground((i < game.gameState.wordsFound.length && game.gameState.wordsFound[i]) ? Color.GREEN : Color.RED);
                wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                wordsPanel.add(wordLabel);
            }
        }

        wordsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 5-letter word
        JLabel fiveLetterTitle = new JLabel("5-Letter Word:");
        fiveLetterTitle.setFont(GAME_FONT);
        fiveLetterTitle.setForeground(Color.WHITE);
        fiveLetterTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordsPanel.add(fiveLetterTitle);

        if (gameActive) {
            // During gameplay: only show if found
            if (game.gameState.wordsFound.length > 5 && game.gameState.wordsFound[5]) {
                String word = (game.gameState.currentWordSet != null && game.gameState.currentWordSet.length > 5 && game.gameState.currentWordSet[5] != null) ? game.gameState.currentWordSet[5] : "???";
                JLabel wordLabel = new JLabel(word + " - FOUND");
                wordLabel.setFont(GAME_FONT);
                wordLabel.setForeground(Color.GREEN);
                wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                wordsPanel.add(wordLabel);
            }
        } else {
            // After game ends: show the word
            String word = (game.gameState.currentWordSet != null && game.gameState.currentWordSet.length > 5 && game.gameState.currentWordSet[5] != null) ? game.gameState.currentWordSet[5] : "???";
            String status = (game.gameState.wordsFound.length > 5 && game.gameState.wordsFound[5]) ? " - FOUND" : " - MISSING";
            JLabel wordLabel = new JLabel(word + status);
            wordLabel.setFont(GAME_FONT);
            wordLabel.setForeground((game.gameState.wordsFound.length > 5 && game.gameState.wordsFound[5]) ? Color.GREEN : Color.RED);
            wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            wordsPanel.add(wordLabel);
        }

        // OK button
        JButton okButton = createRoundedButton("Ok");
        okButton.setPreferredSize(new Dimension(100, 50));
        okButton.setMaximumSize(new Dimension(100, 50));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> {
            // Use the proper game state to determine where to return
            if (game.gameState.currentGameState.equals("roundComplete")) {
                // Return to round complete screen
                showScreen("roundComplete");
            } else if (game.gameState.currentGameState.equals("gameOver")) {
                // Return to game over screen
                showScreen("gameOver");
            } else {
                // Game is still active, return to game screen
                game.resumeGame();
            }
        });

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

    public void updateLetterTiles() {
        letterTilesPanel.removeAll();

        char[] letters = game.gameState.scrambledLetters.toCharArray();
        for (int index = 0; index < letters.length; index++) {
            final char c = letters[index]; // Make it effectively final
            JLabel letterTile = createLetterTile(String.valueOf(c));
            letterTile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Add letter to current input word (allow multiple selections)
                    if (game.gameState.currentInputWord.length() < 5) {
                        game.gameState.currentInputWord += c;
                        updateWordInputDisplay();

                        // Check if this forms a valid word when we have 3+ letters
                        if (game.gameState.currentInputWord.length() >= 3) {
                            // Check if the current word matches any target word
                            String currentWord = game.gameState.currentInputWord;
                            boolean wordFound = false;
                            int wordIndex = -1;

                            // Check against all target words
                            for (int i = 0; i < game.gameState.currentWordSet.length; i++) {
                                if (game.gameState.currentWordSet[i].equalsIgnoreCase(currentWord) && !game.gameState.wordsFound[i]) {
                                    wordFound = true;
                                    wordIndex = i;
                                    break;
                                }
                            }

                            if (wordFound) {
                                // Mark word as found and update score
                                game.gameState.wordsFound[wordIndex] = true;
                                game.gameState.score += 30;
                                scoreLabel.setText("Score: " + game.gameState.score);

                                // Place word in grid
                                game.gameLogic.placeWordInGrid(currentWord, wordIndex, threeLetterLabels, fourLetterLabels, fiveLetterLabels);

                                // Clear the input display IMMEDIATELY
                                game.gameState.currentInputWord = "";
                                updateWordInputDisplay();

                                // Check how many words have been found for auto-reveal bonus
                                int wordsFoundCount = 0;
                                for (boolean found : game.gameState.wordsFound) {
                                    if (found) wordsFoundCount++;
                                }

                                // Auto-reveal a letter after every 2 words found (2, 4, 6...)
                                if (wordsFoundCount > 0 && wordsFoundCount % 2 == 0) {
                                    autoRevealLetter();
                                }

                                // Check if all words are found
                                boolean allWordsFound = true;
                                for (boolean found : game.gameState.wordsFound) {
                                    if (!found) {
                                        allWordsFound = false;
                                        break;
                                    }
                                }

                                if (allWordsFound) {
                                    game.roundComplete();
                                }
                            }
                        }
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

    public void autoRevealLetter() {
        // Find words that haven't been found yet
        java.util.List<Integer> remainingWordIndices = new ArrayList<>();
        for (int i = 0; i < game.gameState.wordsFound.length; i++) {
            if (!game.gameState.wordsFound[i]) {
                remainingWordIndices.add(i);
            }
        }

        if (!remainingWordIndices.isEmpty()) {
            // Get a random word from the remaining words
            int randomIndex = remainingWordIndices.get((int)(Math.random() * remainingWordIndices.size()));
            String word = game.gameState.currentWordSet[randomIndex];

            // Reveal one letter in the appropriate grid
            game.gameLogic.revealLetterInGrid(word, randomIndex, threeLetterLabels, fourLetterLabels, fiveLetterLabels);

            // Show auto-closing notification (2 seconds)
            showAutoClosingMessage("Bonus! A letter has been revealed for finding 2 words!", "Auto Reveal", 2000);
        }
    }

    public void showAutoClosingMessage(String message, String title, int delayMs) {
        JPanel messagePanel = new JPanel(new BorderLayout(0, 10));
        messagePanel.setBackground(new Color(52, 152, 219, 200));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        messagePanel.add(titleLabel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        // Create a dialog that auto-closes
        JDialog messageDialog = new JDialog(game, title, true);
        messageDialog.add(messagePanel);
        messageDialog.setSize(350, 120);
        messageDialog.setLocationRelativeTo(game);
        messageDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        messageDialog.setUndecorated(true);

        // Auto-close after specified delay
        Timer autoCloseTimer = new Timer();
        autoCloseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    messageDialog.dispose();
                });
                autoCloseTimer.cancel();
            }
        }, delayMs);

        messageDialog.setVisible(true);
    }

    public void updateWordInputDisplay() {
        wordInputPanel.removeAll();

        for (char c : game.gameState.currentInputWord.toCharArray()) {
            JLabel letterLabel = new JLabel(String.valueOf(c)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw shadow
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 10, 10);

                    // Draw main tile with cartoon gradient
                    GradientPaint tileGradient = new GradientPaint(
                            0, 0, new Color(120, 200, 120),
                            0, getHeight(), new Color(30, 120, 30));
                    g2d.setPaint(tileGradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                    // Draw highlight
                    g2d.setColor(new Color(255, 255, 255, 150));
                    g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()/3, 8, 8);

                    // Draw border
                    g2d.setColor(new Color(30, 120, 30));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                    g2d.dispose();
                    super.paintComponent(g);
                }
            };

            letterLabel.setPreferredSize(new Dimension(30, 30)); // Slightly bigger
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            letterLabel.setVerticalAlignment(SwingConstants.CENTER);
            letterLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 18)); // Comic Sans MS
            letterLabel.setForeground(Color.WHITE);
            letterLabel.setOpaque(false);

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

    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }

    public void createCategoryGameScreen() {
        JPanel categoryPanel = new JPanel(new BorderLayout(PADDING, PADDING)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // More cartoonish gradient background - brighter colors
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(135, 206, 250), // Sky blue
                        0, getHeight(), new Color(255, 182, 193)); // Light pink
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw birds and bubbles
                categoryAnimationManager.drawAnimations(g2d);

                // Remove this line:
                // drawClouds(g2d);
            }
        };

        // Initialize animations for this screen
        categoryAnimationManager.initializeAnimations(categoryPanel);

        categoryPanel.setOpaque(false);
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel with cute title and info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Add quit and play again buttons to the left side (stacked vertically)
        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
        leftTopPanel.setOpaque(false);

        JButton quitButton = createCuteButton("Quit", new Color(180, 60, 50));
        quitButton.setPreferredSize(new Dimension(130, 30));
        quitButton.setMaximumSize(new Dimension(130, 30));
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(game,
                    "Are you sure you want to quit Level 1?",
                    "Quit Level 1", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                // Stop the category game
                if (game.categoryGame.gameTimer != null) {
                    game.categoryGame.gameTimer.cancel();
                }
                // Return to start screen
                showScreen("start");
            }
        });

        // NEW: Play Again button for Level 1
        JButton playAgainButton = createCuteButton("Play Again", new Color(46, 204, 113));
        playAgainButton.setPreferredSize(new Dimension(130, 30));
        playAgainButton.setMaximumSize(new Dimension(130, 30));
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> {
            // Stop current game
            if (game.categoryGame.gameTimer != null) {
                game.categoryGame.gameTimer.cancel();
            }
            // Restart Level 1
            game.categoryGame.startGame();
            showScreen("categoryGame");
        });

        leftTopPanel.add(quitButton);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftTopPanel.add(playAgainButton);

        // Create a spacer panel for the right side to balance the layout
        JPanel rightTopPanel = new JPanel();
        rightTopPanel.setLayout(new BoxLayout(rightTopPanel, BoxLayout.Y_AXIS));
        rightTopPanel.setOpaque(false);
        rightTopPanel.setPreferredSize(new Dimension(130, 65)); // Same size as left panel

        // Create center panel for title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        // Dynamic title with fun font
        categoryTitleLabel = new JLabel("GUESS THE COUNTRY!", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw text shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(getText(), x + 3, y + 3);

                // Draw main text with gradient
                GradientPaint textGradient = new GradientPaint(
                        0, 0, new Color(200, 160, 12),
                        0, getHeight(), new Color(180, 140, 10));
                g2d.setPaint(textGradient);
                g2d.drawString(getText(), x, y);
            }
        };
        categoryTitleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
        categoryTitleLabel.setForeground(new Color(200, 160, 12));

        titlePanel.add(categoryTitleLabel, BorderLayout.CENTER);

        // Game info panel with cute styling
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        infoPanel.setOpaque(false);

        chancesLabel = createCuteInfoLabel("Lives: 3/3");
        categoryTimerLabel = createCuteInfoLabel("Time: 20");
        categoryScoreLabel = createCuteInfoLabel("Score: 0");

        infoPanel.add(chancesLabel);
        infoPanel.add(categoryTimerLabel);
        infoPanel.add(categoryScoreLabel);

        topPanel.add(leftTopPanel, BorderLayout.WEST);
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        // Center panel with scrambled letters
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20)); // Reduced spacing from 40 to 20
        centerPanel.setOpaque(false);

        // Scrambled letters display with cartoon styling
        scrambledLettersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        scrambledLettersPanel.setOpaque(false);

        // User input display with cute boxes - FIXED: Smaller size and better spacing
        userInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 15)); // Reduced spacing
        userInputPanel.setOpaque(false);
        userInputPanel.setPreferredSize(new Dimension(600, 80)); // Reduced height from 120 to 80

        // Fun progress bar
        categoryProgressBar = new JProgressBar(0, 20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rainbow progress bar
                g2d.setColor(new Color(100, 100, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                int progressWidth = (int)((double)getValue() / getMaximum() * getWidth());
                if (progressWidth > 0) {
                    GradientPaint progressGradient = new GradientPaint(
                            0, 0, new Color(0, 180, 0),
                            progressWidth, 0, new Color(180, 180, 0));
                    g2d.setPaint(progressGradient);
                    g2d.fillRoundRect(0, 0, progressWidth, getHeight(), 20, 20);
                }

                // Draw border
                g2d.setColor(new Color(80, 80, 80));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        categoryProgressBar.setValue(20);
        categoryProgressBar.setStringPainted(false);
        categoryProgressBar.setPreferredSize(new Dimension(400, 25));

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.setOpaque(false);
        progressPanel.add(categoryProgressBar);

        centerPanel.add(scrambledLettersPanel, BorderLayout.NORTH);
        centerPanel.add(userInputPanel, BorderLayout.CENTER);
        centerPanel.add(progressPanel, BorderLayout.SOUTH);

        // Bottom panel with cute buttons (REMOVED SUBMIT BUTTON)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bottomPanel.setOpaque(false);

        JButton clearButton = createCuteButton("Clear", new Color(200, 160, 12));
        clearButton.setPreferredSize(new Dimension(140, 55));
        clearButton.addActionListener(e -> game.categoryGame.clearInput());

        JButton backspaceButton = createCuteButton("Delete", new Color(180, 60, 50));
        backspaceButton.setPreferredSize(new Dimension(140, 55));
        backspaceButton.addActionListener(e -> game.categoryGame.removeLetter());

        bottomPanel.add(clearButton);
        bottomPanel.add(backspaceButton);

        categoryPanel.add(topPanel, BorderLayout.NORTH);
        categoryPanel.add(centerPanel, BorderLayout.CENTER);
        categoryPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(categoryPanel, "categoryGame");
    }

    private JLabel createCuteInfoLabel(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw cute background bubble
                g2d.setColor(new Color(200, 200, 200, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw border
                g2d.setColor(new Color(80, 120, 180));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

                super.paintComponent(g);
            }
        };
        label.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        label.setForeground(new Color(25, 25, 80));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return label;
    }

    private JButton createCuteButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color buttonColor = baseColor;
                if (getModel().isPressed()) {
                    buttonColor = baseColor.darker().darker();
                } else if (getModel().isRollover()) {
                    buttonColor = baseColor.brighter();
                } else {
                    buttonColor = baseColor;
                }

                // Draw button shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(3, 3, getWidth(), getHeight(), 25, 25);

                // Draw main button
                g2d.setColor(buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Draw highlight
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()/2, 20, 20);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Comic Sans MS", Font.BOLD, 14)); // Reduced font size from 16 to 14
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public void updateCategoryGameDisplay() {
        // Update dynamic title directly
        if (categoryTitleLabel != null) {
            categoryTitleLabel.setText("GUESS THE " + game.categoryGame.getCategoryDisplayName() + "!");
        }

        // Update category and chances with plain text only
        // Update lives with plain text
        int livesRemaining = CategoryGame.MAX_CHANCES - game.categoryGame.currentChances;
        String livesText = "Lives: " + livesRemaining + "/" + CategoryGame.MAX_CHANCES;
        chancesLabel.setText(livesText);

        // Update score with plain text
        if (categoryScoreLabel != null) {
            categoryScoreLabel.setText("Score: " + game.categoryGame.totalScore);
        }

        // Update scrambled letters with cartoon styling
        scrambledLettersPanel.removeAll();
        char[] scrambledChars = game.categoryGame.scrambledWord.toCharArray();
        for (int index = 0; index < scrambledChars.length; index++) {
            final char c = scrambledChars[index]; // Make it effectively final
            JLabel letterTile = createCartoonLetterTile(String.valueOf(c));
            letterTile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    game.categoryGame.addLetter(c);
                }
            });
            scrambledLettersPanel.add(letterTile);
        }

        updateCategoryInput();
        scrambledLettersPanel.revalidate();
        scrambledLettersPanel.repaint();
    }

    public void updateCategoryInput() {
        userInputPanel.removeAll();

        String userInput = game.categoryGame.userInput;
        String answer = game.categoryGame.currentAnswer;

        for (int index = 0; index < answer.length(); index++) {
            final int i = index; // Make it effectively final
            JLabel inputTile = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw shadow
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 15, 15);

                    // Draw main tile
                    if (i < userInput.length()) {
                        // Filled tile - green gradient
                        GradientPaint fillGradient = new GradientPaint(
                                0, 0, new Color(120, 200, 120),
                                0, getHeight(), new Color(30, 120, 30));
                        g2d.setPaint(fillGradient);
                    } else {
                        // Empty tile - gray gradient
                        GradientPaint emptyGradient = new GradientPaint(
                                0, 0, new Color(180, 180, 180),
                                0, getHeight(), new Color(120, 120, 120));
                        g2d.setPaint(emptyGradient);
                    }
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                    // Draw border
                    g2d.setColor(new Color(80, 80, 80));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);

                    g2d.dispose();
                    super.paintComponent(g);
                }
            };

            inputTile.setPreferredSize(new Dimension(50, 50)); // Reduced from 100 to 50
            inputTile.setMinimumSize(new Dimension(50, 50));
            inputTile.setMaximumSize(new Dimension(50, 50));
            inputTile.setHorizontalAlignment(SwingConstants.CENTER);
            inputTile.setVerticalAlignment(SwingConstants.CENTER);
            inputTile.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); // Reduced font size from 32 to 20
            inputTile.setOpaque(false);

            if (i < userInput.length()) {
                inputTile.setText(String.valueOf(userInput.charAt(i)));
                inputTile.setForeground(Color.WHITE);
            } else {
                inputTile.setText("");
                inputTile.setForeground(new Color(100, 100, 100));
            }

            userInputPanel.add(inputTile);
        }

        userInputPanel.revalidate();
        userInputPanel.repaint();

        // Auto-submit if the input matches the answer length and is correct
        if (userInput.length() == answer.length()) {
            // Check if the input matches the answer
            if (userInput.equalsIgnoreCase(answer)) {
                // Auto-submit after a short delay to show the completed word
                Timer autoSubmitTimer = new Timer();
                autoSubmitTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            game.categoryGame.submitAnswer();
                        });
                    }
                }, 500); // 500ms delay to show the completed word
            }
        }
    }

    public void updateCategoryTimer() {
        categoryTimerLabel.setText("Time: " + game.categoryGame.timeRemaining);
        categoryProgressBar.setValue(game.categoryGame.timeRemaining);

        // Change color based on time remaining
        if (game.categoryGame.timeRemaining <= 5) {
            // Red for danger
            categoryProgressBar.setForeground(new Color(180, 60, 50));
        } else if (game.categoryGame.timeRemaining <= 10) {
            // Yellow for warning
            categoryProgressBar.setForeground(new Color(200, 160, 12));
        } else {
            // Green for safe
            categoryProgressBar.setForeground(new Color(35, 150, 85));
        }
    }

    public JLabel createCartoonLetterTile(String letter) {
        JLabel tile = new JLabel(letter) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(4, 4, getWidth(), getHeight(), 20, 20);

                // Draw main tile with gradient
                GradientPaint tileGradient = new GradientPaint(
                        0, 0, new Color(200, 180, 0),
                        0, getHeight(), new Color(180, 140, 0));
                g2d.setPaint(tileGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw highlight
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()/3, 15, 15);

                // Draw border
                g2d.setColor(new Color(180, 120, 0));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        tile.setPreferredSize(new Dimension(90, 90));
        tile.setHorizontalAlignment(SwingConstants.CENTER);
        tile.setVerticalAlignment(SwingConstants.CENTER);
        tile.setFont(new Font("Comic Sans MS", Font.BOLD, 36));
        tile.setForeground(new Color(100, 60, 20)); // Darker brown text
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return tile;
    }

    public void showCategorySuccess(int score) {
        categoryAnimationManager.triggerConfetti();

        // Move to next word directly
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    game.categoryGame.nextWord();
                });
            }
        }, 500);
    }

    public void showCategoryWrongAnswer() {
        int remainingChances = CategoryGame.MAX_CHANCES - game.categoryGame.currentChances;

        String[] encouragingMessages = {
                "Oops! Don't worry, try again!",
                "Almost there! You can do it!",
                "Keep trying! You're learning!",
                "No worries! Practice makes perfect!"
        };

        String message = encouragingMessages[(int)(Math.random() * encouragingMessages.length)];
        message += "\n\nThe correct answer was: " + game.categoryGame.currentAnswer;
        message += "\nRemaining lives: " + remainingChances;

        JOptionPane.showMessageDialog(game, message, "Try Again!", JOptionPane.WARNING_MESSAGE);

        // Continue with next word
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    game.categoryGame.nextWord();
                });
            }
        }, 2000);
    }

    public void showCategoryGameOver() {
        String message = "Game Over! But you did great!\n\n";
        message += "Final Statistics:\n";
        message += "Total Score: " + game.categoryGame.totalScore + "\n";
        message += "Words Guessed: " + game.categoryGame.wordsGuessedCorrectly + "\n";
        message += "Accuracy: " + String.format("%.1f", game.categoryGame.getAccuracy()) + "%\n\n";
        message += "Keep practicing to improve your score!";

        // Create custom dialog with play again option
        Object[] options = {"Play Again", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(game, message, "Game Complete!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            // Play again - restart level 1
            game.categoryGame.startGame();
            showScreen("categoryGame");
        } else {
            // Return to start screen
            showScreen("start");
        }
    }

    public void showCategoryGameWon() {
        categoryAnimationManager.triggerConfetti();

        String message = "AMAZING! You completed Level 1!\n\n";
        message += "Final Score: " + game.categoryGame.totalScore + "\n";
        message += "Words Guessed: " + game.categoryGame.wordsGuessedCorrectly + "\n";
        message += "Accuracy: " + String.format("%.1f", game.categoryGame.getAccuracy()) + "%\n\n";
        message += "What would you like to do next?";

        // Create custom dialog with multiple options
        Object[] options = {"Play Level 1 Again", "Go to Level 2", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(game, message, "Level 1 Complete!",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);

        if (choice == 0) {
            // Play Level 1 again
            game.categoryGame.startGame();
            showScreen("categoryGame");
        } else if (choice == 1) {
            // Start Level 2 (main word scramble game)
            game.startMainGame();
        } else {
            // Return to start screen
            showScreen("start");
        }
    }
}
