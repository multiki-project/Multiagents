package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import proj1.ontology.*;

import java.util.*;

public class UnoProviderAgent extends Agent {
    private SLCodec codec = new SLCodec();

    private ArrayList<Card> deck = new ArrayList<>();
    private ArrayList<Card> botHand = new ArrayList<>();
    private Card topCard;
    private int playerCount = 0;

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) handleMessage(msg);
                else block();
            }
        });

        System.out.println("UNO agent started");
    }

    private void handleMessage(ACLMessage msg) {
        if ("UNO_DRAW".equals(msg.getContent())) {
            giveCards(msg.getSender(), 1);
            sendText(msg.getSender(), "UNO_STATUS:You drew one card");
            return;
        }

        try {
            Object content = getContentManager().extractContent(msg);

            if (content instanceof Action) {
                Object action = ((Action) content).getAction();

                if (action instanceof SubscribeToGame) {
                    startGame(msg.getSender());
                }

                if (action instanceof PlayMove) {
                    Card card = ((PlayMove) action).getPlayedCard();
                    playCard(msg.getSender(), card);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGame(AID player) {
        initDeck();

        CardsDealt cards = new CardsDealt();

        for (int i = 0; i < 7; i++) {
            Card p = drawCard();
            Card b = drawCard();

            if (p != null) {
                cards.addCards(p);
                playerCount++;
            }

            if (b != null) {
                botHand.add(b);
            }
        }

        topCard = drawCard();

        sendCards(player, cards);
        sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
        sendText(player, "UNO_STATUS:Your turn");
        sendText(player, "UNO_DECK:" + deck.size());
    }

    private void playCard(AID player, Card playerCard) {
        if (!canPlay(playerCard)) {
            sendText(player, "UNO_RETURN:" + playerCard.getRank() + ":" + playerCard.getSuit());
            sendText(player, "UNO_STATUS:Wrong card");
            return;
        }

        topCard = playerCard;
        playerCount--;

        if (playerCount <= 0) {
            sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
            sendText(player, "UNO_GAME_OVER:YOU WIN");
            return;
        }

        if (playerCard.getRank().equals("+2")) {
            botDrawCards(2);
            sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
            sendText(player, "UNO_STATUS:You played +2. Dealer draws 2. Your turn again.");
            sendText(player, "UNO_DECK:" + deck.size());
            return;
        }

        if (playerCard.getRank().equals("+4")) {
            botDrawCards(4);
            sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
            sendText(player, "UNO_STATUS:You played +4. Dealer draws 4. Your turn again.");
            sendText(player, "UNO_DECK:" + deck.size());
            return;
        }

        botMove(player, playerCard);
    }

    private void botMove(AID player, Card playerCard) {
        Card chosen = null;

        for (Card c : botHand) {
            if (canPlay(c)) {
                chosen = c;
                break;
            }
        }

        if (chosen == null) {
            Card drawn = drawCard();

            if (drawn != null) {
                botHand.add(drawn);

                if (canPlay(drawn)) {
                    chosen = drawn;
                }
            }
        }

        if (chosen == null) {
            sendText(player, "UNO_TOP:" + playerCard.getRank() + ":" + playerCard.getSuit());
            sendText(player, "UNO_STATUS:Dealer drew card. Your turn");
            sendText(player, "UNO_DECK:" + deck.size());
            return;
        }

        botHand.remove(chosen);
        topCard = chosen;

        sendText(player, "UNO_SEQUENCE:"
                + playerCard.getRank() + ":" + playerCard.getSuit()
                + ":" + chosen.getRank() + ":" + chosen.getSuit());

        if (botHand.isEmpty()) {
            sendText(player, "UNO_GAME_OVER:DEALER WINS");
            return;
        }

        if (chosen.getRank().equals("+2")) {
            giveCards(player, 2);
            sendText(player, "UNO_STATUS:Dealer played +2. You draw 2.");
        } else if (chosen.getRank().equals("+4")) {
            giveCards(player, 4);
            sendText(player, "UNO_STATUS:Dealer played +4. You draw 4.");
        } else {
            sendText(player, "UNO_STATUS:Dealer played " + chosen.getRank() + " " + chosen.getSuit());
        }

        sendText(player, "UNO_DECK:" + deck.size());
    }

    private boolean canPlay(Card c) {
        if (c == null || topCard == null) return false;

        if (c.getRank().equals("+4")) return true;

        return c.getSuit().equals(topCard.getSuit())
                || c.getRank().equals(topCard.getRank());
    }

    private void giveCards(AID player, int count) {
        CardsDealt cards = new CardsDealt();

        for (int i = 0; i < count; i++) {
            Card c = drawCard();

            if (c != null) {
                cards.addCards(c);
                playerCount++;
            }
        }

        sendCards(player, cards);
        sendText(player, "UNO_DECK:" + deck.size());
    }

    private void botDrawCards(int count) {
        for (int i = 0; i < count; i++) {
            Card c = drawCard();
            if (c != null) botHand.add(c);
        }
    }

    private void initDeck() {
        deck.clear();
        botHand.clear();
        playerCount = 0;

        String[] colors = {"Red", "Yellow", "Green", "Blue"};
        String[] normalRanks = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (String color : colors) {
            for (String rank : normalRanks) {
                addCard(rank, color);
                addCard(rank, color);
            }

            addCard("+2", color);
            addCard("+2", color);

            addCard("Skip", color);
            addCard("Skip", color);
        }

        addCard("+4", "Black");
        addCard("+4", "Black");
        addCard("+4", "Black");
        addCard("+4", "Black");

        Collections.shuffle(deck);
    }

    private void addCard(String rank, String suit) {
        Card c = new Card();
        c.setRank(rank);
        c.setSuit(suit);
        deck.add(c);
    }

    private Card drawCard() {
        if (deck.isEmpty()) return null;
        return deck.remove(0);
    }

    private void sendCards(AID player, CardsDealt cards) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player);
            msg.setLanguage(codec.getName());
            msg.setOntology(CardGameOntology.getInstance().getName());
            getContentManager().fillContent(msg, cards);
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendText(AID player, String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(player);
        msg.setContent(text);
        send(msg);
    }
}