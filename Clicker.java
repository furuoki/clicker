import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.Insets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.Set;

public class Clicker extends JFrame implements ActionListener {

    private JLabel counterLabel;
    private JButton clickButton;
    private JButton resetButton;
    private JButton autoClickerButton;
    private int counter = 0;
    private static final String SAVE_FILE = "clicker_save.txt";
    private static final String NICKNAME_FILE = "nickname.txt";
    private static final String ACHIEVEMENTS_FILE = "achievements.txt";
    private final File saveFile = new File(SAVE_FILE);
    private final File achievementsFile = new File(ACHIEVEMENTS_FILE);
    private String nickname;
    private boolean autoClickerActive = false;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private JLabel nicknameLabel;
    private JLabel clicksPerSecondLabel;
    private JLabel totalClicksLabel;
    private int clickCountInLastSecond = 0;
    private JLabel byLabel;


    private final int[] achievementThresholds = {1000, 10000, 100000, 1000000, 10000000, 100000000};
    private final String[] achievementMessages = {
            "Первая тысяча кликов!",
            "Десять тысяч кликов!",
            "Сто тысяч кликов!",
            "Миллион кликов!",
            "Десять миллионов кликов!",
            "Сто миллионов кликов!"
    };

    private Set<Integer> achievedLevels = new HashSet<>();

    public Clicker() {
        loadNickname();

        if (nickname == null || nickname.isEmpty()) {
            nickname = showNicknameDialog();
            saveNickname();
        }


        setTitle("Improved Clicker" + " - " + nickname);
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(240, 240, 240)); // Set background color


        loadAchievements();
        loadProgress();

        counterLabel = new JLabel("Clicks: " + counter);
        counterLabel.setFont(new Font("Arial", Font.BOLD, 20));
        counterLabel.setForeground(new Color(50, 50, 50));
        counterLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        counterLabel.setHorizontalAlignment(SwingConstants.CENTER);


        clickButton = new JButton("Click Me!");
        clickButton.setFont(new Font("Arial", Font.BOLD, 16));
        clickButton.setBackground(new Color(150, 200, 250));
        clickButton.setForeground(Color.WHITE);
        clickButton.setFocusPainted(false);
        clickButton.setBorder(new RoundedBorder(10));
        clickButton.addActionListener(this);
        

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.setBackground(new Color(255, 100, 100));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorder(new RoundedBorder(10));
        resetButton.addActionListener(this);


        autoClickerButton = new JButton("Auto Clicker");
        autoClickerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoClickerButton.setBackground(new Color(200, 200, 100));
        autoClickerButton.setForeground(Color.WHITE);
        autoClickerButton.setFocusPainted(false);
        autoClickerButton.setBorder(new RoundedBorder(10));
        autoClickerButton.addActionListener(this);

        nicknameLabel = new JLabel("Player: " + nickname);
        nicknameLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        nicknameLabel.setForeground(new Color(100, 100, 100));
        nicknameLabel.setHorizontalAlignment(SwingConstants.LEFT);


        clicksPerSecondLabel = new JLabel("CPS: 0");
        clicksPerSecondLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        clicksPerSecondLabel.setForeground(new Color(100, 100, 100));
        clicksPerSecondLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        totalClicksLabel = new JLabel("Total clicks: " + counter);
        totalClicksLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        totalClicksLabel.setForeground(new Color(100, 100, 100));
        totalClicksLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
         byLabel = new JLabel("by furuoki");
        byLabel.setFont(new Font("Arial", Font.BOLD, 14));  // Increased font size and make bold
         byLabel.setForeground(new Color(150, 150, 150));
         byLabel.setHorizontalAlignment(SwingConstants.CENTER);
         byLabel.setBorder(new EmptyBorder(5,0,0,0)); // Add some top padding

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(clickButton);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(autoClickerButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(counterLabel, BorderLayout.CENTER);
        topPanel.add(nicknameLabel, BorderLayout.SOUTH);


        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setOpaque(false);
        statsPanel.add(totalClicksLabel, BorderLayout.WEST);
        statsPanel.add(clicksPerSecondLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
        add(byLabel, BorderLayout.PAGE_END);


        startClickPerSecondUpdater();
        setVisible(true);
    }

    class RoundedBorder extends AbstractBorder {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(c.getForeground());
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            try {
                g2d.draw(roundRect);
            } finally {
                g2d.dispose();
            }
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clickButton) {
            counter++;
            clickCountInLastSecond++;
        } else if (e.getSource() == resetButton) {
            counter = 0;
            clickCountInLastSecond = 0;
        } else if (e.getSource() == autoClickerButton) {
            toggleAutoClicker();
        }
        counterLabel.setText("Clicks: " + counter);
        totalClicksLabel.setText("Total clicks: " + counter);
        checkAchievements();
        saveProgress();
    }


    private void toggleAutoClicker() {
        if (!autoClickerActive) {
            autoClickerActive = true;
            executorService.scheduleAtFixedRate(() -> {
                counter++;
                clickCountInLastSecond++;
                counterLabel.setText("Clicks: " + counter);
                totalClicksLabel.setText("Total clicks: " + counter);
                checkAchievements();
                saveProgress();
            }, 0, 1, TimeUnit.SECONDS);
        } else {
            autoClickerActive = false;
            executorService.shutdown();
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
    }


    private void showFloatingText(int x, int y, String text) {
        JLabel floatingLabel = new JLabel(text);
        floatingLabel.setForeground(new Color(100, 100, 100));
        floatingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        floatingLabel.setBounds(x + 10, y - 10, 50, 20);
        getContentPane().add(floatingLabel, JLayeredPane.POPUP_LAYER);

       Timer timer = new Timer(500, new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               getContentPane().remove(floatingLabel);
               getContentPane().repaint();
           }
       });
        timer.setRepeats(false);
        timer.start();
    }

    private void startClickPerSecondUpdater() {
        ScheduledExecutorService cpsExecutor = Executors.newSingleThreadScheduledExecutor();
        cpsExecutor.scheduleAtFixedRate(() -> {
            clicksPerSecondLabel.setText("CPS: " + clickCountInLastSecond);
            clickCountInLastSecond = 0;
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void loadProgress() {
        if (saveFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
                String line = reader.readLine();
                if (line != null) {
                    try {
                        counter = Integer.parseInt(line);
                    } catch (NumberFormatException ex) {
                        showErrorDialog("Некорректный формат файла сохранения. Значение будет сброшено.");
                        counter = 0;
                    }
                }
            } catch (IOException e) {
                showErrorDialog("Не удалось загрузить прогресс: " + e.getMessage());
            }
        }
    }

    private void saveProgress() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write(String.valueOf(counter));
        } catch (IOException e) {
            showErrorDialog("Не удалось сохранить прогресс: " + e.getMessage());
        }
    }

    private void saveNickname() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NICKNAME_FILE))) {
            writer.write(nickname);
        } catch (IOException e) {
            showErrorDialog("Не удалось сохранить никнейм: " + e.getMessage());
        }
    }

    private void loadNickname() {
        File nicknameFile = new File(NICKNAME_FILE);
        if (nicknameFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(nicknameFile))) {
                nickname = reader.readLine();
            } catch (IOException e) {
                showErrorDialog("Не удалось загрузить никнейм: " + e.getMessage());
            }
        }
    }

    private void loadAchievements() {
        if (achievementsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(achievementsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        achievedLevels.add(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        showErrorDialog("Ошибка загрузки достижений");
                    }
                }
            } catch (IOException e) {
                showErrorDialog("Ошибка при загрузке достижений" + e.getMessage());
            }
        }
    }

    private void saveAchievements() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(achievementsFile))) {
            for (int level : achievedLevels) {
                writer.write(String.valueOf(level));
                writer.newLine();
            }
        } catch (IOException e) {
            showErrorDialog("Ошибка при сохранении достижений" + e.getMessage());
        }
    }

    private String showNicknameDialog() {
        String nick;
        do {
            nick = JOptionPane.showInputDialog(this, "Введите ваш никнейм:", "Регистрация никнейма",
                    JOptionPane.PLAIN_MESSAGE);
            if (nick == null) {
                System.exit(0);
            }
            if (nick.trim().isEmpty()) {
                showErrorDialog("Никнейм не должен быть пустым!");
            }
        } while (nick.trim().isEmpty());
        return nick.trim();
    }

    private void checkAchievements() {
        for (int i = 0; i < achievementThresholds.length; i++) {
            if (counter >= achievementThresholds[i] && !achievedLevels.contains(i)) {
                achievedLevels.add(i);
                showAchievementDialog(achievementMessages[i]);
                saveAchievements();
            }
        }
    }

    private void showAchievementDialog(String message) {
        JOptionPane.showMessageDialog(this, "Достижение получено!\n" + message, "Достижение!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Clicker::new);
    }
}