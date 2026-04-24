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
                    Card c = ((PlayMove) action).getPlayedCard();
                    playCard(msg.getSender(), c);
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

            cards.addCards(p);
            botHand.add(b);
            playerCount++;
        }

        topCard = drawCard();

        sendCards(player, cards);
        sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
        sendText(player, "UNO_STATUS:Your turn");
        sendText(player, "UNO_DECK:" + deck.size());
    }

    private void playCard(AID player, Card card) {
        if (!canPlay(card)) {
            sendText(player, "UNO_RETURN:" + card.getRank() + ":" + card.getSuit());
            sendText(player, "UNO_STATUS:Wrong card");
            return;
        }

        topCard = card;
        playerCount--;

        sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());

        if (playerCount == 0) {
            sendText(player, "UNO_STATUS:YOU WIN");
            return;
        }

        botMove(player);
    }

    private void botMove(AID player) {
        Card chosen = null;

        for (Card c : botHand) {
            if (canPlay(c)) {
                chosen = c;
                break;
            }
        }

        if (chosen != null) {
            botHand.remove(chosen);
            topCard = chosen;
            sendText(player, "UNO_TOP:" + topCard.getRank() + ":" + topCard.getSuit());
            sendText(player, "UNO_STATUS:Bot played");
        } else {
            Card d = drawCard();
            if (d != null) botHand.add(d);
            sendText(player, "UNO_STATUS:Bot drew card");
        }

        sendText(player, "UNO_DECK:" + deck.size());
    }

    private boolean canPlay(Card c) {
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

    private void initDeck() {
        deck.clear();
        botHand.clear();
        playerCount = 0;

        String[] colors = {"Red", "Yellow", "Green", "Blue"};
        String[] ranks = {"0","1","2","3","4","5","6","7","8","9","+2","Skip"};

        for (String color : colors) {
            for (String rank : ranks) {
                Card c = new Card();
                c.setRank(rank);
                c.setSuit(color);
                deck.add(c);
            }
        }

        Collections.shuffle(deck);
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