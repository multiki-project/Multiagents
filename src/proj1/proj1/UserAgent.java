package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import proj1.ontology.*;
import javax.swing.*;

public class UserAgent extends Agent {
    private UserAgentGUI myGui;
    private SLCodec codec = new SLCodec();
    private String selectedServerName = null;
    private boolean inGame = false;

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());

        myGui = new UserAgentGUI();
        myGui.setVisible(true);
        myGui.updateLog("Welcome! Press SCAN to find games.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleIncoming(msg);
                } else {
                    block();
                }
            }
        });

        // --- ЛОГІКА КНОПОК ---
        myGui.refreshButton.addActionListener(e -> {
            if (!inGame) scanServers();
            else sendTakeRequest();
        });

        myGui.joinButton.addActionListener(e -> {
            selectedServerName = myGui.getSelectedGame();
            if (selectedServerName != null) {
                myGui.updateLog("Connecting to " + selectedServerName + "...");
                myGui.handListModel.clear();
                SubscribeToGame sub = new SubscribeToGame();
                sub.setGameName1(selectedServerName);
                sendRequest(new AID(selectedServerName, AID.ISLOCALNAME), sub);
                inGame = true;
                myGui.refreshButton.setText("✋ TAKE CARDS");
            } else {
                myGui.updateLog("❌ Select a server first!");
            }
        });

        myGui.playMoveButton.addActionListener(e -> {
            String cardStr = myGui.getSelectedCard();
            if (cardStr != null && inGame && selectedServerName != null) {
                PlayMove m = new PlayMove();
                Card c = parseCardString(cardStr);
                m.setPlayedCard(c);
                sendRequest(new AID(selectedServerName, AID.ISLOCALNAME), m);
                myGui.handListModel.removeElement(cardStr);
            }
        });
    }

    private void handleIncoming(ACLMessage msg) {
        // ТВОЇ ДЕБАГ-РЯДКИ
        System.out.println("DEBUG: Прийшло повідомлення від " + msg.getSender().getLocalName() +
                " з онтологією: " + msg.getOntology());

        if (msg.getContent() != null) {
            System.out.println("DEBUG: Сирий контент: " + msg.getContent());
        }

        // Ігноруємо технічне сміття JADE DF
        if (msg.getContent() != null && msg.getContent().startsWith("((")) return;

        try {
            Object content = getContentManager().extractContent(msg);

            if (content instanceof CardsDealt) {
                CardsDealt cd = (CardsDealt) content;
                jade.util.leap.Iterator it = cd.getCards().iterator();

                while (it.hasNext()) {
                    Card c = (Card) it.next();
                    if (c != null) {
                        myGui.addCardToHand(c.getRank(), c.getSuit());
                    }
                }
                myGui.updateLog("Success: " + cd.getCards().size() + " cards received.");
                return;
            }
        } catch (Exception e) {
            String txt = msg.getContent();
            if (txt != null) {
                if (txt.startsWith("ATTACK:")) {
                    String[] p = txt.split(":");
                    myGui.updateLog("⚠️ SERVER ATTACKS: " + p[1] + " of " + p[2]);
                } else if (txt.startsWith("GAME_START:")) {
                    myGui.updateLog("🎮 " + txt.substring(11));
                } else {
                    myGui.updateLog("SERVER: " + txt);
                }
            }
        }
    }

    private void scanServers() {
        myGui.updateLog("Scanning...");

        // Створюємо окремий потік, щоб не вішати GUI
        new Thread(() -> {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("card-game-provider");
            template.addServices(sd);

            try {
                // Пошук у DF (може зайняти час)
                DFAgentDescription[] results = DFService.search(this, template);

                // Оновлюємо GUI тільки через invokeLater
                SwingUtilities.invokeLater(() -> {
                    myGui.gameListModel.clear();
                    if (results.length == 0) {
                        myGui.updateLog("No active servers found.");
                    } else {
                        for (DFAgentDescription dfd : results) {
                            myGui.addGame(dfd.getName().getLocalName());
                        }
                        myGui.updateLog("Scan complete. Found " + results.length + " servers.");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> myGui.updateLog("Search failed: " + ex.getMessage()));
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendTakeRequest() {
        if (selectedServerName == null) return;
        ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
        m.addReceiver(new AID(selectedServerName, AID.ISLOCALNAME));
        m.setContent("TAKE_CARDS");
        send(m);
        myGui.updateLog("You requested to take cards.");
    }

    private Card parseCardString(String s) {
        Card c = new Card();
        String[] parts = s.split(" ");
        c.setRank(parts[0]);
        String suit = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
        c.setSuit(suit);
        return c;
    }

    private void sendRequest(AID r, jade.content.AgentAction a) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(r);
        msg.setLanguage(codec.getName());
        msg.setOntology(CardGameOntology.getInstance().getName());
        try {
            getContentManager().fillContent(msg, new Action(r, a));
            send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }
}