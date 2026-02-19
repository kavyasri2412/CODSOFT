
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.swing.*;

public class NumberGamePro extends JFrame {

    private JTextField guessField;
    private JLabel statusLabel, scoreLabel, attemptsLabel, title;
    private JProgressBar timerBar;
    private JButton guessButton, newGameButton;

    private int number;
    private int attempts;
    private final int MAX_ATTEMPTS = 7;
    private int score = 0;
    private int highScore = 0;
    private int timeLeft = 30;

    private Timer gameTimer;
    private Timer gradientTimer;
    private float hue = 0;

    private Random random = new Random();

    public NumberGamePro() {

        loadHighScore();

        setTitle("GUESS MASTER PRO");
        setSize(550, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ===== Animated Gradient Panel =====
        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                Color c1 = Color.getHSBColor(hue, 0.6f, 0.5f);
                Color c2 = Color.getHSBColor(hue + 0.2f, 0.6f, 0.3f);

                GradientPaint gp = new GradientPaint(
                        0, 0, c1,
                        getWidth(), getHeight(), c2);

                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // Gradient Animation
        gradientTimer = new Timer(40, e -> {
            hue += 0.002f;
            if (hue > 1) {
                hue = 0;
            }
            repaint();
        });
        gradientTimer.start();

        // ===== Title =====
        title = new JLabel("GUESS MASTER PRO", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== Center Panel =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new GridLayout(7, 1, 15, 15));
        center.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        guessField = new JTextField();
        styleField(guessField);

        guessButton = createNeonButton("GUESS");
        newGameButton = createNeonButton("NEW GAME");

        statusLabel = createLabel("Guess number between 1 and 100");
        attemptsLabel = createLabel("Attempts Left: " + MAX_ATTEMPTS);
        scoreLabel = createLabel("Score: 0 | High Score: " + highScore);

        timerBar = new JProgressBar(0, 30);
        timerBar.setValue(30);
        timerBar.setStringPainted(true);

        center.add(statusLabel);
        center.add(guessField);
        center.add(guessButton);
        center.add(newGameButton);
        center.add(attemptsLabel);
        center.add(scoreLabel);
        center.add(timerBar);

        mainPanel.add(center, BorderLayout.CENTER);

        guessButton.addActionListener(e -> checkGuess());
        newGameButton.addActionListener(e -> startGame());

        startGame();
    }

    private JButton createNeonButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setForeground(Color.CYAN);
        btn.setBackground(new Color(0, 0, 0, 180));
        btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            public void mouseExited(MouseEvent e) {
                btn.setForeground(Color.CYAN);
                btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
            }
        });

        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.BOLD, 18));
        field.setHorizontalAlignment(JTextField.CENTER);
    }

    // ===== Start Game =====
    private void startGame() {

        title.setText("GUESS MASTER PRO");

        number = random.nextInt(100) + 1;
        attempts = MAX_ATTEMPTS;
        timeLeft = 30;

        guessField.setText("");
        guessButton.setEnabled(true);

        attemptsLabel.setText("Attempts Left: " + attempts);
        statusLabel.setText("Guess number between 1 and 100");
        timerBar.setValue(30);

        if (gameTimer != null) {
            gameTimer.stop();
        }

        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            timerBar.setValue(timeLeft);

            if (timeLeft <= 0) {
                endGame(false);
            }
        });

        gameTimer.start();
    }

    // ===== Check Guess =====
    private void checkGuess() {

        try {
            int guess = Integer.parseInt(guessField.getText());
            attempts--;
            attemptsLabel.setText("Attempts Left: " + attempts);

            if (guess == number) {
                endGame(true);
            } else if (guess > number) {
                statusLabel.setText("Too High!");
            } else {
                statusLabel.setText("Too Low!");
            }

            if (attempts <= 0 && guess != number) {
                endGame(false);
            }

        } catch (Exception e) {
            statusLabel.setText("Enter valid number!");
        }
    }

    // ===== End Game =====
    private void endGame(boolean win) {

        if (gameTimer != null) {
            gameTimer.stop();
        }

        guessButton.setEnabled(false);

        if (win) {

            score += timeLeft * 10;

            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }

            title.setText("🎉🏆 CONGRATULATIONS 🏆🎉");
            statusLabel.setText("🔥 YOU ARE THE CHAMPION! 🔥");

            JOptionPane.showMessageDialog(
                    this,
                    "🎉🎉 CONGRATULATIONS! 🎉🎉\n\n"
                    + "🏆 BADGE UNLOCKED: GUESS MASTER 🏆\n\n"
                    + "⭐ Points Earned: " + (timeLeft * 10),
                    "WINNER!",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            title.setText("💀 GAME OVER 💀");
            statusLabel.setText("Number was: " + number);

        }

        scoreLabel.setText("Score: " + score + " | High Score: " + highScore);
    }

    private void saveHighScore() {
        try (PrintWriter out = new PrintWriter("leaderboard.txt")) {
            out.println(highScore);
        } catch (Exception e) {
        }
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader("leaderboard.txt"))) {
            highScore = Integer.parseInt(br.readLine());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NumberGamePro().setVisible(true));
    }
}
