package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import proj1.ontology.*;

import java.awt.*;

public class UserAgent extends Agent {
    private GamePickerGUI pickerGui;
    private DurakGameGUI durakGui;
    private BlackjackGameGUI blackjackGui;
    private UnoGameGUI unoGui;

    private final SLCodec codec = new SLCodec();
    private String selectedServerName = null;
    private String currentGame = "NONE";
    private VisualCard pendingCard = null;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());

        pickerGui = new GamePickerGUI(this);
        pickerGui.setVisible(true);

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleIncoming(msg);
                } else {
                    block();
                }
            }
        });
    }

    public void startDurakGame() {
        currentGame = "DURAK";
        selectedServerName = "Durak";
        pendingCard = null;

        pickerGui.setVisible(false);
        durakGui = new DurakGameGUI(this);
        durakGui.setVisible(true);
        durakGui.updateLog("Joining Durak...");

        SubscribeToGame sub = new SubscribeToGame();
        sub.setGameName1("Durak");
        sendRequest(new AID(selectedServerName, AID.ISLOCALNAME), sub);
    }

    public void startBlackjackGame() {
        currentGame = "BLACKJACK";
        selectedServerName = "Blackjack";
        pendingCard = null;

        pickerGui.setVisible(false);
        blackjackGui = new BlackjackGameGUI(this);
        blackjackGui.setVisible(true);
        blackjackGui.setStatus("Joining Blackjack...");

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(selectedServerName, AID.ISLOCALNAME));
        msg.setContent("JOIN_BJ");
        send(msg);
    }

    public void startUnoGame() {
        currentGame = "UNO";
        selectedServerName = "UNO";
        pendingCard = null;

        pickerGui.setVisible(false);
        unoGui = new UnoGameGUI(this);
        unoGui.setVisible(true);
        unoGui.setStatus("Joining UNO...");

        SubscribeToGame sub = new SubscribeToGame();
        sub.setGameName1("UNO");
        sendRequest(new AID(selectedServerName, AID.ISLOCALNAME), sub);
    }

    private void handleIncoming(ACLMessage msg) {
        if (msg.getSender() != null && msg.getSender().getLocalName().equalsIgnoreCase("df")) return;

        try {
            Object content = getContentManager().extractContent(msg);
            if (content instanceof Action) {
                content = ((Action) content).getAction();
            }

            if (content instanceof CardsDealt) {
                handleCardsDealt((CardsDealt) content);
                return;
            }
        } catch (Exception ignored) {
            // Plain text message, handled below.
        }

        String txt = msg.getContent();
        if (txt == null) return;

        if ("UNO".equals(currentGame)) {
            handleUnoText(txt);
        } else if ("BLACKJACK".equals(currentGame)) {
            handleBlackjackText(txt);
        } else if ("DURAK".equals(currentGame)) {
            handleDurakText(txt);
        } else {
            System.out.println("Message while no game selected: " + txt);
        }
    }

    private void handleCardsDealt(CardsDealt cd) {
        jade.util.leap.Iterator it = cd.getCards().iterator();

        while (it.hasNext()) {
            Card c = (Card) it.next();
            if (c == null) continue;

            if ("UNO".equals(currentGame) && unoGui != null) {
                unoGui.addCardToHand(c.getRank(), c.getSuit());
            } else if ("DURAK".equals(currentGame) && durakGui != null) {
                durakGui.addVisualCardToHand(c.getRank(), c.getSuit());
            }
        }
    }

    private void handleDurakText(String txt) {
        if (durakGui == null) return;

        if (txt.startsWith("DECK_COUNT:")) {
            durakGui.setDeckUI(Integer.parseInt(txt.split(":")[1]));
        }
        else if (txt.startsWith("GAME_START:")) {
            String trumpPart = txt.split(":")[1].replace("Trump is ", "");
            String[] parts = trumpPart.split(" ");
            durakGui.setTrumpUI(parts[0], parts.length > 1 ? parts[1] : "");
            durakGui.setTurnUI(false);
            durakGui.doneButton.setEnabled(false);
        }
        else if (txt.equals("CLEAR_TABLE")) {
            durakGui.clearTable();
        }
        else if (txt.startsWith("SERVER_BEAT:")) {
            String[] p = txt.split(":");
            durakGui.addServerDefenseToTable(p[1], p[2]);
        }
        else if (txt.startsWith("ATTACK:")) {
            String[] p = txt.split(":");
            durakGui.addServerAttackToTable(p[1], p[2]);
            durakGui.setTurnUI(false);
            pendingCard = null;
            durakGui.doneButton.setEnabled(false);
            durakGui.takeButton.setEnabled(true);
        }
        else if (txt.startsWith("SET_TURN:")) {
            String role = txt.split(":")[1];
            boolean isAttacker = role.equals("ATTACK");
            durakGui.setTurnUI(isAttacker);
            durakGui.doneButton.setEnabled(isAttacker);
            durakGui.takeButton.setEnabled(!isAttacker);
        }
        else if (txt.contains("Your turn to attack") || txt.contains("Attack me again")) {
            durakGui.setTurnUI(true);
            durakGui.doneButton.setEnabled(true);
            durakGui.takeButton.setEnabled(false);
        }
        else if (txt.startsWith("RETURN_CARD:")) {
            String[] p = txt.split(":");
            durakGui.addVisualCardToHand(p[1], p[2]);
            pendingCard = null;
        }
        else if (txt.startsWith("SHOW_DEFENSE:")) {
            String[] p = txt.split(":");
            durakGui.addUserDefenseToTable(p[1], p[2]);
            pendingCard = null;
        }
        else if (txt.startsWith("SHOW_ATTACK:")) {
            String[] p = txt.split(":");
            durakGui.addUserAttackToTable(p[1], p[2]);
            pendingCard = null;
        }
        else if (txt.equals("RESET_PENDING")) {
            pendingCard = null;
        }
        else if (txt.contains("Correct!")) {
            pendingCard = null;
            durakGui.updateLog("Defense accepted.");
        }
        else if (txt.contains("Can't play") || txt.contains("Illegal move")) {
            durakGui.updateLog(txt);
            pendingCard = null;
        }
        else if (txt.startsWith("GAME_OVER:")) {
            String result = txt.split(":")[1];
            Color c = result.contains("WIN") ? Color.ORANGE : Color.RED;
            durakGui.displayEndGameMessage(result, c);
            durakGui.updateLog(result);
        }
        else {
            durakGui.updateLog(txt);
        }
    }

    private void handleBlackjackText(String txt) {
        if (blackjackGui == null) return;

        if (txt.startsWith("BJ_STATE:")) {
            String[] parts = txt.split(":");
            blackjackGui.clearCards();

            if (parts.length >= 6) {
                String pScoreStr = parts[4];
                String dScore = parts[5];
                blackjackGui.setScore(dScore, pScoreStr);
            }

            if (parts.length >= 3) {
                String playerPart = parts[1].replace("P=", "");
                String[] pCards = playerPart.split(",");

                for (String pRank : pCards) {
                    if (!pRank.trim().isEmpty()) {
                        blackjackGui.addPlayerCard(pRank.trim(), "Hearts");
                    }
                }

                String dealerPart = parts[2].replace("D=", "");
                String[] dCards = dealerPart.split(",");

                for (String dRank : dCards) {
                    String rank = dRank.trim();
                    if (rank.equalsIgnoreCase("HIDDEN")) {
                        blackjackGui.addDealerCard("?", "BACK");
                    } else if (!rank.isEmpty()) {
                        blackjackGui.addDealerCard(rank, "Spades");
                    }
                }
            }
        }
        else if (txt.startsWith("GAME_OVER:")) {
            String result = txt.split(":")[1];
            Color c = result.contains("WIN") ? Color.ORANGE :
                    (result.contains("DRAW") || result.contains("PUSH") ? Color.WHITE : Color.RED);
            blackjackGui.showGameOver(result, c);
            blackjackGui.setStatus(result);
        }
        else {
            blackjackGui.setStatus(txt);
        }
    }

    private void handleUnoText(String txt) {
        if (txt.startsWith("UNO_TOP:")) {
            String[] p = txt.split(":");
            unoGui.showTopCard(p[1], p[2]);
            pendingCard = null;
        }
        else if (txt.startsWith("UNO_SEQUENCE:")) {
            String[] p = txt.split(":");

            unoGui.showPlayerThenDealerCard(
                    p[1], p[2],
                    p[3], p[4]
            );

            pendingCard = null;
        }
        else if (txt.startsWith("UNO_STATUS:")) {
            unoGui.setStatus(txt.substring("UNO_STATUS:".length()));
        }
        else if (txt.startsWith("UNO_DECK:")) {
            unoGui.setDeck(Integer.parseInt(txt.split(":")[1]));
        }
        else if (txt.startsWith("UNO_RETURN:")) {
            String[] p = txt.split(":");
            unoGui.addCardToHand(p[1], p[2]);
            pendingCard = null;
        }
        else if (txt.startsWith("UNO_GAME_OVER:")) {
            String result = txt.substring("UNO_GAME_OVER:".length());

            if (result.equals("YOU WIN")) {
                unoGui.showGameOver("YOU WIN 🎉", Color.GREEN);
            } else {
                unoGui.showGameOver("DEALER WINS", Color.RED);
            }

            pendingCard = null;
        }
        else {
            unoGui.setStatus(txt);
        }
    }

    public void playVisualCard(VisualCard visualCardComponent) {
        if ("BLACKJACK".equals(currentGame)) return;

        if ("UNO".equals(currentGame)) {
            if (pendingCard != null || unoGui == null) return;

            pendingCard = visualCardComponent;
            unoGui.removeCard(visualCardComponent);

            PlayMove m = new PlayMove();
            Card c = new Card();
            c.setRank(visualCardComponent.getRank());
            c.setSuit(visualCardComponent.getSuit());
            m.setPlayedCard(c);

            sendRequest(new AID("UNO", AID.ISLOCALNAME), m);
            return;
        }

        if ("DURAK".equals(currentGame)) {
            if (selectedServerName == null || pendingCard != null || durakGui == null) return;
            if (visualCardComponent.getParent() != durakGui.visualHandPanel) return;

            pendingCard = visualCardComponent;
            durakGui.removeVisualCard(visualCardComponent);

            PlayMove m = new PlayMove();
            Card c = new Card();
            c.setRank(visualCardComponent.getRank());
            c.setSuit(visualCardComponent.getSuit());
            m.setPlayedCard(c);

            sendRequest(new AID(selectedServerName, AID.ISLOCALNAME), m);
        }
    }

    public void sendDurakTakeRequest() {
        if (!"DURAK".equals(currentGame) || selectedServerName == null || durakGui == null) return;

        if (pendingCard != null) {
            durakGui.addVisualCardToHand(pendingCard.getRank(), pendingCard.getSuit());
            pendingCard = null;
        }

        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(new AID(selectedServerName, AID.ISLOCALNAME));
        m.setContent("TAKE_CARDS");
        send(m);

        durakGui.updateLog("Requesting to take cards...");
        durakGui.takeButton.setEnabled(false);
    }

    public void sendDurakDoneRequest() {
        if (!"DURAK".equals(currentGame) || selectedServerName == null || durakGui == null) return;

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(selectedServerName, AID.ISLOCALNAME));
        msg.setContent("DONE_ROUND");
        send(msg);

        durakGui.updateLog("Sent DONE/PASS");
    }

    public void sendBlackjackCommand(String command) {
        if (!"BLACKJACK".equals(currentGame) || selectedServerName == null || blackjackGui == null) return;

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(selectedServerName, AID.ISLOCALNAME));
        msg.setContent(command);
        send(msg);

        blackjackGui.setStatus("Action: " + command);
    }

    public void sendUnoDrawRequest() {
        if (!"UNO".equals(currentGame) || unoGui == null) return;

        if (pendingCard != null) {
            unoGui.addCardToHand(pendingCard.getRank(), pendingCard.getSuit());
            pendingCard = null;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("UNO", AID.ISLOCALNAME));
        msg.setContent("UNO_DRAW");
        send(msg);

        unoGui.setStatus("Drawing card...");
    }

    public void exitCurrentGame() {
        selectedServerName = null;
        currentGame = "NONE";
        pendingCard = null;

        if (durakGui != null) {
            durakGui.dispose();
            durakGui = null;
        }
        if (blackjackGui != null) {
            blackjackGui.dispose();
            blackjackGui = null;
        }
        if (unoGui != null) {
            unoGui.dispose();
            unoGui = null;
        }
        if (pickerGui != null) {
            pickerGui.setVisible(true);
        }
    }

    private void sendRequest(AID receiver, jade.content.AgentAction action) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(receiver);
        msg.setLanguage(codec.getName());
        msg.setOntology(CardGameOntology.getInstance().getName());

        try {
            getContentManager().fillContent(msg, new Action(receiver, action));
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
