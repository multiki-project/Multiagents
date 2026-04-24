package proj1.proj1;

import javax.swing.*;
import java.awt.*;

public class GamePickerGUI extends JFrame {
    public GamePickerGUI(UserAgent agent) {
        setTitle("Choose Game");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 15, 15));

        JButton blackjack = new JButton("BLACKJACK");
        JButton durak = new JButton("DURAK");
        JButton uno = new JButton("UNO");

        blackjack.setFont(new Font("Arial", Font.BOLD, 22));
        durak.setFont(new Font("Arial", Font.BOLD, 22));
        uno.setFont(new Font("Arial", Font.BOLD, 22));

        blackjack.addActionListener(e -> agent.openClassicLobby());
        durak.addActionListener(e -> agent.openClassicLobby());
        uno.addActionListener(e -> agent.openUnoGame());

        add(blackjack);
        add(durak);
        add(uno);

        setLocationRelativeTo(null);
    }
}