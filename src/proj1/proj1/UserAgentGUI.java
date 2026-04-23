package proj1.proj1;

import javax.swing.*;
import java.awt.*;
import proj1.ontology.Card;

public class UserAgentGUI extends JFrame {
    public DefaultListModel<String> gameListModel = new DefaultListModel<>();
    public DefaultListModel<String> handListModel = new DefaultListModel<>();
    public JList<String> gameList = new JList<>(gameListModel);
    public JList<String> handList = new JList<>(handListModel); // ПРИВ'ЯЗКА МОДЕЛІ ТУТ

    public JButton refreshButton = new JButton("🔍 SCAN");
    public JButton joinButton = new JButton("🚪 JOIN");
    public JButton playMoveButton = new JButton("🃏 PLAY SELECTED");
    private JTextArea logArea = new JTextArea();

    public UserAgentGUI() {
        setTitle("Agent Casino: Ultra Edition");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Ліва панель (Лобі)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("AVAILABLE LOBBIES"));
        leftPanel.add(new JScrollPane(gameList), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // Права панель (Рука гравця)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("YOUR HAND"));
        rightPanel.add(new JScrollPane(handList), BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(300, 0));

        // Центральна панель (Лог)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("GAME ENGINE LOG"));
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        centerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Нижня панель (Кнопки)
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        bottomPanel.setPreferredSize(new Dimension(0, 60));
        bottomPanel.add(refreshButton);
        bottomPanel.add(joinButton);
        bottomPanel.add(playMoveButton);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // МЕТОД ДЛЯ ДОДАВАННЯ КАРТ (КРИТИЧНО)
    public void addCardToHand(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            String icon = getSuitIcon(suit);
            String cardText = rank + " " + icon + " (" + suit + ")";

            // Додаємо в модель
            handListModel.addElement(cardText);

            // Оновлюємо інтерфейс
            handList.repaint();
            handList.revalidate();

            // Прокрутка до нової карти
            handList.ensureIndexIsVisible(handListModel.size() - 1);

            System.out.println("GUI: Відображено карту " + cardText);
        });
    }

    public void updateLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("> " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void addGame(String name) {
        SwingUtilities.invokeLater(() -> gameListModel.addElement(name));
    }

    public String getSelectedGame() {
        return gameList.getSelectedValue();
    }

    public String getSelectedCard() {
        return handList.getSelectedValue();
    }

    private String getSuitIcon(String suit) {
        if (suit == null) return "";
        switch (suit) {
            case "Hearts": return "♥";
            case "Diamonds": return "♦";
            case "Clubs": return "♣";
            case "Spades": return "♠";
            default: return "";
        }
    }
}