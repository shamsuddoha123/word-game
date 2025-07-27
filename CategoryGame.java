import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

public class CategoryGame {
    // Game constants
    public static final int TIME_LIMIT = 20; // 20 seconds per word
    public static final int MAX_CHANCES = 3;

    // Game state
    public int currentChances = 0;
    public int currentWordIndex = 0;
    public int currentCategoryIndex = 0;
    public int timeRemaining = TIME_LIMIT;
    public Timer gameTimer;
    public boolean gameActive = false;
    public String currentAnswer = "";
    public String currentCategory = "";
    public String scrambledWord = "";
    public String userInput = "";

    // Score system
    public int totalScore = 0;
    public int currentCategoryScore = 0;
    public int wordsGuessedCorrectly = 0;
    public int totalWordsAttempted = 0;

    // Categories and words
    public Map<String, List<String>> categories;
    public List<String> categoryOrder;
    public WordScrambleGame mainGame;

    // NEW: Track used words to prevent duplicates
    public Set<String> usedWords = new HashSet<>();

    public CategoryGame(WordScrambleGame mainGame) {
        this.mainGame = mainGame;
        loadCategoriesFromFile();
    }

    public void loadCategoriesFromFile() {
        categories = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("categories.txt"));
            String line;
            String currentCat = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Check if this line is a category header (all caps, no commas)
                if (line.equals(line.toUpperCase()) && !line.contains(",")) {
                    currentCat = line;
                    categories.put(currentCat, new ArrayList<>());
                } else if (currentCat != null && line.contains(",")) {
                    // This is a line with words
                    String[] words = line.split(",");
                    for (String word : words) {
                        categories.get(currentCat).add(word.trim());
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            // Fallback to default categories if file not found
            initializeDefaultCategories();
        }

        categoryOrder = new ArrayList<>(categories.keySet());
    }

    // UPDATED: Default categories as fallback (max 7 letters) - only used if categories.txt file is missing
    public void initializeDefaultCategories() {
        categories = new HashMap<>();

        categories.put("COUNTRIES", Arrays.asList(
                "MEXICO", "BRAZIL", "FRANCE", "JAPAN", "INDIA", "CANADA", "SPAIN", "ITALY", "CHINA", "RUSSIA"
        ));

        categories.put("ANIMALS", Arrays.asList(
                "TIGER", "LION", "ZEBRA", "MONKEY", "EAGLE", "RABBIT", "HORSE", "BEAR", "PANDA", "KOALA"
        ));

        categories.put("COLORS", Arrays.asList(
                "RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "PINK", "BROWN", "BLACK", "WHITE"
        ));

        categories.put("SPORTS", Arrays.asList(
                "TENNIS", "SOCCER", "HOCKEY", "GOLF", "BOXING", "RUGBY", "KARATE", "SKIING", "DIVING", "RACING"
        ));

        categories.put("SCIENCE", Arrays.asList(
                "ATOM", "ENERGY", "PLANET", "GALAXY", "OXYGEN", "CARBON", "PHYSICS", "BIOLOGY", "NEUTRON", "PROTON"
        ));

        categories.put("FOOD", Arrays.asList(
                "PIZZA", "BURGER", "PASTA", "SALAD", "SOUP", "BREAD", "CHEESE", "FISH", "RICE", "CAKE"
        ));
    }

    public void startGame() {
        currentChances = 0;
        currentWordIndex = 0;
        currentCategoryIndex = 0;
        totalScore = 0;
        currentCategoryScore = 0;
        wordsGuessedCorrectly = 0;
        totalWordsAttempted = 0;
        gameActive = true;
        usedWords.clear(); // Clear used words for new game
        nextWord();
    }

    public void nextWord() {
        // FIXED: Randomly select a category each time instead of cycling
        if (categories.isEmpty()) {
            gameWon();
            return;
        }

        // Get all available categories
        List<String> availableCategories = new ArrayList<>(categories.keySet());

        // Randomly select a category
        currentCategory = availableCategories.get((int)(Math.random() * availableCategories.size()));
        List<String> words = categories.get(currentCategory);

        // Filter out used words from this category
        List<String> availableWords = new ArrayList<>();
        for (String word : words) {
            if (!usedWords.contains(word)) {
                availableWords.add(word);
            }
        }

        // If no available words in any category, we've completed the game
        if (availableWords.isEmpty()) {
            // Try other categories
            boolean foundWord = false;
            for (String category : availableCategories) {
                List<String> categoryWords = categories.get(category);
                for (String word : categoryWords) {
                    if (!usedWords.contains(word)) {
                        currentCategory = category;
                        currentAnswer = word;
                        usedWords.add(currentAnswer);
                        foundWord = true;
                        break;
                    }
                }
                if (foundWord) break;
            }

            if (!foundWord) {
                gameWon();
                return;
            }
        } else {
            // Pick a random available word from the selected category
            currentAnswer = availableWords.get((int)(Math.random() * availableWords.size()));
            usedWords.add(currentAnswer); // Mark as used
        }

        scrambledWord = scrambleWord(currentAnswer);
        userInput = "";
        timeRemaining = TIME_LIMIT;
        totalWordsAttempted++;

        // Update UI
        mainGame.uiManager.updateCategoryGameDisplay();

        // Start timer
        startTimer();
    }

    public String scrambleWord(String word) {
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int randomIndex = (int)(Math.random() * chars.length);
            char temp = chars[i];
            chars[i] = chars[randomIndex];
            chars[randomIndex] = temp;
        }
        return new String(chars);
    }

    public void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--;
                SwingUtilities.invokeLater(() -> {
                    mainGame.uiManager.updateCategoryTimer();
                    if (timeRemaining <= 0) {
                        timeUp();
                    }
                });
            }
        }, 1000, 1000);
    }

    public void submitAnswer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        if (userInput.equalsIgnoreCase(currentAnswer)) {
            // Correct answer - fixed 50 points per word
            int wordScore = 50;

            currentCategoryScore += wordScore;
            totalScore += wordScore;
            wordsGuessedCorrectly++;

            SwingUtilities.invokeLater(() -> {
                mainGame.uiManager.showCategorySuccess(wordScore);
            });
        } else {
            // Wrong answer
            wrongAnswer();
        }
    }

    public void wrongAnswer() {
        currentChances++;
        if (currentChances >= MAX_CHANCES) {
            gameOver();
        } else {
            SwingUtilities.invokeLater(() -> {
                mainGame.uiManager.showCategoryWrongAnswer();
            });
        }
    }

    public void timeUp() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        wrongAnswer();
    }

    public void gameOver() {
        gameActive = false;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        SwingUtilities.invokeLater(() -> {
            mainGame.uiManager.showCategoryGameOver();
        });
    }

    public void gameWon() {
        gameActive = false;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        SwingUtilities.invokeLater(() -> {
            mainGame.uiManager.showCategoryGameWon();
        });
    }

    public void nextCategory() {
        currentCategoryIndex++;
        currentCategoryScore = 0; // Reset category score
        nextWord();
    }

    public void addLetter(char letter) {
        if (userInput.length() < currentAnswer.length()) {
            userInput += letter;
            mainGame.uiManager.updateCategoryInput();
        }
    }

    public void removeLetter() {
        if (userInput.length() > 0) {
            userInput = userInput.substring(0, userInput.length() - 1);
            mainGame.uiManager.updateCategoryInput();
        }
    }

    public void clearInput() {
        userInput = "";
        mainGame.uiManager.updateCategoryInput();
    }

    public String getCategoryDisplayName() {
        switch (currentCategory) {
            case "COUNTRIES": return "COUNTRY";
            case "ANIMALS": return "ANIMAL";
            case "COLORS": return "COLOR";
            case "SPORTS": return "SPORT";
            case "SCIENCE": return "SCIENCE TERM";
            case "FOOD": return "FOOD";
            case "SCHOOL": return "SCHOOL ITEM";
            case "NATURE": return "NATURE ITEM";
            case "TRANSPORT": return "TRANSPORT";
            case "HOUSE": return "HOUSE ITEM";
            default: return currentCategory;
        }
    }

    public double getAccuracy() {
        if (totalWordsAttempted == 0) return 0.0;
        return (double) wordsGuessedCorrectly / totalWordsAttempted * 100.0;
    }
}
