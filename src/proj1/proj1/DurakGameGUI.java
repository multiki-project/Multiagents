package proj1.proj1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DurakGameGUI extends JFrame {
    public JPanel visualHandPanel = new JPanel();
    public JPanel battlefieldPanel = new JPanel();

    public JButton takeButton = new JButton("TAKE CARDS");
    public JButton doneButton = new JButton("PASS / DONE");
    public JButton exitButton = new JButton("EXIT GAME");

    public JLabel trumpLabel = new JLabel("Trump: ?");
    public JLabel deckLabel = new JLabel("Deck: ?");
    public JLabel turnLabel = new JLabel("TURN: WAIT");
    public JLabel statusLabel = new JLabel("Durak started");

    private final UserAgent myAgent;

    public DurakGameGUI(UserAgent agent) {
        this.myAgent = agent;

        setTitle("Durak");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new GradientPanel();
        main.setLayout(new BorderLayout(15, 15));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("DURAK", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setOpaque(false);
        setupLabel(trumpLabel, Color.WHITE, 16);
        setupLabel(deckLabel, Color.LIGHT_GRAY, 16);
        setupLabel(turnLabel, Color.YELLOW, 18);
        setupLabel(statusLabel, Color.WHITE, 14);
        infoPanel.add(trumpLabel);
        infoPanel.add(deckLabel);
        infoPanel.add(turnLabel);
        infoPanel.add(statusLabel);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.CENTER);
        top.add(infoPanel, BorderLayout.EAST);

        battlefieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 25));
        battlefieldPanel.setOpaque(false);
        battlefieldPanel.setBorder(makeBorder("TABLE"));

        visualHandPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        visualHandPanel.setOpaque(false);

        JScrollPane handScroll = new JScrollPane(visualHandPanel);
        handScroll.setPreferredSize(new Dimension(0, 205));
        handScroll.setOpaque(false);
        handScroll.getViewport().setOpaque(false);
        handScroll.setBorder(makeBorder("YOUR HAND"));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(0, 55));
        setupButton(takeButton, new Color(30, 30, 30));
        setupButton(doneButton, new Color(30, 30, 30));
        setupButton(exitButton, new Color(120, 0, 0));
        buttonPanel.add(takeButton);
        buttonPanel.add(doneButton);
        buttonPanel.add(exitButton);

        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.setOpaque(false);
        south.add(handScroll, BorderLayout.CENTER);
        south.add(buttonPanel, BorderLayout.SOUTH);

        main.add(top, BorderLayout.NORTH);
        main.add(battlefieldPanel, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        add(main);

        takeButton.addActionListener(e -> myAgent.sendDurakTakeRequest());
        doneButton.addActionListener(e -> myAgent.sendDurakDoneRequest());
        exitButton.addActionListener(e -> myAgent.exitCurrentGame());

        setLocationRelativeTo(null);
    }

    private void setupLabel(JLabel label, Color color, int size) {
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private void setupButton(JButton button, Color bg) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private TitledBorder makeBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                title,
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                Color.WHITE
        );
    }

    public void setTrumpUI(String suit, String icon) {
        SwingUtilities.invokeLater(() -> trumpLabel.setText("Trump: " + icon + " " + suit));
    }

    public void setDeckUI(int count) {
        SwingUtilities.invokeLater(() -> deckLabel.setText("Deck: " + count));
    }

    public void setTurnUI(boolean myTurn) {
        SwingUtilities.invokeLater(() -> {
            if (myTurn) {
                turnLabel.setText("TURN: ATTACK");
                turnLabel.setForeground(Color.GREEN);
            } else {
                turnLabel.setText("TURN: DEFEND");
                turnLabel.setForeground(Color.RED);
            }
        });
    }

    public void addVisualCardToHand(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            visualHandPanel.add(new VisualCard(rank, suit, myAgent));
            visualHandPanel.revalidate();
            visualHandPanel.repaint();
        });
    }

    public void clearHand() {
        SwingUtilities.invokeLater(() -> {
            visualHandPanel.removeAll();
            visualHandPanel.revalidate();
            visualHandPanel.repaint();
        });
    }

    public void removeVisualCard(VisualCard cardComponent) {
        SwingUtilities.invokeLater(() -> {
            visualHandPanel.remove(cardComponent);
            visualHandPanel.revalidate();
            visualHandPanel.repaint();
        });
    }

    public void updateLog(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
        System.out.println("DURAK LOG: " + msg);
    }

    public void clearTable() {
        SwingUtilities.invokeLater(() -> {
            battlefieldPanel.removeAll();
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    public void addServerDefenseToTable(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            battlefieldPanel.add(new VisualCard(rank, suit, null));
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    public void addUserDefenseToTable(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            battlefieldPanel.add(new VisualCard(rank, suit, myAgent));
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    public void addServerAttackToTable(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            battlefieldPanel.add(new VisualCard(rank, suit, null));
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    public void addUserAttackToTable(String rank, String suit) {
        SwingUtilities.invokeLater(() -> {
            VisualCard card = new VisualCard(rank, suit, myAgent);
            for (java.awt.event.MouseListener ml : card.getMouseListeners()) {
                card.removeMouseListener(ml);
            }
            battlefieldPanel.add(card);
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    public void displayEndGameMessage(String text, Color textColor) {
        SwingUtilities.invokeLater(() -> {
            battlefieldPanel.removeAll();
            JLabel winLabel = new JLabel(text, SwingConstants.CENTER);
            winLabel.setFont(new Font("Arial", Font.BOLD, 58));
            winLabel.setForeground(textColor);
            battlefieldPanel.add(winLabel);
            battlefieldPanel.revalidate();
            battlefieldPanel.repaint();
        });
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(0, 40, 0), w, h, Color.BLACK);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(0, 255, 100, 35));
            g2.fillOval(w - 300, h - 280, 300, 300);
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillOval(-130, -130, 300, 300);
        }
    }
}
